package titocc.compiler.elements;

import java.io.IOException;
import java.util.LinkedList;
import titocc.compiler.Assembler;
import titocc.compiler.InternalSymbol;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.types.VoidType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * For statement. Iteration statement that consists of four parts:
 * <br> 1) Initialization statement which can be either an expression
 * statement, object declaration statement or an empty statement.
 * <br> 2) Control expression (Optional). Must have scalar type.
 * <br> 3) Expression (Optional, typically used for loop variable increment).
 * <br> 4) Loop body statement.
 *
 * <br> EBNF definition:
 *
 * <br> FOR_STATEMENT = "for" "(" (EXPRESSION_STATEMENT | DECLARATION_STATEMENT
 * | EMPTY_STATEMENT) [EXPRESSION] ; [EXPRESSION] ")" STATEMENT
 */
public class ForStatement extends Statement
{
	/**
	 * Initialization statement.
	 */
	private final Statement initStatement;

	/**
	 * Control expression (optional).
	 */
	private final Expression controlExpression;

	/**
	 * Increment expression (optional).
	 */
	private final Expression incrementExpression;

	/**
	 * Statement to execute in the loop.
	 */
	private final Statement body;

	/**
	 * Constructs a ForStatement that uses a declaration as the first
	 * part.
	 *
	 * @param initStatement Initialization statement.
	 * @param controlExpression Control expression (can be null).
	 * @param incrementExpression Increment expression (can be null).
	 * @param body Loop body statement.
	 * @param position starting position of the for statement
	 */
	public ForStatement(Statement initStatement, Expression controlExpression,
			Expression incrementExpression, Statement body, Position position)
	{
		super(position);
		this.initStatement = initStatement;
		this.controlExpression = controlExpression;
		this.incrementExpression = incrementExpression;
		this.body = body;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		// The whole for statement creates a new scope.
		Scope loopScope = new Scope(scope, "");
		scope.addSubScope(loopScope);

		// Symbols for break/continue.
		Symbol breakSymbol = new InternalSymbol("Brk", loopScope, "", new VoidType()); //__Brk
		loopScope.add(breakSymbol);
		Symbol continueSymbol = new InternalSymbol("Cont", loopScope, "", new VoidType()); //__Cont
		loopScope.add(continueSymbol);

		// Reserve labels.
		String loopStartLabel = loopScope.makeGloballyUniqueName("lbl");
		String loopTestLabel = loopScope.makeGloballyUniqueName("lbl");

		// Loop initialization code.
		initStatement.compile(asm, loopScope, regs);

		// Loop start; jump to the test.
		asm.emit("jump", loopTestLabel);
		asm.addLabel(loopStartLabel);

		// Body.
		body.compile(asm, loopScope, regs);
		//TODO implement block-item-list so body can't be a single declaration

		// Evaluate the increment expression and ignore return value.
		asm.addLabel(continueSymbol.getReference());
		if (incrementExpression != null)
			incrementExpression.compile(asm, loopScope, regs);

		// Loop test code is after the body so that we only need one
		// jump instruction per iteration.
		compileControlExpression(asm, loopScope, regs, loopStartLabel,
				loopTestLabel);

		// Insert label to be used by break statements.
		asm.addLabel(breakSymbol.getReference());
	}

	private void compileControlExpression(Assembler asm, Scope scope,
			Registers regs, String loopStartLabel, String loopTestLabel)
			throws IOException, SyntaxException
	{
		if (controlExpression != null
				&& !controlExpression.getType(scope).decay().isScalar())
			throw new SyntaxException("For loop control expression must have"
					+ " scalar type.", controlExpression.getPosition());

		asm.addLabel(loopTestLabel);

		// Jump to loop start if not 0. If there is no control expression then
		// make unconditional jump.
		if (controlExpression != null) {
			controlExpression.compile(asm, scope, regs);
			asm.emit("jnzer", regs.get(0).toString(), loopStartLabel);
		} else
			asm.emit("jump", loopStartLabel);
	}

	@Override
	public String toString()
	{
		return "(FOR " + initStatement + " " + controlExpression + " "
				+ incrementExpression + " " + body + ")";
	}

	/**
	 * Attempts to parse a for statement from token stream. If parsing fails
	 * the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return ForStatement object or null if tokens don't form a valid for
	 * statement
	 */
	public static ForStatement parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		ForStatement forStatement = null;

		try {
			if (!tokens.read().toString().equals("for"))
				return null;

			if (!tokens.read().toString().equals("("))
				return null;

			Statement initStatement = parseInitStatement(tokens);
			if (initStatement == null)
				return null;

			Expression controlExpression = Expression.parse(tokens);

			if (!tokens.read().toString().equals(";"))
				return null;

			Expression incrementExpression = Expression.parse(tokens);

			if (!tokens.read().toString().equals(")"))
				return null;

			Statement body = Statement.parse(tokens);
			if (body == null)
				return null;

			forStatement = new ForStatement(initStatement, controlExpression,
					incrementExpression, body, pos);
		} finally {
			tokens.popMark(forStatement == null);
		}

		return forStatement;
	}

	public static Statement parseInitStatement(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();

		Statement statement = ExpressionStatement.parse(tokens);

		if (statement == null)
			statement = DeclarationStatement.parse(tokens);

		// Empty statement.
		if (statement == null && tokens.read().toString().equals(";"))
			statement = new BlockStatement(new LinkedList<Statement>(), pos);

		tokens.popMark(statement == null);
		return statement;
	}
}
