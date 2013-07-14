package titocc.compiler.types;

import java.math.BigInteger;

/**
 * Abstract base class for all integer types. Classified as object, scalar, arithmetic and integer
 * type. Equals only to integer types of the same class.
 */
public abstract class IntegerType extends CType
{
	private final int rank;

	private final boolean signed;

	protected final int tag;

	/**
	 * Constructs an IntegerType
	 *
	 * @param rank rank of the integer type
	 */
	protected IntegerType(int rank, boolean signed, int tag)
	{
		this.rank = rank;
		this.signed = signed;
		this.tag = tag;
	}

	/**
	 * Returns the getRank of an integer type. All standard signed integer types have different
	 * ranks (except char and signed char have same), and all sunsigned integers types have
	 * different ranks. Unsigned version of each integer type has the same getRank as the signed
	 * counterpart.
	 *
	 * @return the getRank
	 */
	public int getRank()
	{
		return rank;
	}

	@Override
	public boolean isObject()
	{
		return true;
	}

	@Override
	public boolean isArithmetic()
	{
		return true;
	}

	@Override
	public boolean isInteger()
	{
		return true;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof IntegerType))
			return false;

		// Integer types are equal if they have same rank, signedness and tag.
		IntegerType type2 = (IntegerType) obj;
		return getRank() == type2.getRank() && isSigned() == type2.isSigned() && tag == type2.tag;
	}

	@Override
	public int getIncrementSize()
	{
		return 1;
	}

	/**
	 * Whether the integer type is signed.
	 *
	 * @return true if signed
	 */
	public boolean isSigned()
	{
		return signed;
	}

	@Override
	public CType promote()
	{
		// ($6.3.1.1/2)
		if (getRank() < INT.getRank()) {
			if (isSigned() || getSize() < INT.getSize())
				return INT;
			else
				return UINT;
		}

		return this;
	}

	@Override
	public String toString()
	{
		return names.get(this);
	}

	@Override
	public int hashCode()
	{
		return 10 * rank + (signed ? 5 : 1) + tag;
	}

	/**
	 * Get the corresponding unsigned integer type. If the type is already unsigned then returns the
	 * type itself
	 *
	 * @return unsigned integer type
	 */
	public IntegerType toUnsigned()
	{
		return this;
	}

	/**
	 * Returns the smallest value reprsentable in this type.
	 *
	 * @return smallest value
	 */
	public BigInteger getMinValue()
	{
		if (isSigned())
			return BigInteger.ZERO.subtract(BigInteger.ONE.shiftLeft(getSize() * 32 - 1));
		else
			return BigInteger.ZERO;
	}

	/**
	 * Returns the largest value reprsentable in this type.
	 *
	 * @return largest value
	 */
	public BigInteger getMaxValue()
	{
		int exp = getSize() * 32 - (isSigned() ? 1 : 0);
		return BigInteger.ONE.shiftLeft(exp).subtract(BigInteger.ONE);
	}
}
