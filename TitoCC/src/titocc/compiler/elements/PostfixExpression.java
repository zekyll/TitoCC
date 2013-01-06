package titocc.compiler.elements;

import java.io.IOException;
import java.util.Arrays;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Expression formed by an operand followed by a postfix operator.
 *
 * <p> EBNF definition:
 *
 * <br> POSTFIX_EXPRESSION = POSTFIX_EXPRESSION ("++" | "--") |
 * FUNCTION_CALL_EXPRESSION | INTRINSIC_CALL_EXPRESSION | PRIMARY_EXPRESSION
 */
public class PostfixExpression extends Expression
{
	static final String[] postfixOperators = {"++", "--"};
	private String operator;
	private Expression operand;

	/**
	 * Constructs a PostfixExpression.
	 *
	 * @param operator postfix operator as a string
	 * @param operand operand expression
	 * @param line starting line number of the postfix expression
	 * @param column starting column/character of the postfix expression
	 */
	public PostfixExpression(String operator, Expression operand, int line, int column)
	{
		super(line, column);
		this.operator = operator;
		this.operand = operand;
	}

	/**
	 * Returns the operator as a string.
	 *
	 * @return the operator
	 */
	public String getOperator()
	{
		return operator;
	}

	/**
	 * Returns the operand expression.
	 *
	 * @return the operand expression
	 */
	public Expression getOperand()
	{
		return operand;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		// Currently the only lvalue expression is variable identifier, so
		// we can just get the variable name.
		String ref = operand.getLvalueReference(scope);
		if (ref == null)
			throw new SyntaxException("Operator requires an lvalue.", getLine(), getColumn());

		// Load value in register.
		asm.emit("load", regs.get(0).toString(), ref);

		// Modify and write back the value.
		asm.emit(operator.equals("++") ? "add" : "sub", regs.get(0).toString(), "=1");
		asm.emit("store", regs.get(0).toString(), ref);

		// Expression must return the old value.
		asm.emit(operator.equals("++") ? "sub" : "add", regs.get(0).toString(), "=1");
	}

	@Override
	public String toString()
	{
		return "(POST_EXPR " + operator + " " + operand + ")";
	}

	/**
	 * Attempts to parse a syntactic postfix expression from token stream. If
	 * parsing fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
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
