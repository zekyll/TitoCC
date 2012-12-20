package titocc.compiler;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import titocc.compiler.elements.TranslationUnit;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Tokenizer;

public class Compiler
{
	private Reader reader;
	private TranslationUnit trUnit;

	public Compiler(Reader reader)
	{
		this.reader = reader;
		this.trUnit = null;
	}

	public Compiler(TranslationUnit trUnit)
	{
		this.reader = null;
		this.trUnit = trUnit;
	}

	public void compile(Writer writer) throws IOException, SyntaxException
	{
		if (trUnit == null)
			tokenizeAndParse();
		trUnit.compile(new Assembler(writer), new Scope(null));
	}

	private void tokenizeAndParse() throws IOException, SyntaxException
	{
		Tokenizer tokenizer = new Tokenizer(reader);
		Parser parser = new Parser(tokenizer.tokenize());
		trUnit = parser.parse();
	}
}
