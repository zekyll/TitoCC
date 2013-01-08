package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.types.CType;
import titocc.compiler.types.VoidType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Function call expression.
 *
 * <p> EBNF definition:
 *
 * <br> FUNCTION_CALL_EXPRESSION = POSTFIX_EXPRESSION ARGUMENT_LIST
 */
public class FunctionCallExpression extends Expression
{
	private Expression function;
	private ArgumentList argumentList;

	/**
	 * Constructs a function call expression.
	 *
	 * @param function expression that will be evaluated as the function
	 * @param argumentList list of arguments passed to the function
	 * @param line starting line number of the function call expression
	 * @param column starting column/character of the function call expression
	 */
	public FunctionCallExpression(Expression function, ArgumentList argumentList,
			int line, int column)
	{
		super(line, column);
		this.function = function;
		this.argumentList = argumentList;
	}

	/**
	 * Returns the function expression.
	 *
	 * @return the function
	 */
	public Expression getFunction()
	{
		return function;
	}

	/**
	 * Returns the argument list.
	 *
	 * @return the argument list
	 */
	public ArgumentList getArgumentList()
	{
		return argumentList;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs) throws SyntaxException, IOException
	{
		Function func = validateFunction(scope);

		// Reserve space for return value.
		if (!func.getReturnType().equals(new VoidType()))
			asm.emit("add", "sp", "=1");

		// Push arguments to stack.
		argumentList.compile(asm, scope, regs, func.getParameterTypes());

		// Make the call.
		asm.emit("call", "sp", func.getReference());

		// Read the return value.
		if (!func.getReturnType().equals(new VoidType()))
			asm.emit("pop", "sp", regs.get(0).toString());
	}

	private Function validateFunction(Scope scope) throws SyntaxException
	{
		Function func = function.getFunction(scope);
		if (func == null)
			throw new SyntaxException("Expression is not a function.", getLine(), getColumn());
		return func;
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		Function func = validateFunction(scope);
		return func.getReturnType();
	}

	@Override
	public String toString()
	{
		return "(FCALL_EXPR " + function + " " + argumentList + ")";
	}

	/**
	 * Attempts to parse a function call expression from token stream, given the
	 * first operand of the expression. If parsing fails the stream is reset to
	 * its initial position.
	 *
	 * @param firstOperand preparsed function expression
	 * @param tokens source token stream
	 * @return FunctionCallExpression object or null if tokens don't form a
	 * valid function call expression
	 */
	public static FunctionCallExpression parse(Expression firstOperand, TokenStream tokens)
	{
		FunctionCallExpression expr = null;

		ArgumentList argList = ArgumentList.parse(tokens);
		if (argList != null) {
			expr = new FunctionCallExpression(firstOperand, argList,
					firstOperand.getLine(), firstOperand.getColumn());
		}

		return expr;
	}
}
