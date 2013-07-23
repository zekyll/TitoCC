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
	 * For symbols with linkage, the symbol to which this symbol is linked.
	 */
	private Symbol linkedSymbol = null;

	/**
	 * Symbol linkage (external/internal/none).
	 */
	private Linkage linkage = Linkage.None;

	/**
	 * Whether the object/function is defined.
	 */
	private boolean defined = false;

	/**
	 * Whether the object has tentative definitions.
	 */
	private boolean hasTentativeDefinition = false;

	/**
	 * Number of source code references to this symnol.
	 */
	private int useCount = 0;

	/**
	 * Constructs a new Symbol.
	 *
	 * @param name name of the symbol
	 * @param type type of the symbol
	 * @param storageClass storage class
	 * @param inline whether a function was declared inline or not; ignored if symbol has object
	 * type
	 */
	public Symbol(String name, CType type, StorageClass storageClass, boolean inline)
	{
		this.name = name;
		this.type = type;
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
		return isStackObject() ? globallyUniqueName + "(FP)" : globallyUniqueName;
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
		VirtualRegister reg = isStackObject() ? VirtualRegister.FP : null;
		return new RhsOperand(addrMode, globallyUniqueName, reg);
	}

	private boolean isStackObject()
	{
		return storageClass == StorageClass.Auto || storageClass == StorageClass.Register;
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

	/**
	 * Check if the object/function has been defined.
	 *
	 * @return
	 */
	public boolean isDefined()
	{
		if (linkedSymbol != null)
			return linkedSymbol.isDefined();
		return defined;
	}

	/**
	 * Marks the object/function as defined.
	 *
	 * @return false if already defined
	 */
	public boolean define()
	{
		if (linkedSymbol != null)
			return linkedSymbol.define();
		boolean ret = !defined;
		defined = true;
		return ret;
	}

	/**
	 * Marks the object as having a tentative definition.
	 */
	public void defineTentatively()
	{
		if (linkedSymbol != null)
			linkedSymbol.defineTentatively();
		else
			hasTentativeDefinition = true;
	}

	/**
	 * Checks whether the object has a tentative definition.
	 *
	 * @return
	 */
	public boolean hasTentativeDefinition()
	{
		if (linkedSymbol != null)
			return linkedSymbol.hasTentativeDefinition();
		return hasTentativeDefinition;
	}

	/**
	 * Increments the usage counter for this symbol.
	 */
	public void increaseUseCount()
	{
		++useCount;
	}

	/**
	 * Get the number of usages. Can be used by optimizations to eliminate unused code etc.
	 *
	 * @return use count
	 */
	public int getUseCount()
	{
		return useCount;
	}

	/**
	 * Gets the linked symbol for symbols with linkage.
	 *
	 * @return linked symbol or null if no linkage
	 */
	public Symbol getLinkedSymbol()
	{
		return linkedSymbol;
	}

	/**
	 * Get the linkage of the symbol.
	 *
	 * @return linkage
	 */
	public Linkage getLinkage()
	{
		return linkage;
	}

	/**
	 * Sets the linkage and linked symbol.
	 *
	 * @param linkage external/internal/none
	 * @param linkedSymbol first declaration of the identifier or null if no linkage
	 */
	public void setLinkage(Linkage linkage, Symbol linkedSymbol)
	{
		this.linkage = linkage;
		this.linkedSymbol = linkedSymbol;
	}
}
