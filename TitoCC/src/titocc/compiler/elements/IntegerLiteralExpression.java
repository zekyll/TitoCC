package titocc.compiler.elements;

import java.io.IOException;
import java.math.BigInteger;
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
	private String rawValue, suffix;

	public IntegerLiteralExpression(String rawValue, String suffix, int line, int column)
	{
		super(line, column);
		this.rawValue = rawValue;
		this.suffix = suffix;
	}

	public String getRawValue()
	{
		return rawValue;
	}

	public String getSuffix()
	{
		return suffix;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		//TODO ICE if registers.empty()

		compileConstantExpression(asm, scope, registers);
	}

	@Override
	public Integer getCompileTimeValue() throws SyntaxException
	{
		if (!suffix.isEmpty())
			throw new SyntaxException("Suffixes on literals are not supported.", getLine(), getColumn());

		// If the literal is too big, only take the least significant 32 bits.
		// BigInteger.intValue() automatically does this.
		return new BigInteger(rawValue).intValue();
	}

	@Override
	public String toString()
	{
		return "(INT_EXPR " + rawValue + (suffix.isEmpty() ? "" : " " + suffix) + ")";
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
