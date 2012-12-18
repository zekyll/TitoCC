package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class BlockStatement extends CodeElement
{
	public BlockStatement(int line, int column)
	{
		super(line, column);
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public static BlockStatement parse(TokenStream tokens)
	{
		return null;
	}
}
