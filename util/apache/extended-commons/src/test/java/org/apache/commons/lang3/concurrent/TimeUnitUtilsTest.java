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

package org.apache.commons.lang3.concurrent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.math.ExtendedNumberUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jul 17, 2013 3:17:09 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TimeUnitUtilsTest extends AbstractTestSupport {
    public TimeUnitUtilsTest() {
        super();
    }

    @Test
    public void testFromNameXformer() {
        for (TimeUnit expected : TimeUnit.values()) {
            String  name=expected.name();
            for (int    index=0; index < name.length(); index++) {
                name = shuffleCase(name);
                TimeUnit    actual=TimeUnitUtils.FROM_NAME_XFORM.transform(name);
                assertSame("Mismatched values for " + name, expected, actual);
            }
        }
    }

    @Test
    public void testByDurationComparator() {
        // NOTE: currently, the ordinal order is same as duration
        List<TimeUnit>  values=Arrays.asList(TimeUnit.values());
        for (int i=0; i < values.size(); i++) {
            TimeUnit    u1=values.get(i);
            for (int j=0; j < values.size(); j++) {
                TimeUnit    u2=values.get(j);
                int         expected=ExtendedNumberUtils.signOf(i - j);
                int         actual=ExtendedNumberUtils.signOf(TimeUnitUtils.BY_DURATION_COMPARATOR.compare(u1, u2));
                assertEquals("Mismatched result for " + u1 + " vs. " + u2, expected, actual);
            }
        }
    }

    @Test
    public void testSameUnitCompare() {
        long    nowMsec=7365L, nowNano=3777347L;
        long    msecSign=ExtendedNumberUtils.compare(nowMsec, nowNano);
        long    nanoSign=ExtendedNumberUtils.compare(nowNano, nowMsec);
        long[]  testValues={
                nowMsec, nowMsec, 0L,
                nowNano, nowNano, 0L,
                nowMsec, nowNano, msecSign,
                nowNano, nowMsec, nanoSign,
                nowMsec, nowMsec + 1L, (-1L),
                nowNano, nowNano - 1L, (+1L)
            };
        for (TimeUnit u : TimeUnitUtils.VALUES) {
            for (int index=0; index < testValues.length; index += 3) {
                long    v1=testValues[index], v2=testValues[index+1];
                int     expected=ExtendedNumberUtils.signOf(testValues[index+2]);
                int     actual=TimeUnitUtils.compare(u, v1, u, v2);
                assertEquals("Mismatched comparison result for " + u + " " + v1 + "/" + v2, expected, actual);
            }
        }
    }

    @Test
    public void testDifferentUnitSameValueCompare() {
        final long[]        testValues={ 7365L, 3777347L, 10281713L };
        for (TimeUnit testUnit : TimeUnitUtils.VALUES) {
            for (TimeUnit u : TimeUnitUtils.VALUES) {
                // avoid precision issues by converting shorter durations to longer ones
                if (TimeUnitUtils.BY_DURATION_COMPARATOR.compare(testUnit, u) <= 0) {
                    continue;
                }
    
                for (long orgValue : testValues) {
                    for (int index=0; index < 2; index++) {
                        if (index > 0) {
                            orgValue = 0L - orgValue;
                        }
    
                        long    convValue=u.convert(orgValue, testUnit);
                        assertEquals("Mismatched comparison result for " + testUnit + "[" + orgValue + "] vs " + u + "[" + convValue + "]",
                                     0, TimeUnitUtils.compare(testUnit, orgValue, u, convValue));
                        assertEquals("Mismatched comparison result for " + u + "[" + convValue + "] vs " + testUnit + "[" + orgValue + "]",
                                     0, TimeUnitUtils.compare(u, convValue, testUnit, orgValue));
                    }
                }
            }
        }
    }

    @Test
    public void testDifferentUnitsDifferentValuesCompare() {
        final long[]  testValues={ 7365L, 3777347L, 10281713L };        
        for (TimeUnit u1 : TimeUnitUtils.VALUES) {
            for (TimeUnit u2 : TimeUnitUtils.VALUES) {
                int expected=TimeUnitUtils.BY_DURATION_COMPARATOR.compare(u1, u2);
                int reversed=0 - ExtendedNumberUtils.signOf(expected);

                for (long v : testValues) {
                    assertEquals("Mismatched " + v + " " + u1 + " vs. " + v + " " + u2 + " comparison result",
                                 expected, TimeUnitUtils.compare(u1, v, u2, v));
                    assertEquals("Mismatched " + v + " " + u2 + " vs. " + v + " " + u1 + " comparison result",
                                 reversed, TimeUnitUtils.compare(u2, v, u1, v));
                }
            }
        }
    }
}
