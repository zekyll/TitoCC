package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Statement that evaluates an expression, ignoring the return value.
 *
 * <p> EBNF definition:
 *
 * <br> EXPRESSION_STATEMENT = EXPRESSION ";"
 */
public class ExpressionStatement extends Statement
{
	private Expression expression;

	/**
	 * Constructs an ExpressionStatement.
	 *
	 * @param expression an expression
	 * @param line starting line number of the expression statement
	 * @param column starting column/character of the expression statement
	 */
	public ExpressionStatement(Expression expression, int line, int column)
	{
		super(line, column);
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
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		expression.compileAsVoid(asm, scope, registers);
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
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		ExpressionStatement exprStatement = null;

		Expression expr = Expression.parse(tokens);
		if (expr != null)
			if (tokens.read().toString().equals(";"))
				exprStatement = new ExpressionStatement(expr, line, column);

		tokens.popMark(exprStatement == null);
		return exprStatement;
	}
}
