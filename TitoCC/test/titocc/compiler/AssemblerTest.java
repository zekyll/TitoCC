package titocc.compiler;

import java.io.IOException;
import java.io.StringWriter;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AssemblerTest
{
	private Assembler asm;

	@Before
	public void setUp()
	{
		asm = new Assembler(new StringWriter());
	}

	@Test
	public void writerIsEmptyWhenNothingIsWritten() throws IOException
	{
		assertEquals("", asm.getWriter().toString());
	}

	@Test
	public void singleOperandFormatIsCorrect() throws IOException
	{
		asm.emit("abc", "xy");
		assertEquals("            abc     xy\n", asm.getWriter().toString());
	}

	@Test
	public void doubleOperandFormatIsCorrect() throws IOException
	{
		asm.emit("abc", Register.R3, "zv");
		assertEquals("            abc     R3, zv\n", asm.getWriter().toString());
	}

	@Test
	public void noLabelIsAddedBeforeNextInstruction() throws IOException
	{
		asm.addLabel("lbl");
		assertEquals("", asm.getWriter().toString());
	}

	@Test
	public void labelIsAddedToSingleOperandInstruction() throws IOException
	{
		asm.addLabel("lbl");
		asm.emit("abc", "xy");
		assertEquals("lbl         abc     xy\n", asm.getWriter().toString());
	}

	@Test
	public void labelIsAddedToDoubleOperandInstruction() throws IOException
	{
		asm.addLabel("lbl2");
		asm.emit("abc", Register.R3, "zv");
		assertEquals("lbl2        abc     R3, zv\n", asm.getWriter().toString());
	}

	@Test
	public void longLabelWithSingleOperandInstruction() throws IOException
	{
		asm.addLabel("this_is_a_very_long_label");
		asm.emit("abc", "xy");
		assertEquals("this_is_a_very_long_label abc     xy\n", asm.getWriter().toString());
	}

	@Test
	public void longLabelWithDoubleOperandInstruction() throws IOException
	{
		asm.addLabel("this_is_a_very_long_label");
		asm.emit("abc", Register.R3, "zv");
		assertEquals("this_is_a_very_long_label abc     R3, zv\n", asm.getWriter().toString());
	}

	@Test
	public void addingTwoLabelsEmitsNop() throws IOException
	{
		asm.addLabel("lbl1");
		asm.addLabel("lbl2");
		asm.emit("abc", Register.R3, "zv");
		assertEquals("lbl1        nop     \nlbl2        abc     R3, zv\n",
				asm.getWriter().toString());
	}

	@Test
	public void finishDoesNothingIfNoLabel() throws IOException
	{
		asm.emit("abc", Register.R3, "zv");
		asm.finish();
		assertEquals("            abc     R3, zv\n", asm.getWriter().toString());
	}

	@Test
	public void finishAddsNopInstructionIfLabel() throws IOException
	{
		asm.emit("abc", Register.R4, "zv");
		asm.addLabel("l");
		asm.finish();
		assertEquals("            abc     R4, zv\nl           nop     \n",
				asm.getWriter().toString());
	}

	@Test
	public void addEmptyLinesWorksWithZeroLines() throws IOException
	{
		asm.emit("abc", "xy");
		asm.addLabel("l");
		asm.addEmptyLines(0);
		asm.emit("abc", "xy");
		assertEquals("            abc     xy\nl           abc     xy\n",
				asm.getWriter().toString());
	}

	@Test
	public void addEmptyLinesWorksWithTwoLines() throws IOException
	{
		asm.emit("abc", "xy");
		asm.addLabel("l");
		asm.addEmptyLines(2);
		asm.emit("abc", "xy");
		assertEquals("            abc     xy\n\n\nl           abc     xy\n",
				asm.getWriter().toString());
	}
}
