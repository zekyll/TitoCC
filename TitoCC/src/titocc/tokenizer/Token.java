package titocc.tokenizer;

public abstract class Token
{
	private String string;
	private int line, column;

	public Token(String string, int line, int column)
	{
		this.string = string;
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

	@Override
	public String toString()
	{
		return string;
	}
}
