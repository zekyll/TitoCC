package titocc.compiler.elements;

import titocc.compiler.DeclarationType;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Cast expression that specifies an explicit conversion. Consists of a type name (given by
 * declaration specifiers and abstract declarator) and an expression.
 *
 * <br> EBNF Definition:
 *
 * <br> CAST_EXPRESSION = "(" TYPE_NAME ")" CAST_EXPRESSION | PREFIX_EXPRESSION
 *
 * <br> TYPE_NAME = DECLARATION_SPECIFIERS ABSTRACT_DECLARATOR
 */
public class CastExpression extends Expression
{
	/**
	 * Specifies part of the target type.
	 */
	private final DeclarationSpecifiers declarationSpecifiers;

	/**
	 * Modifies the target type.
	 */
	private final Declarator declarator;

	/**
	 * Expression to which the conversion is applied.
	 */
	private final Expression operand;

	/**
	 * Constructs a new CastExpression.
	 *
	 * @param declarationSpecifiers declaration specifier for target type
	 * @param declarator declarator for target type
	 * @param operand operand expression
	 * @param position starting position of the cast expression
	 */
	public CastExpression(DeclarationSpecifiers declarationSpecifiers, Declarator declarator,
			Expression operand, Position position)
	{
		super(position);
		this.declarationSpecifiers = declarationSpecifiers;
		this.declarator = declarator;
		this.operand = operand;
	}

	@Override
	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		CType targetType = getType(scope);
		CType operandType = operand.getType(scope).decay();

		// ($6.5.4/2)
		if ((!operandType.isScalar() || !targetType.isScalar()) && !targetType.equals(CType.VOID)) {
			throw new SyntaxException("Illegal cast. Both types must be scalar or target type must"
					+ " be void.", getPosition());
		}

		//TODO forbid cast between float and pointer

		return operand.compileWithConversion(ic, scope, targetType);
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		DeclarationType declType = declarationSpecifiers.compile(scope);
		declType.type = declarator.compile(declType.type, scope, null);
		return declType.type;
	}

	@Override
	public String toString()
	{
		return "(CAST " + declarationSpecifiers + " " + declarator + " " + operand + ")";
	}

	/**
	 * Attempts to parse a syntactic cast expression from token stream. If parsing fails the
	 * stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static Expression parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();

		Expression expr = PrefixExpression.parse(tokens);

		if (expr == null) {
			if (tokens.read().toString().equals("(")) {
				DeclarationSpecifiers declSpecifiers = DeclarationSpecifiers.parse(tokens);
				if (declSpecifiers != null) {
					Declarator declarator = Declarator.parse(tokens, false, true);
					if (declarator != null && tokens.read().toString().equals(")")) {
						Expression operand = CastExpression.parse(tokens);
						if (operand != null)
							expr = new CastExpression(declSpecifiers, declarator, operand, pos);
					}
				}
			}
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
