package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.types.VoidType;
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
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		// While statement creates a new scope.
		Scope loopScope = new Scope(scope, "");
		scope.addSubScope(loopScope);

		// Symbols for break/continue.
		Symbol breakSymbol = new Symbol("Brk", new VoidType(), loopScope,
				"", Symbol.Category.Internal); //__Brk
		loopScope.add(breakSymbol);
		Symbol continueSymbol = new Symbol("Cont", new VoidType(), loopScope,
				"", Symbol.Category.Internal); //__Cont
		loopScope.add(continueSymbol);

		// Loop start.
		String loopStartLabel = scope.makeGloballyUniqueName("lbl");
		asm.emit("jump", continueSymbol.getReference());
		asm.addLabel(loopStartLabel);

		// Body.
		body.compile(asm, loopScope, regs);

		// Loop test code is after the body so that we only need one
		// jump instruction per iteration.
		compileControlExpression(asm, loopScope, regs, loopStartLabel,
				continueSymbol.getReference());

		// Insert label to be used by break statements.
		asm.addLabel(breakSymbol.getReference());
	}

	private void compileControlExpression(Assembler asm, Scope scope,
			Registers regs, String loopStartLabel, String loopTestLabel)
			throws IOException, SyntaxException
	{
		if (!controlExpression.getType(scope).decay().isScalar()) {
			throw new SyntaxException("While loop control expression must have"
					+ " scalar type.", controlExpression.getPosition());
		}

		asm.addLabel(loopTestLabel);
		controlExpression.compile(asm, scope, regs);
		asm.emit("jnzer", regs.get(0).toString(), loopStartLabel);
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
