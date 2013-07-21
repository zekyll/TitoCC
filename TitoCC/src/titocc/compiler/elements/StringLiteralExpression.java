package titocc.compiler.elements;

import java.util.ArrayList;
import java.util.List;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Lvalue;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.VirtualRegister;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.tokenizer.StringLiteralToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * String literal expression. E.g. "abc\ndef". Multiple string literal tokens can concatenated into
 * a single string literal. String literal is null terminated and has the type char[N+1], where N
 * is the length of the literal. The encoding used is UTF-32.
 *
 * <p> EBNF definition:
 *
 * <br> STRING_LITERAL_EXPRESSION = STRING_LITERAL {STRING_LITERAL}
 */
public class StringLiteralExpression extends Expression
{
	/**
	 * Character values.
	 */
	private List<Integer> values;

	/**
	 * Constructs a StringLiteralExpression.
	 *
	 * @param values character values
	 * @param position starting position of the integer literal expression
	 */
	public StringLiteralExpression(List<Integer> values, Position position)
	{
		super(position);
		values.add(0); // Append null termination
		this.values = values;
	}

	@Override
	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		String startLabel = allocateArray(ic, scope);
		VirtualRegister retReg = new VirtualRegister();
		ic.emit("load", retReg, "=" + startLabel);
		return new Rvalue(retReg);
	}

	@Override
	public Lvalue compileAsLvalue(IntermediateCompiler ic, Scope scope, boolean addressOf)
			throws SyntaxException
	{
		if (!addressOf)
			requireLvalueType(scope);

		String startLabel = allocateArray(ic, scope);
		VirtualRegister retReg = new VirtualRegister();
		ic.emit("load", retReg, "=" + startLabel);

		return new Lvalue(retReg);
	}

	/**
	 * Allocates static data area for the string. In current implementations of Titokone
	 * consequtive "dc" commands will allocate consecutive memory locations, so we can take
	 * advantage of that. (Otherwise it would require using "ds" and separate initialization code.)
	 */
	private String allocateArray(IntermediateCompiler ic, Scope scope)
	{
		String startLabel = null;
		for (int i = 0; i < values.size(); ++i) {
			String label = scope.makeGloballyUniqueName("str");
			if (i == 0)
				startLabel = label;
			ic.addLabel(label);
			ic.emit("dc", values.get(i));
		}
		return startLabel;
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		return new ArrayType(CType.CHAR, values.size());
	}

	@Override
	public String toString()
	{
		String ret = "(STR";
		for (int i = 0; i < values.size() - 1; ++i)
			ret += " " + values.get(i);
		return ret + ")";
	}

	/**
	 * Attempts to parse a string literal expression from token stream. If parsing fails the
	 * stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return StringLiteralExpression object or null if tokens don't form a valid string literal
	 */
	public static StringLiteralExpression parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		StringLiteralExpression strExpr = null;

		StringLiteralToken token = parseStringToken(tokens);
		List<Integer> values = null;
		while (token != null) {
			if (values == null)
				values = new ArrayList<Integer>();
			values.addAll(token.getValues());
			token = parseStringToken(tokens);
		}

		if (values != null)
			strExpr = new StringLiteralExpression(values, pos);

		return strExpr;
	}

	private static StringLiteralToken parseStringToken(TokenStream tokens)
	{
		tokens.pushMark();

		Token token = tokens.read();
		StringLiteralToken strToken = null;
		if (token instanceof StringLiteralToken)
			strToken = (StringLiteralToken) token;

		tokens.popMark(strToken == null);
		return strToken;
	}
}