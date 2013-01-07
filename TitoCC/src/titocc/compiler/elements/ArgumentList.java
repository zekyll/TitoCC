package titocc.compiler.elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * List of arguments in a function call.
 *
 * <p> EBNF definition:
 *
 * <br> ARGUMENT_LIST = "(" [EXPRESSION "," {EXPRESSION}] ")"
 */
public class ArgumentList extends CodeElement
{
	private List<Expression> arguments;

	/**
	 * Constructs a new ArgumentList.
	 *
	 * @param arguments list of expressions used as arguments
	 * @param line starting line number of the argument list
	 * @param column starting column/character of the argument list
	 */
	public ArgumentList(List<Expression> arguments, int line, int column)
	{
		super(line, column);
		this.arguments = arguments;
	}

	/**
	 * Returns the arguments.
	 *
	 * @return list of argument expressions
	 */
	public List<Expression> getArguments()
	{
		return arguments;
	}

	/**
	 * Generates assembly code to evaluate the arguments from left to right and
	 * push the values to stack.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope in which the arguments are evaluated
	 * @param regs available registers; must have at least one active register
	 * @throws SyntaxException if argument list contains an error
	 * @throws IOException if assembler throws
	 */
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		for (Expression arg : arguments) {
			arg.compile(asm, scope, regs);
			asm.emit("push", "sp", regs.get(0).toString());
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

	/**
	 * Attempts to parse an argument list from token stream. If parsing fails
	 * the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return ArgumentList object or null if tokens don't form a valid argument
	 * list
	 */
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
