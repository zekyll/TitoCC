package titocc.compiler;

import java.io.IOException;
import java.io.Writer;

/**
 * Formats and writes individual instructions to the output stream. Currently
 * just outputs symbolic assembly language (.k91 format) but this could be
 * changed to machine code (.b91).
 */
public class Assembler
{
	private Writer writer;

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
	 * @param label Optional label.
	 * @param instruction Mnemonic for the intruction.
	 * @param operand First operand (usually register).
	 * @throws IOException If thrown by writer.
	 */
	public void emit(String label, String instruction, String operand) throws IOException
	{
		writer.append(String.format("%-7s %-7s %s\n", label, instruction, operand));
	}

	/**
	 * Emits an instruction with two operands.
	 *
	 * @param label Optional label.
	 * @param instruction Mnemonic for the intruction.
	 * @param operand1 First operand (usually register).
	 * @param operand2 Second (register or memory operand).
	 * @throws IOException If thrown by writer.
	 */
	public void emit(String label, String instruction, String operand1, String operand2)
			throws IOException
	{
		writer.append(String.format("%-7s %-7s %s, %s\n", label, instruction, operand1, operand2));
	}
}
