package titocc.compiler.types;

import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class VoidTypeTest
{
	private CType t;

	@Before
	public void setUp()
	{
		t = CType.VOID;
	}

	@Test
	public void hasRightProperties()
	{
		assertFalse(t.isObject());
		assertFalse(t.isScalar());
		assertFalse(t.isPointer());
		assertFalse(t.isArithmetic());
		assertFalse(t.isInteger());
		assertTrue(t.isValid());
	}

	@Test
	public void sizeIsCorrect()
	{
		assertEquals(0, t.getSize());
	}

	@Test
	public void incrementSizeIsCorrect()
	{
		assertEquals(0, t.getIncrementSize());
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
		assertFalse(t.equals(new ArrayType(new ArrayType(CType.INT, 6), 7)));
		assertFalse(t.equals(CType.INT));
		assertTrue(t.equals(CType.VOID));
		assertFalse(t.equals(new PointerType(new ArrayType(CType.INT, 6))));
		assertFalse(t.equals(new FunctionType(CType.VOID, new ArrayList<CType>())));
		assertFalse(t.equals(new InvalidType()));
	}
}
