package titocc.compiler.elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.compiler.StackAllocator;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * A list of statements surrounded by {}. The compound statement creates a new block.
 *
 * <p> EBNF Definition:
 *
 * <br> COMPOUND_STATEMENT = "{" {STATEMENT} "}"
 */
public class CompoundStatement extends Statement
{
	/**
	 * Statements inside the compound statement.
	 */
	private final List<Statement> statements;

	/**
	 * Constructs a CompoundStatement.
	 *
	 * @param statements list of statements in the compound
	 * @param position starting position of the compound statement
	 */
	public CompoundStatement(List<Statement> statements, Position position)
	{
		super(position);
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
	public void compile(Assembler asm, Scope scope, StackAllocator stack)
			throws IOException, SyntaxException
	{
		// Create new scope for the block.
		Scope blockScope = new Scope(scope, "");
		scope.addSubScope(blockScope);

		// Compile statements.
		for (Statement st : statements)
			st.compile(asm, blockScope, stack);
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
	 * Attempts to parse a compound statement from token stream. If parsing fails the stream is
	 * reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return CompoundStatement object or null if tokens don't form a valid compound statement
	 */
	public static CompoundStatement parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		CompoundStatement compoundStatement = null;

		if (tokens.read().toString().equals("{")) {
			List<Statement> statements = new LinkedList<Statement>();

			Statement statement = Statement.parse(tokens);
			while (statement != null) {
				statements.add(statement);
				statement = Statement.parse(tokens);
			}

			if (tokens.read().toString().equals("}"))
				compoundStatement = new CompoundStatement(statements, pos);
		}

		tokens.popMark(compoundStatement == null);
		return compoundStatement;
	}
}
