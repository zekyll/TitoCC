package titocc.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implements an intermediate phase of compilation before the instructions are sent to the final
 * assembler. First performs some peephole optimizations using virtual registers (e.g.
 * "load V1, =2; add V2, 0(V1)" becomes "add V2, =2"), and then allocates physical registers to
 * virtual registers using simple linear scan allocator and inserts spill code. Finally the
 * instructions are sent to the actual assembler.
 */
public class ExpressionAssembler
{
	/**
	 * Label to add to the next instruction.
	 */
	private String label = null;

	/**
	 * A list of instructions.
	 */
	private ArrayList<Instruction> instructions = new ArrayList<Instruction>();

	/**
	 * Emits an instruction with no operands.
	 *
	 * @param mnemonic mnemonic
	 */
	public void emit(String mnemonic)
	{
		emit(mnemonic, null, null, null);
	}

	/**
	 * Emits a pseudo instruction with one operand.
	 *
	 * @param mnemonic mnemonic
	 * @param pseudoOperand value ("equ", "dc") or size ("ds")
	 */
	public void emit(String mnemonic, int pseudoOperand)
	{
		instructions.add(new Instruction(label, mnemonic, pseudoOperand));
		label = null;
	}

	/**
	 * Emits an instruction with only register RHS operand.
	 *
	 * @param mnemonic mnemonic
	 * @param leftReg required LHS register operand
	 * @param rightReg required RHS register operand
	 */
	public void emit(String mnemonic, VirtualRegister leftReg, VirtualRegister rightReg)
	{
		emit(mnemonic, leftReg, null, rightReg);
	}

	/**
	 * Emits an instruction with only immediate RHS operand.
	 *
	 * @param mnemonic mnemonic
	 * @param leftReg required LHS register operand
	 * @param immediateOperand required RHS immediate value, including address mode ("=", "", "
	 * @")
	 */
	public void emit(String mnemonic, VirtualRegister leftReg, String immediateOperand)
	{
		emit(mnemonic, leftReg, immediateOperand, null);
	}

	/**
	 * Emits an instruction with both immediate value and register in RHS.
	 *
	 * @param mnemonic mnemonic
	 * @param leftReg required LHS register operand
	 * @param immediateOperand RHS immediate value, including address mode ("=", "", "
	 * @")
	 * @param rightReg RHS register operand
	 */
	public void emit(String mnemonic, VirtualRegister leftReg, String immediateOperand,
			VirtualRegister rightReg)
	{
		instructions.add(new Instruction(label, mnemonic, leftReg, immediateOperand, rightReg));
		label = null;
	}

	/**
	 * Adds a label to next instruction.
	 *
	 * @param label
	 */
	public void addLabel(String label)
	{
		if (this.label != null)
			emit("nop");
		this.label = label;
	}

	/**
	 * Applies optimizations.
	 */
	public void optimize()
	{
		//removeNops();
	}

	/**
	 * Maps virtual registers to physical registers (R1-R4). First uses linear scan algorithm to
	 * decide which virtual registers to spill, and then inserts load and store instructions for the
	 * spilled registers.
	 *
	 * @param stack allocator for local stack data; used for reserving register spill locations
	 */
	public void allocateRegisters(StackAllocator stack)
	{
		int spillCount = decideSpillRegisters();
		stack.reserveSpillLocations(spillCount);
		insertLoadsAndStores();
	}

	/**
	 * Sends the instructions to the final assembler.
	 *
	 * @param asm assembler
	 * @throws IOException if assembler throws
	 */
	public void sendToAssembler(Assembler asm) throws IOException
	{
		for (Instruction instr : instructions) {
			if (instr.label != null)
				asm.addLabel(instr.label);
			if (instr.leftReg == VirtualRegister.NONE) {
				asm.emit(instr.mnemonic, instr.getRhsString());
			} else if (instr.leftReg != null) {
				asm.emit(instr.mnemonic, instr.leftReg.realRegister, instr.getRhsString());
			} else
				asm.emit(instr.mnemonic, Integer.toString(instr.pseudoOperand));
		}
		if (label != null)
			asm.addLabel(label);
	}

	private int decideSpillRegisters()
	{
		RegisterSet regs = new RegisterSet();
		List<RangeEvent> events = calculateLiveRanges();

		Set<VirtualRegister> activeRanges = new TreeSet<VirtualRegister>(
				new Comparator<VirtualRegister>()
				{
					@Override
					public int compare(VirtualRegister r1, VirtualRegister r2)
					{
						return r2.liveRangeEnd - r1.liveRangeEnd;
					}
				});

		int spillCount = 0;

		for (RangeEvent e : events) {
			if (e.start) {
				Register reg = regs.allocate();
				activeRanges.add(e.reg);
				if (reg != null)
					e.reg.realRegister = reg;
				else {
					Iterator<VirtualRegister> it = activeRanges.iterator();
					VirtualRegister spilledReg = it.next();
					it.remove();
					e.reg.realRegister = spilledReg.realRegister;
					spilledReg.realRegister = null;
					spilledReg.spillIdx = spillCount++;
				}
			} else {
				if (e.reg.realRegister != null)
					regs.deallocate(e.reg.realRegister);
				activeRanges.remove(e.reg);
			}
		}

		return spillCount;
	}

	/**
	 * Insert loads/store instructions before/after each instruction that uses spilled registers.
	 */
	private void insertLoadsAndStores()
	{
		ArrayList<Instruction> newInstructions = new ArrayList<Instruction>();

		for (Instruction instr : instructions) {
			// If LHS is spilled, load from stack. Move label if necessary.
			int lhsSpillIdx = 0;
			if (instr.leftReg != null && instr.leftReg.realRegister == null) {
				lhsSpillIdx = instr.leftReg.spillIdx;
				if (!instr.discardsLhs())
					newInstructions.add(new Instruction(instr.label, "load", VirtualRegister.R0,
							Integer.toString(lhsSpillIdx), VirtualRegister.FP));
				instr.leftReg = VirtualRegister.R0;
				instr.label = null;
			}

			// If RHS is spilled, load from stack.
			if (instr.rightReg != null && instr.rightReg.realRegister == null) {
				newInstructions.add(new Instruction(instr.label, "load", VirtualRegister.R5,
						Integer.toString(instr.rightReg.spillIdx), VirtualRegister.FP));
				instr.rightReg = VirtualRegister.R5;
				instr.label = null;
			}

			// Copy original instruction.
			newInstructions.add(instr);

			// Write back modified value if necessary.
			if (instr.modifiesLhs() && instr.leftReg == VirtualRegister.R0) {
				newInstructions.add(new Instruction(null, "store", VirtualRegister.R0,
						Integer.toString(lhsSpillIdx), VirtualRegister.FP));
			}
		}

		instructions = newInstructions;
	}

	private void PostOptimize()
	{
		//removeNops();
	}

	private boolean moveLabelToNext(int idx)
	{
		if (instructions.get(idx).label == null)
			return true;

		if (idx + 1 < instructions.size()) {
			if (instructions.get(idx + 1).label != null)
				return false;
			instructions.get(idx + 1).label = instructions.get(idx).label;
			instructions.get(idx).label = null;
		} else {
			if (label != null)
				return false;
			label = instructions.get(idx).label;
		}
		return true;
	}

	private void removeNops()
	{
		int currentEnd = 0;
		for (int i = 0; i < instructions.size(); ++i) {
			Instruction instr = instructions.get(i);
			if (instr.mnemonic.equals("nop") && moveLabelToNext(i))
				continue;
			instructions.set(currentEnd, instr);
			++currentEnd;
		}

		instructions.subList(currentEnd, instructions.size()).clear();
	}

	private List<RangeEvent> calculateLiveRanges()
	{
		List<RangeEvent> events = new ArrayList<RangeEvent>();

		for (int i = 0; i < instructions.size(); ++i) {
			updateLiveRange(instructions.get(i).leftReg, i, true, events);
			updateLiveRange(instructions.get(i).rightReg, i, true, events);
		}

		for (int i = instructions.size(); i-- > 0;) {
			updateLiveRange(instructions.get(i).leftReg, i, false, events);
			updateLiveRange(instructions.get(i).rightReg, i, false, events);
		}

		Collections.sort(events);

		return events;
	}

	private void updateLiveRange(VirtualRegister reg, int idx, boolean start,
			List<RangeEvent> events)
	{
		if (reg == null || reg.realRegister != null)
			return;

		if (start) {
			if (reg.liveRangeStart < 0) {
				reg.liveRangeStart = idx;
				events.add(new RangeEvent(reg, true));
			}
		} else {
			if (reg.liveRangeEnd < 0) {
				reg.liveRangeEnd = idx + 1;
				events.add(new RangeEvent(reg, false));
			}
		}
	}
}
