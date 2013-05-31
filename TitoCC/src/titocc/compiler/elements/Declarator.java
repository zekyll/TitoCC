package titocc.compiler.elements;

import java.io.IOException;
import java.util.List;
import titocc.compiler.Scope;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.compiler.types.FunctionType;
import titocc.compiler.types.PointerType;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Identifier that is modified with arbitrary pointer/array/function
 * declarators. Note that the declarators are parsed in the reverse order. I.e.
 * the innermost declarator becomes the outermost modifier to the type:
 * ((*a)[2])[3] is identifier "a" that is pointer to a 2-sized array of 3-sized
 * arrays.
 *
 * <p> EBNF definition:
 *
 * <br> DECLARATOR = "*" DECLARATOR | DIRECT_DECLARATOR
 *
 * <br> DIRECT_DECLARATOR = IDENTIFIER | "(" DECLARATOR ")" | DIRECT_DECLARATOR
 * "[" EXPRESSION "]" | DIRECT_DECLARATOR PARAMETER_LIST
 */
public abstract class Declarator extends CodeElement
{
	/**
	 * Declarator that is just a simple identifier.
	 */
	private static class IdentifierDeclarator extends Declarator
	{
		private final String name;

		public IdentifierDeclarator(String name, Position position)
		{
			super(position);
			this.name = name;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public CType getModifiedType(CType type, Scope scope)
		{
			return type;
		}

		@Override
		public String toString()
		{
			return "(DCLTOR " + name + ")";
		}
	}

	/**
	 * Declarator that declares an array.
	 */
	private static class ArrayDeclarator extends Declarator
	{
		private final Declarator subDeclarator;

		private final Expression arrayLength;

		public ArrayDeclarator(Declarator subDeclarator, Expression arrayLength,
				Position position)
		{
			super(position);
			this.subDeclarator = subDeclarator;
			this.arrayLength = arrayLength;
		}

		@Override
		public String getName()
		{
			return subDeclarator.getName();
		}

		@Override
		public CType getModifiedType(CType type, Scope scope)
				throws SyntaxException, IOException
		{
			Integer len = arrayLength.getCompileTimeValue();
			if (!type.isObject())
				throw new SyntaxException("Array elements must have object type.", getPosition());
			if (len == null)
				throw new SyntaxException("Array length must be a compile time constant.", getPosition());
			else if (len <= 0)
				throw new SyntaxException("Array length must be a positive integer.", getPosition());
			return subDeclarator.getModifiedType(new ArrayType(type, len), scope);
		}

		@Override
		public String toString()
		{
			return "(DCLTOR " + subDeclarator + " " + arrayLength + ")";
		}
	}

	/**
	 * Declarator that declares a pointer.
	 */
	private static class PointerDeclarator extends Declarator
	{
		private final Declarator subDeclarator;

		public PointerDeclarator(Declarator subDeclarator, Position position)
		{
			super(position);
			this.subDeclarator = subDeclarator;
		}

		@Override
		public String getName()
		{
			return subDeclarator.getName();
		}

		@Override
		public CType getModifiedType(CType type, Scope scope)
				throws SyntaxException, IOException
		{
			return subDeclarator.getModifiedType(new PointerType(type), scope);
		}

		@Override
		public String toString()
		{
			return "(DCLTOR " + subDeclarator + ")";
		}
	}

	/**
	 * Declarator that declares a function.
	 */
	private static class FunctionDeclarator extends Declarator
	{
		private final Declarator subDeclarator;

		private final ParameterList paramList;

		public FunctionDeclarator(Declarator subDeclarator,
				ParameterList paramList, Position position)
		{
			super(position);
			this.subDeclarator = subDeclarator;
			this.paramList = paramList;
		}

		@Override
		public String getName()
		{
			return subDeclarator.getName();
		}

		@Override
		public CType getModifiedType(CType type, Scope scope)
				throws SyntaxException, IOException
		{
			List<CType> paramTypes = paramList.compile(null, scope);
			return subDeclarator.getModifiedType(new FunctionType(type, paramTypes), scope);
		}

		@Override
		public String toString()
		{
			return "(DCLTOR " + subDeclarator + " " + paramList + ")";
		}
	}

	/**
	 * Constructs a Declarator.
	 *
	 * @param position starting position of the declarator
	 */
	protected Declarator(Position position)
	{
		super(position);
	}

	/**
	 * Returns the variable name for this declarator.
	 *
	 * @return the variable name
	 */
	public abstract String getName();

	/**
	 * Modifies the type with this declarator and all its sub declarators. For
	 * example given type "int", two nested 2-sized array declarators would
	 * return int[2][2].
	 *
	 * @param type type to be modified
	 * @param scope scope in which the declarator is compiled
	 * @return modified type
	 * @throws SyntaxException
	 */
	public abstract CType getModifiedType(CType type, Scope scope)
			throws SyntaxException, IOException;

	/**
	 * Attempts to parse a declarator from token stream. If parsing fails the
	 * stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Declarator object or null if tokens don't form a valid declarator
	 */
	public static Declarator parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		Declarator declarator = null;

		if (tokens.read().toString().equals("*")) {
			declarator = Declarator.parse(tokens);
			if (declarator != null)
				declarator = new PointerDeclarator(declarator, pos);
		}

		tokens.popMark(declarator == null);

		if (declarator == null)
			declarator = parseDirectDeclarator(tokens);

		return declarator;
	}

	/**
	 * Parses a direct declarator. It is either a variable name, declarator
	 * inside () parentheses, an array declarator or a function declarator.
	 * declarator inside () parentheses.
	 */
	private static Declarator parseDirectDeclarator(TokenStream tokens)
	{
		Position pos = tokens.getPosition();

		// Identifier declarator
		Declarator declarator = parseIdentifierDeclarator(tokens);

		// Parenthesized declarator
		if (declarator == null)
			declarator = parseParenthesizedDeclarator(tokens);

		// Parse array/function declarators in loop to avoid left recursion.
		if (declarator != null) {
			while (true) {
				// Array declarator
				Expression arrayLength = parseArrayLength(tokens);
				if (arrayLength != null) {
					declarator = new ArrayDeclarator(declarator, arrayLength, pos);
					continue;
				}

				// Function declarator
				ParameterList paramList = ParameterList.parse(tokens);
				if (paramList != null) {
					declarator = new FunctionDeclarator(declarator, paramList, pos);
					continue;
				}

				break;
			}
		}

		return declarator;
	}

	/**
	 * Parses an identifier declarator.
	 */
	private static Declarator parseIdentifierDeclarator(TokenStream tokens)
	{
		Declarator declarator = null;
		tokens.pushMark();

		Token id = tokens.read();
		if (id instanceof IdentifierToken)
			declarator = new IdentifierDeclarator(id.toString(), id.getPosition());

		tokens.popMark(declarator == null);
		return declarator;
	}

	/**
	 * Parses a parenthesized declarator.
	 */
	private static Declarator parseParenthesizedDeclarator(TokenStream tokens)
	{
		Declarator declarator = null;
		tokens.pushMark();

		if (tokens.read().toString().equals("(")) {
			declarator = Declarator.parse(tokens);
			if (declarator != null && !tokens.read().toString().equals(")"))
				declarator = null;
		}

		tokens.popMark(declarator == null);
		return declarator;
	}

	/**
	 * Parses the array length of an array declarator.
	 */
	private static Expression parseArrayLength(TokenStream tokens)
	{
		tokens.pushMark();
		Expression subscript = null;

		if (tokens.read().toString().equals("[")) {
			subscript = Expression.parse(tokens);
			if (subscript != null && !tokens.read().toString().equals("]"))
				subscript = null;
		}

		tokens.popMark(subscript == null);
		return subscript;
	}
}
