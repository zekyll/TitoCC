package titocc.compiler.types;

import java.util.HashMap;
import java.util.Map;
import titocc.compiler.ExpressionAssembler;
import titocc.compiler.Lvalue;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.VirtualRegister;

/**
 * 32-bit signed integer type. Implemented on TTK-91 using one 32-bit machine byte. The standard
 * integer types implemented using this representation are char, signed char, short int, int and
 * long int.
 */
class Int32Type extends IntegerType
{
	static final Map<String, String> instructions = new HashMap<String, String>()
	{
		{
			put("||", "jnzer");
			put("&&", "jzer");
			put("|", "or");
			put("^", "xor");
			put("&", "and");
			put("==", "jequ");
			put("!=", "jnequ");
			put("<", "jles");
			put("<=", "jngre");
			put(">", "jgre");
			put(">=", "jnles");
			put("<<", "shl");
			put(">>", "shr");
			put("+", "add");
			put("-", "sub");
			put("*", "mul");
			put("/", "div");
			put("%", "mod");
		}
	};

	/**
	 * Constructs an Int32Type.
	 *
	 * @param rank rank
	 * @param tag tag
	 */
	public Int32Type(int rank, int tag)
	{
		super(rank, true, tag);
	}

	@Override
	public int getSize()
	{
		return 1;
	}

	@Override
	public CType toUnsigned()
	{
		return new Uint32Type(getRank(), tag);
	}

	@Override
	public Rvalue compileConversion(ExpressionAssembler asm, Scope scope, Rvalue value,
			CType targetType)
	{
		if (targetType.equals(CType.BOOLISH) || targetType instanceof Int32Type
				|| targetType instanceof Uint32Type || targetType instanceof PointerType) {
			return value; // No-op.
		} else
			return super.compileConversion(asm, scope, value, targetType);
	}

	@Override
	public Rvalue compileBinaryBitwiseOperator(ExpressionAssembler asm, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		asm.emit(instructions.get(operator), lhs.getRegister(), rhs.getRegister());
		return lhs;
	}

	@Override
	public Rvalue compileBinaryComparisonOperator(ExpressionAssembler asm, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		String jumpLabel = scope.makeGloballyUniqueName("lbl");
		asm.emit("comp", lhs.getRegister(), rhs.getRegister());
		asm.emit("load", lhs.getRegister(), "=1");
		asm.emit(instructions.get(operator), VirtualRegister.NONE, jumpLabel);
		asm.emit("load", lhs.getRegister(), "=0");
		asm.addLabel(jumpLabel);
		return lhs;
	}

	@Override
	public Rvalue compileBinaryShiftOperator(ExpressionAssembler asm, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		asm.emit(instructions.get(operator), lhs.getRegister(), rhs.getRegister());
		return lhs;
	}

	@Override
	public Rvalue compileBinaryArithmeticOperator(ExpressionAssembler asm, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		asm.emit(instructions.get(operator), lhs.getRegister(), rhs.getRegister());
		return lhs;
	}

	@Override
	public Rvalue compileIncDecOperator(ExpressionAssembler asm, Scope scope, Lvalue operand,
			boolean inc, boolean postfix, int incSize)
	{
		// Load value to new register.
		VirtualRegister retReg = new VirtualRegister();
		asm.emit("load", retReg, "0", operand.getRegister());

		// Modify and write back the value.
		asm.emit(inc ? "add" : "sub", retReg, "=" + incSize);
		asm.emit("store", retReg, "0", operand.getRegister());

		// Postfix operator must return the old value.
		if (postfix)
			asm.emit(inc ? "sub" : "add", retReg, "=" + incSize);

		return new Rvalue(retReg);
	}

	@Override
	public Rvalue compileUnaryPlusMinusOperator(ExpressionAssembler asm, Scope scope,
			Rvalue operand, boolean plus)
	{
		// Unary plus is no-op;
		if (plus)
			return operand;

		// Negative in two's complement: negate all bits and add 1.
		asm.emit("xor", operand.getRegister(), "=-1");
		asm.emit("add", operand.getRegister(), "=1");

		return operand;
	}

	@Override
	public Rvalue compileUnaryBitwiseNegationOperator(ExpressionAssembler asm, Scope scope,
			Rvalue operand)
	{
		// -1 has representation of all 1 bits (0xFFFFFFFF), and therefore xoring with it gives
		// the bitwise negation.
		asm.emit("xor", operand.getRegister(), "=-1");
		return operand;
	}
}
