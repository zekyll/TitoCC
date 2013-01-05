package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Statement that exits the from the current function, optionally setting the
 * return value.
 *
 * <p> EBNF definition:
 *
 * <br> RETURN_STATEMENT = "return" [EXPRESSION] ";"
 */
public class ReturnStatement extends Statement
{
	private Expression expression;

	/**
	 * Constructs a ReturnStatement.
	 *
	 * @param expression expression for the returned value, or null if there is
	 * none
	 * @param line starting line number of the return statement
	 * @param column starting column/character of the return statement
	 */
	public ReturnStatement(Expression expression, int line, int column)
	{
		super(line, column);
		this.expression = expression;
	}

	/**
	 * Returns the return value expression.
	 *
	 * @return return value expression or null if there isn't one
	 */
	public Expression getExpression()
	{
		return expression;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		// No need to check whether function actually has return type
		// because C allows returning stuff from any function.
		if (expression != null) {
			// Loads expression's value to first available register
			expression.compile(asm, scope, registers);

			// Store the register to return value
			Symbol retVal = scope.find("__Ret");
			asm.emit("store", registers.peek().toString(), retVal.getReference());
		}

		// Jump to function end
		Symbol functionEnd = scope.find("__End");
		asm.emit("jump", "sp", functionEnd.getReference());
	}

	@Override
	public String toString()
	{
		return "(RET " + expression + ")";
	}

	/**
	 * Attempts to parse a return statement from token stream. If parsing fails
	 * the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return ReturnStatement object or null if tokens don't form a valid
	 * return statement
	 */
	public static ReturnStatement parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		ReturnStatement returnStatement = null;

		if (tokens.read().toString().equals("return")) {
			Expression expr = Expression.parse(tokens);
			if (tokens.read().toString().equals(";"))
				returnStatement = new ReturnStatement(expr, line, column);
		}

		tokens.popMark(returnStatement == null);
		return returnStatement;
	}
}
