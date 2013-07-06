package titocc.compiler.elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Vstack;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Expression formed by any of the assignment operators and two operands.
 *
 * <p> EBNF definition:
 *
 * <br> ASSIGNMENT_EXPRESSION = BINARY_EXPRESSION [ASSIGNMENT_OPERATOR ASSIGNMENT_EXPRESSION]
 *
 * <br> ASSIGNMENT_OPERATOR = "=" | "+=" | "*=" | "&=" | "|=" | "^=" | "-=" | "/=" | "%=" | "<<=" |
 * ">>="
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

	/**
	 * Assignment operator.
	 */
	private final Operator operator;

	/**
	 * String representation of the operator.
	 */
	private final String operatorString;

	/**
	 * Left hand side expression.
	 */
	private final Expression left;

	/**
	 * Right hand side expression.
	 */
	private final Expression right;

	/**
	 * Constructs a new AssignmentExpression
	 *
	 * @param operator string representation of the operator
	 * @param left left operand
	 * @param right right operand
	 */
	public AssignmentExpression(String operator, Expression left,
			Expression right, Position position)
	{
		super(position);
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
	public void compile(Assembler asm, Scope scope, Vstack vstack)
			throws SyntaxException, IOException
	{
		checkTypes(scope);

		// Compile Operator.
		if (operator.type == Type.SIMPLE)
			compileSimple(asm, scope, vstack);
		else if (operator.type == Type.COMMUTATIVE)
			compileCommutative(asm, scope, vstack);
		else
			compileNoncommutative(asm, scope, vstack);
	}

	private void compileLeft(Assembler asm, Scope scope, Vstack vstack)
			throws SyntaxException, IOException
	{
		// Evaluate RHS; load to 2nd register;
		vstack.enterFrame();
		left.compileAsLvalue(asm, scope, vstack, false);
		vstack.exitFrame(asm);
	}

	private void compileSimple(Assembler asm, Scope scope, Vstack vstack)
			throws SyntaxException, IOException
	{
		// Evaluate RHS; load to 1st register.
		right.compile(asm, scope, vstack);
		Register rightReg = vstack.loadTopValue(asm);

		// Evaluate LHS; load address to 2nd register.
		compileLeft(asm, scope, vstack);

		asm.emit("store", rightReg, vstack.top(0));

		vstack.pop();
	}

	private void compileCommutative(Assembler asm, Scope scope, Vstack vstack)
			throws SyntaxException, IOException
	{
		// Evaluate RHS; load value to 1st register.
		right.compile(asm, scope, vstack);
		Register rightReg = vstack.loadTopValue(asm);

		// If operation is POINTER += INTEGER, we need to scale the integer value.
		int incSize = left.getType(scope).decay().getIncrementSize();
		if (incSize > 1)
			asm.emit("mul", rightReg, "=" + incSize);

		// Evaluate LHS; load address to 2nd register.
		compileLeft(asm, scope, vstack);

		// Because the operation is symmetric, we can use the left operand
		// as the right operand in the assembly instruction, saving one register.
		asm.emit(operator.mnemonic, rightReg, vstack.top(0));
		asm.emit("store", rightReg, vstack.top(0));

		vstack.pop();
	}

	private void compileNoncommutative(Assembler asm, Scope scope, Vstack vstack)
			throws SyntaxException, IOException
	{
		// Allocate 1st register for return value;
		Register retReg = vstack.pushRegisterRvalue(asm);

		// Evaluate RHS; load value to 2nd register.
		vstack.enterFrame();
		right.compile(asm, scope, vstack);
		Register rightReg = vstack.loadTopValue(asm);

		// Evaluate LHS; load address to 3rd register.
		compileLeft(asm, scope, vstack);
		vstack.exitFrame(asm);

		// If operation is POINTER -= INTEGER, we need to scale the integer value.
		int incSize = left.getType(scope).decay().getIncrementSize();
		if (incSize > 1)
			asm.emit("mul", rightReg, "=" + incSize);

		// Load LHS value to 1st register and operate on it.
		asm.emit("load", retReg, vstack.top(0));
		asm.emit(operator.mnemonic, retReg, rightReg.toString());

		// Store result to LHS variable.
		asm.emit("store", retReg, vstack.top(0));

		// Deallocate 2nd and 3rd register.
		vstack.pop();
		vstack.pop();
	}

	private void checkTypes(Scope scope) throws SyntaxException
	{
		CType leftType = left.getType(scope).decay();
		CType rightType = right.getType(scope).decay();

		// Compound assignment rules defined in ($6.5.16.2).
		if (operatorString.equals("=")) {
			if (right.isAssignableTo(leftType, scope))
				return;
		} else if (operatorString.equals("+=") || operatorString.equals("-=")) {
			if (leftType.dereference().isObject() && rightType.isInteger())
				return;
			if (leftType.isArithmetic() && rightType.isArithmetic())
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

		throw new SyntaxException("Incompatible operands for operator " + operatorString + ".",
				getPosition());
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
	 * Attempts to parse a syntactic assignment expression from token stream. If parsing fails the
	 * stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static Expression parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
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
				expr = new AssignmentExpression(op, expr, right, pos);
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
