package titocc.compiler.elements;

import titocc.compiler.DeclarationResult;
import titocc.compiler.DeclarationType;
import titocc.compiler.Scope;
import titocc.compiler.StorageClass;
import titocc.compiler.Symbol;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Single parameter in a function parameter list. Syntactically similar to a declaration, except no
 * initializer is allowed. Also, function prototypes may have unnamed parameters, whereas parameters
 * in function definitions must be named.
 *
 * <p> EBNF definition:
 *
 * <br> PARAMETER = DECLARATION_SPECIFIERS DECLARATOR
 */
public class Parameter extends CodeElement
{
	/**
	 * Declaration specifiers, giving the storage class and part of the type.
	 */
	private final DeclarationSpecifiers declarationSpecifiers;

	/**
	 * Declarator which modifies the type and gives the parameter name.
	 */
	private final Declarator declarator;

	/**
	 * Constructs a Parameter.
	 *
	 * @param declarationSpecifiers declaration specifiers
	 * @param declarator declarator
	 * @param position starting position of the parameter
	 */
	public Parameter(DeclarationSpecifiers declarationSpecifiers, Declarator declarator,
			Position position)
	{
		super(position);
		this.declarationSpecifiers = declarationSpecifiers;
		this.declarator = declarator;
	}

	/**
	 * Checks the parameter type and declares a symbol for the parameter.
	 *
	 * @param scope scope in which the parameter is compiled
	 * @param functionDefinition true if the parameter is part of a function definition; disallows
	 * unnamed parameters
	 * @param idx position of the parameter in parameter list; used for generating an internal name
	 * for unnamed parameters
	 * @return the symbol for the parameter
	 * @throws SyntaxException if the parameter has invalid type or the name was redefined
	 */
	public Symbol compile(Scope scope, boolean functionDefinition, int idx)
			throws SyntaxException
	{
		DeclarationType declType = compileType(scope);
		Symbol sym = addSymbol(scope, declType, functionDefinition, idx);
		return sym;
	}

	/**
	 * Deduce parameter type and check that it is valid.
	 */
	private DeclarationType compileType(Scope scope) throws SyntaxException
	{
		DeclarationType declType = declarationSpecifiers.compile(scope);
		declType = declarator.compile(declType, scope, null);

		// Adjust parameter type as defined in ($6.7.5.3/7-8).
		declType.type = declType.type.decay();

		// Only "register" or no storage class allowed in parameters. ($6.7.5.3/2)
		if (declType.storageClass != null && declType.storageClass != StorageClass.Register)
			throw new SyntaxException("Illegal storage class for parameter.", getPosition());

		if (!declType.type.isObject())
			throw new SyntaxException("Parameter must have object type.", getPosition());

		return declType;
	}

	private Symbol addSymbol(Scope scope, DeclarationType declType, boolean functionDefinition,
			int idx)
			throws SyntaxException
	{
		String name = declarator.getName();
		if (name == null && functionDefinition)
			throw new SyntaxException("Unnamed parameter in function definition.", getPosition());
		if (name == null)
			name = "__param" + idx;

		StorageClass storageCls = declType.storageClass != null ? declType.storageClass
				: StorageClass.Auto;

		Symbol sym = new Symbol(name, declType.type, storageCls, false);
		DeclarationResult declRes = scope.add(sym);
		if (declRes.symbol == null)
			throw new SyntaxException(declRes.msg, declarator.getPosition());

		if (!declRes.symbol.define()) {
			throw new SyntaxException("Redefinition of \"" + name + "\".",
					declarator.getPosition());
		}

		return declRes.symbol;
	}

	@Override
	public String toString()
	{
		return "(PRM " + declarationSpecifiers + " " + declarator + ")";
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

		DeclarationSpecifiers declSpecifiers = DeclarationSpecifiers.parse(tokens);

		if (declSpecifiers != null) {
			Declarator declarator = Declarator.parse(tokens, true, true);
			if (declarator != null)
				param = new Parameter(declSpecifiers, declarator, pos);
		}

		tokens.popMark(param == null);
		return param;
	}
}
