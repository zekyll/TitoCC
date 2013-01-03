package titocc.compiler.elements;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

/**
 * Specifies a type. Currently only "void" and "int" are supported.
 */
public class Type extends CodeElement
{
	static final String[] types = {"void", "int"};
	static final Set<String> typesSet = new HashSet<String>(Arrays.asList(types));
	private String name;

	public Type(String name, int line, int column)
	{
		super(line, column);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public String toString()
	{
		return "(TYPE " + name + ")";
	}

	public static Type parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Type type = null;

		Token token = tokens.read();
		if (typesSet.contains(token.toString()))
			type = new Type(token.toString(), line, column);

		tokens.popMark(type == null);
		return type;
	}
}
