package titocc.compiler.elements;

import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.compiler.types.VoidType;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

/**
 * Sngle parameter in a function parameter list. Cosists of a type and a name.
 * Unnamed parameters are not yet supported.
 *
 * <p> EBNF definition:
 *
 * <br> PARAMETER = TYPE_SPECIFIER DECLARATOR
 */
public class Parameter extends CodeElement implements Symbol
{
	private TypeSpecifier typeSpecifier;
	private Declarator declarator;
	private String globallyUniqueName;
	private CType type;

	/**
	 * Constructs a Parameter.
	 *
	 * @param typeSpecifier type specifier
	 * @param declarator declarator
	 * @param line starting line number of the parameter
	 * @param column starting column/character of the parameter
	 */
	public Parameter(TypeSpecifier typeSpecifier, Declarator declarator, int line, int column)
	{
		super(line, column);
		this.typeSpecifier = typeSpecifier;
		this.declarator = declarator;
	}

	/**
	 * Returns the parameter type.
	 *
	 * @return the type
	 */
	public CType getType()
	{
		return type;
	}

	@Override
	public String getName()
	{
		return declarator.getName();
	}

	/**
	 * Checks the parameter type and defines the symbol for the parameter.
	 *
	 * @param scope scope in which the parameter is evaluated
	 * @return type of the parameter
	 * @throws SyntaxException if the parameter has invalid type or the name was
	 * redefined
	 */
	public CType compile(Scope scope) throws SyntaxException
	{
		// Compile the type and check that it is valid.
		type = declarator.getModifiedType(typeSpecifier.getType());
		if (!type.isObject())
			throw new SyntaxException("Parameter must have object type.", getLine(), getColumn());
		if (type instanceof ArrayType)
			throw new SyntaxException("Array parameters are not supported.", getLine(), getColumn());

		if (!scope.add(this))
			throw new SyntaxException("Redefinition of \"" + getName() + "\".", getLine(), getColumn());
		globallyUniqueName = scope.makeGloballyUniqueName(getName());
		scope.add(this);

		return type;
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
		return "(PRM " + typeSpecifier + " " + declarator + ")";
	}

	/**
	 * Attempts to parse a parameter from token stream. If parsing fails the
	 * stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Parameter object or null if tokens don't form a valid parameter
	 */
	public static Parameter parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Parameter param = null;

		TypeSpecifier typeSpecifier = TypeSpecifier.parse(tokens);

		if (typeSpecifier != null) {
			Declarator declarator = Declarator.parse(tokens);
			if (declarator != null)
				param = new Parameter(typeSpecifier, declarator, line, column);
		}

		tokens.popMark(param == null);
		return param;
	}
}
