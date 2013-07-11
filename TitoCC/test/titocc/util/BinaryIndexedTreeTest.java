package titocc.util;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class BinaryIndexedTreeTest
{
	BinaryIndexedTree bit;

	@Before
	public void setUp()
	{
		bit = new BinaryIndexedTree(7);
	}

	@Test
	public void newTreeContainsZeros()
	{
		for (int i = 0; i <= 7; ++i)
			assertEquals(0, bit.get(i));
	}

	@Test
	public void test()
	{
		bit.update(3, 100);
		bit.update(6, -10);
		bit.update(7, -3);
		bit.update(6, -10);
		bit.update(1, 1000);
		assertEquals(0, bit.get(0));
		assertEquals(1000, bit.get(1));
		assertEquals(1000, bit.get(2));
		assertEquals(1100, bit.get(3));
		assertEquals(1100, bit.get(4));
		assertEquals(1100, bit.get(5));
		assertEquals(1080, bit.get(6));
		assertEquals(1077, bit.get(7));
	}
}
