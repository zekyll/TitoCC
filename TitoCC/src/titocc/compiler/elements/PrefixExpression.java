package titocc.compiler.elements;

import java.io.Writer;
import java.util.Arrays;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class PrefixExpression extends Expression
{
	static final String[] prefixOperators = {"++", "--", "+", "-", "!", "~"};
	private String operator;
	private Expression operand;

	public PrefixExpression(String operator, Expression operand, int line, int column)
	{
		super(line, column);
		this.operator = operator;
		this.operand = operand;
	}

	public String getOperator()
	{
		return operator;
	}

	public Expression getOperand()
	{
		return operand;
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(PRE_EXPR " + operator + " " + operand + ")";
	}

	public static Expression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Expression expr = null;

		String op = tokens.read().toString();
		if (Arrays.asList(prefixOperators).contains(op)) {
			Expression operand = Expression.parse(tokens);
			if (operand != null)
				expr = new PrefixExpression(op, operand, line, column);
		}

		tokens.popMark(expr == null);

		if (expr == null)
			expr = FunctionCallExpression.parse(tokens);

		return expr;
	}
}
