package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.Vstack;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Do statement. Similar to while statement but the control expression is evaluated after the body.
 *
 * <p> EBNF definition:
 *
 * <br> DO_STATEMENT = "do" STATEMENT "while" "(" EXPRESSION ")" ";"
 */
public class DoStatement extends Statement
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
	 * Constructs a DoStatement.
	 *
	 * @param controlExpression control expression
	 * @param body loop body
	 * @param position starting position of the do statement
	 */
	public DoStatement(Expression controlExpression, Statement body, Position position)
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
	 * Returns the body of the do statement.
	 *
	 * @return the body statement
	 */
	public Statement getBody()
	{
		return body;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Vstack vstack)
			throws IOException, SyntaxException
	{
		// Do statement creates a new scope.
		Scope loopScope = new Scope(scope, "");
		scope.addSubScope(loopScope);

		// Symbols for break/continue.
		Symbol breakSymbol = new Symbol("__Brk", CType.VOID, Symbol.Category.Internal,
				null, false);
		loopScope.add(breakSymbol);
		Symbol continueSymbol = new Symbol("__Cont", CType.VOID, Symbol.Category.Internal,
				null, false);
		loopScope.add(continueSymbol);

		// Loop start.
		String loopStartLabel = scope.makeGloballyUniqueName("lbl");
		asm.addLabel(loopStartLabel);

		// Body.
		body.compile(asm, loopScope, vstack);

		// Test.
		compileControlExpression(asm, loopScope, vstack, loopStartLabel,
				continueSymbol.getReference());

		// Insert end label to be used by break statements.
		asm.addLabel(breakSymbol.getReference());
	}

	private void compileControlExpression(Assembler asm, Scope scope,
			Vstack vstack, String loopStartLabel, String loopTestLabel)
			throws IOException, SyntaxException
	{
		if (!controlExpression.getType(scope).decay().isScalar()) {
			throw new SyntaxException("Loop control expression must have a"
					+ " scalar type.", controlExpression.getPosition());
		}

		asm.addLabel(loopTestLabel);
		controlExpression.compile(asm, scope, vstack);
		Register exprReg = vstack.loadTopValue(asm);
		asm.emit("jnzer", exprReg, loopStartLabel);
		vstack.pop();
	}

	@Override
	public String toString()
	{
		return "(DO " + controlExpression + " " + body + ")";
	}

	/**
	 * Attempts to parse a do statement from token stream. If parsing fails the stream is reset to
	 * its initial position.
	 *
	 * @param tokens source token stream
	 * @return DoStatement object or null if tokens don't form a valid do statement
	 */
	public static DoStatement parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		DoStatement doStatement = null;

		if (tokens.read().toString().equals("do")) {
			Statement statement = Statement.parse(tokens);
			if (statement != null) {
				if (tokens.read().toString().equals("while")) {
					if (tokens.read().toString().equals("(")) {
						Expression test = Expression.parse(tokens);
						if (test != null) {
							if (tokens.read().toString().equals(")")
									&& tokens.read().toString().equals(";"))
								doStatement = new DoStatement(test, statement, pos);
						}
					}
				}
			}
		}

		tokens.popMark(doStatement == null);
		return doStatement;
	}
}
