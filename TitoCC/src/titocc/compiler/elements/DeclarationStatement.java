package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
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
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws SyntaxException, IOException
	{
		declaration.compile(asm, scope, registers);
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
