package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.StackAllocator;
import titocc.compiler.Symbol;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Statement that exits the from the current function, optionally setting the return value.
 *
 * <p> EBNF definition:
 *
 * <br> RETURN_STATEMENT = "return" [EXPRESSION] ";"
 */
public class ReturnStatement extends Statement
{
	/**
	 * Returned value expression. Null if not used.
	 */
	private final Expression expression;

	/**
	 * Constructs a ReturnStatement.
	 *
	 * @param expression expression for the returned value, or null if there is none
	 * @param position starting position of the return statement
	 */
	public ReturnStatement(Expression expression, Position position)
	{
		super(position);
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
	public void compile(Assembler asm, Scope scope, StackAllocator stack)
			throws IOException, SyntaxException
	{
		Symbol retVal = scope.find("__Ret");

		// Compile return value.
		if (expression != null) {
			if (!expression.isAssignableTo(retVal.getType(), scope)) {
				throw new SyntaxException("Returned expression doesn't match return value type.",
						getPosition());
			}

			// Load expression to first register and store to the return value.
			Register exprReg = expression.compileAndAllocateRegisters(asm, scope, stack,
					retVal.getType());
			asm.emit("store", exprReg, retVal.getReference());
		} else {
			if (!retVal.getType().equals(CType.VOID))
				throw new SyntaxException("Function must return a value.", getPosition());
		}

		// Jump to function end
		Symbol functionEnd = scope.find("__End");
		asm.emit("jump", Register.SP, functionEnd.getReference());
	}

	@Override
	public String toString()
	{
		return "(RET " + expression + ")";
	}

	/**
	 * Attempts to parse a return statement from token stream. If parsing fails the stream is reset
	 * to its initial position.
	 *
	 * @param tokens source token stream
	 * @return ReturnStatement object or null if tokens don't form a valid return statement
	 */
	public static ReturnStatement parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		ReturnStatement returnStatement = null;

		if (tokens.read().toString().equals("return")) {
			Expression expr = Expression.parse(tokens);
			if (tokens.read().toString().equals(";"))
				returnStatement = new ReturnStatement(expr, pos);
		}

		tokens.popMark(returnStatement == null);
		return returnStatement;
	}
}
