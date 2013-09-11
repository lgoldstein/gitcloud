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

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ExtendedSerializationUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PeriodTest extends AbstractTestSupport {
    public PeriodTest() {
        super();
    }

    @Test
    public void testCanonicalValueEquality() {
        TimeUnit    u1=TimeUnit.DAYS, u2=TimeUnit.HOURS;
        long        c1=7365L, c2=u2.convert(c1, u1);
        Period      p1=Period.valueOf(u1, c1);
        Period      p2=Period.valueOf(u2, c2);
        assertEquals("Mismatched canonical values", p1.getCanonicalValue(), p2.getCanonicalValue());
        assertEquals("Mismatched equality test", p1, p2);
        assertEquals("Mismatched comparison test", 0, p1.compareTo(p2));
        assertEquals("Mismatched reversed comparison test", 0, p2.compareTo(p1));
        assertEquals("Mismatched comparator test", 0, Period.BY_CANONICAL_VALUE_COMPARATOR.compare(p1, p2));
    }

    @Test
    public void testValueOfString() {
        Period  expected=Period.valueOf(TimeUnit.DAYS, 7365L);
        Period  actual=Period.valueOf(expected.toString());
        assertEquals("Mismatched time unit", expected.getUnit(), actual.getUnit());
        assertEquals("Mismatched count", expected.getCount(), actual.getCount());
    }
    
    @Test
    public void testValueOfStringCaseInsensitiveTimeUnit() {
        Period  expected=Period.valueOf(TimeUnit.SECONDS, 3777347L);
        for (int index=0; index < Byte.SIZE; index++) {
            String  value=shuffleCase(expected.toString());
            Period  actual=Period.valueOf(value);
            assertEquals(value + ": Mismatched time unit", expected.getUnit(), actual.getUnit());
            assertEquals(value + ": Mismatched count", expected.getCount(), actual.getCount());
        }
    }
    
    @Test
    public void testValueConversion() {
        Period      baseValue=Period.valueOf(TimeUnit.DAYS, 1L);
        Object[]    pairs={
                TimeUnit.HOURS,     Long.valueOf(24L),
                TimeUnit.MINUTES,   Long.valueOf(24L * 60L),
                TimeUnit.SECONDS,   Long.valueOf(24L * 3600L)
            };
        for (int    index=0; index < pairs.length; index += 2) {
            TimeUnit    unit=(TimeUnit) pairs[index];
            Number      expected=(Number) pairs[index+1];
            long        actual=baseValue.convert(unit);
            assertEquals(unit.name() + ": mismatched result", expected.longValue(), actual);
        }
    }
    
    @Test
    public void testUnitConversion() {
        Period      expected=Period.valueOf(TimeUnit.DAYS, 1L);
        for (TimeUnit    unit : new TimeUnit[] { TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS }) {
        	Period	actual=expected.convertUnit(unit);
        	assertEquals(unit.name() + ": mismatched result", expected, actual);
        }
        
        assertSame("Mismatched same unit instance", expected, expected.convertUnit(expected.getUnit()));
    }
    
    @Test
    public void testClone() {
        Period  original=Period.valueOf(TimeUnit.MILLISECONDS, 10281713L);
        Period  cloned=original.clone();
        assertEquals("Mismatched cloned value", original, cloned);
        assertNotSame("No new cloned instance", original, cloned);
    }
    
    @Test
    public void testSerialization() {
        Period  expected=Period.valueOf(TimeUnit.SECONDS, 7365);
        byte[]  bytes=SerializationUtils.serialize(expected);
        Period  actual=ExtendedSerializationUtils.deserialize(Period.class, bytes);
        assertEquals("Mismatched de-serialized value", expected, actual);
        assertNotSame("No new de-serialized instance", expected, actual);
    }
}
