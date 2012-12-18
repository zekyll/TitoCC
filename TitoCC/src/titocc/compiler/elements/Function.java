package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class Function extends Declaration
{
	private Type returnType;
	private String identifier;
	private ParameterList parameterList;
	private BlockStatement body;

	public Function(Type returnType, String identifier, ParameterList parameterList,
			BlockStatement body, int line, int column)
	{
		super(line, column);
		this.returnType = returnType;
		this.identifier = identifier;
		this.parameterList = parameterList;
		this.body = body;
	}

	public Type getReturnType()
	{
		return returnType;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public ParameterList getParameterList()
	{
		return parameterList;
	}

	public BlockStatement getBody()
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
		return "(FUNC " + returnType + " " + identifier + " " + parameterList
				+ " " + body + ")";
	}

	public static Function parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Function function = null;

		Type retType = Type.parse(tokens);

		if (retType != null) {
			Token id = tokens.read();
			if (id instanceof IdentifierToken) {
				if (tokens.read().toString().equals("(")) {
					ParameterList paramList = ParameterList.parse(tokens);
					if (paramList != null) {
						if (tokens.read().toString().equals(")")) {
							BlockStatement body = BlockStatement.parse(tokens);
							if (body != null) {
								function = new Function(retType, id.toString(), paramList,
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
