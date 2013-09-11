/**
 * 
 */
package org.springframework.format.datetime;

import java.util.Date;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.AbstractSpringTestSupport;

/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringToDateConverterTest extends AbstractSpringTestSupport {
    private static final StringToDateConverter  converter=new StringToDateConverter();

    public StringToDateConverterTest() {
        super();
    }

    @Test
    public void testStringToDateConversion() {
        Date    expected=new Date(System.currentTimeMillis());
        Date    actual=converter.convert(String.valueOf(expected.getTime()));
        assertEquals("Mismatched conversion result", expected, actual);
    }
    
    @Test
    public void testNullOrEmptyConversion() {
        for (String value : new String[] { null, "" }) {
            assertNull("Unexpected result for value='" + value + "'", converter.convert(value));
        }
    }

    @Test
    public void testBadValuesConversion() {
        for (String value : new String[] { "abcd", String.valueOf(Math.PI), "73196565377734710281713098651268422002422" }) {
            try {
                Date    result=converter.convert(value);
                fail("Unexpected success for " + value + ": " + result);
            } catch(NumberFormatException e) {
                // expected - ignored
            }
        }
    }
}
