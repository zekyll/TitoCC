package titocc.compiler.elements;

import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Scope;
import titocc.compiler.StackAllocator;
import titocc.compiler.VirtualRegister;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * If statement. Consists of control expression, a "true statement" and optional "else statement".
 *
 * <p> EBNF definition:
 *
 * <br> IF_STATEMENT = "if" "(" EXPRESSION ")" STATEMENT ["else" STATEMENT]
 */
public class IfStatement extends Statement
{
	/**
	 * Test expression.
	 */
	private final Expression controlExpression;

	/**
	 * Statement executed when test is true.
	 */
	private final Statement trueStatement;

	/**
	 * Statement executed when test is false. Null if not used.
	 */
	private final Statement elseStatement;

	/**
	 * Constructs an IfStatement.
	 *
	 * @param controlExpression expression used as the test
	 * @param trueStatement statement evaluated when test is not 0
	 * @param elseStatement statement evaluated when test is 0; this parameter can be null if there
	 * is no else statement
	 * @param position starting position of the if statement
	 */
	public IfStatement(Expression controlExpression, Statement trueStatement,
			Statement elseStatement, Position position)
	{
		super(position);
		this.controlExpression = controlExpression;
		this.trueStatement = trueStatement;
		this.elseStatement = elseStatement;
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
	 * Returns the true/first statement.
	 *
	 * @return the true statement
	 */
	public Statement getTrueStatement()
	{
		return trueStatement;
	}

	/**
	 * Returns the else/second statement.
	 *
	 * @return the else statement, or null if there isn't one
	 */
	public Statement getElseStatement()
	{
		return elseStatement;
	}

	@Override
	public void compile(IntermediateCompiler ic, Scope scope, StackAllocator stack)
			throws SyntaxException
	{
		String skipTrueLabel = scope.makeGloballyUniqueName("lbl");

		compileControlExpression(controlExpression, ic, scope, null, skipTrueLabel, "jzer");

		// True statement.
		compileInNewScope(ic, scope, stack, trueStatement);

		// Else statement.
		if (elseStatement != null) {
			String skipElseLabel = scope.makeGloballyUniqueName("lbl");
			ic.emit("jump", VirtualRegister.NONE, skipElseLabel);
			ic.addLabel(skipTrueLabel);
			compileInNewScope(ic, scope, stack, elseStatement);
			ic.addLabel(skipElseLabel);
		} else
			ic.addLabel(skipTrueLabel);
	}

	@Override
	public String toString()
	{
		return "(IF " + controlExpression + " " + trueStatement + " " + elseStatement + ")";
	}

	private void compileInNewScope(IntermediateCompiler ic, Scope scope, StackAllocator stack,
			Statement statement) throws SyntaxException
	{
		Scope subScope = new Scope(scope, "");
		scope.addSubScope(subScope);
		statement.compile(ic, subScope, stack);
	}

	/**
	 * Attempts to parse an if statement from token stream. If parsing fails the stream is reset to
	 * its initial position.
	 *
	 * @param tokens source token stream
	 * @return IfStatement object or null if tokens don't form a valid if statement
	 */
	public static IfStatement parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		IfStatement ifStatement = null;

		if (tokens.read().toString().equals("if")) {
			if (tokens.read().toString().equals("(")) {
				Expression test = Expression.parse(tokens);
				if (test != null) {
					if (tokens.read().toString().equals(")")) {
						Statement trueStatement = Statement.parse(tokens);
						if (trueStatement != null) {
							Statement elseStatement = parseElseStatement(tokens);
							ifStatement = new IfStatement(test, trueStatement, elseStatement, pos);
						}
					}
				}
			}
		}

		tokens.popMark(ifStatement == null);
		return ifStatement;
	}

	private static Statement parseElseStatement(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		Statement elseStatement = null;

		if (tokens.read().toString().equals("else"))
			elseStatement = Statement.parse(tokens);

		tokens.popMark(elseStatement == null);
		return elseStatement;
	}
}
