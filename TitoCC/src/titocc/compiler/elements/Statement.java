package titocc.compiler.elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Abstract base for all statements.
 */
public abstract class Statement extends CodeElement
{
	/**
	 * Constructs a new Statement.
	 *
	 * @param line starting line number of the statement
	 * @param column starting column/character of the statement
	 */
	public Statement(int line, int column)
	{
		super(line, column);
	}

	/**
	 * Generates assembly code for the statement.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope in which the statement is evaluated
	 * @param registers available registers; must contain at least one register
	 * @throws SyntaxException if statement contains an error
	 * @throws IOException if assembler throws
	 */
	public abstract void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException;

	/**
	 * Parses a statement from token stream.
	 *
	 * @param tokens source token stream
	 * @return Statement object or null if tokens don't form a valid statement
	 */
	public static Statement parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();

		Statement statement = ExpressionStatement.parse(tokens);

		if (statement == null)
			statement = DeclarationStatement.parse(tokens);

		if (statement == null)
			statement = IfStatement.parse(tokens);

		if (statement == null)
			statement = WhileStatement.parse(tokens);

		if (statement == null)
			statement = BlockStatement.parse(tokens);

		if (statement == null)
			statement = ReturnStatement.parse(tokens);

		// Empty statement.
		if (statement == null && tokens.read().toString().equals(";"))
			statement = new BlockStatement(new LinkedList<Statement>(), line, column);

		tokens.popMark(statement == null);
		return statement;
	}
}
