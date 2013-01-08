package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * While statement. Consists of a test expression and a statement.
 *
 * <p> EBNF definition:
 *
 * <br> WHILE_STATEMENT = "while" "(" EXPRESSION ")" STATEMENT
 */
public class WhileStatement extends Statement
{
	private Expression test;
	private Statement statement;

	/**
	 * Constructs a WhileStatement.
	 *
	 * @param test
	 * @param statement
	 * @param line starting line number of the while statement
	 * @param column starting column/character of the while statement
	 */
	public WhileStatement(Expression test, Statement statement, int line, int column)
	{
		super(line, column);
		this.test = test;
		this.statement = statement;
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
	 * Returns the body of the while statement.
	 *
	 * @return the body statement
	 */
	public Statement getStatement()
	{
		return statement;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		if (!test.getType(scope).isScalar())
			throw new SyntaxException("Scalar expression required.", getLine(), getColumn());

		// Loop start.
		String loopStartLabel = scope.makeGloballyUniqueName("lbl");
		String loopTestLabel = scope.makeGloballyUniqueName("lbl");
		asm.emit("jump", loopTestLabel);
		asm.addLabel(loopStartLabel);

		// Body.
		Scope subScope = new Scope(scope, "");
		scope.addSubScope(subScope);
		statement.compile(asm, subScope, regs);

		// Loop test code is after the body so that we only need one
		// jump instruction per iteration.
		asm.addLabel(loopTestLabel);
		test.compile(asm, scope, regs);
		asm.emit("jnzer", regs.get(0).toString(), loopStartLabel);
	}

	@Override
	public String toString()
	{
		return "(WHILE " + test + " " + statement + ")";
	}

	/**
	 * Attempts to parse a while statement from token stream. If parsing fails
	 * the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return WhileStatement object or null if tokens don't form a valid while
	 * statement
	 */
	public static WhileStatement parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		WhileStatement whileStatement = null;

		TypeSpecifier retType = TypeSpecifier.parse(tokens);

		if (tokens.read().toString().equals("while")) {
			if (tokens.read().toString().equals("(")) {
				Expression test = Expression.parse(tokens);
				if (test != null) {
					if (tokens.read().toString().equals(")")) {
						Statement statement = Statement.parse(tokens);
						if (statement != null)
							whileStatement = new WhileStatement(test, statement, line, column);
					}
				}
			}
		}

		tokens.popMark(whileStatement == null);
		return whileStatement;
	}
}
