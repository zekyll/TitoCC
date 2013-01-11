package titocc.compiler.elements;

import java.io.IOException;
import java.math.BigInteger;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.types.CType;
import titocc.compiler.types.IntType;
import titocc.tokenizer.IntegerLiteralToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

/**
 * Integer literal expression. Consists of digits and suffix. Suffixes are not
 * currently supported.
 *
 * <p> EBNF definition:
 *
 * <br> INTEGER_LITERAL_EXPRESSION = INTEGER_LITERAL
 */
public class IntegerLiteralExpression extends Expression
{
	/**
	 * Digits as a string.
	 */
	private final String rawValue;
	/**
	 * Suffix as a string.
	 */
	private final String suffix;

	/**
	 * Constructs an IntegerLiteralExpression.
	 *
	 * @param rawValue integer digits as a string
	 * @param suffix suffix
	 * @param line starting line number of the integer literal expression
	 * @param column starting column/character of the integer literal expression
	 */
	public IntegerLiteralExpression(String rawValue, String suffix, int line, int column)
	{
		super(line, column);
		this.rawValue = rawValue;
		this.suffix = suffix;
	}

	/**
	 * Returns the digits of the integer literal.
	 *
	 * @return string representation of the digits
	 */
	public String getRawValue()
	{
		return rawValue;
	}

	/**
	 * Returns the suffix of the literal.
	 *
	 * @return the suffix
	 */
	public String getSuffix()
	{
		return suffix;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		compileConstantExpression(asm, scope, regs);
	}

	@Override
	public CType getType(Scope scope)
	{
		return new IntType();
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

	/**
	 * Attempts to parse an integer literal expression from token stream. If
	 * parsing fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
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
