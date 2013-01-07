package titocc.compiler.elements;

import java.io.IOException;
import java.util.Arrays;
import titocc.compiler.Assembler;
import titocc.compiler.InternalCompilerException;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Expression formed by a binary operator and two operands.
 *
 * <p> EBNF Definition:
 *
 * <br> BINARY_EXPRESSION = [BINARY_EXPRESSION "||"] BINARY_EXPRESSION2
 *
 * <br> BINARY_EXPRESSION2 = [BINARY_EXPRESSION2 "&&"] BINARY_EXPRESSION3
 *
 * <br> BINARY_EXPRESSION3 = [BINARY_EXPRESSION3 "|"] BINARY_EXPRESSION4
 *
 * <br> BINARY_EXPRESSION4 = [BINARY_EXPRESSION4 "^"] BINARY_EXPRESSION5
 *
 * <br> BINARY_EXPRESSION5 = [BINARY_EXPRESSION5 "&"] BINARY_EXPRESSION6
 *
 * <br> BINARY_EXPRESSION6 = [BINARY_EXPRESSION6 "=="] BINARY_EXPRESSION7
 *
 * <br> BINARY_EXPRESSION7 = [BINARY_EXPRESSION7 "!=") BINARY_EXPRESSION8
 *
 * <br> BINARY_EXPRESSION8 = [BINARY_EXPRESSION8 ("<" | "<=" | ">" | ">=")]
 * BINARY_EXPRESSION9
 *
 * <br> BINARY_EXPRESSION9 = [BINARY_EXPRESSION9 ("<<" | ">>")]
 * BINARY_EXPRESSION10
 *
 * <br> BINARY_EXPRESSION10 = [BINARY_EXPRESSION10 ("+" | "-")]
 * BINARY_EXPRESSION11
 *
 * <br> BINARY_EXPRESSION11 = [BINARY_EXPRESSION11 ("*" | "/" | "%")]
 * PREFIX_EXPRESSION
 */
public class BinaryExpression extends Expression
{
	// Binary operators grouped according to their priority.
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
	private String operator;
	private Expression left, right;

	/**
	 *
	 * @param operator operator as string
	 * @param left left operand
	 * @param right right operand
	 * @param line starting line number of the binary expression
	 * @param column starting column/character of the binary expression
	 */
	public BinaryExpression(String operator, Expression left, Expression right,
			int line, int column)
	{
		super(line, column);
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	/**
	 * Returns the operator.
	 *
	 * @return the operator
	 */
	public String getOperator()
	{
		return operator;
	}

	/**
	 * Returns the left operand.
	 *
	 * @return the left operand
	 */
	public Expression getLeft()
	{
		return left;
	}

	/**
	 * Returns the right operand
	 *
	 * @return the right operand
	 */
	public Expression getRight()
	{
		return right;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		// Evaluate left expression and store it in the first register.
		left.compile(asm, scope, regs);

		// Allocate a second register for right operand.
		regs.allocate(asm);

		// Evaluate right expression and store it in the second register.
		// With logical or/and don't evaluate yet because of the short circuit
		// Evaluation.
		if (!operator.equals("||") && !operator.equals("&&"))
			compileRight(asm, scope, regs);

		// Evaluate the operation and store the result in the left register.
		compileOperator(asm, scope, regs);

		// Deallocate the second register.
		regs.deallocate(asm);
	}

	private void compileRight(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		regs.removeFirst();
		right.compile(asm, scope, regs);
		regs.addFirst();
	}

	private void compileOperator(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		String jumpLabel, jumpLabel2;
		switch (operator) {
			case "||":
				// Short circuit evaluation; only evaluate RHS if LHS is false.
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				jumpLabel2 = scope.makeGloballyUniqueName("lbl");
				asm.emit("jnzer", regs.get(0).toString(), jumpLabel);
				compileRight(asm, scope, regs);
				asm.emit("jnzer", regs.get(1).toString(), jumpLabel);
				asm.emit("load", regs.get(0).toString(), "=0");
				asm.emit("jump", regs.get(0).toString(), jumpLabel2);
				asm.addLabel(jumpLabel);
				asm.emit("load", regs.get(0).toString(), "=1");
				asm.addLabel(jumpLabel2);
				break;
			case "&&":
				// Short circuit evaluation; only evaluate RHS if LHS is true.
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				jumpLabel2 = scope.makeGloballyUniqueName("lbl");
				asm.emit("jzer", regs.get(0).toString(), jumpLabel);
				compileRight(asm, scope, regs);
				asm.emit("jzer", regs.get(1).toString(), jumpLabel);
				asm.emit("load", regs.get(0).toString(), "=1");
				asm.emit("jump", regs.get(0).toString(), jumpLabel2);
				asm.addLabel(jumpLabel);
				asm.emit("load", regs.get(0).toString(), "=0");
				asm.addLabel(jumpLabel2);
				break;
			case "|":
				asm.emit("or", regs.get(0).toString(), regs.get(1).toString());
				break;
			case "^":
				asm.emit("xor", regs.get(0).toString(), regs.get(1).toString());
				break;
			case "&":
				asm.emit("and", regs.get(0).toString(), regs.get(1).toString());
				break;
			case "==":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", regs.get(0).toString(), regs.get(1).toString());
				asm.emit("load", regs.get(0).toString(), "=1");
				asm.emit("jequ", regs.get(0).toString(), jumpLabel);
				asm.emit("load", regs.get(0).toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case "!=":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", regs.get(0).toString(), regs.get(1).toString());
				asm.emit("load", regs.get(0).toString(), "=1");
				asm.emit("jnequ", regs.get(0).toString(), jumpLabel);
				asm.emit("load", regs.get(0).toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case "<":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", regs.get(0).toString(), regs.get(1).toString());
				asm.emit("load", regs.get(0).toString(), "=1");
				asm.emit("jles", regs.get(0).toString(), jumpLabel);
				asm.emit("load", regs.get(0).toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case "<=":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", regs.get(0).toString(), regs.get(1).toString());
				asm.emit("load", regs.get(0).toString(), "=1");
				asm.emit("jngre", regs.get(0).toString(), jumpLabel);
				asm.emit("load", regs.get(0).toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case ">":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", regs.get(0).toString(), regs.get(1).toString());
				asm.emit("load", regs.get(0).toString(), "=1");
				asm.emit("jgre", regs.get(0).toString(), jumpLabel);
				asm.emit("load", regs.get(0).toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case ">=":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", regs.get(0).toString(), regs.get(1).toString());
				asm.emit("load", regs.get(0).toString(), "=1");
				asm.emit("jnles", regs.get(0).toString(), jumpLabel);
				asm.emit("load", regs.get(0).toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case "<<":
				asm.emit("shl", regs.get(0).toString(), regs.get(1).toString());
				break;
			case ">>":
				asm.emit("shr", regs.get(0).toString(), regs.get(1).toString());
				break;
			case "+":
				asm.emit("add", regs.get(0).toString(), regs.get(1).toString());
				break;
			case "-":
				asm.emit("sub", regs.get(0).toString(), regs.get(1).toString());
				break;
			case "*":
				asm.emit("mul", regs.get(0).toString(), regs.get(1).toString());
				break;
			case "/":
				asm.emit("div", regs.get(0).toString(), regs.get(1).toString());
				break;
			case "%":
				asm.emit("mod", regs.get(0).toString(), regs.get(1).toString());
				break;
			default:
				throw new InternalCompilerException("Invalid operator in BinaryExpression.");
		}
	}

	@Override
	public Integer getCompileTimeValue()
	{
		// Compile time evaluation of binary operators could be implemented here.

		return null;
	}

	@Override
	public String toString()
	{
		return "(BIN_EXPR " + operator + " " + left + " " + right + ")";
	}

	/**
	 * Attempts to parse a syntactic binary expression from token stream. If
	 * parsing fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
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
