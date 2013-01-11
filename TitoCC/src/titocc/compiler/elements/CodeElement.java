package titocc.compiler.elements;

/**
 * Abstract base for all the code elements. Stores the position of the element
 * within the source file so that it can be used by compiler messages.
 */
public abstract class CodeElement
{
	/**
	 * Starting line number.
	 */
	private final int line;
	/**
	 * Starting column number.
	 */
	private final int column;

	/**
	 * Constructs a code element.
	 *
	 * @param line starting line number of the element
	 * @param column starting column/character of the element
	 */
	public CodeElement(int line, int column)
	{
		this.line = line;
		this.column = column;
	}

	/**
	 * Returns the line number where the code element starts.
	 *
	 * @return line number
	 */
	public int getLine()
	{
		return line;
	}

	/**
	 * Returns the column/character where the code element starts.
	 *
	 * @return column
	 */
	public int getColumn()
	{
		return column;
	}

	/**
	 * Returns a string representation of the code element for testing and
	 * debugging purposes.
	 *
	 * @return the string representation
	 */
	@Override
	public String toString()
	{
		return "";
	}
}
