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

package org.apache.commons.lang3.time;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jul 30, 2013 12:50:50 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedDateUtilsTest extends AbstractTestSupport {
    public ExtendedDateUtilsTest() {
        super();
    }

    @Test
    public void tastAsEpochSeconds() {
        assertNull("Non-null result", ExtendedDateUtils.asEpochSeconds(null));
        
        Date    orig=new Date(System.currentTimeMillis());
        Date    actual=assertEpochSeconds("Bad result", ExtendedDateUtils.asEpochSeconds(orig));
        assertNotSame("No new instance", orig, actual);
        
        // make sure that if called on an already epoch-ed value nothing changes
        Date    repeated=assertEpochSeconds("Repeated call", ExtendedDateUtils.asEpochSeconds(actual));
        assertEquals("Mismatched repeated result", actual, repeated);
    }
    
    @Test
    public void testSetEpochSeconds() {
        assertEquals("Bad null result", (-1L), ExtendedDateUtils.setEpochSeconds(null));

        Date    now=new Date(System.currentTimeMillis());
        long    orig=now.getTime(), diff=ExtendedDateUtils.setEpochSeconds(now);
        assertEpochSeconds("Value not updated", now);
        assertEquals("Mismatched updated value", orig, now.getTime() + diff);
        
        orig = now.getTime();
        assertEquals("Mismatched repeated diff", 0L, ExtendedDateUtils.setEpochSeconds(now));
        assertEquals("Repeated diff value changed", orig, now.getTime());
    }

    private static Date assertEpochSeconds(String message, Date d) {
        Calendar    cal=Calendar.getInstance();
        cal.setTime(d);
        assertEquals(message, 0, cal.get(Calendar.MILLISECOND));
        return d;
    }
}
