package titocc.compiler.elements;

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
}
