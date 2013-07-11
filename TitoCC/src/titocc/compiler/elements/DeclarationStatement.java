package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.compiler.StackAllocator;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * A statement that declares a local variable.
 *
 * <p> EBNF definition:
 *
 * <br> DECLARATION_STATEMENT = VARIABLE_DECLARATION
 */
public class DeclarationStatement extends Statement
{
	/**
	 * Declaration for for this declaration statement.
	 */
	private final VariableDeclaration declaration;

	/**
	 * Constructs a DeclarationStatement.
	 *
	 * @param declaration variable declaration
	 * @param position starting position of the declaration statement
	 */
	public DeclarationStatement(VariableDeclaration declaration, Position position)
	{
		super(position);
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
	public void compile(Assembler asm, Scope scope, StackAllocator stack)
			throws SyntaxException, IOException
	{
		declaration.compile(asm, scope, stack);
	}

	@Override
	public String toString()
	{
		return "(DECL_ST " + declaration + ")";
	}

	/**
	 * Attempts to parse a declaration statement from token stream. If parsing fails the stream is
	 * reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return DeclarationStatement object or null if tokens don't form a valid declaration
	 * statement
	 */
	public static DeclarationStatement parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		DeclarationStatement declStatement = null;

		VariableDeclaration varDecl = VariableDeclaration.parse(tokens);
		if (varDecl != null)
			declStatement = new DeclarationStatement(varDecl, pos);

		return declStatement;
	}
}
