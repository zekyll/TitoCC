package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.types.CType;
import titocc.compiler.types.FunctionType;
import titocc.compiler.types.VoidType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Function call expression.
 *
 * <p> EBNF definition:
 *
 * <br> FUNCTION_CALL_EXPRESSION = POSTFIX_EXPRESSION ARGUMENT_LIST
 */
public class FunctionCallExpression extends Expression
{
	/**
	 * Expression used as the function.
	 */
	private final Expression function;

	/**
	 * Argument list for the function call.
	 */
	private final ArgumentList argumentList;

	/**
	 * Constructs a function call expression.
	 *
	 * @param function expression that will be evaluated as the function
	 * @param argumentList list of arguments passed to the function
	 * @param position starting position of the function call expression
	 */
	public FunctionCallExpression(Expression function, ArgumentList argumentList,
			Position position)
	{
		super(position);
		this.function = function;
		this.argumentList = argumentList;
	}

	/**
	 * Returns the function expression.
	 *
	 * @return the function
	 */
	public Expression getFunctionExpression()
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
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		Symbol func = validateFunction(scope);
		FunctionType funcType = ((FunctionType) func.getType());

		// Reserve space for return value.
		if (!funcType.getReturnType().equals(new VoidType()))
			asm.emit("add", "sp", "=1");

		// Push arguments to stack.
		argumentList.compile(asm, scope, regs, funcType.getParameterTypes());

		// Make the call.
		asm.emit("call", "sp", func.getReference());

		// Read the return value.
		if (!funcType.getReturnType().equals(new VoidType()))
			asm.emit("pop", "sp", regs.get(0).toString());
	}

	private Symbol validateFunction(Scope scope) throws SyntaxException
	{
		Symbol func = function.getFunction(scope);
		if (func == null)
			throw new SyntaxException("Expression is not a function.", getPosition());
		return func;
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		Symbol func = validateFunction(scope);
		FunctionType funcType = ((FunctionType) func.getType());
		return funcType.getReturnType();
	}

	@Override
	public String toString()
	{
		return "(FCALL_EXPR " + function + " " + argumentList + ")";
	}

	/**
	 * Attempts to parse a function call expression from token stream, given the first operand of
	 * the expression. If parsing fails the stream is reset to its initial position.
	 *
	 * @param firstOperand preparsed function expression
	 * @param tokens source token stream
	 * @return FunctionCallExpression object or null if tokens don't form a valid function call
	 * expression
	 */
	public static FunctionCallExpression parse(Expression firstOperand, TokenStream tokens)
	{
		FunctionCallExpression expr = null;

		ArgumentList argList = ArgumentList.parse(tokens);
		if (argList != null)
			expr = new FunctionCallExpression(firstOperand, argList, firstOperand.getPosition());

		return expr;
	}
}
