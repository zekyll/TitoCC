package titocc.compiler.types;

import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ArrayTypeTest
{
	private CType t;

	@Before
	public void setUp()
	{
		t = new ArrayType(new ArrayType(new IntType(), 6), 7);
	}

	@Test
	public void hasRightProperties()
	{
		assertTrue(t.isObject());
		assertFalse(t.isScalar());
		assertFalse(t.isPointer());
		assertFalse(t.isArithmetic());
		assertFalse(t.isInteger());
		assertTrue(t.isValid());
	}

	@Test
	public void sizeIsCorrect()
	{
		assertEquals(7 * 6 * new IntType().getSize(), t.getSize());
	}

	@Test
	public void incrementSizeIsCorrect()
	{
		assertEquals(6, t.getIncrementSize());
	}

	@Test
	public void dereferenceReturnsCorrectType()
	{
		assertEquals(new ArrayType(new IntType(), 6), t.dereference());
	}

	@Test
	public void decayReturnsCorrectType()
	{
		assertEquals(new PointerType(new ArrayType(new IntType(), 6)), t.decay());
	}

	@Test
	public void equalsWorksCorrectly()
	{
		assertTrue(t.equals(new ArrayType(new ArrayType(new IntType(), 6), 7)));
		assertFalse(t.equals(new ArrayType(new ArrayType(new IntType(), 5), 7)));
		assertFalse(t.equals(new ArrayType(new ArrayType(new IntType(), 6), 8)));
		assertFalse(t.equals(new IntType()));
		assertFalse(t.equals(new VoidType()));
		assertFalse(t.equals(new PointerType(new ArrayType(new IntType(), 6))));
		assertFalse(t.equals(new FunctionType(new VoidType(), new ArrayList<CType>())));
		assertFalse(t.equals(new InvalidType()));
	}
}
