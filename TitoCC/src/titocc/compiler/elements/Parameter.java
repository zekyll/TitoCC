package titocc.compiler.elements;

import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
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
 * <br> PARAMETER = TYPE IDENTIFIER
 */
public class Parameter extends CodeElement implements Symbol
{
	private Type type;
	private String name;
	private String globallyUniqueName;

	/**
	 * Constructs a Parameter.
	 *
	 * @param type type of the parameter
	 * @param name name of the parameter
	 * @param line starting line number of the parameter
	 * @param column starting column/character of the parameter
	 */
	public Parameter(Type type, String name, int line, int column)
	{
		super(line, column);
		this.type = type;
		this.name = name;
	}

	/**
	 * Returns the parameter type.
	 *
	 * @return the type
	 */
	public Type getType()
	{
		return type;
	}

	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * Checks the parameter type and defines the symbol for the parameter.
	 *
	 * @param scope scope in which the parameter is evaluated
	 * @throws SyntaxException if the parameter has invalid type or the name was
	 * redefined
	 */
	public void compile(Scope scope) throws SyntaxException
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
