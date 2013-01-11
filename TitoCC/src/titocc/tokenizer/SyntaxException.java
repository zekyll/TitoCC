package titocc.tokenizer;

/**
 * An exception caused by errors in source code.
 */
public class SyntaxException extends Exception
{
	/**
	 * Line number where the compilation error occured. First line is 0.
	 */
	private final int line;
	/**
	 * Column number where the compilation error occured.
	 */
	private final int column;

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
