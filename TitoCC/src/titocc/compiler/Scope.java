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
		if (parent == null)
			globallyUniqueNames = new HashSet<String>();
		else
			globallyUniqueNames = parent.globallyUniqueNames;
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
		Symbol sym = symbols.get(name);
		if (sym == null && parent != null)
			sym = parent.find(name);
		return sym;
	}

	/**
	 * Adds a new symbol to the scope if no symbols with the same name exist already. Also reserves
	 * and sets the globally unique name for the symbol.
	 *
	 * @param symbol Symbol to be added.
	 * @return true if succeeded, or false if symbol was not added (already exists)
	 */
	public boolean add(Symbol symbol)
	{
		if (symbols.containsKey(symbol.getName()))
			return false;
		symbols.put(symbol.getName(), symbol);
		String globalName = makeGloballyUniqueName(symbol.getName());
		symbol.setGlobalName(globalName);
		return true;
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
