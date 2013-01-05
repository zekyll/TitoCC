package titocc.compiler.elements;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

/**
 * Specifies a type. Currently only "void" and "int" are supported.
 *
 * <p> EBNF definition:
 *
 * <br> TYPE = "void" | "int"
 */
public class Type extends CodeElement
{
	static final String[] types = {"void", "int"};
	static final Set<String> typesSet = new HashSet<String>(Arrays.asList(types));
	private String name;

	/**
	 * Constructs a Type.
	 *
	 * @param name name of the type
	 * @param line starting line number of the type
	 * @param column starting column/character of the type
	 */
	public Type(String name, int line, int column)
	{
		super(line, column);
		this.name = name;
	}

	/**
	 * Returns the name of the type.
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	@Override
	public String toString()
	{
		return "(TYPE " + name + ")";
	}

	/**
	 * Attempts to parse a type specifier from token stream. If parsing fails
	 * the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Type object or null if tokens don't form a valid type specifier
	 */
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
