package titocc.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implements an intermediate phase of compilation before the instructions are sent to the final
 * assembler. First performs some peephole optimizations using virtual registers (e.g.
 * "load V1, =2; add V2, 0(V1)" becomes "add V2, =2"), and then allocates physical registers to
 * virtual registers using simple linear scan allocator and inserts spill code. Finally the
 * instructions are sent to the actual assembler.
 */
public class IntermediateCompiler
{
	/**
	 * Auxiliary register for temporarily loading spilled register that is used as LHS operand.
	 */
	static final VirtualRegister AUX_REG1 = VirtualRegister.R0;

	/**
	 * Auxiliary register for temporarily loading spilled register that is used as RHS operand.
	 */
	static final VirtualRegister AUX_REG2 = VirtualRegister.R5;

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
		instructions.add(new Instruction(label, mnemonic));
		label = null;
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
		int realAddressingMode = Instruction.extractRealAddressingMode(mnemonic, immediateOperand);
		String immediateValue = Instruction.extractImmediateValue(immediateOperand);
		instructions.add(new Instruction(label, mnemonic, leftReg, realAddressingMode,
				immediateValue, rightReg));
		label = null;
	}

	/**
	 * Emits an instruction, taking the RHS information from an RhsOperand structure.
	 *
	 * @param mnemonic mnemonic
	 * @param leftReg required LHS register operand
	 * @param rhs RHS operand
	 */
	public void emit(String mnemonic, VirtualRegister leftReg, RhsOperand rhs)
	{
		instructions.add(new Instruction(label, mnemonic, leftReg, rhs.addrMode,
				rhs.immediateValue, rhs.register));
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
		applyRhsValuePropagation();
		//applyRhsValuePropagation();
		removeNops();
	}

	/**
	 * Maps virtual registers to physical registers (R1-R4). First uses linear scan algorithm to
	 * decide which virtual registers to spill, and then inserts load and store instructions for the
	 * spilled registers.
	 *
	 * @param stack allocator for local stack data; used for reserving register spill locations
	 */
	public void compile(StackAllocator stack)
	{
//		if (this.label != null)
//			emit("nop");

		optimize();
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
					newInstructions.add(new Instruction(instr.label, "load", AUX_REG1, 1,
							Integer.toString(lhsSpillIdx), VirtualRegister.FP));
				instr.leftReg = AUX_REG1;
				instr.label = null;
			}

			// If RHS is spilled, load from stack.
			int rhsSpillIdx = 0;
			if (instr.rightReg != null && instr.rightReg.realRegister == null) {
				rhsSpillIdx = instr.rightReg.spillIdx;
				if (!instr.discardsLhs())
					newInstructions.add(new Instruction(instr.label, "load", AUX_REG2, 1,
							Integer.toString(rhsSpillIdx), VirtualRegister.FP));
				instr.rightReg = AUX_REG2;
				instr.label = null;
			}

			// Copy original instruction.
			newInstructions.add(instr);

			// Write back modified value if necessary.
			VirtualRegister modifiedRegister = instr.getModifiedRegister();
			if (modifiedRegister != null && (instr.leftReg == AUX_REG1
					|| instr.leftReg == AUX_REG2)) {
				int spillIdx = modifiedRegister == AUX_REG1 ? lhsSpillIdx : rhsSpillIdx;
				newInstructions.add(new Instruction(null, "store", modifiedRegister, 0,
						Integer.toString(spillIdx), VirtualRegister.FP));
			}
		}

		instructions = newInstructions;
	}

	/**
	 * Moves instruction label to next instruction.
	 */
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

	/**
	 * Removes no-op instructions that are not needed for labels.
	 */
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

	/**
	 * Calculates live ranges of all virtual registers.
	 */
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

	/**
	 * Optimization that eliminates cases where a value is loaded in register when it could be used
	 * as RHS operand instead. Assumes that there are no jumps outside the analyzed block that have
	 * their target inside the block. (E.g. a single expression or function body.)
	 */
	private void applyRhsValuePropagation()
	{
		Map<String, JumpOriginRange> jumpOrigins = getJumpOriginRanges();

		Map<VirtualRegister, List<Integer>> allUsages = getRegisterUsages();

		for (Map.Entry<VirtualRegister, List<Integer>> e : allUsages.entrySet()) {
			if (!attemptRhsConstantPropagation(e.getKey(), e.getValue(), jumpOrigins))
				attemptRhsVariablePropagation(e.getKey(), e.getValue(), jumpOrigins);
		}
	}

	/**
	 * Propagates a loaded constant.
	 * e.g. "load V1, =x; add V2, V1; mul V3, V1" --> "add V2, =x; mul V3, =x"
	 */
	private boolean attemptRhsConstantPropagation(VirtualRegister reg, List<Integer> usages,
			Map<String, JumpOriginRange> jumpOrigins)
	{
		Instruction firstInstr = instructions.get(usages.get(0));

		if (usages.size() < 2)
			return false;

		// First usage of the register must load a constant value.
		if (!firstInstr.isConstantLoad(0))
			return false;

		// There must be no jumps from outside the live range.
		if (hasJumpsFromOutside(usages.get(0), usages.get(usages.size() - 1), jumpOrigins))
			return false;

		// Loaded register must not appear on left side of second instruction.
		List<Instruction> replacedInstructions = replaceRhsOperands(firstInstr, usages);
		if (replacedInstructions != null) {
			instructions.set(usages.get(0), firstInstr.makeNop());
			for (int i = 0; i < replacedInstructions.size(); ++i)
				instructions.set(usages.get(i + 1), replacedInstructions.get(i));
		}

		return true;
	}

	/**
	 * Propagates a loaded variable value.
	 * e.g. "load V1, x(fp); add V2, V1" --> "add V2, x(fp)"
	 */
	private boolean attemptRhsVariablePropagation(VirtualRegister reg, List<Integer> usages,
			Map<String, JumpOriginRange> jumpOrigins)
	{
		// Only allow if two consecutive instructions.
		if (usages.size() != 2 || usages.get(0) + 1 != usages.get(1))
			return false;

		Instruction first = instructions.get(usages.get(0));
		Instruction second = instructions.get(usages.get(1));

		// First instruction loads a memory location.
		if (!first.isConstantLoad(1))
			return false;

		// No jumps to between instructions.
		if (hasJumpsFromOutside(usages.get(0), usages.get(1), jumpOrigins))
			return false;

		Instruction newInstr = second.propagateRhsValue(first, 1);
		if (newInstr != null) {
			instructions.set(usages.get(0), first.makeNop());
			instructions.set(usages.get(1), newInstr);
		}

		return true;
	}

	private List<Instruction> replaceRhsOperands(Instruction first, List<Integer> usages)
	{
		// All usages of the virtual register after the first one must be such that the register
		// is used on RHS only and can be replaced with the constant.
		List<Instruction> replacedInstructions = new ArrayList<Instruction>();
		for (Integer idx : usages.subList(1, usages.size())) {
			Instruction newInstr = instructions.get(idx).propagateRhsValue(first, 0);
			if (newInstr == null)
				return null;
			replacedInstructions.add(newInstr);
		}
		return replacedInstructions;
	}

	/**
	 * Takes an instruction range as input and checks whether there is a jump outside that range
	 * that targets a label inside the range.
	 */
	private boolean hasJumpsFromOutside(int start, int end,
			Map<String, JumpOriginRange> jumpOrigins)
	{
		for (int i = end + 1; i <= end; ++i) {
			if (instructions.get(i).label != null) {
				JumpOriginRange range = jumpOrigins.get(instructions.get(i).label);
				if (range.min < start || range.max > end)
					return true;
			}
		}
		return false;
	}

	private static class JumpOriginRange
	{
		int min, max;
	}

	/**
	 * For each label finds out the first and last jump targeting that particular label.
	 */
	private Map<String, JumpOriginRange> getJumpOriginRanges()
	{
		Map<String, JumpOriginRange> allRanges = new HashMap<String, JumpOriginRange>();
		for (int i = 0; i < instructions.size(); ++i) {
			if (instructions.get(i).isJumpInstruction()) {
				JumpOriginRange thisRange = allRanges.get(i);
				if (thisRange == null) {
					thisRange = new JumpOriginRange();
					thisRange.min = thisRange.max = i;
					allRanges.put(instructions.get(i).label, thisRange);
				} else {
					thisRange.min = Math.min(thisRange.min, i);
					thisRange.max = Math.max(thisRange.min, i);
				}
			}
		}
		return allRanges;
	}

//	private Map<String, Integer> getLabelPositions()
//	{
//		Map<String, Integer> labelPositions = new HashMap<String, Integer>();
//		for (int i = 0; i < instructions.size(); ++i)
//			if (instructions.get(i).label != null)
//				labelPositions.put(label, i);
//		return labelPositions;
//	}
	/**
	 * For each virtual register finds out all the instructions using that register.
	 */
	private Map<VirtualRegister, List<Integer>> getRegisterUsages()
	{
		Map<VirtualRegister, List<Integer>> allUsages =
				new HashMap<VirtualRegister, List<Integer>>();
		for (int i = 0; i < instructions.size(); ++i) {
			addUsage(instructions.get(i).leftReg, i, allUsages);
			if (instructions.get(i).leftReg != instructions.get(i).rightReg)
				addUsage(instructions.get(i).rightReg, i, allUsages);
		}
		return allUsages;
	}

	private static void addUsage(VirtualRegister reg, int idx,
			Map<VirtualRegister, List<Integer>> allUsages)
	{
		if (reg == null)
			return;

		List<Integer> thisUsages = allUsages.get(reg);
		if (thisUsages == null) {
			thisUsages = new ArrayList<Integer>();
			allUsages.put(reg, thisUsages);
		}
		thisUsages.add(idx);
	}
}
