package titocc.tokenizer;

/**
 * Word token that is not a keyword.
 */
public class IdentifierToken extends WordToken
{
	public IdentifierToken(String string, int line, int column)
	{
		super(string, line, column);
	}
}
