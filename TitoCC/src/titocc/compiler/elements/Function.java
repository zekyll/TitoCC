package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class Function extends Declaration
{
	private Type returnType;
	private Identifier identifier;
	private ArgumentList argumentList;
	private BlockStatement body;

	public Function(Type returnType, Identifier identifier, ArgumentList argumentList,
			BlockStatement body, int line, int column)
	{
		super(line, column);
		this.returnType = returnType;
		this.identifier = identifier;
		this.argumentList = argumentList;
		this.body = body;
	}

	public Type returnType()
	{
		return returnType;
	}

	public Identifier identifier()
	{
		return identifier;
	}

	public ArgumentList argumentList()
	{
		return argumentList;
	}

	public BlockStatement body()
	{
		return body;
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(FUNC " + returnType + " " + identifier + " " + argumentList
				+ " " + body + ")";
	}

	public static Function parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Function function = null;

		Type returnType = Type.parse(tokens);

		if (returnType != null) {
			Identifier identifier = Identifier.parse(tokens);
			if (identifier != null) {
				if (tokens.read().toString().equals("(")) {
					ArgumentList argumentList = ArgumentList.parse(tokens);
					if (argumentList != null) {
						if (tokens.read().toString().equals(")")) {
							BlockStatement body = BlockStatement.parse(tokens);
							if (body != null) {
								function = new Function(returnType, identifier, argumentList,
										body, line, column);
							}
						}
					}
				}
			}

		}

		tokens.popMark(function == null);
		return function;
	}
}
