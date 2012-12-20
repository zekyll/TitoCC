package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class ReturnStatement extends Statement
{
	private Expression expression;

	public ReturnStatement(Expression expression, int line, int column)
	{
		super(line, column);
		this.expression = expression;
	}

	public Expression getExpression()
	{
		return expression;
	}

	@Override
	public void compile(Assembler asm, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(RET " + expression + ")";
	}

	public static ReturnStatement parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		ReturnStatement returnStatement = null;

		if (tokens.read().toString().equals("return")) {
			Expression expr = Expression.parse(tokens);
			if(tokens.read().toString().equals(";"))
				returnStatement = new ReturnStatement(expr, line, column);
		}

		tokens.popMark(returnStatement == null);
		return returnStatement;
	}
}
