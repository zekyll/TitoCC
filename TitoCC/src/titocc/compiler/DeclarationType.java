package titocc.compiler;

import titocc.compiler.types.CType;

/**
 * Holds information about the type of a declared object or function. Includes the type (including
 * qualifiers), storage class and possible inline function specifier.
 */
public final class DeclarationType
{
	/**
	 * Constructs a DeclarationType structure.
	 *
	 * @param type type
	 * @param storageClass storage class
	 * @param inline whether a function is inline or not; the value is ignored for object types
	 */
	public DeclarationType(CType type, StorageClass storageClass, boolean inline)
	{
		this.type = type;
		this.storageClass = storageClass;
		this.inline = inline;
	}

	/**
	 * Type of the object/function. This includes const/volatile qualifiers.
	 */
	public CType type;

	/**
	 * Storage class.
	 */
	public final StorageClass storageClass;

	/**
	 * Whether the function was declared inline or not. Only applicable to functions.
	 */
	public final boolean inline;
}
