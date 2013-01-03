package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Abstract base for all declarations.
 */
public abstract class Declaration extends CodeElement
{
	public Declaration(int line, int column)
	{
		super(line, column);
	}

	public abstract void compile(Assembler asm, Scope scope, Stack<Register> registers) throws IOException, SyntaxException;

	public static Declaration parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();

		Declaration declaration = VariableDeclaration.parse(tokens);

		if (declaration == null)
			declaration = Function.parse(tokens);

		tokens.popMark(declaration == null);
		return declaration;
	}
}
