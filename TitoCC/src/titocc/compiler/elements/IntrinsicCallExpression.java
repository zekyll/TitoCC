package titocc.compiler.elements;

import java.io.IOException;
import java.util.Arrays;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.types.CType;
import titocc.compiler.types.IntType;
import titocc.compiler.types.VoidType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Similar to function call expression but calls an intrinsic function.
 * Currently two instrinsic functions are supported: in() and out(x) that
 * correspond to the in/out instructions in ttk-91.
 *
 * <p> EBNF definition:
 *
 * <br> INTRINSIC_CALL_EXPRESSION = ("in" | "out") ARGUMENT_LIST
 */
public class IntrinsicCallExpression extends Expression
{
	/**
	 * List of instrinsic function names.
	 */
	static final String[] intrinsicFunctions = {"in", "out"};
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
	 * @param line starting line number of the intrinsic call expression
	 * @param column starting column/character of the intrinsic call expression
	 */
	public IntrinsicCallExpression(String name, ArgumentList argumentList,
			int line, int column)
	{
		super(line, column);
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
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		if (name.equals("in"))
			compileIn(asm, scope, regs);
		else if (name.equals("out"))
			compileOut(asm, scope, regs);
	}

	private void compileIn(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		if (!argumentList.getArguments().isEmpty())
			throw new SyntaxException("Number of arguments doesn't match the number of parameters.", getLine(), getColumn());

		asm.emit("in", regs.get(0).toString(), "=kbd");
	}

	private void compileOut(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		if (argumentList.getArguments().size() != 1)
			throw new SyntaxException("Number of arguments doesn't match the number of parameters.", getLine(), getColumn());

		argumentList.getArguments().get(0).compile(asm, scope, regs);
		asm.emit("out", regs.get(0).toString(), "=crt");
	}

	@Override
	public CType getType(Scope scope)
	{
		if (name.equals("in"))
			return new IntType();
		else if (name.equals("out"))
			return new VoidType();
		return null;
	}

	@Override
	public String toString()
	{
		return "(INTR_EXPR " + name + " " + argumentList + ")";
	}

	/**
	 * Attempts to parse an intrinsic call expression from token stream, given
	 * the first operand of the expression. If parsing fails the stream is reset
	 * to its initial position.
	 *
	 * @param firstOperand preparsed function expression
	 * @param tokens source token stream
	 * @return IntrinsicCallExpression object or null if tokens don't form a
	 * valid intrinsic call expression
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
							firstOperand.getLine(), firstOperand.getColumn());
				}
			}
		}

		return expr;
	}
}
