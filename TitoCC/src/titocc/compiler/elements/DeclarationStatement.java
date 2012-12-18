package titocc.compiler.elements;

import java.io.Writer;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class DeclarationStatement extends Statement
{
	private VariableDeclaration declaration;

	public DeclarationStatement(VariableDeclaration declaration, int line, int column)
	{
		super(line, column);
		this.declaration = declaration;
	}

	public Declaration getDeclaration()
	{
		return declaration;
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(DECL_ST " + declaration + ")";
	}

	public static DeclarationStatement parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		DeclarationStatement declStatement = null;

		VariableDeclaration varDecl = VariableDeclaration.parse(tokens);
		if (varDecl != null)
			declStatement = new DeclarationStatement(varDecl, line, column);

		return declStatement;
	}
}
