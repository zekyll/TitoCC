package titocc.compiler.types;

import java.util.HashMap;
import java.util.Map;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.InternalCompilerException;
import titocc.compiler.Lvalue;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.VirtualRegister;

/**
 * 32-bit unsigned integer type. Implemented on TTK-91 using one 32-bit machine byte. The standard
 * integer types implemented using this representation are unsigned char, unsigned short int,
 * unsigned int and unsigned long int.
 */
class Uint32Type extends IntegerType
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
	 * Constructs a Uint32Type.
	 *
	 * @param rank rank
	 * @param tag tag
	 */
	public Uint32Type(int rank, int tag)
	{
		super(rank, false, tag);
	}

	@Override
	public int getSize()
	{
		return 1;
	}

	@Override
	public Rvalue compileConversion(IntermediateCompiler ic, Scope scope, Rvalue value,
			CType targetType)
	{
		if (targetType.equals(CType.BOOLISH) || targetType instanceof Int32Type
				|| targetType instanceof Uint32Type || targetType instanceof PointerType) {
			return value; // No-op.
		} else
			return super.compileConversion(ic, scope, value, targetType);
	}

	@Override
	public Rvalue compileBinaryBitwiseOperator(IntermediateCompiler ic, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		return new Int32Type(0, 0).compileBinaryBitwiseOperator(ic, scope, lhs, rhs, operator);
	}

	@Override
	public Rvalue compileBinaryComparisonOperator(IntermediateCompiler ic, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		// For relational operators apply an offset of 0x80000000 to both operands and then do
		// the comparison normally. For equality operators the signed operation can be used as is.
		if (!operator.equals("==") && !operator.equals("!=")) {
			Symbol msym = scope.find("__m");
			if (msym == null)
				throw new InternalCompilerException("Intrinsic __m not found.");
			ic.emit("xor", lhs.getRegister(), msym.getReference());
			ic.emit("xor", rhs.getRegister(), msym.getReference());
		}
		return new Int32Type(0, 0).compileBinaryComparisonOperator(ic, scope, lhs, rhs, operator);
	}

	@Override
	public Rvalue compileBinaryShiftOperator(IntermediateCompiler ic, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		// For unsigned ints right shift must be logical (not arithmetic right shift).
		ic.emit(instructions.get(operator), lhs.getRegister(), rhs.getRegister());
		return lhs;
	}

	@Override
	public Rvalue compileBinaryArithmeticOperator(IntermediateCompiler ic, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		if (operator.equals("/"))
			return compileDivisionOperator(ic, scope, lhs, rhs);
		else if (operator.equals("%"))
			return compileRemainderOperator(ic, scope, lhs, rhs);
		else {
			return new Int32Type(0, 0).compileBinaryArithmeticOperator(ic, scope, lhs, rhs,
					operator);
		}
	}

	@Override
	public Rvalue compileIncDecOperator(IntermediateCompiler ic, Scope scope, Lvalue operand,
			boolean inc, boolean postfix, int incSize)
	{
		return new Int32Type(0, 0).compileIncDecOperator(ic, scope, operand, inc, postfix, incSize);
	}

	@Override
	public Rvalue compileUnaryPlusMinusOperator(IntermediateCompiler ic, Scope scope,
			Rvalue operand, boolean plus)
	{
		return new Int32Type(0, 0).compileUnaryPlusMinusOperator(ic, scope, operand, plus);
	}

	@Override
	public Rvalue compileUnaryBitwiseNegationOperator(IntermediateCompiler ic, Scope scope,
			Rvalue operand)
	{
		return new Int32Type(0, 0).compileUnaryBitwiseNegationOperator(ic, scope, operand);
	}

	/**
	 * Implementation of 32-bit unsigned division using the __udiv intrinsic.
	 */
	private Rvalue compileDivisionOperator(IntermediateCompiler ic, Scope scope, Rvalue lhs,
			Rvalue rhs)
	{
		ic.emit("add", VirtualRegister.SP, "=" + getSize());
		ic.emit("push", VirtualRegister.SP, lhs.getRegister());
		ic.emit("push", VirtualRegister.SP, rhs.getRegister());
		Symbol udivSym = scope.find("__udiv");
		if (udivSym == null)
			throw new InternalCompilerException("Intrinsic __udiv not found.");
		ic.emit("call", VirtualRegister.SP, udivSym.getReference());
		ic.emit("pop", VirtualRegister.SP, lhs.getRegister());

		return lhs;
	}

	/**
	 * Implementation of 32-bit unsigned division using the __udiv intrinsic.
	 */
	private Rvalue compileRemainderOperator(IntermediateCompiler ic, Scope scope, Rvalue lhs,
			Rvalue rhs)
	{
		ic.emit("add", VirtualRegister.SP, "=" + getSize());
		ic.emit("push", VirtualRegister.SP, lhs.getRegister());
		ic.emit("push", VirtualRegister.SP, rhs.getRegister());
		Symbol udivSym = scope.find("__udiv");
		if (udivSym == null)
			throw new InternalCompilerException("Intrinsic __udiv not found.");
		ic.emit("call", VirtualRegister.SP, udivSym.getReference());
		VirtualRegister tmpReg = new VirtualRegister();
		ic.emit("pop", VirtualRegister.SP, tmpReg);
		ic.emit("mul", rhs.getRegister(), tmpReg);
		ic.emit("sub", lhs.getRegister(), rhs.getRegister());

		return lhs;
	}
}
