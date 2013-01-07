package titocc.compiler.elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import titocc.compiler.Assembler;
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
	private enum Type
	{
		SIMPLE, COMPARISON, BOOLEAN
	};

	/**
	 * Binary operator with mnemonic and operation type.
	 */
	private static class Operator
	{
		public String mnemonic;
		public Type type;
		int priority;

		public Operator(String mnemonic, Type type, int priority)
		{
			this.mnemonic = mnemonic;
			this.type = type;
			this.priority = priority;
		}
	}
	// Binary operators, their main instructions and priorities.
	static final Map<String, Operator> binaryOperators = new HashMap<String, Operator>()
	{
		{
			put("||", new Operator("jnzer", Type.BOOLEAN, 1));
			put("&&", new Operator("jzer", Type.BOOLEAN, 2));
			put("|", new Operator("or", Type.SIMPLE, 3));
			put("^", new Operator("xor", Type.SIMPLE, 4));
			put("&", new Operator("and", Type.SIMPLE, 5));
			put("==", new Operator("jequ", Type.COMPARISON, 6));
			put("!=", new Operator("jnequ", Type.COMPARISON, 7));
			put("<", new Operator("jles", Type.COMPARISON, 8));
			put("<=", new Operator("jngre", Type.COMPARISON, 8));
			put(">", new Operator("jgre", Type.COMPARISON, 8));
			put(">=", new Operator("jnles", Type.COMPARISON, 8));
			put("<<", new Operator("shl", Type.SIMPLE, 9));
			put(">>", new Operator("shr", Type.SIMPLE, 9));
			put("+", new Operator("add", Type.SIMPLE, 10));
			put("-", new Operator("sub", Type.SIMPLE, 10));
			put("*", new Operator("mul", Type.SIMPLE, 11));
			put("/", new Operator("div", Type.SIMPLE, 11));
			put("%", new Operator("mod", Type.SIMPLE, 11));
		}
	};
	private String operator;
	private Expression left, right;

	/**
	 * Constructs a BinaryExpression.
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

		// Compile right expression and the operator.
		if (binaryOperators.get(operator).type == Type.SIMPLE)
			compileSimpleOperator(asm, scope, regs);
		else if (binaryOperators.get(operator).type == Type.BOOLEAN)
			compileBooleanOperator(asm, scope, regs);
		else if (binaryOperators.get(operator).type == Type.COMPARISON)
			compileComparisonOperator(asm, scope, regs);

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

	private void compileSimpleOperator(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		compileRight(asm, scope, regs);
		asm.emit(binaryOperators.get(operator).mnemonic, regs.get(0).toString(), regs.get(1).toString());
	}

	private void compileComparisonOperator(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		compileRight(asm, scope, regs);
		String jumpLabel = scope.makeGloballyUniqueName("lbl");
		asm.emit("comp", regs.get(0).toString(), regs.get(1).toString());
		asm.emit("load", regs.get(0).toString(), "=1");
		asm.emit(binaryOperators.get(operator).mnemonic, regs.get(0).toString(), jumpLabel);
		asm.emit("load", regs.get(0).toString(), "=0");
		asm.addLabel(jumpLabel);
	}

	private void compileBooleanOperator(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		// Short circuit evaluation; only evaluate RHS if LHS is false.
		String jumpLabel = scope.makeGloballyUniqueName("lbl");
		String jumpLabel2 = scope.makeGloballyUniqueName("lbl");
		asm.emit(binaryOperators.get(operator).mnemonic, regs.get(0).toString(), jumpLabel);
		compileRight(asm, scope, regs);
		asm.emit(binaryOperators.get(operator).mnemonic, regs.get(1).toString(), jumpLabel);
		asm.emit("load", regs.get(0).toString(), operator.equals("||") ? "=0" : "=1");
		asm.emit("jump", regs.get(0).toString(), jumpLabel2);
		asm.addLabel(jumpLabel);
		asm.emit("load", regs.get(0).toString(), operator.equals("||") ? "=1" : "=0");
		asm.addLabel(jumpLabel2);
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
		if (priority == 12)
			return PrefixExpression.parse(tokens);

		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Expression expr = parseImpl(tokens, priority + 1);

		if (expr != null)
			while (true) {
				tokens.pushMark();
				Expression right = null;
				String op = tokens.read().toString();
				if (binaryOperators.containsKey(op) && binaryOperators.get(op).priority == priority)
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
