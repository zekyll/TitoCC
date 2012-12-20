package titocc.compiler;

/**
 * Class for symbols that are only used internally by the compiler, like code
 * positions inside functions etc.
 */
public class InternalSymbol implements Symbol
{
	private String label;
	private String name;

	/**
	 * Constructs a new internal symbol.
	 *
	 * @param name Name of the symbol.
	 * @param scope Scope this symbol belongs to.
	 */
	public InternalSymbol(String name, Scope scope)
	{
		// In C names starting with two underscores are reserved for implementation.
		this.name = "__" + name; 
		this.label = scope.makeGloballyUniqueName(name);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getReference()
	{
		return label;
	}
}
