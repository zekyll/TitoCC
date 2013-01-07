package titocc.compiler;

import java.io.IOException;
import java.io.StringReader;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import titocc.compiler.elements.TranslationUnit;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Tokenizer;

public class ParserTest
{
	private Tokenizer tokenizer;

	public ParserTest()
	{
	}

	@BeforeClass
	public static void setUpClass()
	{
	}

	@AfterClass
	public static void tearDownClass()
	{
	}

	@Before
	public void setUp()
	{
	}

	@After
	public void tearDown()
	{
	}

	private String parse(String code) throws IOException, SyntaxException
	{
		tokenizer = new Tokenizer(new StringReader(code));
		TranslationUnit translationUnit = Parser.parse(tokenizer.tokenize());
		return translationUnit.toString();
	}

	@Test
	public void matchVariableDeclaration() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) abc null))",
				parse("int abc;"));
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) _cba123 null))",
				parse("int _cba123;"));
	}

	@Test
	public void matchVariableDeclarationWithInitializer() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) xyz (INT_EXPR 0 U)))",
				parse("int xyz = 0U;"));
	}

	@Test
	public void matchIdentifierExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) x (ID_EXPR a)))",
				parse("int x = a;"));
	}

	@Test
	public void matchBraceExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) x (ID_EXPR a)))",
				parse("int x = (a);"));
	}

	@Test
	public void matchFunctionCallExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) x (FCALL_EXPR (ID_EXPR bar) (ARG_LIST (ID_EXPR a) (INT_EXPR 7)))))",
				parse("int x = bar(a, 7);"));
	}

	@Test
	public void matchIntrinsicCallExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) x (INTR_EXPR out (ARG_LIST (INT_EXPR 2)))))",
				parse("int x = out(2);"));
	}

	@Test
	public void matchBinaryExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) x (BIN_EXPR + (BIN_EXPR - (ID_EXPR a) (INT_EXPR 7)) (INT_EXPR 4))))",
				parse("int x = a - 7 + 4;"));
	}

	@Test
	public void matchPrefixExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) x (PRE_EXPR ! (PRE_EXPR -- (ID_EXPR a)))))",
				parse("int x = !--a;"));
	}

	@Test
	public void matchPostfixExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) x (POST_EXPR ++ (ID_EXPR y))))",
				parse("int x = y++;"));
	}

	@Test
	public void matchFunction() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) foo (PRM_LIST) (BLK_ST)))",
				parse("void foo() {}"));
	}

	@Test
	public void matchFunctionWithParameters() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) foo (PRM_LIST (PRM (TYPE int) a) (PRM (TYPE int) b)) (BLK_ST)))",
				parse("void foo(int a, int b) {}"));
	}

	@Test
	public void matchEmptyStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (BLK_ST))))",
				parse("void f() { ; }"));
	}

	@Test
	public void matchBlockStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (BLK_ST (DECL_ST (VAR_DECL (TYPE int) x null))))))",
				parse("void f() { { int x; } }"));
	}

	@Test
	public void matchReturnStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (RET (INT_EXPR 2)))))",
				parse("void f() { return 2; }"));
	}

	@Test
	public void matchDeclarationStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (DECL_ST (VAR_DECL (TYPE int) x null)))))",
				parse("void f() { int x; }"));
	}

	@Test
	public void matchEmptyReturnStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (RET null))))",
				parse("void f() { return; }"));
	}

	@Test
	public void matchIfStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (IF (ID_EXPR a) (EXPR_ST (PRE_EXPR ++ (ID_EXPR a))) null))))",
				parse("void f() { if(a) ++a; }"));
	}

	@Test
	public void matchIfElseStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (IF (ID_EXPR a) (EXPR_ST (PRE_EXPR ++ (ID_EXPR a))) (BLK_ST)))))",
				parse("void f() { if(a) ++a; else ; }"));
	}

	@Test
	public void matchWhileStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (WHILE (BIN_EXPR == (ID_EXPR y) (INT_EXPR 2)) (BLK_ST)))))",
				parse("void f() { while(y == 2) {} }"));
	}

	@Test
	public void throwsOnIllegalSyntax() throws IOException, SyntaxException
	{
		try {
			parse("void f()");
			fail("SyntaxException not thrown.");
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testEverything() throws IOException, SyntaxException
	{
		String code = "int foo(int a, int b) {"
				+ ";"
				+ "int a;"
				+ "int b = (3);"
				+ "5 * 2 + b--;"
				+ "b /= 5;"
				+ "{"
				+ "if(1u) a;"
				+ "if(0) return b; else {}"
				+ "}"
				+ "while(--i) f(i);"
				+ "return 7;"
				+ "}"
				+ "int x;";
		Tokenizer t = new Tokenizer(new StringReader(code));
		TranslationUnit tunit = Parser.parse(t.tokenize());
		assertEquals(""
				+ "(TRUNIT "
				+ "(FUNC (TYPE int) foo "
				+ "(PRM_LIST (PRM (TYPE int) a) (PRM (TYPE int) b)) "
				+ "(BLK_ST "
				+ "(BLK_ST) "
				+ "(DECL_ST (VAR_DECL (TYPE int) a null)) "
				+ "(DECL_ST (VAR_DECL (TYPE int) b (INT_EXPR 3))) "
				+ "(EXPR_ST (BIN_EXPR + (BIN_EXPR * (INT_EXPR 5) (INT_EXPR 2)) (POST_EXPR -- (ID_EXPR b)))) "
				+ "(EXPR_ST (ASGN_EXPR /= (ID_EXPR b) (INT_EXPR 5))) "
				+ "(BLK_ST "
				+ "(IF (INT_EXPR 1 u) (EXPR_ST (ID_EXPR a)) null) "
				+ "(IF (INT_EXPR 0) (RET (ID_EXPR b)) (BLK_ST))"
				+ ") "
				+ "(WHILE (PRE_EXPR -- (ID_EXPR i)) (EXPR_ST (FCALL_EXPR (ID_EXPR f) (ARG_LIST (ID_EXPR i))))) "
				+ "(RET (INT_EXPR 7))"
				+ ")) "
				+ "(VAR_DECL (TYPE int) x null)"
				+ ")",
				tunit.toString());
	}
}
