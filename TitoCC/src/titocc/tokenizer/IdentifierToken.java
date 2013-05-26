package titocc.tokenizer;

import titocc.util.Position;

/**
 * Word token that is not a keyword.
 */
public class IdentifierToken extends WordToken
{
	/**
	 * Constructs an IdentifierToken.
	 *
	 * @param string identifier string
	 * @param position starting position of the token
	 */
	public IdentifierToken(String string, Position position)
	{
		super(string, position);
	}
}
