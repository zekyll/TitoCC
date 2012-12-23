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

public class Parameter extends CodeElement implements Symbol
{
	private Type type;
	private String name;
	private String globallyUniqueName;

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

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers) throws SyntaxException
	{
		if (type.getName().equals("void"))
			throw new SyntaxException("Parameter type cannot be void.", getLine(), getColumn());
		if (!scope.add(this))
			throw new SyntaxException("Redefinition of \"" + name + "\".", getLine(), getColumn());
		globallyUniqueName = scope.makeGloballyUniqueName(name);
		scope.add(this);
	}

	@Override
	public String getGlobalName()
	{
		return globallyUniqueName;
	}

	@Override
	public String getReference()
	{
		return globallyUniqueName + "(fp)";
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