package titocc.compiler;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import titocc.compiler.elements.TranslationUnit;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Tokenizer;

/**
 * Main compiler class. Accepts either a C source file or parser output (TranslationUnit object) as
 * input and produces assembly code. The main compilation work is done in the code element classes,
 * and Compiler class just works as a wrapper and initializes some necessary variables.
 */
public class Compiler
{
	/**
	 * Names reserved by ttk-91.
	 */
	static final String[] reservedGlobalNames = {
		"r0", "r1", "r2", "r3", "r3", "r5", "crt", "kbd", "stdin", "stdout",
		"halt", "read", "write", "time", "date"
	};

	/**
	 * Input reader object.
	 */
	private final Reader reader;

	/**
	 * Input TranslationUnit or null if input reader was used instead.
	 */
	private TranslationUnit translationUnit;

	/**
	 * Constructs a compiler object that takes a C source file as input.
	 *
	 * @param reader reader object for reading the source file
	 */
	public Compiler(Reader reader)
	{
		this.reader = reader;
		this.translationUnit = null;
	}

	/**
	 * Constructs a compiler object that uses parser output.
	 *
	 * @param translationUnit TranslationUnit object produced by Parser class
	 */
	public Compiler(TranslationUnit translationUnit)
	{
		this.reader = null;
		this.translationUnit = translationUnit;
	}

	/**
	 * Compiles the translation unit.
	 *
	 * @param writer Writer object that receives the compiler output
	 * @throws IOException if writer throws
	 * @throws SyntaxException if the translation unit contains errors
	 */
	public void compile(Writer writer) throws IOException, SyntaxException
	{
		if (translationUnit == null)
			tokenizeAndParse();
		Scope scope = new Scope(null, "");
		reserveNames(scope);
		Assembler asm = new Assembler(writer);
		translationUnit.compile(asm, scope, new Vstack(new RegisterAllocator()));
		asm.finish();
	}

	/**
	 * Runs tokenizer and parser for the source file.
	 *
	 * @throws IOException if writer throws
	 * @throws SyntaxException if the translation unit contains errors
	 */
	private void tokenizeAndParse() throws IOException, SyntaxException
	{
		Tokenizer tokenizer = new Tokenizer(reader);
		translationUnit = Parser.parse(tokenizer.tokenize());
	}

	/**
	 * Reserves names that cannot be used as identifiers in ttk-91 assembly language. These include
	 * register names, devices and other predefined symbols. They can still be used as identifiers
	 * in the C program; only the generated globally unique names in the assembly code will be
	 * different.
	 *
	 * @param scope
	 */
	private void reserveNames(Scope scope)
	{
		for (String name : reservedGlobalNames)
			scope.makeGloballyUniqueName(name);
	}
}
