package titocc.compiler.types;

import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class IntTypeTest
{
	private CType t;

	@Before
	public void setUp()
	{
		t = new IntType();
	}

	@Test
	public void hasRightProperties()
	{
		assertTrue(t.isObject());
		assertTrue(t.isScalar());
		assertFalse(t.isPointer());
		assertTrue(t.isArithmetic());
		assertTrue(t.isInteger());
		assertTrue(t.isValid());
	}

	@Test
	public void sizeIsCorrect()
	{
		assertEquals(1, t.getSize());
	}

	@Test
	public void incrementSizeIsCorrect()
	{
		assertEquals(1, t.getIncrementSize());
	}

	@Test
	public void dereferenceReturnsCorrectType()
	{
		assertTrue(t.dereference() instanceof InvalidType);
	}

	@Test
	public void decayReturnsCorrectType()
	{
		assertEquals(t, t.decay());
	}

	@Test
	public void equalsWorksCorrectly()
	{
		assertFalse(t.equals(new ArrayType(new ArrayType(new IntType(), 6), 7)));
		assertTrue(t.equals(new IntType()));
		assertFalse(t.equals(new VoidType()));
		assertFalse(t.equals(new PointerType(new ArrayType(new IntType(), 6))));
		assertFalse(t.equals(new FunctionType(new VoidType(), new ArrayList<CType>())));
		assertFalse(t.equals(new InvalidType()));
	}
}
