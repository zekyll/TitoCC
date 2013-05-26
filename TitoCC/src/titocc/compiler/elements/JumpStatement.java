package titocc.compiler.elements;

import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Abstract placeholder class for parsing jump statements.
 *
 * <p> EBNF definition:
 *
 * <br> JUMP_STATEMENT = GOTO_STATEMENT | CONTINUE_STATEMENT | BREAK_STATEMENT
 * | RETURN_STATEMENT
 */
public class JumpStatement
{
	/**
	 * Attempts to parse a jump statement from token stream. If parsing fails
	 * the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Statement object or null if tokens don't form a valid jump
	 * statement
	 */
	public static Statement parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();

		Statement statement = null;

		if (statement == null)
			statement = ContinueStatement.parse(tokens);

		if (statement == null)
			statement = BreakStatement.parse(tokens);

		if (statement == null)
			statement = ReturnStatement.parse(tokens);

		tokens.popMark(statement == null);
		return statement;
	}
}
