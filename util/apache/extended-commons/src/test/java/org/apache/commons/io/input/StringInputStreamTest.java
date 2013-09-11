/**
 * 
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringInputStreamTest extends AbstractTestSupport {
    public StringInputStreamTest() {
        super();
    }
    
    @Test
    public void testSimpleString() throws IOException {
        String      expected=getClass().getName() + "#testSimpleString()";
        InputStream s=new StringInputStream(expected);
        try {
            String  actual=IOUtils.toString(s);
            assertEquals("Mismatched read string", expected, actual);
        } finally {
            s.close();
        }
    }
}
