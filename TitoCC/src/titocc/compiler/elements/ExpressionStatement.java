package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class ExpressionStatement extends Statement
{
	private Expression expression;

	public ExpressionStatement(Expression expression, int line, int column)
	{
		super(line, column);
		this.expression = expression;
	}

	public Expression expression()
	{
		return expression;
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(EXPR_ST " + expression + ")";
	}

	public static ExpressionStatement parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		ExpressionStatement exprStatement = null;

		Expression expr = Expression.parse(tokens);
		if (expr != null)
			if (tokens.read().toString().equals(";"))
				exprStatement = new ExpressionStatement(expr, line, column);

		tokens.popMark(exprStatement == null);
		return exprStatement;
	}
}
