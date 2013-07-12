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
	 * Storage class.
	 */
	private final StorageClass storageClass;

	/**
	 * Whether function was declared with inline specifier.
	 */
	private final boolean inline;

	/**
	 * Globally unique name. Set when adding the symbol to a scope.
	 */
	private String globallyUniqueName = null;

	/**
	 * True is local variable.
	 */
	private final Category category;

	/**
	 * Constructs a new Symbol.
	 *
	 * @param name name of the symbol
	 * @param type type of the symbol
	 * @param category Symbol category (local variable, parameter, internal etc)
	 * @param storageClass storage class
	 * @param inline whether a function was declared inline or not; ignored if symbol has object
	 * type
	 */
	public Symbol(String name, CType type, Category category, StorageClass storageClass,
			boolean inline)
	{
		this.name = name;
		this.type = type;
		this.category = category;
		this.storageClass = storageClass;
		this.inline = inline;
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
		// Stack variables (automatic local variables and parameters) are accessed through stack
		// pointer (sp).
		return storageClass == StorageClass.Auto ? globallyUniqueName + "(FP)"
				: globallyUniqueName;
	}

	/**
	 * Creates an RHS operand for assembly instruction that corresponds to this symbol.
	 *
	 * @param dereference true if the value of the variable is required, false when address
	 * @return RHS operand
	 */
	public RhsOperand getRhsOperand(boolean dereference)
	{
		int addrMode = dereference ? 1 : 0;
		VirtualRegister reg = storageClass == storageClass.Auto ? VirtualRegister.FP : null;
		return new RhsOperand(addrMode, globallyUniqueName, reg);
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

	/**
	 * Returns storage class.
	 *
	 * @return storage class
	 */
	public StorageClass getStorageClass()
	{
		return storageClass;
	}

	/**
	 * Returns the inline specifier flag.
	 *
	 * @return true if inline function
	 */
	public boolean getInline()
	{
		return inline;
	}
}
