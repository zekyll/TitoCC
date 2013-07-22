package titocc.compiler;

/**
 * Linkage type for a symbol.
 */
public enum Linkage
{
	/**
	 * Linkage between translation units.
	 */
	External,
	/**
	 * Linkage inside one translation unit.
	 */
	Internal,
	/**
	 * No linkage.
	 */
	None
}
