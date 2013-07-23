package titocc.compiler.elements;

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
 * While statement. Consists of a scalar control expression and a body statement.
 *
 * <p> EBNF definition:
 *
 * <br> WHILE_STATEMENT = "while" "(" EXPRESSION ")" STATEMENT
 */
public class WhileStatement extends Statement
{
	/**
	 * Control expression.
	 */
	private final Expression controlExpression;

	/**
	 * Statement to execute in the loop.
	 */
	private final Statement body;

	/**
	 * Constructs a WhileStatement.
	 *
	 * @param controlExpression
	 * @param body
	 * @param position starting position of the while statement
	 */
	public WhileStatement(Expression controlExpression, Statement body, Position position)
	{
		super(position);
		this.controlExpression = controlExpression;
		this.body = body;
	}

	/**
	 * Returns the control expression.
	 *
	 * @return the control expression
	 */
	public Expression getControlExpression()
	{
		return controlExpression;
	}

	/**
	 * Returns the body of the while statement.
	 *
	 * @return the body statement
	 */
	public Statement getBody()
	{
		return body;
	}

	@Override
	public void compile(IntermediateCompiler ic, Scope scope, StackAllocator stack)
			throws SyntaxException
	{
		// While statement creates a new scope.
		Scope loopScope = new Scope(scope, "");
		scope.addSubScope(loopScope);

		// Symbols for break/continue.
		Symbol breakSymbol = new Symbol("__Brk", CType.VOID, StorageClass.Static, false);
		loopScope.add(breakSymbol);
		Symbol continueSymbol = new Symbol("__Cont", CType.VOID, StorageClass.Static, false);
		loopScope.add(continueSymbol);

		// Loop start.
		String loopStartLabel = scope.makeGloballyUniqueName("lbl");
		ic.emit("jump", VirtualRegister.NONE, continueSymbol.getReference());
		ic.addLabel(loopStartLabel);

		// Body.
		body.compile(ic, loopScope, stack);

		// Loop test code is after the body so that we only need one
		// jump instruction per iteration.
		compileControlExpression(controlExpression, ic, loopScope, continueSymbol.getReference(),
				loopStartLabel, "jnzer");

		// Insert label to be used by break statements.
		ic.addLabel(breakSymbol.getReference());
	}

	@Override
	public String toString()
	{
		return "(WHILE " + controlExpression + " " + body + ")";
	}

	/**
	 * Attempts to parse a while statement from token stream. If parsing fails the stream is reset
	 * to its initial position.
	 *
	 * @param tokens source token stream
	 * @return WhileStatement object or null if tokens don't form a valid while statement
	 */
	public static WhileStatement parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		WhileStatement whileStatement = null;

		if (tokens.read().toString().equals("while")) {
			if (tokens.read().toString().equals("(")) {
				Expression test = Expression.parse(tokens);
				if (test != null) {
					if (tokens.read().toString().equals(")")) {
						Statement statement = Statement.parse(tokens);
						if (statement != null)
							whileStatement = new WhileStatement(test, statement, pos);
					}
				}
			}
		}

		tokens.popMark(whileStatement == null);
		return whileStatement;
	}
}
