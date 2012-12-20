package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public abstract class PrimaryExpression extends Expression
{
	public PrimaryExpression(int line, int column)
	{
		super(line, column);
	}

	@Override
	public void compile(Assembler asm, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public static Expression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();

		Expression expr = IdentifierExpression.parse(tokens);

		if (expr == null)
			expr = IntegerLiteralExpression.parse(tokens);

		if (expr == null) {
			if(tokens.read().toString().equals("(")) {
				expr = Expression.parse(tokens);
				if(expr != null && !tokens.read().toString().equals(")"))
					expr = null;
			}
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
