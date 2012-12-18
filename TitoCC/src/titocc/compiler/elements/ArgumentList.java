package titocc.compiler.elements;

import java.io.Writer;
import java.util.List;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class ArgumentList extends CodeElement
{
	private List<VariableDeclaration> arguments;
	
	public ArgumentList(List<VariableDeclaration> arguments, int line, int column)
	{
		super(line, column);
		this.arguments = arguments;
	}
	
	public List<VariableDeclaration> arguments()
	{
		return arguments;
	}
	
	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static ArgumentList parse(TokenStream tokens)
	{
		return null;
	}	
}
