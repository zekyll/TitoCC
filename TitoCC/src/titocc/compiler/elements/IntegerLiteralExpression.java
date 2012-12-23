package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.IntegerLiteralToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class IntegerLiteralExpression extends Expression
{
	private int value;
	private String suffix;

	public IntegerLiteralExpression(int value, String suffix, int line, int column)
	{
		super(line, column);
		this.value = value;
		this.suffix = suffix;
	}

	public int getValue()
	{
		return value;
	}

	public String getSuffix()
	{
		return suffix;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException
	{
		//TODO ICE if registers.empty()

		// Use immediate operand if value fits in 16 bits; otherwise allocate
		// a data constant. Load value in first available register.
		if (value < 65536 && value >= -65536)
			asm.emit("load", registers.peek().toString(), "=" + value);
		else {
			String name = scope.makeGloballyUniqueName("int_lit_" + value);
			asm.addLabel(name);
			asm.emit("dc", "" + value);
			asm.emit("load", registers.peek().toString(), name);
		}
	}

	@Override
	public Integer getCompileTimeValue() throws SyntaxException
	{
		if (!suffix.isEmpty())
			throw new SyntaxException("Suffixes on literals are not supported.", getLine(), getColumn());
		return value;
	}

	@Override
	public String toString()
	{
		return "(INT_EXPR " + value + (suffix.isEmpty() ? "" : " " + suffix) + ")";
	}

	public static IntegerLiteralExpression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		IntegerLiteralExpression intExpr = null;

		Token token = tokens.read();
		if (token instanceof IntegerLiteralToken) {
			IntegerLiteralToken intToken = (IntegerLiteralToken) token;
			intExpr = new IntegerLiteralExpression(intToken.getValue(), intToken.getSuffix(), line, column);
		}

		tokens.popMark(intExpr == null);
		return intExpr;
	}
}
