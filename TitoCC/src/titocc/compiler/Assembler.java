package titocc.compiler;

import java.io.IOException;
import java.io.Writer;

/**
 * Formats and writes individual instructions to the output stream. Currently
 * just outputs symbolic assembly language (.k91 format) but this could maybe be
 * changed to machine code (.b91) if necessary.
 */
public class Assembler
{
	/**
	 * Writer object for outputting the instructions.
	 */
	private final Writer writer;
	/**
	 * Label to add to the next instruction.
	 */
	private String label = "";

	/**
	 * Constructs a new assembler object.
	 *
	 * @param writer Writer object that is used for instruction output.
	 */
	public Assembler(Writer writer)
	{
		this.writer = writer;
	}

	/**
	 * Emits an instruction with a single operand.
	 *
	 * @param instruction Mnemonic for the intruction.
	 * @param operand First operand (usually register).
	 * @throws IOException If thrown by writer.
	 */
	public void emit(String instruction, String operand) throws IOException
	{
		writer.append(String.format("%-11s %-7s %s\n", label, instruction, operand));
		label = "";
	}

	/**
	 * Emits an instruction with two operands.
	 *
	 * @param instruction Mnemonic for the intruction.
	 * @param operand1 First operand (usually register).
	 * @param operand2 Second (register or memory operand).
	 * @throws IOException If thrown by writer.
	 */
	public void emit(String instruction, String operand1, String operand2)
			throws IOException
	{
		writer.append(String.format("%-11s %-7s %s, %s\n", label, instruction, operand1, operand2));
		label = "";
	}

	/**
	 * Adds empty lines that have no instructions. Just for cosmetic purposes.
	 *
	 * @param n number of empty lines to add
	 * @throws IOException if writer throws
	 */
	public void addEmptyLines(int n) throws IOException
	{
		for (int i = 0; i < n; ++i)
			writer.append('\n');
	}

	/**
	 * Adds a label for the next instruction.
	 *
	 * @param label Label.
	 * @throws IOException if writer throws
	 */
	public void addLabel(String label) throws IOException
	{
		if (!this.label.isEmpty())
			emit("nop", "");
		this.label = label;
	}

	/**
	 * Emits a "nop" instruction in the end if there is a label without a
	 * corresponding instruction.
	 *
	 * @throws IOException if writer throws
	 */
	public void finish() throws IOException
	{
		if (!this.label.isEmpty())
			emit("nop", "");
	}

	/**
	 * Returns the writer object for this Assembler.
	 *
	 * @return the writer
	 */
	public Writer getWriter()
	{
		return writer;
	}
}
