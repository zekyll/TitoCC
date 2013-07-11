package titocc.compiler;

/**
 * Helper class for the linear scan register allocator. Marks either the start or end of a live
 * range for a virtual register.
 */
class RangeEvent implements Comparable<RangeEvent>
{
	/**
	 * Virtual register.
	 */
	VirtualRegister reg;

	/**
	 * Whether this is start or end of live range.
	 */
	boolean start;

	/**
	 * Constructs a new range event for given register.
	 *
	 * @param reg virtual register
	 * @param start true if start of live range, false if end
	 */
	RangeEvent(VirtualRegister reg, boolean start)
	{
		this.reg = reg;
		this.start = start;
	}

	/**
	 * Get the instruction index of this event.
	 *
	 * @return index
	 */
	int getIdx()
	{
		return start ? reg.liveRangeStart : reg.liveRangeEnd;
	}

	@Override
	public int compareTo(RangeEvent e2)
	{
		// The ordering of events is such that if one live range ends in same instruction as 
		// another one starts, the range start is handled first. This makes sure two different
		// physical registers are used (although not always necessary).
		if (getIdx() != e2.getIdx())
			return getIdx() - e2.getIdx();
		else if (start != e2.start)
			return start ? 1 : -1;
		return 0;
	}
}