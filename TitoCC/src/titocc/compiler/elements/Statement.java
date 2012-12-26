package titocc.compiler.elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

public abstract class Statement extends CodeElement
{
	public Statement(int line, int column)
	{
		super(line, column);
	}

	public abstract void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException;

	public static Statement parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();

		Statement statement = ExpressionStatement.parse(tokens);

		if (statement == null)
			statement = DeclarationStatement.parse(tokens);

		if (statement == null)
			statement = IfStatement.parse(tokens);

		if (statement == null)
			statement = WhileStatement.parse(tokens);

		if (statement == null)
			statement = BlockStatement.parse(tokens);

		if (statement == null)
			statement = ReturnStatement.parse(tokens);

		// Empty statement.
		if (statement == null && tokens.read().toString().equals(";"))
			statement = new BlockStatement(new LinkedList<Statement>(), line, column);

		tokens.popMark(statement == null);
		return statement;
	}
}
