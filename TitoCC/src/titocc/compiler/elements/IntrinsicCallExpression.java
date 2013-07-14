package titocc.compiler.elements;

import java.math.BigInteger;
import java.util.Arrays;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.InternalCompilerException;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.VirtualRegister;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Similar to function call expression but calls an intrinsic function. Currently four instrinsic
 * functions are supported: in()/out(x) that correspond to TTK-91 I/O instructions "in Rx, =KBD" /
 * "out Rx, =CRT" and in2(dev)/out2(dev, x) which allow I/O with arbitrary devices. The device
 * number must be a compile time integer constant.
 *
 * <p> EBNF definition:
 *
 * <br> INTRINSIC_CALL_EXPRESSION = ("in" | "out" | "in2" | "out2") ARGUMENT_LIST
 */
public class IntrinsicCallExpression extends Expression
{
	/**
	 * List of instrinsic function names.
	 */
	static final String[] intrinsicFunctions = {"in", "out", "in2", "out2"};

	/**
	 * Name of the intrinsic function to call.
	 */
	private final String name;

	/**
	 * Argument list for the intrinsic call.
	 */
	private ArgumentList argumentList;

	/**
	 * Constructs an IntrinsicCallExpression.
	 *
	 * @param name name of the intrinsic call
	 * @param argumentList arguments given to the intrinsic call
	 * @param position starting position of the intrinsic call expression
	 */
	public IntrinsicCallExpression(String name, ArgumentList argumentList,
			Position position)
	{
		super(position);
		this.name = name;
		this.argumentList = argumentList;
	}

	/**
	 * Returns the name of the intrinsic function.
	 *
	 * @return intrinsic function name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the argument list.
	 *
	 * @return the argument list.
	 */
	public ArgumentList getArgumentList()
	{
		return argumentList;
	}

	@Override
	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		if (name.equals("in"))
			return compileIn(ic, scope);
		else if (name.equals("in2"))
			return compileIn2(ic, scope);
		else if (name.equals("out"))
			return compileOut(ic, scope);
		else //if (name.equals("out2"))
			return compileOut2(ic, scope);
	}

	private Rvalue compileIn(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		checkArgumentCount(0);

		VirtualRegister retReg = new VirtualRegister();
		ic.emit("in", retReg, "=kbd");

		return new Rvalue(retReg);
	}

	private Rvalue compileIn2(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		checkArgumentCount(1);

		VirtualRegister retReg = new VirtualRegister();
		ic.emit("in", retReg, "=" + getDeviceNumber());

		return new Rvalue(retReg);
	}

	private Rvalue compileOut(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		checkArgumentCount(1);

		Rvalue argVal = argumentList.getArguments().get(0).compile(ic, scope);
		ic.emit("out", argVal.getRegister(), "=crt");

		return new Rvalue(null);
	}

	private Rvalue compileOut2(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		checkArgumentCount(2);

		Rvalue argVal = argumentList.getArguments().get(1).compile(ic, scope);
		ic.emit("out", argVal.getRegister(), "=" + getDeviceNumber());

		return new Rvalue(null);
	}

	private int getDeviceNumber() throws SyntaxException
	{
		BigInteger device = argumentList.getArguments().get(0).getCompileTimeValue();
		if (device == null) {
			throw new SyntaxException("Invalid device number for input function. Compile time "
					+ "constant required.", getPosition());
		}
		return device.intValue();
	}

	private void checkArgumentCount(int expected) throws SyntaxException
	{
		//TODO check argument type
		if (argumentList.getArguments().size() != expected) {
			throw new SyntaxException("Number of arguments doesn't match the number of parameters.",
					getPosition());
		}
	}

	@Override
	public CType getType(Scope scope)
	{
		if (name.equals("in") || name.equals("in2"))
			return CType.INT;
		else if (name.equals("out") || name.equals("out2"))
			return CType.VOID;
		throw new InternalCompilerException("Intrinsic call return value not specified.");
	}

	@Override
	public String toString()
	{
		return "(INTR_EXPR " + name + " " + argumentList + ")";
	}

	/**
	 * Attempts to parse an intrinsic call expression from token stream, given the first operand of
	 * the expression. If parsing fails the stream is reset to its initial position.
	 *
	 * @param firstOperand preparsed function expression
	 * @param tokens source token stream
	 * @return IntrinsicCallExpression object or null if tokens don't form a valid intrinsic call
	 * expression
	 */
	public static IntrinsicCallExpression parse(Expression firstOperand, TokenStream tokens)
	{
		IntrinsicCallExpression expr = null;

		if (firstOperand instanceof IdentifierExpression) {
			IdentifierExpression id = (IdentifierExpression) firstOperand;
			if (Arrays.asList(intrinsicFunctions).contains(id.getIdentifier())) {
				ArgumentList argList = ArgumentList.parse(tokens);
				if (argList != null) {
					expr = new IntrinsicCallExpression(id.getIdentifier(), argList,
							firstOperand.getPosition());
				}
			}
		}

		return expr;
	}
}
