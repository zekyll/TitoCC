package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Statement that breaks the execution of a loop (for/do-while/for) or switch
 * statement and jumps to the next statement after it. Can only appear in
 * loop/switch body.
 *
 * <p> EBNF definition:
 *
 * <br> BREAK_STATEMENT = "break" ";"
 */
public class BreakStatement extends Statement
{
	/**
	 * Constructs a BreakStatement.
	 *
	 * @param position starting position of the break statement
	 */
	public BreakStatement(Position position)
	{
		super(position);
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		Symbol jumpPosition = scope.find("__Brk");

		if (jumpPosition == null)
			throw new SyntaxException("Break used outside loop or switch.",
					getPosition());

		// Jump to end of the loop/switch
		asm.emit("jump", "sp", jumpPosition.getReference());
	}

	@Override
	public String toString()
	{
		return "(BRK)";
	}

	/**
	 * Attempts to parse a break statement from token stream. If parsing fails
	 * the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return BreakStatement object or null if tokens don't form a valid
	 * break statement
	 */
	public static BreakStatement parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		BreakStatement breakStatement = null;

		if (tokens.read().toString().equals("break")) {
			if (tokens.read().toString().equals(";"))
				breakStatement = new BreakStatement(pos);
		}

		tokens.popMark(breakStatement == null);
		return breakStatement;
	}
}
