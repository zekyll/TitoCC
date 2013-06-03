package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Abstract base for all declarations.
 *
 * <p> EBNF definition:
 *
 * <br> DECLARATION = VARIABLE_DECLARATION | FUNCTION
 */
public abstract class Declaration extends CodeElement
{
	/**
	 * Constructs a new Declaration.
	 *
	 * @param position starting position of the declaration
	 */
	public Declaration(Position position)
	{
		super(position);
	}

	/**
	 * Generates assembly code for the declaration.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope in which the declaration is evaluated
	 * @param regs available registers; must have at least one active register
	 * @throws SyntaxException if the declaration contains an error
	 * @throws IOException if assembler throws
	 */
	public abstract void compile(Assembler asm, Scope scope, Registers regs) throws IOException, SyntaxException;

	/**
	 * Attempts to parse a declaration from token stream. If parsing fails the stream is reset to
	 * its initial position.
	 *
	 * @param tokens source token stream
	 * @return Declaration object or null if tokens don't form a valid declaration
	 */
	public static Declaration parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();

		Declaration declaration = VariableDeclaration.parse(tokens);

		if (declaration == null)
			declaration = Function.parse(tokens);

		tokens.popMark(declaration == null);
		return declaration;
	}
}
