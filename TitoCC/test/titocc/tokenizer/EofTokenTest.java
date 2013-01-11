package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import static org.junit.Assert.*;
import org.junit.Test;

public class EofTokenTest
{
	@Test
	public void match() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader(""));
		EofToken token = EofToken.parse(cr);
		assertNotNull(token);
		assertEquals("<End of file>", token.toString());
	}

	@Test
	public void nomatch() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("a"));
		assertNull(EofToken.parse(cr));
		assertEquals('a', cr.read());
	}

}
