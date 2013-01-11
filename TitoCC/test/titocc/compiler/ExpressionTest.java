package titocc.compiler;

import java.io.IOException;
import java.io.StringReader;
import static org.junit.Assert.*;
import org.junit.Test;
import titocc.compiler.elements.Expression;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.tokenizer.Tokenizer;

public class ExpressionTest
{
	private String parse(String code) throws IOException, SyntaxException
	{
		Tokenizer tokenizer = new Tokenizer(new StringReader(code));
		Expression expr = Expression.parse(new TokenStream(tokenizer.tokenize()));
		return expr.toString();
	}

	// Tests the precedence of operators.
	@Test
	public void assignmentExpressionParsedAfterBinaryExpression1()
			throws IOException, SyntaxException
	{
		assertEquals("(ASGN_EXPR = (BIN_EXPR || (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("a || b = c"));
	}

	@Test
	public void binaryExpression1ParsedAfterBinaryExpression2()
			throws IOException, SyntaxException
	{
		assertEquals("(BIN_EXPR || (BIN_EXPR && (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("a && b || c"));
	}

	@Test
	public void binaryExpression2ParsedAfterBinaryExpression3()
			throws IOException, SyntaxException
	{
		assertEquals("(BIN_EXPR && (BIN_EXPR | (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("a | b && c"));
	}

	@Test
	public void binaryExpression3ParsedAfterBinaryExpression4()
			throws IOException, SyntaxException
	{
		assertEquals("(BIN_EXPR | (BIN_EXPR & (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("a & b | c"));
	}

	@Test
	public void binaryExpression4ParsedAfterBinaryExpression5()
			throws IOException, SyntaxException
	{
		assertEquals("(BIN_EXPR & (BIN_EXPR == (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("a == b & c"));
	}

	@Test
	public void binaryExpression5ParsedAfterBinaryExpression6()
			throws IOException, SyntaxException
	{
		assertEquals("(BIN_EXPR == (BIN_EXPR != (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("a != b == c"));
	}

	@Test
	public void binaryExpression6ParsedAfterBinaryExpression7()
			throws IOException, SyntaxException
	{
		assertEquals("(BIN_EXPR != (BIN_EXPR < (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("a < b != c"));
	}

	@Test
	public void binaryExpression7ParsedAfterBinaryExpression8()
			throws IOException, SyntaxException
	{
		assertEquals("(BIN_EXPR < (BIN_EXPR << (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("a << b < c"));
	}

	@Test
	public void binaryExpression8ParsedAfterBinaryExpression9()
			throws IOException, SyntaxException
	{
		assertEquals("(BIN_EXPR << (BIN_EXPR + (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("a + b << c"));
	}

	@Test
	public void binaryExpression9ParsedAfterBinaryExpression10()
			throws IOException, SyntaxException
	{
		assertEquals("(BIN_EXPR + (BIN_EXPR * (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("a * b + c"));
	}

	@Test
	public void binaryExpression10ParsedAfterBinaryExpression11()
			throws IOException, SyntaxException
	{
		assertEquals("(BIN_EXPR + (BIN_EXPR * (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("a * b + c"));
	}

	@Test
	public void binaryExpression11ParsedAfterPrefixExpression()
			throws IOException, SyntaxException
	{
		assertEquals("(BIN_EXPR * (PRE_EXPR ++ (ID_EXPR b)) (ID_EXPR a))",
				parse("++b * a"));
	}

	@Test
	public void prefixExpressionParsedAfterPostfixExpression()
			throws IOException, SyntaxException
	{
		assertEquals("(PRE_EXPR ++ (POST_EXPR ++ (ID_EXPR a)))",
				parse("++a++"));
	}

	@Test
	public void postfixExpressionParsedAfterOtherExpressions()
			throws IOException, SyntaxException
	{
		assertEquals("(POST_EXPR ++ (BIN_EXPR + (ID_EXPR a) (ID_EXPR b)))",
				parse("(a + b)++"));
		assertEquals("(BIN_EXPR * (BIN_EXPR + (ID_EXPR a) (ID_EXPR b)) (ID_EXPR c))",
				parse("(a + b) * c"));
	}
}
