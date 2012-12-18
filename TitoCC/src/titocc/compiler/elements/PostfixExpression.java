package titocc.compiler.elements;

import java.io.Writer;
import java.util.Arrays;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class PostfixExpression extends Expression
{
	static final String[] postfixOperators = {"++", "--"};
	private String operator;
	private Expression operand;

	public PostfixExpression(String operator, Expression operand, int line, int column)
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
		return "(POST_EXPR " + operator + " " + operand + ")";
	}

	public static Expression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		
		Expression expr = PrimaryExpression.parse(tokens);

		if(expr != null) {
			String op = tokens.read().toString();
			if (Arrays.asList(postfixOperators).contains(op))
				expr = new PostfixExpression(op, expr, line, column);
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
