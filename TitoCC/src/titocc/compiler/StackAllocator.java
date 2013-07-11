package titocc.compiler;

/**
 * Manages space in stack frame. (Currently just spill locations.)
 */
public class StackAllocator
{
	/**
	 * Total number of spilled registers.
	 */
	private int spillCount = 0;

	/**
	 * Returns the biggest number of spilled registers since the last call to resetMaxSpillCount.
	 *
	 * @return max spill count
	 */
	public int getSpillCount()
	{
		return spillCount;
	}

	/**
	 * Reserves stack space for (at least) requested number of spill locations.
	 *
	 * @param spillCount number of spill locations
	 */
	public void reserveSpillLocations(int spillCount)
	{
		this.spillCount = Math.max(this.spillCount, spillCount);
	}
}
