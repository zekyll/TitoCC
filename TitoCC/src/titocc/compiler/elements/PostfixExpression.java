package titocc.compiler.elements;

import java.util.Arrays;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Lvalue;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Expression formed by an operand followed by a postfix operator.
 *
 * <p> EBNF definition:
 *
 * <br> POSTFIX_EXPRESSION = POSTFIX_EXPRESSION ("++" | "--") | INTRINSIC_CALL_EXPRESSION
 * | FUNCTION_CALL_EXPRESSION | SUBSCRIPT_EXPRESSION | PRIMARY_EXPRESSION
 */
public class PostfixExpression extends Expression
{
	/**
	 * List of postfix operators.
	 */
	static final String[] postfixOperators = {"++", "--"};

	/**
	 * Operator for this postfix expression.
	 */
	private final String operator;

	/**
	 * Operand expression.
	 */
	private final Expression operand;

	/**
	 * Constructs a PostfixExpression.
	 *
	 * @param operator postfix operator as a string
	 * @param operand operand expression
	 * @param position starting position of the postfix expression
	 */
	public PostfixExpression(String operator, Expression operand, Position position)
	{
		super(position);
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
	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		// ($6.5.2.4)
		CType operandType = operand.getType(scope).decay();
		if (!operandType.isArithmetic() && !operandType.dereference().isObject()) {
			//TODO arithmetic -> real
			throw new SyntaxException("Operator " + operator
					+ " requires an arithmetic or object pointer type.", getPosition());
		}

		Lvalue val = operand.compileAsLvalue(ic, scope, false);
		boolean inc = operator.equals("++");
		return operandType.compileIncDecOperator(ic, scope, val, inc, true, 1);
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		return operand.getType(scope).decay();
	}

	@Override
	public String toString()
	{
		return "(POST_EXPR " + operator + " " + operand + ")";
	}

	/**
	 * Attempts to parse a syntactic postfix expression from token stream. If parsing fails the
	 * stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static Expression parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();

		Expression expr = PrimaryExpression.parse(tokens);

		if (expr != null) {
			Expression postfixExpr = null;
			do {
				postfixExpr = null;

				tokens.pushMark();
				String op = tokens.read().toString();
				if (Arrays.asList(postfixOperators).contains(op))
					postfixExpr = new PostfixExpression(op, expr, pos);
				tokens.popMark(postfixExpr == null);

				if (postfixExpr == null)
					postfixExpr = IntrinsicCallExpression.parse(expr, tokens);

				if (postfixExpr == null)
					postfixExpr = FunctionCallExpression.parse(expr, tokens);

				if (postfixExpr == null)
					postfixExpr = SubscriptExpression.parse(expr, tokens);

				if (postfixExpr != null)
					expr = postfixExpr;
			} while (postfixExpr != null);
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
