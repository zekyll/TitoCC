package titocc.compiler;

/**
 * Represents a declared symbol.
 */
public interface Symbol
{
	/**
	 * Returns name (aka identifier) of the symbol.
	 *
	 * @return
	 */
	public String getName();

	/**
	 * Returns a globally unique name for the symbol.
	 *
	 * @return
	 */
	public String getGlobalName();

	/**
	 * Returns an assembly code reference to this symbol.
	 *
	 * @return
	 */
	public String getReference();
}
