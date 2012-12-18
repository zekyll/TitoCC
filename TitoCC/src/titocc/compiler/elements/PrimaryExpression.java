package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public abstract class PrimaryExpression extends Expression
{
	public PrimaryExpression(int line, int column)
	{
		super(line, column);
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public static PrimaryExpression parse(TokenStream tokens)
	{
		return null;
	}
}
