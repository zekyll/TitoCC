package titocc.compiler.elements;

import titocc.compiler.ExpressionAssembler;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.VirtualRegister;
import titocc.compiler.types.CType;
import titocc.compiler.types.FunctionType;
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
	public Rvalue compile(ExpressionAssembler asm, Scope scope) throws SyntaxException
	{
		FunctionType funcType = getFunctionType(scope);

		// Reserve space for return value.
		if (!funcType.getReturnType().equals(CType.VOID))
			asm.emit("add", VirtualRegister.SP, "=" + funcType.getReturnType().getSize());

		// Push arguments to stack.
		argumentList.compile(asm, scope, funcType.getParameterTypes());

		// Evaluate the function pointer.
		Rvalue funcPtrVal = functionPointer.compile(asm, scope);

		// Make the call.
		asm.emit("call", VirtualRegister.SP, funcPtrVal.getRegister());

		// Read the return value.
		VirtualRegister retReg = null;
		if (!funcType.getReturnType().equals(CType.VOID)) {
			retReg = new VirtualRegister();
			asm.emit("pop", VirtualRegister.SP, retReg);
		}

		return new Rvalue(retReg);
	}

	private FunctionType getFunctionType(Scope scope) throws SyntaxException
	{
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
