package titocc.compiler.elements;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Expression formed by a prefix operator followed by an operand.
 *
 * <p> EBNF definition:
 *
 * <br> PREFIX_EXPRESSION = ("++" | "--" | "+" | "-" | "!" | "~")
 * PREFIX_EXPRESSION | POSTFIX_EXPRESSION
 */
public class PrefixExpression extends Expression
{
	static final String[] prefixOperators = {"++", "--", "+", "-", "!", "~"};
	private String operator;
	private Expression operand;

	/**
	 * Constructs a PrefixExpression
	 *
	 * @param operator prefix operator as a string
	 * @param operand operand expression
	 * @param line starting line number of the prefix expression
	 * @param column starting column/character of the prefix expression
	 */
	public PrefixExpression(String operator, Expression operand, int line, int column)
	{
		super(line, column);
		this.operator = operator;
		this.operand = operand;
	}

	/**
	 * Returns the operator as a string.
	 *
	 * @return the operator
	 */
	public String getOperator()
	{
		return operator;
	}

	/**
	 * Returns the operand expression.
	 *
	 * @return the operand expression
	 */
	public Expression getOperand()
	{
		return operand;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		if (compileConstantExpression(asm, scope, registers))
			return;

		if (operator.equals("++") || operator.equals("--"))
			compileIncDec(asm, scope, registers);
		else if (operator.equals("+"))
			operand.compile(asm, scope, registers);
		else if (operator.equals("-"))
			compileUnaryMinus(asm, scope, registers);
		else if (operator.equals("!"))
			compileLogicalNegation(asm, scope, registers);
		else if (operator.equals("~"))
			compileBitwiseNegation(asm, scope, registers);
	}

	private void compileIncDec(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		// Currently the only lvalue expression is variable identifier, so
		// we can just get the variable name.
		String ref = operand.getLvalueReference(scope);
		if (ref == null)
			throw new SyntaxException("Operator requires an lvalue.", getLine(), getColumn());

		// Load value in register.
		asm.emit("load", registers.peek().toString(), ref);

		// Modify and write back the value.
		asm.emit(operator.equals("++") ? "add" : "sub", registers.peek().toString(), "=1");
		asm.emit("store", registers.peek().toString(), ref);
	}

	private void compileUnaryMinus(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		operand.compile(asm, scope, registers);

		// Negative in two's complement: negate all bits and add 1.
		asm.emit("xor", registers.peek().toString(), "=-1");
		asm.emit("add", registers.peek().toString(), "=1");
	}

	private void compileLogicalNegation(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		operand.compile(asm, scope, registers);

		// Compares operand to zero and sets register value according to
		// the result.
		asm.emit("comp", registers.peek().toString(), "=0");
		asm.emit("load", registers.peek().toString(), "=1");
		String jumpLabel = scope.makeGloballyUniqueName("lbl");
		asm.emit("jequ", jumpLabel);
		asm.emit("load", registers.peek().toString(), "=0");
		asm.addLabel(jumpLabel);
	}

	private void compileBitwiseNegation(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		operand.compile(asm, scope, registers);

		// -1 has representation of all 1 bits (0xFFFFFFFF), and therefore
		// xoring with it gives the bitwise negation.
		asm.emit("xor", registers.peek().toString(), "=-1");
	}

	@Override
	public Integer getCompileTimeValue() throws SyntaxException
	{
		// Handle unary minus for literals as a special case. Literals need to 
		// be non-negative so this is a way of simulating negative literals.
		// Also, because 2147483648 doesn't fit int range, this is necessary for
		// expressing the smallest int value of -2147483648.
		if (operator.equals("-") && operand instanceof IntegerLiteralExpression) {
			((IntegerLiteralExpression) operand).getCompileTimeValue();
			String rawValue = ((IntegerLiteralExpression) operand).getRawValue();
			return new BigInteger("-" + rawValue).intValue();
		}

		// Compile time evaluation of operators + - ~ ! could be implemented here.

		return null;
	}

	@Override
	public String toString()
	{
		return "(PRE_EXPR " + operator + " " + operand + ")";
	}

	/**
	 * Attempts to parse a syntactic prefix expression from token stream. If
	 * parsing fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static Expression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Expression expr = null;

		String op = tokens.read().toString();
		if (Arrays.asList(prefixOperators).contains(op)) {
			Expression operand = PrefixExpression.parse(tokens);
			if (operand != null)
				expr = new PrefixExpression(op, operand, line, column);
		}

		tokens.popMark(expr == null);

		if (expr == null)
			expr = PostfixExpression.parse(tokens);

		return expr;
	}
}
