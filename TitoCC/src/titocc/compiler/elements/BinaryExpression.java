package titocc.compiler.elements;

import java.io.Writer;
import java.util.Arrays;
import titocc.compiler.Scope;
import titocc.tokenizer.TokenStream;

public class BinaryExpression extends Expression
{
	static final String[][] binaryOperators = {
		{"||"},
		{"&&"},
		{"|"},
		{"^"},
		{"&"},
		{"=="},
		{"!="},
		{"<", "<=", ">", ">="},
		{"<<", ">>"},
		{"+", "-"},
		{"*", "/", "%"}
	};
	private String operation;
	private Expression left, right;

	public BinaryExpression(String operation, Expression left, Expression right,
			int line, int column)
	{
		super(line, column);
		this.operation = operation;
		this.left = left;
		this.right = right;
	}

	public String operation()
	{
		return operation;
	}

	public Expression getLeft()
	{
		return left;
	}

	public Expression getRight()
	{
		return right;
	}

	@Override
	public void compile(Writer writer, Scope scope)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		return "(BIN_EXPR " + left + " " + right + ")";
	}

	public static Expression parse(TokenStream tokens)
	{
		return parseImpl(tokens, 0);
	}

	private static Expression parseImpl(TokenStream tokens, int priority)
	{
		if (priority == binaryOperators.length)
			return PrefixExpression.parse(tokens);

		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Expression expr = parseImpl(tokens, priority + 1);

		if (expr != null)
			while (true) {
				tokens.pushMark();
				Expression right = null;
				String op = tokens.read().toString();
				if (Arrays.asList(binaryOperators[priority]).contains(op))
					right = parseImpl(tokens, priority + 1);

				tokens.popMark(right == null);
				if (right != null)
					expr = new BinaryExpression(op, expr, right, line, column);
				else
					break;
			}

		tokens.popMark(expr == null);
		return expr;
	}
}
