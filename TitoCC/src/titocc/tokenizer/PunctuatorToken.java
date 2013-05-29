package titocc.tokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import titocc.util.Position;

/**
 * Operator or other punctuator.
 */
public class PunctuatorToken extends Token
{
	/**
	 * List of all punctuators. An important fact is that all multi-character
	 * punctuators start with some other punctuator (e.g. ">>=" contains ">>"
	 * which contains ">"). This means that only one character needs to be
	 * looked ahead.
	 */
	private static final String[] punctuatorList = {
		"+",
		"++",
		"+=",
		"-",
		"--",
		"-=",
		"*",
		"*=",
		"/",
		"/=",
		"%",
		"%=",
		"|",
		"|=",
		"||",
		"&",
		"&=",
		"&&",
		"~",
		"~=",
		"^",
		"^=",
		"!",
		"=",
		">>",
		">>=",
		"<<",
		"<<=",
		"==",
		"!=",
		"<",
		"<=",
		">",
		">=",
		"{",
		"}",
		"(",
		")",
		"[",
		"]",
		";",
		","
	};

	private static Set<String> punctuators = new HashSet<String>(Arrays.asList(punctuatorList));

	/**
	 * Constructs a PunctuatorToken.
	 *
	 * @param string punctuator string
	 * @param position starting position of the token
	 */
	public PunctuatorToken(String string, Position position)
	{
		super(string, position);
	}

	/**
	 * Attempts to parse a punctuator from input. If the characters don't match
	 * any punctuators then resets the stream to its original position and
	 * returns null.
	 *
	 * @param reader code reader from which charactes are read
	 * @return PunctuatorToken object or null if no valid punctuator was found
	 * @throws IOException if code reader throws
	 */
	public static PunctuatorToken parse(CodeReader reader) throws IOException
	{
		PunctuatorToken token = null;
		Position pos = reader.getPosition();

		StringBuilder tokenString = new StringBuilder();

		while (punctuators.contains(tokenString.toString() + reader.peek()))
			tokenString.append(reader.read());

		if (tokenString.length() > 0)
			token = new PunctuatorToken(tokenString.toString(), pos);

		return token;
	}
}
