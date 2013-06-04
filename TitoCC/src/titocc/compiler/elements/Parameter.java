package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Single parameter in a function parameter list. Consists of a type and a name. Parameters in
 * function definitions must have names, elsewhere unnamed parameters can be used.
 *
 * <p> EBNF definition:
 *
 * <br> PARAMETER = TYPE_SPECIFIER [DECLARATOR]
 */
public class Parameter extends CodeElement
{
	/**
	 * Type specifier (e.g. void/int).
	 */
	private final TypeSpecifier typeSpecifier;

	/**
	 * Declarator which has the parameter name and which modifies the type specifier.
	 */
	private final Declarator declarator;

	/**
	 * Parameter type. Set when compiling the function.
	 */
	private CType type;

	/**
	 * Constructs a Parameter.
	 *
	 * @param typeSpecifier type specifier
	 * @param declarator declarator, or null if parameter is unnamed
	 * @param position starting position of the parameter
	 */
	public Parameter(TypeSpecifier typeSpecifier, Declarator declarator, Position position)
	{
		super(position);
		this.typeSpecifier = typeSpecifier;
		this.declarator = declarator;
	}

	/**
	 * Checks the parameter type and declares a symbol for the parameter.
	 *
	 * @param scope scope in which the parameter is compiled
	 * @param functionDefinition true if the parameter is part of a function definition; disallows
	 * unnamed parameters
	 * @return the symbol for the parameter
	 * @throws SyntaxException if the parameter has invalid type or the name was redefined
	 * @throws IOException
	 */
	public Symbol compile(Scope scope, boolean functionDefinition)
			throws SyntaxException, IOException
	{
		compileType(scope);
		Symbol sym = addSymbol(scope, functionDefinition);
		return sym;
	}

	/**
	 * Deduce parameter type and check that it is valid.
	 */
	private void compileType(Scope scope) throws SyntaxException, IOException
	{
		type = declarator.compile(typeSpecifier.getType(), scope, null);

		if (!type.isObject())
			throw new SyntaxException("Parameter must have object type.", getPosition());
		if (type instanceof ArrayType)
			throw new SyntaxException("Array parameters are not supported.", getPosition());
	}

	private Symbol addSymbol(Scope scope, boolean functionDefinition)
			throws SyntaxException
	{
		String name = declarator.getName();
		if (name == null && functionDefinition)
			throw new SyntaxException("Unnamed parameter in function definition.", getPosition());
		if (name == null)
			name = "__param";

		Symbol sym = new Symbol(name, type, "(fp)", Symbol.Category.Parameter);
		if (!scope.add(sym))
			throw new SyntaxException("Redefinition of \"" + name + "\".", getPosition());
		return sym;
	}

	@Override
	public String toString()
	{
		return "(PRM " + typeSpecifier + " " + declarator + ")";
	}

	/**
	 * Attempts to parse a parameter from token stream. If parsing fails the stream is reset to its
	 * initial position.
	 *
	 * @param tokens source token stream
	 * @return Parameter object or null if tokens don't form a valid parameter
	 */
	public static Parameter parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		Parameter param = null;

		TypeSpecifier typeSpecifier = TypeSpecifier.parse(tokens);

		if (typeSpecifier != null) {
			Declarator declarator = Declarator.parse(tokens, true);
			if (declarator != null)
				param = new Parameter(typeSpecifier, declarator, pos);
		}

		tokens.popMark(param == null);
		return param;
	}
}
