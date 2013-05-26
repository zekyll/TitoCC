package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Statement that jumps to the next iteration of a loop. Can only appear in
 * loop body.
 *
 * <p> EBNF definition:
 *
 * <br> CONTINUE_STATEMENT = "continue" ";"
 */
public class ContinueStatement extends Statement
{
	/**
	 * Constructs a ContinueStatement.
	 *
	 * @param line starting line number of the continue statement
	 * @param column starting column/character of the continue statement
	 */
	public ContinueStatement(int line, int column)
	{
		super(line, column);
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		Symbol jumpPosition = scope.find("__Cont");

		if (jumpPosition == null)
			throw new SyntaxException("Continue used outside of loop.",
					getLine(), getColumn());

		// Jump to next iteration.
		asm.emit("jump", "sp", jumpPosition.getReference());
	}

	@Override
	public String toString()
	{
		return "(CONT)";
	}

	/**
	 * Attempts to parse a continue statement from token stream. If parsing
	 * fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return ContinueStatement object or null if tokens don't form a valid
	 * continue statement
	 */
	public static ContinueStatement parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		ContinueStatement continueStatement = null;

		if (tokens.read().toString().equals("continue")) {
			if (tokens.read().toString().equals(";"))
				continueStatement = new ContinueStatement(line, column);
		}

		tokens.popMark(continueStatement == null);
		return continueStatement;
	}
}
