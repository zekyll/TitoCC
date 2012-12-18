package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class VariableDeclaration extends Declaration
{
	private Type type;
	private String identifier;
	private Expression initializer;

	public VariableDeclaration(Type type, String identifier,
			Expression initializer, int line, int column)
	{
		super(line, column);
		this.type = type;
		this.identifier = identifier;
		this.initializer = initializer;
	}

	private Type getType()
	{
		return type;
	}

	private String getIdentifier()
	{
		return identifier;
	}

	private Expression getInitializer()
	{
		return initializer;
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(VARDECL " + type + " " + identifier + " " + initializer + ")";
	}

	public static VariableDeclaration parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		VariableDeclaration varDeclaration = null;

		Type type = Type.parse(tokens);

		if (type != null) {
			Token id = tokens.read();
			if (id instanceof IdentifierToken) {
				Expression init = parseInitializer(tokens);
				if (tokens.read().toString().equals(";"))
					varDeclaration = new VariableDeclaration(type, id.toString(), init, line, column);
			}
		}

		tokens.popMark(varDeclaration == null);
		return varDeclaration;
	}

	private static Expression parseInitializer(TokenStream tokens)
	{
		tokens.pushMark();
		Expression init = null;

		if (tokens.read().toString().equals("="))
			init = Expression.parse(tokens);

		tokens.popMark(init == null);
		return init;
	}
}
