package titocc.tokenizer;

/**
 * An exception caused by errors in source code.
 */
public class SyntaxException extends Exception
{
	private int line, column;

	/**
	 * Constructs a SyntaxException.
	 *
	 * @param message error message
	 * @param line line number where the error occured
	 * @param column column number where the error occured
	 */
	public SyntaxException(String message, int line, int column)
	{
		super(message);
		this.line = line;
		this.column = column;
	}

	/**
	 * Returns the line number of the error.
	 *
	 * @return the line number
	 */
	public int getLine()
	{
		return line;
	}

	/**
	 * Returns the column number of the error.
	 *
	 * @return the column number
	 */
	public int getColumn()
	{
		return column;
	}
}
