package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
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
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public static IdentifierExpression parse(TokenStream tokens)
	{
		return null;
	}
}
