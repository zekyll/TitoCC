package titocc.compiler.elements;

import java.util.HashMap;
import java.util.Map;
import titocc.compiler.types.CType;
import titocc.compiler.types.IntType;
import titocc.compiler.types.VoidType;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Specifies a type. Currently only "void" and "int" are supported.
 *
 * <p> EBNF definition:
 *
 * <br> TYPE_SPECIFIER = "void" | "int"
 */
public class TypeSpecifier extends CodeElement
{
	/**
	 * Supported fundamental types.
	 */
	static final Map<String, CType> typeMap = new HashMap<String, CType>()
	{
		{
			put("void", new VoidType());
			put("int", new IntType());
		}
	};

	/**
	 * Type name for this type specifier.
	 */
	private final String name;

	/**
	 * Constructs a Type.
	 *
	 * @param name name of the type
	 * @param position starting position of the type
	 */
	public TypeSpecifier(String name, Position position)
	{
		super(position);
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

	/**
	 * Returns the type corresponding to the type specifier.
	 *
	 * @return the type
	 */
	public CType getType()
	{
		return typeMap.get(name);
	}

	@Override
	public String toString()
	{
		return "(TYPE " + name + ")";
	}

	/**
	 * Attempts to parse a type specifier from token stream. If parsing fails the stream is reset to
	 * its initial position.
	 *
	 * @param tokens source token stream
	 * @return Type object or null if tokens don't form a valid type specifier
	 */
	public static TypeSpecifier parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		TypeSpecifier type = null;

		Token token = tokens.read();
		if (typeMap.containsKey(token.toString()))
			type = new TypeSpecifier(token.toString(), pos);

		tokens.popMark(type == null);
		return type;
	}
}
