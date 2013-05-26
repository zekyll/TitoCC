package titocc.util;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class PositionTest
{
	Position pos;

	@Test
	public void equalsReturnsTrue()
	{
		assertEquals(pos, new Position(5, 7));
	}

	@Before
	public void setUp()
	{
		pos = new Position(5, 7);
	}

	@Test
	public void equalsReturnsTrueWhenDifferent()
	{
		assertFalse(pos.equals(new Position(5, 8)));
		assertFalse(pos.equals(new Position(4, 7)));
	}

	@Test
	public void compareTo()
	{
		assertTrue(pos.compareTo(new Position(5, 8)) < 0);
		assertTrue(pos.compareTo(new Position(6, 7)) < 0);
		assertTrue(pos.compareTo(new Position(5, 7)) == 0);
		assertTrue(pos.compareTo(new Position(5, 6)) > 0);
		assertTrue(pos.compareTo(new Position(4, 7)) > 0);
	}
}
