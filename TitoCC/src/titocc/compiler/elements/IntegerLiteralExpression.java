package titocc.compiler.elements;

import java.math.BigInteger;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.types.CType;
import titocc.tokenizer.IntegerLiteralToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Integer literal expression. Consists of digits and suffix. Suffixes are not currently supported.
 *
 * <p> EBNF definition:
 *
 * <br> INTEGER_LITERAL_EXPRESSION = INTEGER_LITERAL
 */
public class IntegerLiteralExpression extends Expression
{
	/**
	 * Token that specifies the digits, suffix and base system.
	 */
	private final IntegerLiteralToken token;

	/**
	 * Constructs an IntegerLiteralExpression.
	 *
	 * @param integerToken corresponding token
	 * @param position starting position of the integer literal expression
	 */
	public IntegerLiteralExpression(IntegerLiteralToken integerToken, Position position)
	{
		super(position);
		this.token = integerToken;
	}

	@Override
	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		return compileConstantExpression(ic, scope);
	}

	@Override
	public CType getType(Scope scope)
	{
		return CType.INT;
	}

	@Override
	public BigInteger getCompileTimeValue() throws SyntaxException
	{
		if (!token.getSuffix().isEmpty())
			throw new SyntaxException("Suffixes on literals are not supported.", getPosition());

		return new BigInteger(token.getValue(), token.getBase());
	}

	@Override
	public String toString()
	{
		return "(INT_EXPR " + token.toString() + ")";
	}

	/**
	 * Attempts to parse an integer literal expression from token stream. If parsing fails the
	 * stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static IntegerLiteralExpression parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		IntegerLiteralExpression intExpr = null;

		Token token = tokens.read();
		if (token instanceof IntegerLiteralToken)
			intExpr = new IntegerLiteralExpression((IntegerLiteralToken) token, pos);

		tokens.popMark(intExpr == null);
		return intExpr;
	}
}
