package titocc.tokenizer;

import titocc.util.Position;

/**
 * Abstract base for all token types.
 */
public abstract class Token
{
	/**
	 * Token as a string.
	 */
	private final String string;

	/**
	 * Position of the token within a text file.
	 */
	private final Position position;

	/**
	 * Constructs a Token.
	 *
	 * @param string string representation of the token as it appears in the
	 * source text
	 * @param position starting position of the token
	 */
	protected Token(String string, Position position)
	{
		this.string = string;
		this.position = position;
	}

	/**
	 * Returns the position of the token.
	 *
	 * @return the position
	 */
	public Position getPosition()
	{
		return position;
	}

	/**
	 * Returns the token as a String.
	 *
	 * @return token as String
	 */
	@Override
	public String toString()
	{
		return string;
	}
}
