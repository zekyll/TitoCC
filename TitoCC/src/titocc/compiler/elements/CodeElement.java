package titocc.compiler.elements;

import java.io.IOException;
import java.io.Writer;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;

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

	public abstract void compile(Assembler asm, Scope scope) throws IOException, SyntaxException;
}
