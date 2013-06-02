package titocc.compiler.elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * List of parameters for a function type. A parameter list can occur in two
 * different contexts: in function definitions, where parameter names are
 * required, and in function declarations or other type declarators where
 * unnamed parameters are allowed.
 *
 * <p> EBNF definition:
 *
 * <br> PARAMETER_LIST = "(" [PARAMETER {"," PARAMETER}] ")"
 */
public class ParameterList extends CodeElement
{
	/**
	 * List of parameters.
	 */
	private final List<Parameter> parameters;

	/**
	 * Construcs a ParameterList.
	 *
	 * @param parameters list of parameters
	 * @param position starting position of the parameter list
	 */
	public ParameterList(List<Parameter> parameters, Position position)
	{
		super(position);
		this.parameters = parameters;
	}

	/**
	 * Returns the parameters.
	 *
	 * @return the parameters
	 */
	public List<Parameter> getParameters()
	{
		return parameters;
	}

	/**
	 * Generates the constants for accessing the parameters, declares their
	 * symbols and deduces parameter types.
	 *
	 * @param asm assembler used for generating the code; if null then the
	 * parameter list is not part of a function definition and no code is
	 * generated and only parameter types are deduced
	 * @param scope scope in which the parameter list is evaluated
	 * @return list of parameter types
	 * @throws SyntaxException if the parameters contain errors
	 * @throws IOException if assembler throws
	 */
	public List<CType> compile(Assembler asm, Scope scope)
			throws SyntaxException, IOException
	{
		boolean isFunctionDefinition = asm != null;

		// For function definitions use the given function scope, otherwise
		// create a temporary subscope for checking duplicate parameter names.
		Scope paramScope = scope;
		if (!isFunctionDefinition) {
			paramScope = new Scope(scope, "");
			scope.addSubScope(paramScope);
		}

		List<CType> paramTypes = new ArrayList<CType>();
		int paramOffset = -1 - parameters.size();
		for (Parameter p : parameters) {
			Symbol paramSymbol = p.compile(paramScope, !isFunctionDefinition);
			paramTypes.add(paramSymbol.getType());
			if (asm != null) {
				asm.addLabel(paramSymbol.getGlobalName());
				asm.emit("equ", "" + paramOffset);
			}
			++paramOffset;
		}
		return paramTypes;
	}

	@Override
	public String toString()
	{
		String str = "(PRM_LIST";
		for (Parameter p : parameters)
			str += " " + p;
		return str + ")";
	}

	/**
	 * Attempts to parse a parameter list from token stream. If parsing fails
	 * the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return ParameterList object or null if tokens don't form a valid
	 * parameter list
	 */
	public static ParameterList parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		ParameterList paramList = null;

		if (tokens.read().toString().equals("(")) {
			List<Parameter> params = new LinkedList<Parameter>();
			Parameter param = Parameter.parse(tokens);
			while (param != null) {
				tokens.pushMark();
				params.add(param);
				param = null;
				if (tokens.read().toString().equals(","))
					param = Parameter.parse(tokens);
				tokens.popMark(param == null);
			}

			if (tokens.read().toString().equals(")"))
				paramList = new ParameterList(params, pos);
		}

		tokens.popMark(paramList == null);
		return paramList;
	}
}
