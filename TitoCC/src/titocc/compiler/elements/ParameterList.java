package titocc.compiler.elements;

import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import titocc.compiler.Scope;
import titocc.tokenizer.EofToken;
import titocc.tokenizer.TokenStream;

public class ParameterList extends CodeElement
{
	private List<VariableDeclaration> parameters;

	public ParameterList(List<VariableDeclaration> parameters, int line, int column)
	{
		super(line, column);
		this.parameters = parameters;
	}

	public List<VariableDeclaration> getParameters()
	{
		return parameters;
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		String str = "(PRMLIST";
		for (VariableDeclaration p : parameters)
			str += " " + p;
		return str + ")";
	}

	public static ParameterList parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();

		List<VariableDeclaration> params = new LinkedList<VariableDeclaration>();

		VariableDeclaration varDecl = VariableDeclaration.parse(tokens);
		while (varDecl != null) {
			params.add(varDecl);
			varDecl = null;
			if (tokens.read().toString().equals(","))
				varDecl = VariableDeclaration.parse(tokens);
		}

		return new ParameterList(params, line, column);
	}
}
