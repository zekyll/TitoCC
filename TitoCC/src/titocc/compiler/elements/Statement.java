package titocc.compiler.elements;

import java.util.LinkedList;
import titocc.tokenizer.TokenStream;

public abstract class Statement extends CodeElement
{
	public Statement(int line, int column)
	{
		super(line, column);
	}

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
