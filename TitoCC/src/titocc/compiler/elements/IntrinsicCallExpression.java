package titocc.compiler.elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class IntrinsicCallExpression extends Expression
{
	static final String[] intrinsicFunctions = {"in", "out"};
	private String name;
	private ArgumentList argumentList;

	public IntrinsicCallExpression(String name, ArgumentList argumentList,
			int line, int column)
	{
		super(line, column);
		this.name = name;
		this.argumentList = argumentList;
	}

	public String getName()
	{
		return name;
	}

	public ArgumentList getArgumentList()
	{
		return argumentList;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws SyntaxException, IOException
	{
		compile(asm, scope, registers, true);
	}

	@Override
	public void compileAsVoid(Assembler asm, Scope scope, Stack<Register> registers)
			throws SyntaxException, IOException
	{
		compile(asm, scope, registers, false);
	}

	private void compile(Assembler asm, Scope scope, Stack<Register> registers,
			boolean returnValueRequired) throws SyntaxException, IOException
	{
		if (name.equals("in"))
			compileIn(asm, scope, registers, returnValueRequired);
		else if (name.equals("out"))
			compileOut(asm, scope, registers, returnValueRequired);
	}

	private void compileIn(Assembler asm, Scope scope, Stack<Register> registers,
			boolean returnValueRequired) throws SyntaxException, IOException
	{
		if (!argumentList.getArguments().isEmpty())
			throw new SyntaxException("Number of arguments doesn't match the number of parameters.", getLine(), getColumn());

		asm.emit("in", registers.peek().toString(), "=kbd");
	}

	private void compileOut(Assembler asm, Scope scope, Stack<Register> registers,
			boolean returnValueRequired) throws SyntaxException, IOException
	{
		if (returnValueRequired)
			throw new SyntaxException("Void return value used in an expression.", getLine(), getColumn());

		if (argumentList.getArguments().size() != 1)
			throw new SyntaxException("Number of arguments doesn't match the number of parameters.", getLine(), getColumn());

		argumentList.getArguments().get(0).compile(asm, scope, registers);
		asm.emit("out", registers.peek().toString(), "=crt");
	}

	@Override
	public String toString()
	{
		return "(INTR_EXPR " + name + " " + argumentList + ")";
	}

	public static Expression parse(TokenStream tokens)
	{
		// Test if matches IntrinsicCallExpression.
		Expression expr = parseSelf(tokens);

		// If not, try FunctionCallExpression.
		if (expr == null)
			expr = FunctionCallExpression.parse(tokens);

		return expr;
	}

	private static IntrinsicCallExpression parseSelf(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		IntrinsicCallExpression expr = null;

		Token name = tokens.read();
		if (name instanceof IdentifierToken) {
			if (Arrays.asList(intrinsicFunctions).contains(name.toString())) {
				ArgumentList argList = ArgumentList.parse(tokens);
				if (argList != null)
					expr = new IntrinsicCallExpression(name.toString(), argList, line, column);
			}
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
