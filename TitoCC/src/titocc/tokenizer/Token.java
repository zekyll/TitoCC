package titocc.tokenizer;

/**
 * Abstract base for all token types.
 */
public abstract class Token
{
	private String string;
	private int line, column;

	/**
	 * Constructs a Token.
	 *
	 * @param string string representation of the token as it appears in the
	 * source text
	 * @param line starting line number of the token
	 * @param column starting column/character of the token
	 */
	protected Token(String string, int line, int column)
	{
		this.string = string;
		this.line = line;
		this.column = column;
	}

	/**
	 * Returns the line number of the token.
	 *
	 * @return the line number
	 */
	public int getLine()
	{
		return line;
	}

	/**
	 * Returns the column of the token.
	 *
	 * @return the column
	 */
	public int getColumn()
	{
		return column;
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
