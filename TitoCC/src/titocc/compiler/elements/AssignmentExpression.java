package titocc.compiler.elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import titocc.compiler.Assembler;
import titocc.compiler.Lvalue;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.types.CType;
import titocc.compiler.types.IntType;
import titocc.compiler.types.VoidType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Expression formed by any of the assignment operators and two operands.
 *
 * <p> EBNF definition:
 *
 * <br> ASSIGNMENT_EXPRESSION = BINARY_EXPRESSION [ASSIGNMENT_OPERATOR
 * ASSIGNMENT_EXPRESSION]
 *
 * <br> ASSIGNMENT_OPERATOR = "=" | "+=" | "*=" | "&=" | "|=" | "^=" | "-=" |
 * "/=" | "%=" | "<<=" | ">>="
 */
public class AssignmentExpression extends Expression
{
	private enum Type
	{
		SIMPLE, COMMUTATIVE, NONCOMMUTATIVE
	};

	/**
	 * Assignment operator with operator mnemonic and operation type.
	 */
	private static class Operator
	{
		public String mnemonic;
		public Type type;

		public Operator(String mnemonic, Type type)
		{
			this.mnemonic = mnemonic;
			this.type = type;
		}
	}
	/**
	 * Map of assignment operators.
	 */
	static final Map<String, Operator> assignmentOperators = new HashMap<String, Operator>()
	{
		{
			put("=", new Operator("", Type.SIMPLE));
			put("+=", new Operator("add", Type.COMMUTATIVE));
			put("*=", new Operator("mul", Type.COMMUTATIVE));
			put("&=", new Operator("and", Type.COMMUTATIVE));
			put("|=", new Operator("or", Type.COMMUTATIVE));
			put("^=", new Operator("xor", Type.COMMUTATIVE));
			put("-=", new Operator("sub", Type.NONCOMMUTATIVE));
			put("/=", new Operator("div", Type.NONCOMMUTATIVE));
			put("%=", new Operator("mod", Type.NONCOMMUTATIVE));
			put("<<=", new Operator("shl", Type.NONCOMMUTATIVE));
			put(">>=", new Operator("shr", Type.NONCOMMUTATIVE));
		}
	};
	private Operator operator;
	private String operatorString;
	private Expression left, right;

	/**
	 * Constructs a new AssignmentExpression
	 *
	 * @param operator string representation of the operator
	 * @param left left operand
	 * @param right right operand
	 * @param line starting line number of the assignment expression
	 * @param column starting column/character of the assignment expression
	 */
	public AssignmentExpression(String operator, Expression left,
			Expression right, int line, int column)
	{
		super(line, column);
		this.operatorString = operator;
		this.operator = assignmentOperators.get(operator);
		this.left = left;
		this.right = right;
	}

	/**
	 * Returns the left operand.
	 *
	 * @return left operand
	 */
	public Expression getLeft()
	{
		return left;
	}

	/**
	 * Returns the right operand.
	 *
	 * @return right operand
	 */
	public Expression getRight()
	{
		return right;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		checkTypes(scope);

		// Allocate second register.
		regs.allocate(asm);

		// Compile Operator.
		if (operator.type == Type.SIMPLE)
			compileSimple(asm, scope, regs);
		else if (operator.type == Type.COMMUTATIVE)
			compileCommutative(asm, scope, regs);
		else
			compileNoncommutative(asm, scope, regs);

		// Deallocate second register.
		regs.deallocate(asm);
	}

	private Lvalue compileLeft(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		regs.removeFirst();
		Lvalue leftVal = left.compileAsLvalue(asm, scope, regs);
		regs.addFirst();
		return leftVal;
	}

	private void compileSimple(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		// Evaluate RHS and load value to 1st register.
		right.compile(asm, scope, regs);

		// Evaluate LHS and load address to 2nd register.
		Lvalue leftVal = compileLeft(asm, scope, regs);

		asm.emit("store", regs.get(0).toString(), leftVal.getReference());
	}

	private void compileCommutative(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		// Evaluate RHS; load value to 1st register.
		right.compile(asm, scope, regs);

		// If operation is POINTER += INTEGER, we need to scale the integer value.
		int incSize = left.getType(scope).getIncrementSize();
		if (incSize > 1) {
			asm.emit("mul", regs.get(0).toString(), "=" + incSize);
		}

		// Evaluate LHS; load address to 2nd register.
		Lvalue leftVal = compileLeft(asm, scope, regs);

		// Because the operation is symmetric, we can use the left operand
		// as the right operand in the assembly instruction, saving one register.
		asm.emit(operator.mnemonic, regs.get(0).toString(), leftVal.getReference());
		asm.emit("store", regs.get(0).toString(), leftVal.getReference());
	}

	private void compileNoncommutative(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		// Evaluate LHS; load address to 2nd register.
		Lvalue leftVal = compileLeft(asm, scope, regs);

		// Evaluate RHS; load value to 3rd register.
		regs.allocate(asm);
		regs.removeFirst();
		regs.removeFirst();
		right.compile(asm, scope, regs);
		regs.addFirst();
		regs.addFirst();

		// If operation is POINTER -= INTEGER, we need to scale the integer value.
		int incSize = left.getType(scope).getIncrementSize();
		if (incSize > 1) {
			asm.emit("mul", regs.get(2).toString(), "=" + incSize);
		}

		// Load LHS value to 1st register and operate on it.
		asm.emit("load", regs.get(0).toString(), leftVal.getReference());
		asm.emit(operator.mnemonic, regs.get(0).toString(), regs.get(2).toString());

		// Store result to LHS variable.
		asm.emit("store", regs.get(0).toString(), leftVal.getReference());

		// Deallocate the third register.
		regs.deallocate(asm);
	}

	private void checkTypes(Scope scope) throws SyntaxException
	{
		CType leftType = left.getType(scope);
		CType rightType = right.getType(scope).decay();
		CType leftDeref = leftType.dereference();

		if (operatorString.equals("=")) {
			if (right.isAssignableTo(leftType, scope))
				return;
		} else if (operatorString.equals("+=") || operatorString.equals("-=")) {
			if (leftType.isArithmetic() && rightType.isArithmetic())
				return;
			if (leftType.isPointer() && leftDeref.isObject() && rightType.isInteger())
				return;
		} else if (operatorString.equals("&=") || operatorString.equals("|=")
				|| operatorString.equals("^=")) {
			if (leftType.isInteger() && rightType.isInteger())
				return;
		} else if (operatorString.equals("%=")) {
			if (leftType.isArithmetic() && rightType.isInteger())
				return;
		} else {
			if (leftType.isArithmetic() && rightType.isArithmetic())
				return;
		}

		throw new SyntaxException("Incompatible operands for operator " + operatorString + ".", getLine(), getColumn());
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		return left.getType(scope);
	}

	@Override
	public String toString()
	{
		return "(ASGN_EXPR " + operatorString + " " + left + " " + right + ")";
	}

	/**
	 * Attempts to parse a syntactic assignment expression from token stream. If
	 * parsing fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static Expression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Expression expr = BinaryExpression.parse(tokens);

		if (expr != null) {
			tokens.pushMark();
			Expression right = null;
			String op = tokens.read().toString();
			if (assignmentOperators.containsKey(op))
				right = AssignmentExpression.parse(tokens);

			tokens.popMark(right == null);
			if (right != null)
				expr = new AssignmentExpression(op, expr, right, line, column);
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
