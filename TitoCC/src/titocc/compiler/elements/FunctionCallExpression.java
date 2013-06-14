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
	 * Expression used as the function pointer.
	 */
	private final Expression functionPointer;

	/**
	 * Argument list for the function call.
	 */
	private final ArgumentList argumentList;

	/**
	 * Constructs a function call expression.
	 *
	 * @param functionPointer expression that will be evaluated as the function pointer
	 * @param argumentList list of arguments passed to the function
	 * @param position starting position of the function call expression
	 */
	public FunctionCallExpression(Expression functionPointer, ArgumentList argumentList,
			Position position)
	{
		super(position);
		this.functionPointer = functionPointer;
		this.argumentList = argumentList;
	}

	/**
	 * Returns the function pointer expression.
	 *
	 * @return the function
	 */
	public Expression getFunctionPointerExpression()
	{
		return functionPointer;
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
		FunctionType funcType = getFunctionType(scope);

		// Reserve space for return value.
		if (!funcType.getReturnType().equals(CType.VOID))
			asm.emit("add", "sp", "=" + funcType.getReturnType().getSize());

		// Push arguments to stack.
		argumentList.compile(asm, scope, regs, funcType.getParameterTypes());

		// Evaluate the function pointer.
		String funcReference = compileFunctionPointer(asm, scope, regs);

		// Make the call.
		asm.emit("call", "sp", funcReference);

		// Read the return value.
		if (!funcType.getReturnType().equals(CType.VOID))
			asm.emit("pop", "sp", regs.get(0).toString());
	}

	private String compileFunctionPointer(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		Symbol func = functionPointer.getFunction(scope);
		if (func != null)
			return func.getReference();

		functionPointer.compile(asm, scope, regs);
		return regs.get(0).toString();
	}

	private FunctionType getFunctionType(Scope scope) throws SyntaxException
	{
		Symbol func = functionPointer.getFunction(scope);
		if (func != null)
			return (FunctionType) func.getType();

		CType funcType = functionPointer.getType(scope).decay().dereference();
		if (!funcType.isFunction()) {
			throw new SyntaxException("Expression does not evaluate to a function pointer.",
					getPosition());
		}

		return (FunctionType) funcType;
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		return getFunctionType(scope).getReturnType();
	}

	@Override
	public String toString()
	{
		return "(FCALL_EXPR " + functionPointer + " " + argumentList + ")";
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
