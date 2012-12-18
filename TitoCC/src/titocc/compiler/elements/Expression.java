package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public abstract class Expression extends CodeElement
{
	public Expression(int line, int column)
	{
		super(line, column);
	}

	public static Expression parse(TokenStream tokens)
	{
		return AssignmentExpression.parse(tokens);
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
