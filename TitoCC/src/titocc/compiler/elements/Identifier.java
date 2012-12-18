package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class Identifier extends CodeElement
{
	private String name;

	public Identifier(String name, int line, int column)
	{
		super(line, column);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(ID " + name + ")";
	}

	public static Identifier parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Identifier id = null;

		Token token = tokens.read();
		if (token instanceof IdentifierToken)
			id = new Identifier(token.toString(), line, column);

		tokens.popMark(id == null);
		return id;
	}
}
