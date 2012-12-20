package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class IdentifierExpression extends Expression
{
	private String identifier;

	public IdentifierExpression(String identifier, int line, int column)
	{
		super(line, column);
		this.identifier = identifier;
	}

	public String identifier()
	{
		return identifier;
	}

	@Override
	public void compile(Assembler asm, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Integer getCompileTimeValue()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return "(ID_EXPR " + identifier + ")";
	}

	public static IdentifierExpression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		IdentifierExpression idExpr = null;

		Token token = tokens.read();
		if (token instanceof IdentifierToken)
			idExpr = new IdentifierExpression(token.toString(), line, column);

		tokens.popMark(idExpr == null);
		return idExpr;
	}
}
