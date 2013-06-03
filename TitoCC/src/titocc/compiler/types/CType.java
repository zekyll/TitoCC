package titocc.compiler.types;

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
	 * Returns type obtained from dereferencing a pointer or array type.
	 *
	 * @return dereferenced type or an instance of InvalidType if this is not a pointer or array
	 * type
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
	 * If the type is not incrementable then returns 0.
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
	 * Decays an array type into a pointer type. All other types are unaffected.
	 *
	 * @return the decayed type
	 */
	public CType decay()
	{
		return this;
	}
}
