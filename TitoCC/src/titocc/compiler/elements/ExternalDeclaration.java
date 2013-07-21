package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Scope;
import titocc.compiler.StackAllocator;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Abstract base class for file-scope declarations (normal declarations or function definitions).
 *
 * <p> EBNF definition:
 *
 * <br> EXTERNAL_DECLARATION = DECLARATION | FUNCTION_DEFINITION
 */
public abstract class ExternalDeclaration extends CodeElement
{
	/**
	 * Constructs a new ExternalDeclaration.
	 *
	 * @param position starting position of the external declaration
	 */
	public ExternalDeclaration(Position position)
	{
		super(position);
	}

	/**
	 * Generates assembly code for the external declaration.
	 *
	 * @param asm assembler used for code generation (global declarations)
	 * @param ic intermediate compiler used for code generation (local declarations)
	 * @param scope scope in which the declaration is evaluated
	 * @param stack allocator for local stack data (not used for global declarations)
	 * @throws SyntaxException if the declaration contains an error
	 * @throws IOException if assembler throws
	 */
	public abstract void compile(Assembler asm, IntermediateCompiler ic,
			Scope scope, StackAllocator stack) throws IOException, SyntaxException;

	/**
	 * Attempts to parse an external declaration from token stream. If parsing fails the stream is
	 * reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return ExternalDeclaration object or null if tokens don't form a valid external declaration
	 */
	public static ExternalDeclaration parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();

		ExternalDeclaration declaration = Declaration.parse(tokens);

		if (declaration == null)
			declaration = FunctionDefinition.parse(tokens);

		tokens.popMark(declaration == null);
		return declaration;
	}
}
