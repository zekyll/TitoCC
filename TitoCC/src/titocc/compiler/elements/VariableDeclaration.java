package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class VariableDeclaration extends Declaration
{
	private Type type;
	private Identifier identifier;
	private Expression initializer;

	public VariableDeclaration(Type type, Identifier identifier,
			Expression initializer, int line, int column)
	{
		super(line, column);
		this.type = type;
		this.identifier = identifier;
		this.initializer = initializer;
	}

	private Type type()
	{
		return type;
	}

	private Identifier identifier()
	{
		return identifier;
	}

	private Expression initializer()
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
			Identifier id = Identifier.parse(tokens);
			if (id != null) {
				Expression init = parseInitializer(tokens);
				if (tokens.read().toString().equals(";"))
					varDeclaration = new VariableDeclaration(type, id, init, line, column);
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
