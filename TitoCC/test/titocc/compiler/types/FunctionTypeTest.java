package titocc.compiler.types;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class FunctionTypeTest
{
	private CType t;
	private List<CType> params;

	@Before
	public void setUp()
	{
		params = new ArrayList();
		params.add(new IntType());
		t = new FunctionType(new IntType(), params);
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
		assertFalse(t.equals(new ArrayType(new ArrayType(new IntType(), 6), 7)));
		assertFalse(t.equals(new IntType()));
		assertFalse(t.equals(new VoidType()));
		assertFalse(t.equals(new PointerType(new ArrayType(new IntType(), 6))));
		assertTrue(t.equals(new FunctionType(new IntType(), params)));
		List<CType> params2 = new ArrayList();
		params2.add(new PointerType(new IntType()));
		assertFalse(t.equals(new FunctionType(new IntType(), params2)));
		assertFalse(t.equals(new FunctionType(new VoidType(), params)));
		assertFalse(t.equals(new InvalidType()));
	}
}
