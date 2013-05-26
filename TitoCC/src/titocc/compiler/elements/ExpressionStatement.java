package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Statement that evaluates an expression, ignoring the return value.
 *
 * <p> EBNF definition:
 *
 * <br> EXPRESSION_STATEMENT = EXPRESSION ";"
 */
public class ExpressionStatement extends Statement
{
	/**
	 * The expression that the statement evaluates.
	 */
	private final Expression expression;

	/**
	 * Constructs an ExpressionStatement.
	 *
	 * @param expression an expression
	 * @param position starting position of the expression statement
	 */
	public ExpressionStatement(Expression expression, Position position)
	{
		super(position);
		this.expression = expression;
	}

	/**
	 * Returns the expression.
	 *
	 * @return the expression
	 */
	public Expression expression()
	{
		return expression;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		expression.compile(asm, scope, regs);
	}

	@Override
	public String toString()
	{
		return "(EXPR_ST " + expression + ")";
	}

	/**
	 * Attempts to parse an expression statement from token stream. If parsing
	 * fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return ExpressionStatement object or null if tokens don't form a valid
	 * expression statement
	 */
	public static ExpressionStatement parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		ExpressionStatement exprStatement = null;

		Expression expr = Expression.parse(tokens);
		if (expr != null)
			if (tokens.read().toString().equals(";"))
				exprStatement = new ExpressionStatement(expr, pos);

		tokens.popMark(exprStatement == null);
		return exprStatement;
	}
}
