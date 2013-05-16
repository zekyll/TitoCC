package titocc.compiler.elements;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import titocc.compiler.Assembler;
import titocc.compiler.Lvalue;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.compiler.types.IntType;
import titocc.compiler.types.PointerType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Expression formed by a prefix operator followed by an operand.
 *
 * <p> EBNF definition:
 *
 * <br> PREFIX_EXPRESSION = ("++" | "--" | "+" | "-" | "!" | "~"| "&"| "*")
 * PREFIX_EXPRESSION | POSTFIX_EXPRESSION
 */
public class PrefixExpression extends Expression
{
	/**
	 * List of supported prefix operators.
	 */
	static final String[] prefixOperators = {"++", "--", "+", "-", "!", "~", "&", "*"};
	/**
	 * Operator for this prefix expression.
	 */
	private final String operator;
	/**
	 * Operand expression.
	 */
	private final Expression operand;

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
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		if (compileConstantExpression(asm, scope, regs))
			return;

		if (operator.equals("++") || operator.equals("--"))
			compileIncDec(asm, scope, regs);
		else if (operator.equals("+") || operator.equals("-"))
			compileUnaryPlusMinus(asm, scope, regs);
		else if (operator.equals("!"))
			compileLogicalNegation(asm, scope, regs);
		else if (operator.equals("~"))
			compileBitwiseNegation(asm, scope, regs);
		else if (operator.equals("&"))
			compileAddressOf(asm, scope, regs);
		else if (operator.equals("*"))
			compileDereference(asm, scope, regs);
	}

	@Override
	public Lvalue compileAsLvalue(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		if (!operator.equals("*"))
			throw new SyntaxException("Operation requires an lvalue.", getLine(), getColumn());

		// Operand for * must be a pointer so we just load its value.
		operand.compile(asm, scope, regs);

		return new Lvalue(regs.get(0));
	}

	private void compileIncDec(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		CType operandType = operand.getType(scope);
		if (!operandType.isArithmetic() && !(operandType.isPointer() && operandType.dereference().isObject()))
			throw new SyntaxException("Operator " + operator + " requires an arithmetic or object pointer type.", getLine(), getColumn());

		// Evaluate operand; load address to 2nd register.
		regs.allocate(asm);
		regs.removeFirst();
		Lvalue val = operand.compileAsLvalue(asm, scope, regs);
		regs.addFirst();

		// Load value to first register.
		asm.emit("load", regs.get(0).toString(), val.getReference());

		// Modify and write back the value.
		int incSize = operand.getType(scope).getIncrementSize();
		asm.emit(operator.equals("++") ? "add" : "sub", regs.get(0).toString(), "=" + incSize);
		asm.emit("store", regs.get(0).toString(), val.getReference());

		// Deallocate the second register.
		regs.deallocate(asm);
	}

	private void compileUnaryPlusMinus(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		if (!operand.getType(scope).isArithmetic())
			throw new SyntaxException("Operator " + operator + " requires an arithmetic type.", getLine(), getColumn());

		operand.compile(asm, scope, regs);
		operand.compile(asm, scope, regs);

		// Negative in two's complement: negate all bits and add 1.
		if (operator.equals("-")) {
			asm.emit("xor", regs.get(0).toString(), "=-1");
			asm.emit("add", regs.get(0).toString(), "=1");
		}
	}

	private void compileLogicalNegation(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		if (!operand.getType(scope).decay().isScalar())
			throw new SyntaxException("Operator " + operator + " requires a scalar type.", getLine(), getColumn());

		operand.compile(asm, scope, regs);

		// Compares operand to zero and sets register value according to
		// the result.
		asm.emit("comp", regs.get(0).toString(), "=0");
		asm.emit("load", regs.get(0).toString(), "=1");
		String jumpLabel = scope.makeGloballyUniqueName("lbl");
		asm.emit("jequ", jumpLabel);
		asm.emit("load", regs.get(0).toString(), "=0");
		asm.addLabel(jumpLabel);
	}

	private void compileBitwiseNegation(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		if (!operand.getType(scope).isInteger())
			throw new SyntaxException("Operator " + operator + " requires an integer type.", getLine(), getColumn());

		operand.compile(asm, scope, regs);

		// -1 has representation of all 1 bits (0xFFFFFFFF), and therefore
		// xoring with it gives the bitwise negation.
		asm.emit("xor", regs.get(0).toString(), "=-1");
	}

	private void compileAddressOf(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		// Load the address of the operand in the first register.
		Lvalue val = operand.compileAsLvalue(asm, scope, regs);
		val.loadAddressToRegister(asm);
	}

	private void compileDereference(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		if (!operand.getType(scope).dereference().isValid())
			throw new SyntaxException("Operator * requires a pointer or array type.", getLine(), getColumn());

		// Operand must be a pointer; load the address.
		operand.compile(asm, scope, regs);

		// Dereference the pointer unless the result type is an array!
		if (!(getType(scope) instanceof ArrayType))
			asm.emit("load", regs.get(0).toString(), "@" + regs.get(0).toString());
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		if (operator.equals("&")) {
			return new PointerType(operand.getType(scope));
		} else if (operator.equals("*")) {
			if (!operand.getType(scope).dereference().isValid())
				throw new SyntaxException("Operator * requires a pointer or array type.", getLine(), getColumn());
			return operand.getType(scope).dereference();
		} else if (operator.equals("!") || operator.equals("~")) {
			return new IntType();
		} else
			return operand.getType(scope).decay();
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
