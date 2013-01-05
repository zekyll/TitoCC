package titocc.compiler;

/**
 * Represents a declared name like variable, function or code position.
 */
public interface Symbol
{
	/**
	 * Returns name (aka identifier) of the symbol.
	 *
	 * @return the name
	 */
	public String getName();

	/**
	 * Returns a globally unique name for the symbol. This is the name used in
	 * the ttk-91 code, accounting for the fact that there is only one namespace
	 * and names are case insensitive.
	 *
	 * @return the globally unique name
	 */
	public String getGlobalName();

	/**
	 * Returns the assembly code reference to this symbol. Can be used as the
	 * right side operand in most instructions. E.g. for global variables and
	 * functions it's the same as global name and for stack frame variables it
	 * is suffixed with "(fp)".
	 *
	 * @return assembly code reference
	 */
	public String getReference();
}
