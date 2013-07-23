package titocc.compiler.elements;

import java.util.LinkedList;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Scope;
import titocc.compiler.StackAllocator;
import titocc.compiler.StorageClass;
import titocc.compiler.Symbol;
import titocc.compiler.VirtualRegister;
import titocc.compiler.types.CType;
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
 * <br> FOR_STATEMENT = "for" "(" (EXPRESSION_STATEMENT | DECLARATION_STATEMENT | EMPTY_STATEMENT)
 * [EXPRESSION] ; [EXPRESSION] ")" STATEMENT
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
	 * Constructs a ForStatement that uses a declaration as the first part.
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
	public void compile(IntermediateCompiler ic, Scope scope, StackAllocator stack)
			throws SyntaxException
	{
		// The whole for statement creates a new scope.
		Scope loopScope = new Scope(scope, "");
		scope.addSubScope(loopScope);

		// Loop initialization code.
		compileInitStatement(ic, loopScope, stack);

		// Symbols for break/continue.
		Symbol breakSymbol = new Symbol("__Brk", CType.VOID, StorageClass.Static, false);
		loopScope.add(breakSymbol);
		Symbol continueSymbol = new Symbol("__Cont", CType.VOID, StorageClass.Static, false);
		loopScope.add(continueSymbol);

		// Reserve labels.
		String loopStartLabel = loopScope.makeGloballyUniqueName("lbl");
		String loopTestLabel = loopScope.makeGloballyUniqueName("lbl");

		// Loop start; jump to the test.
		ic.emit("jump", VirtualRegister.NONE, loopTestLabel);
		ic.addLabel(loopStartLabel);

		// Body.
		body.compile(ic, loopScope, stack);
		//TODO implement block-item-list so body can't be a single declaration

		// Evaluate the increment expression and ignore return value.
		ic.addLabel(continueSymbol.getReference());
		if (incrementExpression != null)
			incrementExpression.compileWithConversion(ic, loopScope, CType.VOID);

		// Loop test code is after the body so that we only need one
		// jump instruction per iteration.
		compileControlExpression(controlExpression, ic, loopScope, loopTestLabel,
				loopStartLabel, "jnzer");

		// Insert label to be used by break statements.
		ic.addLabel(breakSymbol.getReference());
	}

	private void compileInitStatement(IntermediateCompiler ic, Scope scope, StackAllocator stack)
			throws SyntaxException
	{
		initStatement.compile(ic, scope, stack);

		// Only object declarations with auto/register (or no) storage class are allowed. ($6.8.5/3)
		for (Symbol s : scope.getSymbols()) {
			if (s.getType().isFunction()) {
				throw new SyntaxException("Function declaration in for loop initialization.",
						initStatement.getPosition());
			}

			if (s.getStorageClass() != null && s.getStorageClass() != StorageClass.Auto
					&& s.getStorageClass() != StorageClass.Register) {
				throw new SyntaxException("Illegal storage class in for loop declaration.",
						initStatement.getPosition());
			}
		}
	}

	@Override
	public String toString()
	{
		return "(FOR " + initStatement + " " + controlExpression + " "
				+ incrementExpression + " " + body + ")";
	}

	/**
	 * Attempts to parse a for statement from token stream. If parsing fails the stream is reset to
	 * its initial position.
	 *
	 * @param tokens source token stream
	 * @return ForStatement object or null if tokens don't form a valid for statement
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
			statement = new CompoundStatement(new LinkedList<Statement>(), pos);

		tokens.popMark(statement == null);
		return statement;
	}
}
