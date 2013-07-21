package titocc.compiler.elements;

import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Scope;
import titocc.compiler.StackAllocator;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * A statement that declares a function or object in block scope.
 *
 * <p> EBNF definition:
 *
 * <br> DECLARATION_STATEMENT = DECLARATION
 */
public class DeclarationStatement extends Statement
{
	/**
	 * Declaration for for this declaration statement.
	 */
	private final Declaration declaration;

	/**
	 * Constructs a DeclarationStatement.
	 *
	 * @param declaration declaration
	 * @param position starting position of the declaration statement
	 */
	public DeclarationStatement(Declaration declaration, Position position)
	{
		super(position);
		this.declaration = declaration;
	}

	@Override
	public void compile(IntermediateCompiler ic, Scope scope, StackAllocator stack)
			throws SyntaxException
	{
		declaration.compile(ic, scope, stack);
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

		Declaration varDecl = Declaration.parse(tokens);
		if (varDecl != null)
			declStatement = new DeclarationStatement(varDecl, pos);

		return declStatement;
	}
}
