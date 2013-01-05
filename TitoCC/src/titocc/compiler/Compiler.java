package titocc.compiler;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Stack;
import titocc.compiler.elements.TranslationUnit;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Tokenizer;

/**
 * Main compiler class. Accepts either a C source file or parser output
 * (TranslationUnit object) as input and produces assembly code. The main
 * compilation work is done in the code element classes, and Compiler class just
 * works as a wrapper and initializes some necessary variables.
 */
public class Compiler
{
	static final String[] reservedGlobalNames = {
		"r0", "r1", "r2", "r3", "r3", "r5", "crt", "kbd", "stdin", "stdout",
		"halt", "read", "write", "time", "date"
	};
	private Reader reader;
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
		translationUnit.compile(asm, scope, getUsableRegisters());
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
		Parser parser = new Parser(tokenizer.tokenize());
		translationUnit = parser.parse();
	}

	/**
	 * Creates a stack of registers that will be available for code generation.
	 *
	 * @return stack that contains the available registers
	 */
	private Stack<Register> getUsableRegisters()
	{
		// Use all general purpose registers except R0 because it behaves differently.
		Stack<Register> registers = new Stack<Register>();
		registers.push(Register.R5);
		registers.push(Register.R4);
		registers.push(Register.R3);
		registers.push(Register.R2);
		registers.push(Register.R1);
		return registers;
	}

	/**
	 * Reserves names that cannot be used as identifiers in ttk-91 assembly
	 * language. These include register names, devices and other predefined
	 * symbols. They can still be used as identifiers in the C program; only the
	 * generated globally unique names in the assembly code will be different.
	 *
	 * @param scope
	 */
	private void reserveNames(Scope scope)
	{
		for (String name : reservedGlobalNames)
			scope.makeGloballyUniqueName(name);
	}
}
