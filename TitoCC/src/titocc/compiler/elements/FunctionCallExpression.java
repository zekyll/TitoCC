package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.Vstack;
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
	public void compile(Assembler asm, Scope scope, Vstack vstack)
			throws SyntaxException, IOException
	{
		FunctionType funcType = getFunctionType(scope);

		// Reserve space for return value.
		if (!funcType.getReturnType().equals(CType.VOID))
			asm.emit("add", Register.SP, "=" + funcType.getReturnType().getSize());

		// Push arguments to stack.
		argumentList.compile(asm, scope, vstack, funcType.getParameterTypes());

		// Evaluate the function pointer.
		functionPointer.compile(asm, scope, vstack);
		Register funcPtrReg = vstack.loadTopValue(asm); //TODO use @ instead of loading to register

		// Make the call.
		asm.emit("call", Register.SP, funcPtrReg.toString());

		// Deallocate the register reserved for function pointer.
		vstack.pop();

		// Read the return value.
		if (!funcType.getReturnType().equals(CType.VOID)) {
			Register retReg = vstack.pushRegisterRvalue(asm);
			asm.emit("pop", Register.SP, retReg.toString());
		}
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
