package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

public class ReturnStatement extends Statement
{
	private Expression expression;

	public ReturnStatement(Expression expression, int line, int column)
	{
		super(line, column);
		this.expression = expression;
	}

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
