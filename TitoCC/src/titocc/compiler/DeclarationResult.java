package titocc.compiler;

/**
 * Helper class for returning either a declared symbol or error message.
 */
public class DeclarationResult
{
	/**
	 * Declared symbol or null if the declaration was invalid.
	 */
	public final Symbol symbol;

	/**
	 * Error message for invalid declarations.
	 */
	public final String msg;

	/**
	 * Constructs a DeclarationResult.
	 *
	 * @param message error message
	 */
	public DeclarationResult(Symbol symbol)
	{
		this.symbol = symbol;
		this.msg = null;
	}

	/**
	 * Constructs a DeclarationResult.
	 *
	 * @param msg declared symbol
	 */
	public DeclarationResult(String msg)
	{
		this.symbol = null;
		this.msg = msg;
	}
}
