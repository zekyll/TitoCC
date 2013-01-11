package titocc.compiler.elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * A compound statement that executes a list of statements.
 *
 * <p> EBNF Definition:
 *
 * <br> BLOCK_STATEMENT = "{" {STATEMENT} "}"
 */
public class BlockStatement extends Statement
{
	/**
	 * Statements inside the block statement.
	 */
	private final List<Statement> statements;

	/**
	 * Constructs a new block statement.
	 *
	 * @param statements list of statements in the block
	 * @param line starting line number of the block statement
	 * @param column starting column/character of the block statement
	 */
	public BlockStatement(List<Statement> statements, int line, int column)
	{
		super(line, column);
		this.statements = statements;
	}

	/**
	 * Returns the statements.
	 *
	 * @return list of statements
	 */
	public List<Statement> getStatements()
	{
		return statements;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		//Create new scope for the block.
		Scope blockScope = new Scope(scope, "");
		scope.addSubScope(blockScope);

		// Compile statements
		for (Statement st : statements)
			st.compile(asm, blockScope, regs);
	}

	@Override
	public String toString()
	{
		String str = "(BLK_ST";
		for (Statement st : statements)
			str += " " + st;
		return str + ")";
	}

	/**
	 * Attempts to parse a block statement from token stream. If parsing fails
	 * the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return BlockStatement object or null if tokens don't form a valid
	 * block statement
	 */
	public static BlockStatement parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		BlockStatement blockStatement = null;

		if (tokens.read().toString().equals("{")) {
			List<Statement> statements = new LinkedList<Statement>();

			Statement statement = Statement.parse(tokens);
			while (statement != null) {
				statements.add(statement);
				statement = Statement.parse(tokens);
			}

			if (tokens.read().toString().equals("}"))
				blockStatement = new BlockStatement(statements, line, column);
		}

		tokens.popMark(blockStatement == null);
		return blockStatement;
	}
}
