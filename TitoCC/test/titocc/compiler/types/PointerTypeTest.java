package titocc.compiler.types;

import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class PointerTypeTest
{
	private CType t;

	@Before
	public void setUp()
	{
		t = new PointerType(new ArrayType(CType.INT, 6));
	}

	@Test
	public void hasRightProperties()
	{
		assertTrue(t.isObject());
		assertTrue(t.isScalar());
		assertTrue(t.isPointer());
		assertFalse(t.isArithmetic());
		assertFalse(t.isInteger());
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
		assertEquals(6, t.getIncrementSize());
	}

	@Test
	public void dereferenceReturnsCorrectType()
	{
		assertEquals(new ArrayType(CType.INT, 6), t.dereference());
	}

	@Test
	public void decayReturnsCorrectType()
	{
		assertEquals(t, t.decay());
	}

	@Test
	public void equalsWorksCorrectly()
	{
		assertFalse(t.equals(new ArrayType(new ArrayType(CType.INT, 6), 7)));
		assertFalse(t.equals(CType.INT));
		assertFalse(t.equals(CType.VOID));
		assertTrue(t.equals(new PointerType(new ArrayType(CType.INT, 6))));
		assertFalse(t.equals(new PointerType(new ArrayType(CType.INT, 5))));
		assertFalse(t.equals(new FunctionType(CType.VOID, new ArrayList<CType>())));
		assertFalse(t.equals(new InvalidType()));
	}
}
