package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * A statement that declares a local variable.
 *
 * <p> EBNF definition:
 *
 * <br> DECLARATION_STATEMENT = VARIABLE_DECLARATION
 */
public class DeclarationStatement extends Statement
{
	private VariableDeclaration declaration;

	/**
	 * Constructs a DeclarationStatement.
	 *
	 * @param declaration variable declaration
	 * @param line starting line number of the declaration statement
	 * @param column starting column/character of the declaration statement
	 */
	public DeclarationStatement(VariableDeclaration declaration, int line, int column)
	{
		super(line, column);
		this.declaration = declaration;
	}

	/**
	 * Returns the variable declaration.
	 *
	 * @return the variable declaration.
	 */
	public VariableDeclaration getDeclaration()
	{
		return declaration;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		declaration.compile(asm, scope, regs);
	}

	@Override
	public String toString()
	{
		return "(DECL_ST " + declaration + ")";
	}

	/**
	 * Attempts to parse a declaration statement from token stream. If parsing
	 * fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return DeclarationStatement object or null if tokens don't form a valid
	 * declaration statement
	 */
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
