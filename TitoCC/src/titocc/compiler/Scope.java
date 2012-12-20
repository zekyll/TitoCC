package titocc.compiler;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a scope (aka namespace) that contains symbols declared within that
 * scope.
 */
public class Scope
{
	private Scope parent;
	private Map<String, Symbol> symbols = new HashMap<String, Symbol>();

	/**
	 * Constructs a new Scope.
	 *
	 * @param parent Parent scope. Null if this is the global scope.
	 */
	public Scope(Scope parent)
	{
		this.parent = parent;
	}

	/**
	 * Tests whether this is the global scope.
	 *
	 * @return True if global scope.
	 */
	public boolean isGlobal()
	{
		return parent == null;
	}

	/**
	 * Finds a symbol (e.g. a variable or a function) defined in this scope.
	 *
	 * @param name Identifier of the object.
	 * @return Searched symbol or null if none was found.
	 */
	public Symbol find(String name)
	{
		return symbols.get(name);
	}

	/**
	 * Finds a symbol (e.g. a variable or a function) defined in this scope or
	 * any of its parent scopes.
	 *
	 * @param name Identifier of the object.
	 * @return Searched symbol or null if none was found.
	 */
	public Symbol findFromAllScopes(String name)
	{
		Symbol sym = find(name);
		if (sym == null && parent != null)
			sym = parent.find(name);
		return sym;
	}

	/**
	 * Adds a new symbol to the scope if no symbols with the same name exist
	 * already.
	 *
	 * @param symbol Symbol to be added.
	 * @return False if symbol was not added (already exists).
	 */
	public boolean add(Symbol symbol)
	{
		if (symbols.containsKey(symbol.getName()))
			return false;
		symbols.put(symbol.getName(), symbol);
		return true;
	}
}
