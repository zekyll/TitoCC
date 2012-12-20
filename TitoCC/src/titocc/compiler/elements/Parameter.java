package titocc.compiler.elements;

import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class Parameter extends CodeElement
{
	private Type type;
	private String name;

	public Parameter(Type type, String name, int line, int column)
	{
		super(line, column);
		this.type = type;
		this.name = name;
	}

	public Type getType()
	{
		return type;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers) throws SyntaxException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(PRM " + type + " " + name + ")";
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
