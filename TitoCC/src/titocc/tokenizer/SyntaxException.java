package titocc.tokenizer;

public class SyntaxException extends Exception
{
	private int line, column;

	public SyntaxException(String message, int line, int column)
	{
		super(message);
		this.line = line;
		this.column = column;
	}

	public int getLine()
	{
		return line;
	}

	public int getColumn()
	{
		return column;
	}
}
