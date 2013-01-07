package titocc.compiler.elements;

import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.compiler.types.PointerType;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

/**
 * Identifier that is modified with arbitrary pointer/array declarators.
 *
 * <p> EBNF definition:
 *
 * <br> DECLARATOR = "*" DECLARATOR | DIRECT_DECLARATOR
 *
 * <br> DIRECT-DECLARATOR = IDENTIFIER | "(" DECLARATOR ")" | DIRECT_DECLARATOR
 * "[" EXPRESSION "]"
 */
public abstract class Declarator extends CodeElement
{
	private static class IdentifierDeclarator extends Declarator
	{
		private String name;

		public IdentifierDeclarator(String name, int line, int column)
		{
			super(line, column);
			this.name = name;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public CType getModifiedType(CType type)
		{
			return type;
		}

		@Override
		public String toString()
		{
			return "(DCLTOR " + name + ")";
		}
	}

	private static class ArrayDeclarator extends Declarator
	{
		private Declarator subDeclarator;
		private Expression arrayLength;

		public ArrayDeclarator(Declarator subDeclarator, Expression arrayLength, int line, int column)
		{
			super(line, column);
			this.subDeclarator = subDeclarator;
			this.arrayLength = arrayLength;
		}

		@Override
		public String getName()
		{
			return subDeclarator.getName();
		}

		@Override
		public CType getModifiedType(CType type) throws SyntaxException
		{
			Integer len = arrayLength.getCompileTimeValue();
			if (!type.isObject())
				throw new SyntaxException("Array elements must have object type.", getLine(), getColumn());
			if (len == null)
				throw new SyntaxException("Array length must be a compile time constant.", getLine(), getColumn());
			else if(len <= 0)
				throw new SyntaxException("Array length must be a positive integer.", getLine(), getColumn());
			return subDeclarator.getModifiedType(new ArrayType(type, len));
		}

		@Override
		public String toString()
		{
			return "(DCLTOR " + subDeclarator + " " + arrayLength + ")";
		}
	}

	private static class PointerDeclarator extends Declarator
	{
		private Declarator subDeclarator;

		public PointerDeclarator(Declarator subDeclarator, int line, int column)
		{
			super(line, column);
			this.subDeclarator = subDeclarator;
		}

		@Override
		public String getName()
		{
			return subDeclarator.getName();
		}

		@Override
		public CType getModifiedType(CType type) throws SyntaxException
		{
			return subDeclarator.getModifiedType(new PointerType(type));
		}

		@Override
		public String toString()
		{
			return "(DCLTOR " + subDeclarator + ")";
		}
	}

	public Declarator(int line, int column)
	{
		super(line, column);
	}

	public abstract String getName();

	public abstract CType getModifiedType(CType type) throws SyntaxException;

	public static Declarator parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Declarator declarator = null;

		if (tokens.read().toString().equals("*")) {
			declarator = Declarator.parse(tokens);
			if (declarator != null)
				declarator = new PointerDeclarator(declarator, line, column);
		}

		tokens.popMark(declarator == null);

		if (declarator == null)
			declarator = parseDirectDeclarator(tokens);

		return declarator;
	}

	private static Declarator parseDirectDeclarator(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();

		Declarator declarator = parseIdentifierDeclarator(tokens);

		if (declarator == null)
			declarator = parseParenthesizedDeclarator(tokens);


		if (declarator != null)
			while (true) {
				Expression arrayLength = parseArrayLength(tokens);
				if (arrayLength != null)
					declarator = new ArrayDeclarator(declarator, arrayLength, line, column);
				else
					break;
			}

		return declarator;
	}

	private static Declarator parseIdentifierDeclarator(TokenStream tokens)
	{
		Declarator declarator = null;
		tokens.pushMark();

		Token id = tokens.read();
		if (id instanceof IdentifierToken)
			declarator = new IdentifierDeclarator(id.toString(), id.getLine(), id.getColumn());

		tokens.popMark(declarator == null);
		return declarator;
	}

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
