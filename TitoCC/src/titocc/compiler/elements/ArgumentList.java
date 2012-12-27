package titocc.compiler.elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

public class ArgumentList extends CodeElement
{
	private List<Expression> arguments;

	public ArgumentList(List<Expression> arguments, int line, int column)
	{
		super(line, column);
		this.arguments = arguments;
	}

	public List<Expression> getArguments()
	{
		return arguments;
	}

	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws SyntaxException, IOException
	{
		for (Expression arg : arguments) {
			arg.compile(asm, scope, registers);
			asm.emit("push", "sp", registers.peek().toString());
		}
	}

	@Override
	public String toString()
	{
		String str = "(ARG_LIST";
		for (Expression e : arguments)
			str += " " + e;
		return str + ")";
	}

	public static ArgumentList parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		ArgumentList argList = null;

		if (tokens.read().toString().equals("(")) {
			List<Expression> args = new LinkedList<Expression>();
			Expression expr = Expression.parse(tokens);
			while (expr != null) {
				tokens.pushMark();
				args.add(expr);
				expr = null;
				if (tokens.read().toString().equals(","))
					expr = Expression.parse(tokens);
				tokens.popMark(expr == null);
			}

			if (tokens.read().toString().equals(")"))
				argList = new ArgumentList(args, line, column);
		}

		tokens.popMark(argList == null);
		return argList;
	}
}
