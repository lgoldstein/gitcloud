/**
 * 
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedCharSequenceReaderTest extends AbstractTestSupport {
    public ExtendedCharSequenceReaderTest() {
        super();
    }

    /**
     * Makes sure that {@link ExtendedCharSequenceReader} <code>read</code> methods
     * @throws IOException if invoked after closing the {@link Reader}
     */
    @Test
    public void testNoDataAfterClose() throws IOException {
        final String    TEST_DATA="testNoDataAfterClose";
        Reader  r=new ExtendedCharSequenceReader(TEST_DATA);
        try {
            for (int index=0; index < TEST_DATA.length(); index++) {
                char    expected=TEST_DATA.charAt(index);
                int     nValue=r.read();
                assertTrue("Unexpected EOF after " + index + " characters", nValue != (-1));
                assertTrue("Non-Unicode value at index=" + index + ": " + nValue, (nValue >= 0) && (nValue < 0xFFFF));
                assertEquals("Mismatched character at index=" + index, expected, (char) nValue);
            } 
        } finally {
            r.close();
        }
        
        try {
            int nValue=r.read();
            fail("Unexpected single value read: " + nValue);
        } catch(IOException e) {
            // expected - ignored
        }
        
        char[]  cbuf=new char[Byte.SIZE];
        try {
            int nRead=r.read(cbuf);
            fail("Unexpected full char buffer value read: " + nRead);
        } catch(IOException e) {
            // expected - ignored
        }

        try {
            int nRead=r.read(cbuf, 0, cbuf.length / 2);
            fail("Unexpected partial char buffer value read: " + nRead);
        } catch(IOException e) {
            // expected - ignored
        }

        try {
            int nRead=r.read(CharBuffer.wrap(cbuf));
            fail("Unexpected CharBuffer value read: " + nRead);
        } catch(IOException e) {
            // expected - ignored
        }
    }
}
