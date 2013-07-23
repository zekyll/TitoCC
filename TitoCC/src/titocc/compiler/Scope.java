package titocc.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a scope (aka namespace) that contains symbols declared within that scope.
 */
public class Scope
{
	/**
	 * Parent scope.
	 */
	private final Scope parent;

	/**
	 * Map of symbols in this scope.
	 */
	private final Map<String, Symbol> symbols = new HashMap<String, Symbol>();

	/**
	 * Subscopes of this scope.
	 */
	private final List<Scope> subScopes = new ArrayList<Scope>();

	/**
	 * All the reserved names in the ttk-91 namespace. This is list is shared by all Scope objects.
	 */
	private final Set<String> globallyUniqueNames;

	/**
	 * If an identifier has linkage then this map contains mapping from that identifier to the
	 * first declaration of that identifier. Contains both internal and external linkages.
	 */
	private final Map<String, Symbol> linkages;

	/**
	 * Prefix added for this scope when generating globally unique names.
	 */
	private final String globalNamePrefix;

	/**
	 * Constructs a new Scope.
	 *
	 * @param parent parent scope, or null if this is the global scope
	 * @param globalNamePrefix prefix used for this scope when generating globally unique names
	 */
	public Scope(Scope parent, String globalNamePrefix)
	{
		this.parent = parent;
		this.globalNamePrefix = globalNamePrefix;
		if (parent == null) {
			globallyUniqueNames = new HashSet<String>();
			linkages = new HashMap<String, Symbol>();
		} else {
			globallyUniqueNames = parent.globallyUniqueNames;
			linkages = parent.linkages;
		}
	}

	/**
	 * Tests whether this is the global scope.
	 *
	 * @return true if global scope
	 */
	public boolean isGlobal()
	{
		return parent == null;
	}

	/**
	 * Returns the parent scope.
	 *
	 * @return the parent scope
	 */
	public Scope getParent()
	{
		return parent;
	}

	/**
	 * Returns a collection of all symbols in this scope.
	 *
	 * @return collection of symbols
	 */
	public Collection<Symbol> getSymbols()
	{
		return new ArrayList<Symbol>(symbols.values());
	}

	/**
	 * Finds a symbol (e.g. a variable or a function) defined in this scope or
	 * any of its parent scopes.
	 *
	 * @param name identifier of the object
	 * @return the searched symbol, or null if none was found
	 */
	public Symbol find(String name)
	{
		return find(name, true);
	}

	private Symbol find(String name, boolean incrementUseCount)
	{
		Symbol sym = symbols.get(name);
		if (sym == null && parent != null)
			sym = parent.find(name, incrementUseCount);
		if (sym != null && incrementUseCount)
			sym.increaseUseCount();
		return sym;
	}

	/**
	 * Adds a new symbol to the scope if no symbols with the same name exist already. Also reserves
	 * and sets the globally unique name for the symbol and sets up the linkage.
	 *
	 * @param symbol Symbol to be added.
	 * @return the symbol itself or existing symbol with same name if one exists already; if the
	 * type of the existing symbol is conflicting then returns null
	 */
	public DeclarationResult add(Symbol symbol)
	{
		Linkage linkage = getLinkage(symbol);

		// If symbol already exists in current scope, return it. (Must have same type and right
		// linkage.)
		Symbol prevSymbol = symbols.get(symbol.getName());
		if (prevSymbol != null)
			return checkCompatibility(symbol, prevSymbol, linkage);

		// Link the symbol if it has linkage and determine global name.
		String globalName;
		if (linkage != Linkage.None) {
			// Internal/external linkage; get global name from earlier declaration.
			Symbol linkedSymbol = linkages.get(symbol.getName());
			if (linkedSymbol != null) {
				DeclarationResult declRes = checkCompatibility(symbol, linkedSymbol, linkage);
				if (declRes.symbol == null)
					return declRes;
				globalName = linkedSymbol.getGlobalName();
			} else {
				linkages.put(symbol.getName(), symbol);
				globalName = makeGloballyUniqueName(symbol.getName());
			}
			symbol.setLinkage(linkage, linkedSymbol);
		} else {
			// No linkage; generate new global name.
			globalName = makeGloballyUniqueName(symbol.getName());
		}
		symbol.setGlobalName(globalName);

		symbols.put(symbol.getName(), symbol);

		return new DeclarationResult(symbol);
	}

	/**
	 * Determines linkage of the added symbol based on its storage class, type, scope and possible
	 * previously declared symbol that has linkage.
	 */
	private Linkage getLinkage(Symbol symbol)
	{
		// If extern or function without storage class, and a prior declaration with linkage is
		// visible then use the same linkage. ($6.2.2/4-5)
		if (symbol.getStorageClass() == StorageClass.Extern || (symbol.getType().isFunction()
				&& symbol.getStorageClass() == null)) {
			Symbol prevSymbol = find(symbol.getName(), false);
			if (prevSymbol != null) {
				return prevSymbol.getLinkage() != Linkage.None ? prevSymbol.getLinkage()
						: Linkage.External;
			}
		}

		// If no prior declaration with linkage, set linkage based on storage class and scope.
		if (symbol.getStorageClass() == StorageClass.Extern || symbol.getStorageClass() == null)
			return Linkage.External;
		else if (isGlobal() && symbol.getStorageClass() == StorageClass.Static)
			return Linkage.Internal;
		else
			return Linkage.None;
	}

	/**
	 * Checks that the new declaration is compatible with previous declaration.
	 */
	private DeclarationResult checkCompatibility(Symbol sym, Symbol prevSymbol, Linkage linkage)
	{
		// ($6.7/4) and ($6.2.7/2)
		if (!prevSymbol.getType().equals(sym.getType())) {
			return new DeclarationResult("Redeclaration of \"" + sym.getName()
					+ "\" with incompatible type.");
		}

		// ($6.7/3)
		if (prevSymbol.getLinkage() == Linkage.None) {
			return new DeclarationResult("Redeclaration of \"" + sym.getName()
					+ "\" with no linkage.");
		}

		// ($6.7/3) and ($6.2.2/7)
		if (prevSymbol.getLinkage() != linkage) {
			return new DeclarationResult("Redeclaration of \"" + sym.getName()
					+ "\" with different linkage.");
		}

		return new DeclarationResult(prevSymbol);
	}

	/**
	 * Adds a new subscope in this scope.
	 *
	 * @param subScope
	 */
	public void addSubScope(Scope subScope)
	{
		subScopes.add(subScope);
	}

	/**
	 * Returns a collection of all subscopes.
	 *
	 * @return collection of subscopes
	 */
	public Collection<Scope> getSubScopes()
	{
		return new ArrayList<Scope>(subScopes);
	}

	/**
	 * Generates a globally unique name by first adding the prefixes of the scope and all its parent
	 * scopes. Then tries number suffixes starting from 2 until the name is unique.
	 *
	 * @param name local name
	 * @return a globally unique name
	 */
	public String makeGloballyUniqueName(String name)
	{
		if (name.startsWith("__"))
			name = name.substring(2);

		if (name.isEmpty())
			throw new InternalCompilerException("Declaring empty identifier.");

		String uniqueNameBase = generateGlobalNamePrefix() + name;
		String uniqueName = uniqueNameBase;
		for (int i = 2; !globallyUniqueNames.add(uniqueName.toLowerCase()); ++i)
			uniqueName = uniqueNameBase + i;
		return uniqueName;
	}

	/**
	 * Generates the global name prefix for this scope by combining the prefixes from all parent
	 * scopes.
	 *
	 * @return the combined prefix
	 */
	private String generateGlobalNamePrefix()
	{
		if (parent != null)
			return parent.generateGlobalNamePrefix() + globalNamePrefix;
		else
			return globalNamePrefix;
	}
}
