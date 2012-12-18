package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class Function extends Declaration
{
	public Function(int line, int column)
	{
		super(line, column);
	}

	public static Function parse(TokenStream tokens)
	{
		return null;
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
