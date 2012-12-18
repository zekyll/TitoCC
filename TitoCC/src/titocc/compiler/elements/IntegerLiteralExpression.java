package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.IntegerLiteralToken;
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
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(INT_EXPR " + value + " " + suffix + ")";
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
