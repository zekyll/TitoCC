package titocc.compiler.elements;

import java.util.LinkedList;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.StackAllocator;
import titocc.compiler.VirtualRegister;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Abstract base for all statements.
 *
 * <p> EBNF definition:
 *
 * <br> STATEMENT = EXPRESSION_STATEMENT | DECLARATION_STATEMENT | IF_STATEMENT
 * | WHILE_STATEMENT | FOR_STATEMENT | COMPOUND_STATEMENT | JUMP_STATEMENT | ";"
 */
public abstract class Statement extends CodeElement
{
	/**
	 * Constructs a Statement.
	 *
	 * @param position starting position of the statement
	 */
	public Statement(Position position)
	{
		super(position);
	}

	/**
	 * Generates assembly code for the statement.
	 *
	 * @param ic intermediate compiler used for code generation
	 * @param scope scope in which the statement is evaluated
	 * @param stack allocator for local stack data
	 * @throws SyntaxException if statement contains an error
	 */
	public abstract void compile(IntermediateCompiler ic, Scope scope, StackAllocator stack)
			throws SyntaxException;

	/**
	 * Helper function for compiling if/for/do/while control expressions and corresponding jumps.
	 *
	 * @param controlExpr control expression, if null then unconditional jump is generated
	 * @param ic intermediate compiler used for code generation
	 * @param scope scope in which the control expression is evaluated
	 * @param testLabel label added just before the test
	 * @param jumpLabel jump target label when test is true
	 * @param jumpInstr mnemonic for the jump/test instruction
	 */
	protected static void compileControlExpression(Expression controlExpr, IntermediateCompiler ic,
			Scope scope, String testLabel, String jumpLabel, String jumpInstr)
			throws SyntaxException
	{
		if (controlExpr != null && !controlExpr.getType(scope).decay().isScalar()) {
			throw new SyntaxException("Illegal control expression. Scalar type required.",
					controlExpr.getPosition());
		}

		if (testLabel != null)
			ic.addLabel(testLabel);

		// Only generate code for the test if there is a control expression. Otherwise make
		// unconditional jump (only allowed in for loops).
		if (controlExpr != null) {
			Rvalue val = controlExpr.compileWithConversion(ic, scope, CType.BOOLISH);

			// Jump if test was false/true (depending on jump instruction).
			ic.emit(jumpInstr, val.getRegister(), jumpLabel); // jnzer/jzer
		} else
			ic.emit("jump", VirtualRegister.NONE, jumpLabel);
	}

	/**
	 * Attempts to parse statement from token stream. If parsing fails the stream is reset to its
	 * initial position.
	 *
	 * @param tokens source token stream
	 * @return Statement object or null if tokens don't form a valid statement
	 */
	public static Statement parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();

		Statement statement = ExpressionStatement.parse(tokens);

		if (statement == null)
			statement = DeclarationStatement.parse(tokens);

		if (statement == null)
			statement = IfStatement.parse(tokens);

		if (statement == null)
			statement = WhileStatement.parse(tokens);

		if (statement == null)
			statement = DoStatement.parse(tokens);

		if (statement == null)
			statement = ForStatement.parse(tokens);

		if (statement == null)
			statement = CompoundStatement.parse(tokens);

		if (statement == null)
			statement = JumpStatement.parse(tokens);

		// Empty statement.
		if (statement == null && tokens.read().toString().equals(";"))
			statement = new CompoundStatement(new LinkedList<Statement>(), pos);

		tokens.popMark(statement == null);
		return statement;
	}
}
