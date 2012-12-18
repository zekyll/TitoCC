package titocc.compiler.elements;

import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class BlockStatement extends Statement
{
	private List<Statement> statements;

	public BlockStatement(List<Statement> statements, int line, int column)
	{
		super(line, column);
		this.statements = statements;
	}

	public List<Statement> getStatements()
	{
		return statements;
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		String str = "(BLK_ST ";
		for (Statement st : statements)
			str += " " + st;
		return str + ")";
	}

	public static BlockStatement parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		BlockStatement blockStatement = null;

		if (tokens.read().toString().equals("{")) {
			List<Statement> statements = new LinkedList<Statement>();

			Statement statement = Statement.parse(tokens);
			while (statement != null) {
				statements.add(statement);
				statement = Statement.parse(tokens);
			}

			if (tokens.read().toString().equals("}"))
				blockStatement = new BlockStatement(statements, line, column);
		}

		tokens.popMark(blockStatement == null);
		return blockStatement;
	}
}
