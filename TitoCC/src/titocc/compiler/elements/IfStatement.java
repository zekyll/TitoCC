package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * If statement. Consists of test expression, a "true statement" and optional "else statement".
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
	private final Expression test;

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
	 * @param test expression used as the test
	 * @param trueStatement statement evaluated when test is not 0
	 * @param elseStatement statement evaluated when test is 0; this parameter can be null if there
	 * is no else statement
	 * @param position starting position of the if statement
	 */
	public IfStatement(Expression test, Statement trueStatement, Statement elseStatement,
			Position position)
	{
		super(position);
		this.test = test;
		this.trueStatement = trueStatement;
		this.elseStatement = elseStatement;
	}

	/**
	 * Returns the test expression.
	 *
	 * @return the test expression
	 */
	public Expression getTest()
	{
		return test;
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
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		if (!test.getType(scope).decay().isScalar())
			throw new SyntaxException("Scalar expression required.", test.getPosition());

		// Evaluates and loads the test expression in the first register.
		test.compile(asm, scope, regs);

		// Skip true statement if test was false.
		String skipTrueLabel = scope.makeGloballyUniqueName("lbl");
		asm.emit("jzer", regs.get(0).toString(), skipTrueLabel);

		// True statement.
		compileInNewScope(asm, scope, regs, trueStatement);

		// Else statement.
		if (elseStatement != null) {
			String skipElseLabel = scope.makeGloballyUniqueName("lbl");
			asm.emit("jump", skipElseLabel);
			asm.addLabel(skipTrueLabel);
			compileInNewScope(asm, scope, regs, elseStatement);
			asm.addLabel(skipElseLabel);
		} else
			asm.addLabel(skipTrueLabel);
	}

	@Override
	public String toString()
	{
		return "(IF " + test + " " + trueStatement + " " + elseStatement + ")";
	}

	private void compileInNewScope(Assembler asm, Scope scope, Registers registers,
			Statement statement) throws IOException, SyntaxException
	{
		Scope subScope = new Scope(scope, "");
		scope.addSubScope(subScope);
		statement.compile(asm, subScope, registers);
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

		TypeSpecifier retType = TypeSpecifier.parse(tokens);

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
