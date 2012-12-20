package titocc.compiler.elements;

import java.util.Arrays;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class AssignmentExpression extends Expression
{
	static final String[] assignmentOperators = {
		"=", "+=", "-=", "*=", "/=", "%=", "<<", ">>", "&=", "^=", "|="
	};
	private String operator;
	private Expression left, right;

	public AssignmentExpression(String operator, Expression left,
			Expression right, int line, int column)
	{
		super(line, column);
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	public String getOperator()
	{
		return operator;
	}

	public Expression getLeft()
	{
		return left;
	}

	public Expression getRight()
	{
		return right;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Integer getCompileTimeValue()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return "(ASGN_EXPR " + operator + " " + left + " " + right + ")";
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
