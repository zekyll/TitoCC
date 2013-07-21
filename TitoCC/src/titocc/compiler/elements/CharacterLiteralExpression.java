package titocc.compiler.elements;

import java.math.BigInteger;
import java.util.List;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.types.CType;
import titocc.tokenizer.CharacterLiteralToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Character literal expression. E.g. 'a' or 'ab'. Has "int" type. If the literal specifies
 * multiple characters then the value of the last character is used. (Implementation defined
 * behavior.) Because both char and int are 32 bits on TTK-91, the int value resulting from
 * multicharacter literals can only use one of the characters.
 *
 * <p> EBNF definition:
 *
 * <br> CHARACTER_LITERAL_EXPRESSION = CHARACTER_LITERAL
 */
public class CharacterLiteralExpression extends Expression
{
	/**
	 * List of int values specified by the character literal.
	 */
	private List<Integer> values;

	/**
	 * Constructs an IntegerLiteralExpression.
	 *
	 * @param value value of the character literal
	 * @param position starting position of the integer literal expression
	 */
	public CharacterLiteralExpression(List<Integer> values, Position position)
	{
		super(position);
		this.values = values;
	}

	@Override
	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		return compileConstantExpression(ic, scope);
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		return CType.INT;
	}

	@Override
	public BigInteger getCompileTimeValue(Scope scope) throws SyntaxException
	{
		return BigInteger.valueOf(values.get(values.size() - 1));
	}

	@Override
	public String toString()
	{
		String ret = "(CHR";
		for (Integer i : values)
			ret += " " + i;
		return ret + ")";
	}

	/**
	 * Attempts to parse a character literal expression from token stream. If parsing fails the
	 * stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return CharacterLiteralExpression object or null if tokens don't form a valid character
	 * literal expression
	 */
	public static CharacterLiteralExpression parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		CharacterLiteralExpression charExpr = null;

		Token token = tokens.read();
		if (token instanceof CharacterLiteralToken) {
			CharacterLiteralToken charToken = (CharacterLiteralToken) token;
			charExpr = new CharacterLiteralExpression(charToken.getValues(), pos);
		}

		tokens.popMark(charExpr == null);
		return charExpr;
	}
}
