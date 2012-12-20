package titocc.compiler.elements;

import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.tokenizer.EofToken;
import titocc.tokenizer.TokenStream;

public class ParameterList extends CodeElement
{
	private List<Parameter> parameters;

	public ParameterList(List<Parameter> parameters, int line, int column)
	{
		super(line, column);
		this.parameters = parameters;
	}

	public List<Parameter> getParameters()
	{
		return parameters;
	}

	@Override
	public void compile(Assembler asm, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		String str = "(PRM_LIST";
		for (Parameter p : parameters)
			str += " " + p;
		return str + ")";
	}

	public static ParameterList parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
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
				paramList = new ParameterList(params, line, column);
		}

		tokens.popMark(paramList == null);
		return paramList;
	}
}
