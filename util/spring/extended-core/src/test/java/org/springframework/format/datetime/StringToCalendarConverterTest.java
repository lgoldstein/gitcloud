/*
 * Copyright 2013 Lyor Goldstein
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.format.datetime;

import java.util.Calendar;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.AbstractSpringTestSupport;

/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringToCalendarConverterTest extends AbstractSpringTestSupport {
    private static final StringToCalendarConverter  converter=new StringToCalendarConverter();

    public StringToCalendarConverterTest() {
        super();
    }

    @Test
    public void testStringToDateConversion() {
        Calendar    expected=Calendar.getInstance();
        Calendar    actual=converter.convert(String.valueOf(expected.getTimeInMillis()));
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
                Calendar    result=converter.convert(value);
                fail("Unexpected success for " + value + ": " + result);
            } catch(NumberFormatException e) {
                // expected - ignored
            }
        }
    }

}
