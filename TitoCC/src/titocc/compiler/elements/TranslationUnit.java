package titocc.compiler.elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.tokenizer.EofToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

public class TranslationUnit extends CodeElement
{
	private List<Declaration> declarations;

	public TranslationUnit(List<Declaration> declarations, int line, int column)
	{
		super(line, column);
		this.declarations = declarations;
	}

	public List<Declaration> getDeclarations()
	{
		return declarations;
	}

	public void compile(Assembler asm, Scope scope, Stack<Register> registers) throws IOException, SyntaxException
	{
		// Call main function and then halt.
		asm.emit("add", "sp", "=1");
		asm.emit("call", "sp", "main");
		asm.emit("svc", "sp", "=halt");

		for (Declaration decl : declarations)
			decl.compile(asm, scope, registers);

		if (!mainFunctionExists(scope))
			throw new SyntaxException("Function \"int main()\" was not found.", getLine(), getColumn());
	}

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

	private boolean mainFunctionExists(Scope scope)
	{
		Symbol sym = scope.find("main");
		if (sym == null || !(sym instanceof Function))
			return false;

		Function main = (Function) sym;
		return main.getReturnType().getName().equals("int") && main.getParameterCount() == 0;
	}
}
