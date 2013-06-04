package titocc.compiler;

import titocc.compiler.types.CType;

/**
 * Represents a declared name like variable, function or code position.
 *
 * <br> Symbols with names starting with double underscore "__" are used for internal symbols (C
 * standard reserves those identifiers for implementation).
 */
public class Symbol
{
	/**
	 * Symbol categories.
	 */
	public enum Category
	{
		Function, GlobalVariable, LocalVariable, Parameter, Internal
	};

	/**
	 * Name of the symbol.
	 */
	private final String name;

	/**
	 * Type of the symbol.
	 */
	private final CType type;

	/**
	 * Globally unique name. Set when adding the symbol to a scope.
	 */
	private String globallyUniqueName = null;

	/**
	 * Suffix to add to the reference. (for example "(fp)")
	 */
	private final String referenceSuffix;

	/**
	 * True is local variable.
	 */
	private final Category category;

	/**
	 * Constructs a new Symbol.
	 *
	 * @param name name of the symbol
	 * @param type type of the symbol
	 * @param referenceSuffix suffix that is added to global name to get the reference ("(fp)" can
	 * be used for stack frame variables)
	 * @param category Symbol category (local variable, parameter, internal etc)
	 */
	public Symbol(String name, CType type, String referenceSuffix, Category category)
	{
		this.name = name;
		this.type = type;
		this.referenceSuffix = referenceSuffix;
		this.category = category;
	}

	/**
	 * Returns name (aka identifier) of the symbol.
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns a globally unique name for the symbol. This is the name used in the ttk-91 code,
	 * accounting for the fact that there is only one namespace and names are case insensitive.
	 *
	 * @return the globally unique name or null if not set
	 */
	public String getGlobalName()
	{
		return globallyUniqueName;
	}

	/**
	 * Sets the globally unique name for this symbol. Meant to be only called by Scope.addsymbol.
	 */
	public void setGlobalName(String globalName)
	{
		globallyUniqueName = globalName;
	}

	/**
	 * Returns the assembly code reference to this symbol. Can be used as the right side operand in
	 * most instructions. E.g. for global variables and functions it's the same as global name and
	 * for stack frame variables it is suffixed with "(fp)".
	 *
	 * @return assembly code reference
	 */
	public String getReference()
	{
		return globallyUniqueName + referenceSuffix;
	}

	/**
	 * Returns the type of the object corresponding to this symbol.
	 *
	 * @return the object type
	 */
	public CType getType()
	{
		return type;
	}

	/**
	 * Returns the symbol category.
	 *
	 * @return category
	 */
	public Category getCategory()
	{
		return category;
	}
}
