package titocc.compiler;

/**
 * Class for symbols that are only used internally by the compiler, like code
 * positions inside functions etc.
 */
public class InternalSymbol implements Symbol
{
	private String name, globallyUniqueName, suffix;

	/**
	 * Constructs a new internal symbol.
	 *
	 * @param name Name of the symbol.
	 * @param scope Scope this symbol belongs to.
	 */
	public InternalSymbol(String name, Scope scope, String suffix)
	{
		// In C names starting with two underscores are reserved for implementation.
		this.name = "__" + name;
		this.globallyUniqueName = scope.makeGloballyUniqueName(name);
		this.suffix = suffix;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getGlobalName()
	{
		return globallyUniqueName;
	}

	@Override
	public String getReference()
	{
		return globallyUniqueName + suffix;
	}
}
