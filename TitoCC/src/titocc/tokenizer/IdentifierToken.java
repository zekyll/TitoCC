package titocc.tokenizer;

/**
 * Word token that is not a keyword.
 */
public class IdentifierToken extends WordToken
{
	/**
	 * Constructs an IdentifierToken.
	 *
	 * @param string identifier string
	 * @param line line number where the token is located
	 * @param column column number where the token is located
	 */
	public IdentifierToken(String string, int line, int column)
	{
		super(string, line, column);
	}
}
