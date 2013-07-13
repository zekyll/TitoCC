package titocc.compiler.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import titocc.compiler.DeclarationType;
import titocc.compiler.Scope;
import titocc.compiler.StorageClass;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Initial part of the a declaration that specifies the storage class and part of the type. It is a
 * non-empty list of type specifiers, type qualifiers, storage class specifiers and function
 * specifiers.
 *
 * <p> EBNF definition:
 *
 * <br> DECLARATION_SPECIFIERS = (STORAGE_CLASS_SPECIFIER | TYPE_SPECIFIER | TYPE_QUALIFIER
 * | FUNCTIon_SPECIFIER) [DECLARATION_SPECIFIERS]
 *
 * <br> STORAGE_CLASS_SPECIFIER = "typedef" | "extern" | "static" | "auto" | "register"
 *
 * <br> TYPE_SPECIFIER = "void" | "char" | "short" | "int" | "long" | "float" | "double" | "signed"
 * | "unsigned" | "_Bool" | "_Complex"
 *
 * <br> TYPE_QUALIFIER = "const" | "volatile" | "restrict"
 *
 * <br> FUNCTION_SPECIFIER = "inline"
 */
public class DeclarationSpecifiers extends CodeElement
{
	/**
	 * All storage classes.
	 */
	static final Map<String, StorageClass> allStorageClassSpecifiers =
			new HashMap<String, StorageClass>()
			{
				{
					put("typedef", StorageClass.Typedef);
					put("extern", StorageClass.Extern);
					put("static", StorageClass.Static);
					put("auto", StorageClass.Auto);
					put("register", StorageClass.Register);
				}
			};

	/**
	 * Set of all type specifiers.
	 */
	static final Set<String> allTypeSpecifiers = new HashSet<String>(
			Arrays.asList(new String[]{"void", "char", "short", "int", "long", "float",
				"double", "signed", "unsigned", "_Bool", "_Complex"}));

	/**
	 * Set of all type qualifiers.
	 */
	static final Set<String> allTypeQualifiers = new HashSet<String>(
			Arrays.asList(new String[]{"const", "volatile", "restrict"}));

	/**
	 * Set of all function specifiers.
	 */
	static final Set<String> allFunctionSpecifiers = new HashSet<String>(
			Arrays.asList(new String[]{"inline"}));

	static final Set<String> allSpecifiers = new HashSet<String>()
	{
		{
			addAll(allStorageClassSpecifiers.keySet());
			addAll(allTypeSpecifiers);
			addAll(allTypeQualifiers);
			addAll(allFunctionSpecifiers);
		}
	};

	/**
	 * Mapping of type specifier sets to types, as defined in ($6.7.2/2). Entries with
	 * null value
	 * are not supported yet.
	 */
	static final Map<List<String>, CType> types = new HashMap<List<String>, CType>()
	{
		void put(String[] typeSpecifiers, CType type)
		{
			List<String> key = Arrays.asList(typeSpecifiers);
			Collections.sort(key);
			put(key, type);
		}

		{
			put(new String[]{"void"}, CType.VOID);
			put(new String[]{"char"}, CType.CHAR);
			put(new String[]{"signed", "char"}, CType.SCHAR);
			put(new String[]{"unsigned", "char"}, CType.UCHAR);
			put(new String[]{"short"}, CType.SHORT);
			put(new String[]{"signed", "short"}, CType.SHORT);
			put(new String[]{"short", "int"}, CType.SHORT);
			put(new String[]{"signed", "short", "int"}, CType.SHORT);
			put(new String[]{"unsigned", "short"}, CType.USHORT);
			put(new String[]{"unsigned", "short", "int"}, CType.USHORT);
			put(new String[]{"int"}, CType.INT);
			put(new String[]{"signed"}, CType.INT);
			put(new String[]{"signed", "int"}, CType.INT);
			put(new String[]{"unsigned"}, CType.UINT);
			put(new String[]{"unsigned", "int"}, CType.UINT);
			put(new String[]{"long"}, CType.LONG);
			put(new String[]{"signed", "long"}, CType.LONG);
			put(new String[]{"long", "int"}, CType.LONG);
			put(new String[]{"signed", "long", "int"}, CType.LONG);
			put(new String[]{"unsigned", "long"}, CType.ULONG);
			put(new String[]{"unsigned", "long", "int"}, CType.ULONG);
			put(new String[]{"long", "long"}, null);
			put(new String[]{"signed", "long", "long"}, null);
			put(new String[]{"long", "long", "int"}, null);
			put(new String[]{"signed", "long", "long", "int"}, null);
			put(new String[]{"unsigned", "long", "long"}, null);
			put(new String[]{"unsigned", "long", "long", "int"}, null);
			put(new String[]{"float"}, null);
			put(new String[]{"double"}, null);
			put(new String[]{"long", "double"}, null);
			put(new String[]{"_Bool"}, null);
			put(new String[]{"float", "_Complex"}, null);
			put(new String[]{"double", "_Complex"}, null);
			put(new String[]{"long", "double", "_Complex"}, null);
		}
	};

	private List<String> specifiers;

	/**
	 * Constructs DeclarationSpecifiers.
	 *
	 * @param specifiers list of all specifiers
	 * @param position starting position of the declaration specifiers
	 */
	public DeclarationSpecifiers(List<String> specifiers, Position position)
	{
		super(position);
		this.specifiers = specifiers;
	}

	/**
	 * Deduces the type, storage class and possible inline keyword given by the declaration
	 * specifiers.
	 *
	 * @param scope scope containing the declaration specifiers
	 * @return DeclarationType object containing the type, storage class and inline flag
	 * @throws SyntaxException if there are unsupported specifiers or the combination of specifiers
	 * is illegal
	 */
	public DeclarationType compile(Scope scope) throws SyntaxException
	{
		for (String s : specifiers) {
			if (allTypeQualifiers.contains(s)) {
				throw new SyntaxException("Type qualifiers are not supported yet.",
						getPosition());
			}

			if (s.equals("inline")) {
				throw new SyntaxException("Inline functions are not supported yet.",
						getPosition());
			}
		}

		CType type = getType(specifiers);
		StorageClass storageClass = getStorageClass(specifiers);
		boolean inline = false;

		return new DeclarationType(type, storageClass, inline);
	}

	private StorageClass getStorageClass(List<String> specifiers) throws SyntaxException
	{
		StorageClass storageClass = null;
		for (String s : specifiers) {
			StorageClass storageClass2 = allStorageClassSpecifiers.get(s);
			if (storageClass2 != null) {
				if (storageClass != null) {
					throw new SyntaxException("Multiple storage classes in declaration specifiers.",
							getPosition());
				}
				storageClass = storageClass2;
			}
		}

		if (storageClass != null)
			throw new SyntaxException("Storage classes are not supported yet.", getPosition());

		return null;
	}

	private CType getType(List<String> specifiers) throws SyntaxException
	{
		List<String> typeSpecifiers = new ArrayList<String>();
		for (String s : specifiers) {
			if (allTypeSpecifiers.contains(s))
				typeSpecifiers.add(s);
		}
		Collections.sort(typeSpecifiers);

		if (!types.containsKey(typeSpecifiers))
			throw new SyntaxException("Invalid type in declaration specifiers.", getPosition());
		CType type = types.get(typeSpecifiers);
		if (type == null)
			throw new SyntaxException("Unsupported type in declaration specifiers.", getPosition());

		return type;
	}

	@Override
	public String toString()
	{
		String str = "(DS";
		for (String s : specifiers)
			str += " " + s;
		return str + ")";
	}

	/**
	 * Attempts to parse declaration specifiers from token stream. If parsing fails the
	 * stream is
	 * reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return ParameterList object or null if tokens don't form valid declaration
	 * specifiers
	 */
	public static DeclarationSpecifiers parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		DeclarationSpecifiers declSpecifiers = null;

		List<String> specifierNames = new LinkedList<String>();

		while (true) {
			tokens.pushMark();
			String token = tokens.read().toString();
			if (allSpecifiers.contains(token)) {
				specifierNames.add(token.toString());
				tokens.popMark(false);
			} else {
				tokens.popMark(true);
				break;
			}
		}

		if (!specifierNames.isEmpty())
			declSpecifiers = new DeclarationSpecifiers(specifierNames, pos);

		return declSpecifiers;
	}
}
