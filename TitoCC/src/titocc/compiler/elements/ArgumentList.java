package titocc.compiler.elements;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Vstack;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * List of arguments in a function call.
 *
 * <p> EBNF definition:
 *
 * <br> ARGUMENT_LIST = "(" [ASSIGNMENT_EXPRESSION {"," ASSIGNMENT_EXPRESSION}] ")"
 */
public class ArgumentList extends CodeElement
{
	/**
	 * Argument expressions.
	 */
	private final List<Expression> arguments;

	/**
	 * Constructs a new ArgumentList.
	 *
	 * @param arguments list of expressions used as arguments
	 * @param position starting position of the argument list
	 */
	public ArgumentList(List<Expression> arguments, Position position)
	{
		super(position);
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
	 * Generates assembly code to evaluate the arguments from left to right and push the values to
	 * program stack.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope in which the arguments are evaluated
	 * @param vstack virtual stack
	 * @param paramTypes parameter types for the called function
	 * @throws SyntaxException if argument list contains an error
	 * @throws IOException if assembler throws
	 */
	public void compile(Assembler asm, Scope scope, Vstack vstack, List<CType> paramTypes)
			throws SyntaxException, IOException
	{
		if (paramTypes.size() != arguments.size()) {
			throw new SyntaxException("Number of arguments doesn't match the number of parameters.",
					getPosition());
		}

		Iterator<CType> paramIterator = paramTypes.iterator();
		for (Expression arg : arguments) {
			CType paramType = paramIterator.next();
			if (!arg.isAssignableTo(paramType, scope)) {
				throw new SyntaxException("Argument type doesn't match type of the parameter.",
						arg.getPosition());
			}

			arg.compileWithConversion(asm, scope, vstack, paramType);
			asm.emit("push", Register.SP, vstack.top(0));
			vstack.pop();
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
	 * Attempts to parse an argument list from token stream. If parsing fails the stream is reset to
	 * its initial position.
	 *
	 * @param tokens source token stream
	 * @return ArgumentList object or null if tokens don't form a valid argument list
	 */
	public static ArgumentList parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		ArgumentList argList = null;

		if (tokens.read().toString().equals("(")) {
			List<Expression> args = new LinkedList<Expression>();
			Expression expr = AssignmentExpression.parse(tokens);
			while (expr != null) {
				tokens.pushMark();
				args.add(expr);
				expr = null;
				if (tokens.read().toString().equals(","))
					expr = AssignmentExpression.parse(tokens);
				tokens.popMark(expr == null);
			}

			if (tokens.read().toString().equals(")"))
				argList = new ArgumentList(args, pos);
		}

		tokens.popMark(argList == null);
		return argList;
	}
}
