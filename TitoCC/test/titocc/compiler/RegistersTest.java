package titocc.compiler;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class RegistersTest
{
	private Registers regs;
	private Assembler asm;
	private Writer writer;

	@Before
	public void setUp()
	{
		regs = new Registers();
		writer = new StringWriter()
		{
		};
		asm = new Assembler(writer);
	}

	@Test
	public void newManagerHasOneActiveRegister()
	{
		assertEquals(1, regs.getActiveRegisterCount());
		assertEquals(Register.R1, regs.get(0));
	}

	@Test
	public void allocateAddsAnActiveRegister() throws IOException
	{
		regs.allocate(asm);
		assertEquals(2, regs.getActiveRegisterCount());
		assertEquals(Register.R1, regs.get(0));
		assertEquals(Register.R2, regs.get(1));
		assertEquals("", writer.toString());
	}

	@Test
	public void deallocateRemovesAnActiveRegister() throws IOException
	{
		regs.allocate(asm);
		regs.deallocate(asm);
		assertEquals(1, regs.getActiveRegisterCount());
		assertEquals(Register.R1, regs.get(0));
		assertEquals("", writer.toString());
	}

	@Test
	public void removeFirstRemovesAnActiveRegister() throws IOException
	{
		regs.allocate(asm);
		regs.removeFirst();
		assertEquals(1, regs.getActiveRegisterCount());
		assertEquals(Register.R2, regs.get(0));
		assertEquals("", writer.toString());
	}

	@Test
	public void addFirstAddsAnActiveRegister() throws IOException
	{
		regs.allocate(asm);
		regs.removeFirst();
		regs.addFirst();
		assertEquals(2, regs.getActiveRegisterCount());
		assertEquals(Register.R1, regs.get(0));
		assertEquals(Register.R2, regs.get(1));
		assertEquals("", writer.toString());
	}

	@Test
	public void getThrowsIfOutOfRange() throws IOException
	{
		try {
			regs.get(1);
			fail("InternalCompilerException not thrown.");
		} catch (InternalCompilerException e) {
		}
	}

	@Test
	public void removeFirstThrowsIfNoActiveRegisters() throws IOException
	{
		regs.removeFirst();
		try {
			regs.removeFirst();
			fail("InternalCompilerException not thrown.");
		} catch (InternalCompilerException e) {
		}
	}

	@Test
	public void addFirstThrowsIfNoRemovedRegisters() throws IOException
	{
		try {
			regs.addFirst();
			fail("InternalCompilerException not thrown.");
		} catch (InternalCompilerException e) {
		}

		regs.removeFirst();
		regs.addFirst();
		try {
			regs.addFirst();
			fail("InternalCompilerException not thrown.");
		} catch (InternalCompilerException e) {
		}
	}

	@Test
	public void canAllocateFiveBeforePushing() throws IOException
	{
		for (int i = 0; i < 4; ++i)
			regs.allocate(asm);
		assertEquals(5, regs.getActiveRegisterCount());
		assertEquals(Register.R5, regs.get(4));
		assertEquals("", writer.toString());
	}

	@Test
	public void pushesToStackWhenAllocatedMoreThanFiveRegisters() throws IOException
	{
		for (int i = 0; i < 4; ++i)
			regs.allocate(asm);
		regs.removeFirst();
		regs.allocate(asm);

		assertEquals(5, regs.getActiveRegisterCount());
		assertEquals(Register.R2, regs.get(0));
		assertEquals(Register.R1, regs.get(4));
		assertTrue(writer.toString().matches(".*push.*R1.*\n"));
	}

	@Test
	public void deallocatePopsPushedRegister() throws IOException
	{
		for (int i = 0; i < 4; ++i)
			regs.allocate(asm);
		regs.removeFirst();
		regs.allocate(asm);
		regs.deallocate(asm);

		assertEquals(4, regs.getActiveRegisterCount());
		assertEquals(Register.R2, regs.get(0));
		assertEquals(Register.R5, regs.get(3));
		assertTrue(writer.toString().matches(".*push.*R1.*\n.*pop.*R1.*\n"));
	}

	@Test
	public void allocateThrowsWhenTooManyActiveRegisters() throws IOException
	{
		regs.removeFirst();
		for (int i = 0; i < 5; ++i)
			regs.allocate(asm);
		try {
			regs.allocate(asm);
			fail("InternalCompilerException not thrown.");
		} catch (InternalCompilerException e) {
		}
	}

	@Test
	public void deallocateThrowsIfNoActiveRegisters() throws IOException
	{
		regs.removeFirst();
		try {
			regs.deallocate(asm);
			fail("InternalCompilerException not thrown.");
		} catch (InternalCompilerException e) {
		}
	}

	@Test
	public void pushedRegisterCanbeReactivated() throws IOException
	{
		for (int i = 0; i < 4; ++i)
			regs.allocate(asm);
		regs.removeFirst();
		regs.allocate(asm);
		regs.deallocate(asm);
		regs.addFirst();

		assertEquals(5, regs.getActiveRegisterCount());
		assertEquals(Register.R1, regs.get(0));
		assertEquals(Register.R5, regs.get(4));
		assertTrue(writer.toString().matches(".*push.*R1.*\n.*pop.*R1.*\n"));
	}

	@Test
	public void canPushArbitraryNumberOfRegisters() throws IOException
	{
		for (int i = 0; i < 20; ++i) {
			regs.allocate(asm);
			regs.removeFirst();
		}
		for (int i = 0; i < 20; ++i) {
			regs.deallocate(asm);
			regs.addFirst();
		}
		assertEquals(1, regs.getActiveRegisterCount());
		assertEquals(Register.R1, regs.get(0));
	}
}
