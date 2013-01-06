package titocc.compiler.elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.tokenizer.EofToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Top level code element that represents a single translation unit. Formed by a
 * list of declarations.
 *
 * <p> EBNF definition:
 *
 * <br> TRANSLATION_UNIT = {DECLARATION} EOF
 */
public class TranslationUnit extends CodeElement
{
	private List<Declaration> declarations;

	/**
	 * Constructs a TranslationUnit.
	 *
	 * @param declarations list of declarations in the translation unit
	 * @param line starting line number of the translation unit
	 * @param column starting column/character of the translation unit
	 */
	public TranslationUnit(List<Declaration> declarations, int line, int column)
	{
		super(line, column);
		this.declarations = declarations;
	}

	/**
	 * Returns declarations in the translation unit.
	 *
	 * @return list of declarations
	 */
	public List<Declaration> getDeclarations()
	{
		return declarations;
	}

	/**
	 * Generates code for the translation unit. Compiles all declarations,
	 * searches for the main function and emits code for calling the main
	 * function.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope in which the translation unit is compiled (should be
	 * global scope)
	 * @param regs available registers; must have at least one active register
	 * @throws SyntaxException if translation unit contains an error
	 * @throws IOException if assembler throws
	 */
	public void compile(Assembler asm, Scope scope, Registers regs) throws IOException, SyntaxException
	{
		// Call main function and then halt.
		asm.emit("add", "sp", "=1");
		asm.emit("call", "sp", "main");
		asm.emit("svc", "sp", "=halt");

		for (Declaration decl : declarations)
			decl.compile(asm, scope, regs);
		
		if (!mainFunctionExists(scope))
			throw new SyntaxException("Function \"int main()\" was not found.", getLine(), getColumn());
	}

	/**
	 * Attempts to parse a translation unit from token stream. If parsing fails
	 * the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return TranslationUnit object or null if tokens don't form a valid
	 * translation unit
	 */
	public static TranslationUnit parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		TranslationUnit translUnit = null;

		List<Declaration> declarations = new LinkedList<Declaration>();

		Declaration d = Declaration.parse(tokens);
		while (d != null) {
			declarations.add(d);
			d = Declaration.parse(tokens);
		}

		if (tokens.read() instanceof EofToken)
			translUnit = new TranslationUnit(declarations, line, column);

		tokens.popMark(translUnit == null);
		return translUnit;
	}

	@Override
	public String toString()
	{
		String s = "(TRUNIT";
		for (Declaration d : declarations)
			s += " " + d;
		return s + ")";
	}

	/**
	 * Checks if valid main function exists.
	 *
	 * @param scope (the global) scope
	 * @return true if main() was found
	 */
	private boolean mainFunctionExists(Scope scope)
	{
		Symbol sym = scope.find("main");
		if (sym == null || !(sym instanceof Function))
			return false;

		Function main = (Function) sym;
		return main.getReturnType().getName().equals("int") && main.getParameterCount() == 0;
	}
}
