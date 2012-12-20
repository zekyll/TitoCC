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
	String getName();

	/**
	 * Returns an assembly code reference to this symbol.
	 *
	 * @return
	 */
	String getReference();
}
