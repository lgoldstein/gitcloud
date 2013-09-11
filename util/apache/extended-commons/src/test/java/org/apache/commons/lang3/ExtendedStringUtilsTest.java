/**
 * 
 */
package org.apache.commons.lang3;

import java.text.MessageFormat;

import javax.management.ObjectName;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author lgoldstein
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedStringUtilsTest extends AbstractTestSupport {
	public ExtendedStringUtilsTest() {
		super();
	}

    @Test
    public void testSafeCompareNulls () {
        assertEquals("Mismatched null/null", 0, ExtendedStringUtils.safeCompare(null, null));
        assertTrue("Mismatched null/sth.", ExtendedStringUtils.safeCompare(null, "sth.") < 0);
        assertTrue("Mismatched sth./null", ExtendedStringUtils.safeCompare("sth.", null) > 0);
        assertTrue("Mismatched null/empty", ExtendedStringUtils.safeCompare(null, "") < 0);
        assertTrue("Mismatched empty/null", ExtendedStringUtils.safeCompare("", null) > 0);
    }

    @Test
    public void testSafeToString () {
        final String    TEST_NAME="testSafeToString";
        assertNull("Non null result", ExtendedStringUtils.safeToString(null));
        assertSame("Mismatched string result", TEST_NAME, ExtendedStringUtils.safeToString(TEST_NAME));
        assertEquals("Mismatched builder result", TEST_NAME, ExtendedStringUtils.safeToString(new StringBuilder(TEST_NAME)));
    }

    @Test
    public void testSafeCompareNonNulls () {
        final String[]  VALUES={
                "abcd", "abcd",         // equality
                "LyOr", "lYoR",         // case sensitivity
                "1aGswqef", "aj21389"   // total difference
            };
        for (int    vIndex=0; vIndex < VALUES.length; vIndex += 2) {
            final String    s1=VALUES[vIndex], s2=VALUES[vIndex + 1];
            assertEquals("Mismatched results for s1=" + s1 + "/s2=" + s2, s1.compareTo(s2), ExtendedStringUtils.safeCompare(s1, s2));
        }
    }

    @Test
    public void testSafeCompareCaseInsensitive () {
        for (String value : new String[] { "abcd", "ABCD", "1234" }) {
            String  upper=value.toUpperCase(), lower=value.toLowerCase();
            assertEquals("Mismatched result for " + value, 0, ExtendedStringUtils.safeCompare(upper, lower, false));
        }
    }

    @Test
    public void testHashCode () {
        for (String value : new String[] { "abcd", "ABCD", "1234" }) {
            String  upper=value.toUpperCase(), lower=value.toLowerCase();
            int     upperHash=ExtendedStringUtils.hashCode(value, Boolean.TRUE);
            int     lowerHash=ExtendedStringUtils.hashCode(value, Boolean.FALSE);
            assertEquals("Mismatched UPPER hash for " + value, upper.hashCode(), upperHash);
            assertEquals("Mismatched LOWER hash for " + value, lower.hashCode(), lowerHash);
        }
    }

    @Test
    public void testSmartQuoteObjectName () {
        for (String value : new String[] { "unqoted", "\"quoted\"" }) {
            String  result=ExtendedStringUtils.smartQuoteObjectName(value);
            if (value.charAt(0) == '"')
                assertSame("Mismatched quoted value instance", value, result);
            else
                assertEquals("Mismatched quoted result", ObjectName.quote(value), result);
        }
    }

    @Test
    public void testSmartQuoteObjectNameOnEmptyStrings() {
        final String    QUOTED_EMPTY="\"\"";
        assertNull("Mismatched null string value", ExtendedStringUtils.smartQuoteObjectName(null));
        assertEquals("Mismatched empty string value", QUOTED_EMPTY, ExtendedStringUtils.smartQuoteObjectName(""));
        assertSame("Mismatched quoted empty string value", QUOTED_EMPTY, ExtendedStringUtils.smartQuoteObjectName(QUOTED_EMPTY));
    }

    @Test
    public void testSmartQuoteObjectNameOnImbalancedQuotesOrNull() {
        for (String value : new String[] { "\"", "\"abcd", "abcd\"" }) {
            try {
                String  result=ExtendedStringUtils.smartQuoteObjectName(value);
                fail("Unexpected result for " + value + ": " + result);
            } catch(IllegalArgumentException e) {
                // expected - ignored
            }
        }
    }

    @Test
    public void testSmartUnquoteObjectName() {
        for (String value : new String[] { "unqoted", "\"quoted\"" }) {
            String  result=ExtendedStringUtils.smartUnquoteObjectName(value);
            if (value.charAt(0) == '"')
                assertEquals("Mismatched unquoted result", ObjectName.unquote(value), result);
            else
                assertSame("Mismatched unquoted value instance", value, result);
        }
    }

    @Test
    public void testSmartUnquoteObjectNameOnEmptyStrings() {
        final String    EMPTY_STRING="";
        assertNull("Mismatched null string value", ExtendedStringUtils.smartUnquoteObjectName(null));
        assertSame("Mismatched empty string value", EMPTY_STRING, ExtendedStringUtils.smartUnquoteObjectName(EMPTY_STRING));
        assertEquals("Mismatched quoted empty string value", EMPTY_STRING, ExtendedStringUtils.smartUnquoteObjectName("\"\""));
    }

    @Test
    public void testSmartUnquoteObjectNameOnImbalancedQuotesOrNull() {
        for (String value : new String[] { "\"", "\"abcd", "abcd\"" }) {
            try {
                String  result=ExtendedStringUtils.smartUnquoteObjectName(value);
                fail("Unexpected result for " + value + ": " + result);
            } catch(IllegalArgumentException e) {
                // expected - ignored
            }
        }
    }

    @Test
    public void testFindFirstNonMatchingCharacter () {
        Object[][] srcArray = new Object[][] {
	        //	{ <s1>, <s2>, startIndex, maxLen, expected },
	          { null, null, Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(-1) },
	          { null,   "", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(-1) },
	          {   "", null, Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(-1) },
	          {   "",   "", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(-1) },
	
	          {   "abc",  "abd", Integer.valueOf(0), Integer.valueOf(3), Integer.valueOf(2) },
	          {   "abc",  "abd", Integer.valueOf(0), Integer.valueOf(2), Integer.valueOf(-1) },
	
	          {   "bbc",  "abc", Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(-1) },
	          {   "bbc",  "abd", Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(2) },
	
	          {   "bbc",  "abcd", Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(-1) },
	          {   "bbc",  "abde", Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(-1) },
	
	          {   "abc",  "abcd", Integer.valueOf(0), Integer.valueOf(4), Integer.valueOf(3) },
	          {   "abcd", "abc",  Integer.valueOf(0), Integer.valueOf(4), Integer.valueOf(3) },
	        };
        for (Object[] input : srcArray) {
            assertEquals(MessageFormat.format("testFindFirstNonMatchingCharacter({0},{1}) start={2}, len={3}", input),
            			 ((Number) input[4]).intValue(),
            			 ExtendedStringUtils.findFirstNonMatchingCharacterIndex((CharSequence) input[0], (CharSequence)input[1],
            					 								  ((Number) input[2]).intValue(), ((Number) input[3]).intValue()));
        }
    }

	@Test
	public void testStringTrimToSize ()
	{
		final String	TEST_STRING="testStringTrimToSize";
		assertSame("Not same string instance", TEST_STRING, ExtendedStringUtils.trimToSize(TEST_STRING, TEST_STRING.length() + 1));

		for (int	length=0; length < TEST_STRING.length(); length++)
		{
			final String	expected=TEST_STRING.substring(0, length),
							actual=ExtendedStringUtils.trimToSize(TEST_STRING, length);
			assertNotSame("Same instance for length=" + length, expected, actual);
			assertEquals("Mismatched data for length=" + length, expected, actual);
		}
	}
}
