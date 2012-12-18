package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class FunctionCallExpression extends Expression
{
	private Expression function;
	private ArgumentList argumentList;

	public FunctionCallExpression(Expression function, ArgumentList argumentList,
			int line, int column)
	{
		super(line, column);
		this.function = function;
		this.argumentList = argumentList;
	}

	public Expression getFunction()
	{
		return function;
	}

	public ArgumentList getArgumentList()
	{
		return argumentList;
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(FCALL_EXPR " + function + " " + argumentList + ")";
	}

	public static Expression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();

		Expression expr = PostfixExpression.parse(tokens);
		if (expr != null) {
			ArgumentList argList = ArgumentList.parse(tokens);
			if (argList != null)
				expr = new FunctionCallExpression(expr, argList, line, column);
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
