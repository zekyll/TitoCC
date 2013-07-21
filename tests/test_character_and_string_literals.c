int main()
{
	// Character literal
	out('a' == 97);
	
	// Character literal: escape sequences
	out('\n' == 10);
	out('\012' == 012);
	out('\x1F' == 0x1f);
	out('\u00c4' == 0xc4);

	// Character literal: multiple characters
	out('abc' == 99);
	out('a\xfF' == 0xff);

	// Character literal: works as a null pointer constant
	void* p = '\0';
	out(!p);

	// String literal
	char* s = "abc";
	out(s[0] == 'a');
	out(s[1] == 'b');
	out(s[2] == 'c');
	out(s[3] == '\0');
	
	// String literal: escape sequences
	out("a\b"[1] == 8);
	out("a\012"[1] == 012);
	out("a\x1F"[1] == 0x1f);
	out("a\u00c4"[1] == 0xc4);

	// String literal: concatenation
	s = "ab"  "ce";
	out(s[0] == 'a');
	out(s[1] == 'b');
	out(s[2] == 'c');
	out(s[3] == 'e');
	out(s[4] == '\0');

	// String literal: taking address
	char (*s2)[4] = &"xy""z";
	out((*s2)[0] == 'x');
}