/**
 * 
 */
package org.apache.commons.lang3;

import java.nio.CharBuffer;
import java.util.UUID;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author lgoldstein
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedCharSequenceUtilsTest extends AbstractTestSupport {
	public ExtendedCharSequenceUtilsTest() {
		super();
	}

	@Test
	public void testSafeToCharSequenceOnCharSequence() {
		final String	VALUE="testSafeToCharSequenceOnCharSequence";
		for (Object expected : new Object[] {
				VALUE, new StringBuilder(VALUE), new StringBuffer(VALUE), CharBuffer.wrap(VALUE)
			}) {
			CharSequence	actual=ExtendedCharSequenceUtils.safeToCharSequence(expected);
			assertSame(expected.getClass().getSimpleName() + ": Mismatched instance", expected, actual);
		}
	}
	
	@Test
	public void testSafeToCharSequenceOnNonCharSequence() {
		for (Object expected : new Object[] {
				Long.valueOf(System.currentTimeMillis()), Double.valueOf(Math.random()), UUID.randomUUID()	
			}) {
			CharSequence	actual=ExtendedCharSequenceUtils.safeToCharSequence(expected);
			assertEquals(expected.getClass().getSimpleName() + ": Mismatched value", expected.toString(), actual);
		}
	}
	
	@Test
	public void testSafeToCharSequenceOnNull() {
		assertNull("Unexpected sequence", ExtendedCharSequenceUtils.safeToCharSequence(null));
	}
	
	@Test
	public void testCapitalize() {
	    assertNull("Non-null result for null input", ExtendedCharSequenceUtils.capitalize(null));

	    for (String expected : new String[] { "", getClass().getSimpleName(), "A", "1234"}) {
	        String actual=ExtendedCharSequenceUtils.capitalize(expected);
	        assertSame("Mismatched instances", expected, actual);
	    }
	    
	    for (int   index=0; index < Long.SIZE; index++) {
	        String value=shuffleCase(getClass().getSimpleName());
	        String expected=StringUtils.capitalize(value);
	        String actual=ExtendedCharSequenceUtils.capitalize(expected);
	        assertEquals("Mismatched results", expected, actual);
	    }
	}

	@Test
	public void testCharSequenceTrimToSize ()
	{
		final CharSequence	TEST_SEQUENCE=new StringBuilder("testCharSequenceTrimToSize");
		assertSame("Not same string instance", TEST_SEQUENCE, ExtendedCharSequenceUtils.trimToSize(TEST_SEQUENCE, TEST_SEQUENCE.length() + 1));

		for (int	length=0; length < TEST_SEQUENCE.length(); length++)
		{
			final CharSequence	expected=TEST_SEQUENCE.subSequence(0, length),
								actual=ExtendedCharSequenceUtils.trimToSize(TEST_SEQUENCE, length);
			assertNotSame("Same instance for length=" + length, expected, actual);
			assertEquals("Mismatched data for length=" + length, expected.toString(), actual.toString());
		}
	}

    @Test
    public void testBadStripQuotes () {
        for (CharSequence value : new CharSequence[] { "\"", "'", "'abcd", "\"abcd", "1234'", "1234\"",
                new StringBuilder("'1234"), CharBuffer.wrap("hello\"") }) {
            try {
                CharSequence    result=ExtendedCharSequenceUtils.stripQuotes(value);
                fail("Unexpected success for " + value + ": " + result);
            } catch(IllegalArgumentException e) {
                // expected - ignored
            }
        }
    }

    @Test
    public void testStripQuotesOnUnmodifiedValues () {
        for (CharSequence value : new CharSequence[] { null, "", "abcd",
                                        new StringBuilder("1234"), CharBuffer.wrap("hello") }) {
            assertSame("Mismatched instance for " + value, value, ExtendedCharSequenceUtils.stripQuotes(value));
        }
    }
    
    @Test
    public void testStripQuotes () {
        CharSequence[]  seqs={
                "\"double-quoted\"", "double-quoted",
                "'single-quoted'", "single-quoted",
                new StringBuilder("\"StringBuilder DBL QUOTE\""), "StringBuilder DBL QUOTE",
                new StringBuilder("'StringBuilder SNGL QUOTE'"), "StringBuilder SNGL QUOTE",
                CharBuffer.wrap("\"CharBuffer DBL QUOTE\""), "CharBuffer DBL QUOTE",
                CharBuffer.wrap("'CharBuffer SNGL QUOTE'"), "CharBuffer SNGL QUOTE"
            };
        for (int    index=0; index < seqs.length; index += 2) {
            CharSequence    cs=seqs[index];
            String          expected=seqs[index+1].toString();
            CharSequence    result=ExtendedCharSequenceUtils.stripQuotes(cs);
            assertEquals("Mismatched results", expected, result.toString());
        }
    }

    @Test
    public void testGetSafeLength () {
        assertEquals("Mismatched value for null", 0, ExtendedCharSequenceUtils.getSafeLength(null));
        assertEquals("Mismatched value for empty", 0, ExtendedCharSequenceUtils.getSafeLength(""));
        assertEquals("Mismatched value for builder", 0, ExtendedCharSequenceUtils.getSafeLength(new StringBuilder()));
        
        final StringBuilder    TEST_VALUE=new StringBuilder(getClass().getSimpleName() + "@" + System.nanoTime());
        for (int    curLength=TEST_VALUE.length() - 1; curLength >= 0; curLength--) {
            TEST_VALUE.setLength(curLength);
            assertEquals("Mismatched value for " + TEST_VALUE, TEST_VALUE.length(), ExtendedCharSequenceUtils.getSafeLength(TEST_VALUE));
        }
    }
}
