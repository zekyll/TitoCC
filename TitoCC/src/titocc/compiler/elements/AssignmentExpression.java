package titocc.compiler.elements;

import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class AssignmentExpression extends Expression
{
	static final String[] assignmentOperators = {
		"=", "+=", "-=", "*=", "/=", "%=", "<<", ">>", "&=", "^=", "|="
	};
	private String operation;
	private Expression left, right;

	public AssignmentExpression(String operation, Expression left,
			Expression right, int line, int column)
	{
		super(line, column);
		this.operation = operation;
		this.left = left;
		this.right = right;
	}

	public String operation()
	{
		return operation;
	}

	public Expression left()
	{
		return left;
	}

	public Expression right()
	{
		return right;
	}

	@Override
	public String toString()
	{
		return "(ASGN " + left + " " + right + ")";
	}

	public static Expression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Expression expr = BinaryExpression.parse(tokens);

		if (expr != null) {
			tokens.pushMark();
			Expression right = null;
			Token op = tokens.read();
			if (op.toString().equals("||"))
				right = AssignmentExpression.parse(tokens);

			tokens.popMark(right == null);
			if (right != null)
				expr = new AssignmentExpression(op.toString(), expr, right, line, column);
		}

		tokens.popMark(expr == null);
		return expr;
	}
}