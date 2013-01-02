package titocc.compiler.elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
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
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws SyntaxException, IOException
	{
		// Currently the only lvalue expression is variable identifier, so
		// we can just get the variable name.
		String ref = operand.getLvalueReference(scope);
		if (ref == null)
			throw new SyntaxException("Operator requires an lvalue.", getLine(), getColumn());

		// Load value in register.
		asm.emit("load", registers.peek().toString(), ref);

		// Modify and write back the value.
		asm.emit(operator.equals("++") ? "add" : "sub", registers.peek().toString(), "=1");
		asm.emit("store", registers.peek().toString(), ref);

		// Expression must return the old value.
		asm.emit(operator.equals("++") ? "sub" : "add", registers.peek().toString(), "=1");
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

		if (expr != null) {
			Expression postfixExpr = null;
			do {
				postfixExpr = null;

				tokens.pushMark();
				String op = tokens.read().toString();
				if (Arrays.asList(postfixOperators).contains(op))
					postfixExpr = new PostfixExpression(op, expr, line, column);
				tokens.popMark(postfixExpr == null);

				if (postfixExpr == null)
					postfixExpr = IntrinsicCallExpression.parse(expr, tokens);

				if (postfixExpr == null)
					postfixExpr = FunctionCallExpression.parse(expr, tokens);

				if (postfixExpr != null)
					expr = postfixExpr;
			} while (postfixExpr != null);
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
