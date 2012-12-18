package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;

public abstract class CodeElement
{
	private int line, column;

	public CodeElement(int line, int column)
	{
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

	public abstract void compile(Writer writer, Scope scope);
}
