package titocc.compiler.types;

import java.util.HashMap;
import java.util.Map;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.InternalCompilerException;
import titocc.compiler.Lvalue;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;

/**
 * Abstract base class for representing types in C type system. Allows testing equality between
 * types and querying their features.
 */
public abstract class CType
{
	/**
	 * Returns whether the type is an object (i.e. not a function or void).
	 *
	 * @return true if object type
	 */
	public boolean isObject()
	{
		return false;
	}

	/**
	 * Returns whether the type is a function type.
	 *
	 * @return true if function type
	 */
	public boolean isFunction()
	{
		return false;
	}

	/**
	 * Returns whether the type is incomplete (e.g void or array without size).
	 *
	 * @return true if incomplete type
	 */
	public boolean isIncomplete()
	{
		return false;
	}

	/**
	 * Returns whether the type is a scalar type (arithmetic or pointer).
	 *
	 * @return true if scalar type
	 */
	public boolean isScalar()
	{
		return isPointer() || isArithmetic();
	}

	/**
	 * Returns true if the type is a pointer type (including void pointers).
	 *
	 * @return true if pointer type
	 */
	public boolean isPointer()
	{
		return false;
	}

	/**
	 * Returns type obtained from dereferencing a pointer type.
	 *
	 * @return dereferenced type or an instance of InvalidType if this is not a pointer type
	 */
	public CType dereference()
	{
		return new InvalidType();
	}

	/**
	 * Returns whether the type is arithmetic (integer or floating point).
	 *
	 * @return true if arithmetic type
	 */
	public boolean isArithmetic()
	{
		return false;
	}

	/**
	 * Returns whether the type is an integer type.
	 *
	 * @return true if integer type
	 */
	public boolean isInteger()
	{
		return false;
	}

	/**
	 * Returns the size of the type in chars. Note that in ttk-91 char is the same as int (i.e. 4
	 * bytes), so sizeof(int) == 1.
	 *
	 * @return the size in chars or 0 if not an object type
	 */
	public int getSize()
	{
		return 0;
	}

	/**
	 * Returns the step size when incrementing/decrementing the type using -- ++ + += etc operators.
	 * If the type is not incrementable (i.e. pointer or real type) then returns 0.
	 *
	 * @return increment size or 0 if not incrementable
	 */
	public int getIncrementSize()
	{
		return dereference().getSize();
	}

	/**
	 * Returns whether the type is valid. (i.e. it is not instance of InvalidType).
	 *
	 * @return true if valid
	 */
	public boolean isValid()
	{
		return !(this instanceof InvalidType);
	}

	/**
	 * Decays an array type into a pointer and a function type into a function pointer according
	 * to ($6.3.2.1/3) and ($6.3.2.1/4). All other types are unaffected.
	 *
	 * @return the decayed type
	 */
	public CType decay()
	{
		return this;
	}

	/**
	 * Apply integer promotions to the type.
	 *
	 * @return the promoted type
	 */
	public CType promote()
	{
		return this;
	}

	/**
	 * Finds the common type for two arithmetic types according to "usual arithmetic conversions".
	 * ($6.3.1.8/1)
	 *
	 * @param left type of first operand
	 * @param left type of second operand
	 */
	public static CType getCommonType(CType left, CType right)
	{
		if (!left.isArithmetic() || !right.isArithmetic()) {
			throw new InternalCompilerException("Arithmetic conversion attempted on"
					+ "non-arithmetic type.");
		}

		//TODO long double, double, float

		// Both must be integers.
		IntegerType leftPromoted = (IntegerType) left.promote();
		IntegerType rightPromoted = (IntegerType) right.promote();

		// If types are same then use that type.
		if (leftPromoted.equals(rightPromoted))
			return leftPromoted;

		// If both signed or both unsigned, use type with higher getRank.
		if (leftPromoted.isSigned() == rightPromoted.isSigned())
			return leftPromoted.getRank() > rightPromoted.getRank() ? leftPromoted : rightPromoted;

		// Other type is signed and other one unsigned.
		IntegerType signed = leftPromoted.isSigned() ? leftPromoted : rightPromoted;
		IntegerType unsigned = !leftPromoted.isSigned() ? leftPromoted : rightPromoted;

		// If unsigned has higher getRank, use the unsigned type.
		if (unsigned.getRank() > signed.getRank())
			return unsigned;

		// If unsigned fits the signed, use the signed type.
		if (unsigned.getSize() < signed.getSize())
			return signed;

		// Unsigned type corresponding to the signed type.
		return signed.toUnsigned();
	}

	/**
	 * Generates code that converts a value of this type to the target type.
	 *
	 * @param ic intermediate compiler used for code generation
	 * @param scope scope in which the compilation takes place; only used for adding labels
	 * @param value operand value
	 * @param targetType target type of the conversion
	 * @return Rvalue object describing the result value
	 */
	public Rvalue compileConversion(IntermediateCompiler ic, Scope scope, Rvalue value,
			CType targetType)
	{
		if (targetType.equals(CType.VOID))
			return new Rvalue(null);
		else
			throw new InternalCompilerException("Unimplemented type conversion.");
	}

	/**
	 * Generates code for binary bitwise operator with two operands of this type.
	 *
	 * @param ic intermediate compiler used for code generation
	 * @param scope scope in which the compilation takes place; only used for adding labels
	 * @param lhs LHS value
	 * @param rhs RHS value
	 * @param operator operator as a string
	 * @return Rvalue object describing the result value
	 */
	public Rvalue compileBinaryBitwiseOperator(IntermediateCompiler ic, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		throw new InternalCompilerException("Unimplemented binary bitwise operator.");
	}

	/**
	 * Generates code for binary comparison operator with two operands of this type.
	 *
	 * @param ic intermediate compiler used for code generation
	 * @param scope scope in which the compilation takes place; only used for adding labels
	 * @param lhs LHS value
	 * @param rhs RHS value
	 * @param operator operator as a string
	 * @return Rvalue object describing the result value
	 */
	public Rvalue compileBinaryComparisonOperator(IntermediateCompiler ic, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		throw new InternalCompilerException("Unimplemented binary comparison operator.");
	}

	/**
	 * Generates code for binary shift operator, where left operand has this type and right
	 * operand has "int" type.
	 *
	 * @param ic intermediate compiler used for code generation
	 * @param scope scope in which the compilation takes place; only used for adding labels
	 * @param lhs LHS value
	 * @param rhs RHS value
	 * @param operator operator as a string
	 * @return Rvalue object describing the result value
	 */
	public Rvalue compileBinaryShiftOperator(IntermediateCompiler ic, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		throw new InternalCompilerException("Unimplemented binary shift operator.");
	}

	/**
	 * Generates code for binary arithmetic operator with two operands of this type.
	 *
	 * @param ic intermediate compiler used for code generation
	 * @param scope scope in which the compilation takes place; only used for adding labels
	 * @param lhs LHS value
	 * @param rhs RHS value
	 * @param operator operator as a string
	 * @return Rvalue object describing the result value
	 */
	public Rvalue compileBinaryArithmeticOperator(IntermediateCompiler ic, Scope scope, Rvalue lhs,
			Rvalue rhs, String operator)
	{
		throw new InternalCompilerException("Unimplemented binary arithmetic operator.");
	}

	/**
	 * Generates code for unary increment/decrement operator for this type.
	 *
	 * @param ic intermediate compiler used for code generation
	 * @param scope scope in which the compilation takes place; only used for adding labels
	 * @param operand modifiable lvalue operand
	 * @param inc true if increment operator, false if decrement
	 * @param postfix true if postfix operator, false if prefix operator
	 * @param incSize how many steps are incremented/decrement
	 * @return Rvalue object describing the result value
	 */
	public Rvalue compileIncDecOperator(IntermediateCompiler ic, Scope scope, Lvalue operand,
			boolean inc, boolean postfix, int incSize)
	{
		throw new InternalCompilerException("Unimplemented increment/decrement operator.");
	}

	/**
	 * Generates code for unary plus/minus operator for this type.
	 *
	 * @param ic intermediate compiler used for code generation
	 * @param scope scope in which the compilation takes place; only used for adding labels
	 * @param operand operand value
	 * @param plus true if unary plus, false if unary minus
	 * @return Rvalue object describing the result value
	 */
	public Rvalue compileUnaryPlusMinusOperator(IntermediateCompiler ic, Scope scope,
			Rvalue operand, boolean plus)
	{
		throw new InternalCompilerException("Unimplemented unary plus/minus operator.");
	}

	/**
	 * Generates code for unary bitwise negation operator for this type.
	 *
	 * @param ic intermediate compiler used for code generation
	 * @param scope scope in which the compilation takes place; only used for adding labels
	 * @param operand operand value
	 * @return Rvalue object describing the result value
	 */
	public Rvalue compileUnaryBitwiseNegationOperator(IntermediateCompiler ic, Scope scope,
			Rvalue operand)
	{
		throw new InternalCompilerException("Unimplemented unary bitwise negation operator.");
	}

	/**
	 * Standard "void" type.
	 */
	public static CType VOID = new VoidType();

	/**
	 * Standard "char" type.
	 */
	public static IntegerType CHAR = new Int32Type(0, 0);

	/**
	 * Standard "signed char" type.
	 */
	public static IntegerType SCHAR = new Int32Type(0, 1);

	/**
	 * Standard "short int" type.
	 */
	public static IntegerType SHORT = new Int32Type(1, 0);

	/**
	 * Standard "int" type.
	 */
	public static IntegerType INT = new Int32Type(2, 0);

	/**
	 * Standard "long int" type.
	 */
	public static IntegerType LONG = new Int32Type(3, 0);

	/**
	 * Standard "long long int" type.
	 */
	public static IntegerType LLONG = new Int64Type(4, 0);

	/**
	 * Standard "unsigned int type" type.
	 */
	public static IntegerType UCHAR = new Uint32Type(0, 0);

	/**
	 * Standard "unsigned short int" type.
	 */
	public static IntegerType USHORT = new Uint32Type(1, 0);

	/**
	 * Standard "unsigned int" type.
	 */
	public static IntegerType UINT = new Uint32Type(2, 0);

	/**
	 * Standard "unsigned long int" type.
	 */
	public static IntegerType ULONG = new Uint32Type(3, 0);

	/**
	 * Standard "unsigned long long int" type.
	 */
	public static IntegerType ULLONG = new Uint64Type(4, 0);

	/**
	 * Result type for subtraction between two pointers (ptrdiff_t).
	 */
	public static IntegerType PTRDIFF_T = LONG;

	/**
	 * Unsigned integer type that is able to hold the size of any object (size_t).
	 */
	public static IntegerType SIZE_T = ULONG;

	/**
	 * Signed integer type with one-to-one mapping with void*.
	 */
	public static IntegerType INTPTR_T = LONG;

	/**
	 * Unsigned integer type with one-to-one mapping with void*.
	 */
	public static IntegerType UINTPTR_T = ULONG;

	/**
	 * Type used for wide characters (wchar_t).
	 */
	public static IntegerType WCHAR_T = INT;

	/**
	 * Dummy type used when converting control expressions.
	 */
	public static CType BOOLISH = new BoolishType();

	/**
	 * Canonical names for standard types.
	 */
	protected static Map<IntegerType, String> names = new HashMap<IntegerType, String>()
	{
		{
			put(CType.CHAR, "char");
			put(CType.UCHAR, "unsigned char");
			put(CType.SCHAR, "signed char");
			put(CType.SHORT, "short int");
			put(CType.USHORT, "unsigned short int");
			put(CType.INT, "int");
			put(CType.UINT, "unsigned int");
			put(CType.LONG, "long int");
			put(CType.ULONG, "unsigned long int");
			put(CType.LLONG, "long long int");
			put(CType.ULLONG, "unsigned long long int");
		}
	};
}
