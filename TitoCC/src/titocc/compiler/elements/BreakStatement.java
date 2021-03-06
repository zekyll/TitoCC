package titocc.compiler.elements;

import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Scope;
import titocc.compiler.StackAllocator;
import titocc.compiler.Symbol;
import titocc.compiler.VirtualRegister;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Statement that breaks the execution of a loop (for/do-while/for) or switch statement and jumps to
 * the next statement after it. Can only appear in loop/switch body.
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
	public void compile(IntermediateCompiler ic, Scope scope, StackAllocator stack)
			throws SyntaxException
	{
		Symbol jumpPosition = scope.find("__Brk");

		if (jumpPosition == null)
			throw new SyntaxException("Break used outside loop or switch.", getPosition());

		// Jump to end of the loop/switch
		ic.emit("jump", VirtualRegister.NONE, jumpPosition.getReference());
	}

	@Override
	public String toString()
	{
		return "(BRK)";
	}

	/**
	 * Attempts to parse a break statement from token stream. If parsing fails the stream is reset
	 * to its initial position.
	 *
	 * @param tokens source token stream
	 * @return BreakStatement object or null if tokens don't form a valid break statement
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
