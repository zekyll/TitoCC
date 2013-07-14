package titocc.compiler.elements;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.types.CType;
import titocc.compiler.types.IntegerType;
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
	 * Mapping of suffixes to types, as specified in ($6.4.4.1/5). Long long not supported yet.
	 */
	private static Map<String, IntegerType[]> suffixes = new HashMap<String, IntegerType[]>()
	{
		void put(String[] sfxs, IntegerType[] types)
		{
			for (String sfx : sfxs)
				put(sfx, types);
		}

		{
			put(new String[]{""},
					new IntegerType[]{CType.INT, CType.UINT, CType.LONG, CType.ULONG});
			put(new String[]{"u", "U"},
					new IntegerType[]{CType.UINT, CType.ULONG});
			put(new String[]{"l", "L"},
					new IntegerType[]{CType.LONG, CType.ULONG});
			put(new String[]{"ul", "uL", "Ul", "UL"},
					new IntegerType[]{CType.ULONG});
			put(new String[]{"lu", "lU", "Lu", "LU"},
					new IntegerType[]{CType.ULONG});
			put(new String[]{"ll", "LL"},
					null);
			put(new String[]{"ull", "uLL", "Ull", "ULL"},
					null);
			put(new String[]{"llu", "llU", "LLu", "LLU"},
					null);
		}
	};

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
	public CType getType(Scope scope) throws SyntaxException
	{
		String suffix = token.getSuffix().replace("U", "u").replace("LL", "ll");
		IntegerType[] types = suffixes.get(suffix);
		if (types == null) {
			if (suffixes.containsKey(suffix))
				throw new SyntaxException("Unsupported suffix on integer literal.", getPosition());
			else
				throw new SyntaxException("Illegal suffix on integer literal.", getPosition());
		}

		BigInteger val = new BigInteger(token.getValue(), token.getBase());
		IntegerType biggest = null;
		for (IntegerType type : types) {
			if (token.getBase() == 10 && !suffix.contains("u") && !type.isSigned())
				continue;
			if (val.compareTo(type.getMinValue()) >= 0 && val.compareTo(type.getMaxValue()) <= 0)
				return type;
			biggest = type;
		}

		biggest = biggest.toUnsigned();
		if (val.compareTo(biggest.getMinValue()) >= 0 && val.compareTo(biggest.getMaxValue()) <= 0)
			throw new SyntaxException("Integer literal is too large to fit signed type.",
					getPosition());

		throw new SyntaxException("Integer literal is too large to fit any supported type.",
				getPosition());
	}

	@Override
	public BigInteger getCompileTimeValue(Scope scope) throws SyntaxException
	{
		// Type of the compile time constant doesn't matter (yet), but we still need to check
		// correctness.
		getType(scope);

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
