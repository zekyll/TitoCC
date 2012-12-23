package titocc.compiler.elements;

import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

public abstract class Expression extends CodeElement
{
	public Expression(int line, int column)
	{
		super(line, column);
	}

	public Integer getCompileTimeValue() throws SyntaxException
	{
		return null;
	}

	public String getLvalueReference(Scope scope) throws SyntaxException
	{
		return null;
	}

	public static Expression parse(TokenStream tokens)
	{
		return AssignmentExpression.parse(tokens);
	}
}
