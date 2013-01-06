package titocc.compiler;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import titocc.tokenizer.SyntaxException;

public class CompilerTest
{
	public CompilerTest()
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

	private String compile(String s) throws IOException, SyntaxException
	{
		Compiler c = new Compiler(new StringReader(s));
		StringWriter writer = new StringWriter();
		c.compile(writer);
		return writer.toString();
	}

	private void testErr(String src, String msg, int line, int column) throws IOException
	{
		try {
			compile(src);
		} catch (SyntaxException e) {
			assertEquals(msg, e.getMessage());
			assertEquals(line, e.getLine());
			assertEquals(column, e.getColumn());
		}
	}

	@Test
	public void throwsIfMainNotFound() throws IOException
	{
		testErr("int main2() {}", "Function \"int main()\" was not found.", 0, 0);
	}

	@Test
	public void throwsWhenMainReturnTypeWrong() throws IOException
	{
		testErr("void main2() {}", "Function \"int main()\" was not found.", 0, 0);
	}

	@Test
	public void errorWhenMainNotFunction() throws IOException
	{
		testErr("\nint main;", "Function \"int main()\" was not found.", 0, 0);
	}

	@Test
	public void errorWhenMainParamCountWrong() throws IOException
	{
		testErr("\nint main(int a) {}", "Function \"int main()\" was not found.", 0, 0);
	}

	@Test
	public void errorWhenAssigningToLiteral() throws IOException
	{
		testErr("\nint main() { 0 = 1; }", "Left side cannot be assigned to.", 1, 13);
	}

	@Test
	public void errorWhenAssigningToExpression() throws IOException
	{
		testErr("\nint main() { (1 + 2) = 1; }", "Left side cannot be assigned to.", 1, 13);
	}

	@Test
	public void errorWhenAssigningToFunction() throws IOException
	{
		testErr("\nint main() { main = 1; }", "Identifier \"main\" is not a variable.", 1, 13);
	}

	@Test
	public void errorWhenUsingUndeclaredVariable() throws IOException
	{
		testErr("\nint main() { x; }", "Undeclared identifier \"x\".", 1, 13);
		testErr("\nint main() { { int x; } x; }", "Undeclared identifier \"x\".", 1, 24);
	}

	@Test
	public void errorWhenUsingUndeclaredFunction() throws IOException
	{
		testErr("\nint main() { f(); }", "Undeclared identifier \"f\".", 1, 13);
	}

	@Test
	public void errorWhenRedefiningAFunction() throws IOException
	{
		testErr("\nint main() { }\nvoid main() {}", "Redefinition of \"main\".", 2, 0);
	}

	@Test
	public void errorWhenVoidUsedInExpression() throws IOException
	{
		testErr("\nvoid f() { 2 + f(); }", "Void return value used in an expression.", 1, 15);
		testErr("\nvoid f() { int x = f(); }", "Void return value used in an expression.", 1, 19);
		testErr("\nvoid f(int a) { f(out()); }", "Void return value used in an expression.", 1, 18);
	}

	@Test
	public void errorWhenCallingNonFunction() throws IOException
	{
		testErr("\nvoid f() { 0(); }", "Expression is not a function.", 1, 11);
		testErr("\nvoid f() { int x; x(); }", "Identifier \"x\" is not a function.", 1, 18);
	}

	@Test
	public void errorWhenWrongNumberOfArguments() throws IOException
	{
		testErr("\nvoid f() { f(1); }", "Number of arguments doesn't match the number of parameters.", 1, 11);
		testErr("\nvoid f(int a) { f(); }", "Number of arguments doesn't match the number of parameters.", 1, 16);
	}

	@Test
	public void errorWhenWrongNumberOfArgumentsForIntrinsics() throws IOException
	{
		testErr("\nvoid f() { in(1); }", "Number of arguments doesn't match the number of parameters.", 1, 11);
		testErr("\nvoid f() { out(); }", "Number of arguments doesn't match the number of parameters.", 1, 11);
	}

	@Test
	public void errorWhenUsingSuffixOnLiteral() throws IOException
	{
		testErr("\nvoid f() { 1u; }", "Suffixes on literals are not supported.", 1, 11);
	}

	@Test
	public void errorWhenUsingVoidParameter() throws IOException
	{
		testErr("\nvoid f(void a) { }", "Parameter type cannot be void.", 1, 7);
	}

	@Test
	public void errorWhenRedefiningAParameter() throws IOException
	{
		// should this be col 18 instead?
		testErr("\nvoid f(int a, int a) { }", "Redefinition of \"a\".", 1, 14);
	}
	
	@Test
	public void errorWhenIncrementingNonLValue() throws IOException
	{
		testErr("\nvoid f() { 0++; }", "Operator requires an lvalue.", 1, 11);
		testErr("\nvoid f() { ++0; }", "Operator requires an lvalue.", 1, 11);
	}	

	@Test
	public void errorWhenDecrementingNonLValue() throws IOException
	{
		testErr("\nvoid f() { 0--; }", "Operator requires an lvalue.", 1, 11);
		testErr("\nvoid f() { --0; }", "Operator requires an lvalue.", 1, 11);
	}	

	@Test
	public void errorWhenRedefiningAVariable() throws IOException
	{
		testErr("\nvoid f() { int a = 0; int a; }", "Redefinition of \"a\".", 1, 22);
		testErr("\nvoid f(int a) { int a; }", "Redefinition of \"a\".", 1, 16);
	}		
	
	@Test
	public void errorWhenGlobalVariableInitializerNotConstant() throws IOException
	{
		testErr("\nint a; int b = a;", "Global variable must be initialized with a compile time constant.", 1, 7);
	}		
}

