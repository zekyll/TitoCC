package titocc.compiler;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import titocc.compiler.elements.TranslationUnit;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Tokenizer;

public class ParserTest
{
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

	@Test
	public void megaTest() throws IOException, SyntaxException
	{
		String code = "int foo(int a, int b) {"
				+ ";"
				+ "int a;"
				+ "int b = 3;"
				+ "{"
				+ "if(1u) a;"
				+ "if(0) return b; else {}"
				+ "}"
				+ "while(--i) f(i);"
				+ "}"
				+ "int x;";
		Tokenizer t = new Tokenizer(new StringReader(code));
		Parser p = new Parser(t.tokenize());
		TranslationUnit tunit = p.parse();
		assertEquals(""
				+ "(TRUNIT "
				+ "(FUNC (TYPE int) foo "
				+ "(PRM_LIST (PRM (TYPE int) a) (PRM (TYPE int) b)) "
				+ "(BLK_ST "
				+ "(BLK_ST) "
				+ "(DECL_ST (VAR_DECL (TYPE int) a null)) "
				+ "(DECL_ST (VAR_DECL (TYPE int) b (INT_EXPR 3 ))) "
				+ "(BLK_ST "
				+ "(IF (INT_EXPR 1 u) (EXPR_ST (ID_EXPR a)) null) "
				+ "(IF (INT_EXPR 0 ) (RET (ID_EXPR b)) (BLK_ST))"
				+ ") "
				+ "(WHILE (PRE_EXPR -- (ID_EXPR i)) (EXPR_ST (FCALL_EXPR (ID_EXPR f) (ARG_LIST (ID_EXPR i)))))"
				+ ")) "
				+ "(VAR_DECL (TYPE int) x null)"
				+ ")",
				tunit.toString());
	}
}
