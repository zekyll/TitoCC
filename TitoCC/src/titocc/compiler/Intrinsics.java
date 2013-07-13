package titocc.compiler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import titocc.compiler.elements.Declaration;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;
import titocc.tokenizer.Tokenizer;

/**
 * Manages intrinsic functions/objects that are implemented using C language. They can be called
 * during the compilation process to implement other operations.
 */
class Intrinsics
{
	private static class Intrinsic
	{
		Symbol symbol;

		String definition;

		boolean compiled = false;

		Intrinsic(Symbol symbol, String definition)
		{
			this.symbol = symbol;
			this.definition = definition;
		}

		boolean define(Assembler asm, Scope scope) throws IOException, SyntaxException
		{
			if (symbol.getUseCount() == 0 || compiled)
				return false;

			Tokenizer tokenizer = new Tokenizer(new StringReader(definition));
			TokenStream tokenStream = new TokenStream(tokenizer.tokenize());
			Declaration declaration = Declaration.parse(tokenStream);
			if (declaration == null) {
				Token token = tokenStream.getFurthestReadToken();
				throw new SyntaxException("Unexpected token \"" + token + "\".",
						token.getPosition());
			}
			declaration.compile(asm, null, scope, null);
			compiled = true;
			return true;
		}
	}

	private List<Intrinsic> intrinsics = new ArrayList<Intrinsic>();

	/**
	 * Constructor. Sets up all necessary data for declaring/defining the intrinsics.
	 */
	Intrinsics()
	{
	}

	/**
	 * Declares the intrinsic symbols.
	 *
	 * @param scope scope for the declarations (should be global scope)
	 */
	void declare(Scope scope)
	{
		for (Intrinsic intr : intrinsics) {
			scope.add(intr.symbol);
		}
	}

	/**
	 * Compiles and defines the intrinsics conditionally. I.e. the assembly code for intrinsic
	 * is only emitted if the symbol is used.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope where they intrinsics are compiled
	 * @throws IOException if assembler throws
	 */
	void define(Assembler asm, Scope scope) throws IOException
	{
		try {
			boolean end;
			do {
				end = true;
				for (Intrinsic intr : intrinsics)
					end |= !intr.define(asm, scope);
			} while (!end);
		} catch (SyntaxException e) {
			throw new InternalCompilerException("Compiler error in intrinsic function: "
					+ e.getMessage());
		}
	}

	private void add(Symbol symbol, String definition)
	{
		intrinsics.add(new Intrinsic(symbol, definition));
	}
}
