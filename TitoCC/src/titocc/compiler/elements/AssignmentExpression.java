package titocc.compiler.elements;

import java.io.Writer;
import java.util.Arrays;
import titocc.compiler.Scope;
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
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
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
			String op = tokens.read().toString();
			if (Arrays.asList(assignmentOperators).contains(op))
				right = AssignmentExpression.parse(tokens);

			tokens.popMark(right == null);
			if (right != null)
				expr = new AssignmentExpression(op, expr, right, line, column);
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
