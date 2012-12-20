package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class Parameter extends CodeElement
{
	private Type type;
	private String identifier;

	public Parameter(Type type, String identifier, int line, int column)
	{
		super(line, column);
		this.type = type;
		this.identifier = identifier;
	}

	private Type getType()
	{
		return type;
	}

	private String getIdentifier()
	{
		return identifier;
	}

	@Override
	public void compile(Assembler asm, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(PRM " + type + " " + identifier + ")";
	}

	public static Parameter parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Parameter param = null;

		Type type = Type.parse(tokens);

		if (type != null) {
			Token id = tokens.read();
			if (id instanceof IdentifierToken)
				param = new Parameter(type, id.toString(), line, column);
		}

		tokens.popMark(param == null);
		return param;
	}
}
