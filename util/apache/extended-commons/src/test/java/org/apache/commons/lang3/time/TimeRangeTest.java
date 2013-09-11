/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections15.ExtendedComparatorUtils;
import org.apache.commons.lang3.ExtendedObjectUtils;
import org.apache.commons.lang3.concurrent.TimeUnitUtils;
import org.apache.commons.lang3.math.ExtendedNumberUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Aug 20, 2013 9:27:06 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TimeRangeTest extends AbstractTestSupport {
    public TimeRangeTest() {
        super();
    }

    /**
     * Ensures that {@link TimeRange#fullRangeOf(TimeUnit)} returns the same
     * (cached) instance when invoked repeatedly with the same unit
     */
    @Test
    public void testFullRangeOfCaching() {
        for (TimeUnit u : TimeUnitUtils.VALUES) {
            TimeRange   expected=TimeRange.fullRangeOf(u);
            for (int    index=0; index < Byte.SIZE; index++) {
                TimeRange   actual=TimeRange.fullRangeOf(u);
                assertSame("Mismatched instance for " + u + " at index=" + index, expected, actual);
            }
        }
    }

    /**
     * Ensures that all full ranges are equal to each other regardless of their {@link TimeUnit}
     */
    @Test
    public void testFullRangeEquality() {
        for (TimeUnit u1 : TimeUnitUtils.VALUES) {
            TimeRange   r1=TimeRange.fullRangeOf(u1);
            for (TimeUnit u2 : TimeUnitUtils.VALUES) {
                TimeRange   r2=TimeRange.fullRangeOf(u2);
                assertEquals("Non-euqivalent ranges for " + u1 + " vs " + u2, r1, r2);
            }
        }
    }
    
    @Test
    public void testValueOfString() throws IOException {
        StringBuilder   sb=new StringBuilder(48);
        for (TimeUnit u : TimeUnitUtils.VALUES) {
            TimeRange   expected=TimeRange.valueOf(u, 7365L, 3777347L);

            for (int index=0; index < Long.SIZE; index++) {
                sb.setLength(0);

                appendRandomPadding(sb, ' ', Byte.SIZE).append(TimeRange.RANGE_START_DELIM);
                appendRandomPadding(sb, ' ', Byte.SIZE).append(expected.getStartValue());
                appendRandomPadding(sb, ' ', Byte.SIZE).append(TimeRange.RANGE_SEP_DELIM);
                appendRandomPadding(sb, ' ', Byte.SIZE).append(expected.getEndValue());
                appendRandomPadding(sb, ' ', Byte.SIZE).append(TimeRange.RANGE_END_DELIM);
                appendRandomPadding(sb, ' ', Byte.SIZE).append(shuffleCase(u.name()));

                String      str=appendRandomPadding(sb, ' ', Byte.SIZE).toString();
                TimeRange   actual=TimeRange.valueOf(str);
                assertEquals("Mismatched range for " + str, expected, actual);
            }
        }
    }

    @Test
    public void testBadValueOfString() {
        for (String s : new String[] {
                "   1 - 2 ] " + TimeUnit.SECONDS.name(),    // missing start separator
                " [ 3 - 4 " + TimeUnit.SECONDS.name(),      // missing end separator
                "   5 - 6  " + TimeUnit.SECONDS.name(),     // missing start AND end separator(s)
                " [ 7   8 ] " + TimeUnit.SECONDS.name(),    // missing values separator
                " extra [ 9 -  10 ] " + TimeUnit.SECONDS.name(),    // extra text before value(s)
                " [ 11 -  12 ] extra " + TimeUnit.SECONDS.name(),    // extra text after value(s)
                " [ 13 -  14 ] " + TimeUnit.SECONDS.name() + " extra",    // extra text after unit
                " [ aa -  15 ] " + TimeUnit.SECONDS.name(),    // bad start value
                " [ 16 -  bb ] " + TimeUnit.SECONDS.name(),    // bad end value
                " [ 17 -  18 ] clocks",                     // bad unit value
                " [ 19 -  20 ]",                            // no unit value
            }) {
            try {
                TimeRange   range=TimeRange.valueOf(s);
                fail("Unexpected success for " + s + ": " + range);
            } catch(IllegalArgumentException e) {
                // expected - ignored
            }
        }
    }
    @Test
    public void testValueOfFullRangeString() throws IOException {
        StringBuilder   sb=new StringBuilder(48);
        for (TimeUnit u : TimeUnitUtils.VALUES) {
            TimeRange   expected=TimeRange.fullRangeOf(u);
            for (int index=0; index < Long.SIZE; index++) {
                sb.setLength(0);

                appendRandomPadding(sb, ' ', Byte.SIZE).append(TimeRange.RANGE_START_DELIM);
                appendRandomPadding(sb, ' ', Byte.SIZE).append(TimeRange.RANGE_SEP_DELIM);
                appendRandomPadding(sb, ' ', Byte.SIZE).append(TimeRange.RANGE_END_DELIM);
                appendRandomPadding(sb, ' ', Byte.SIZE).append(shuffleCase(u.name()));

                String      str=appendRandomPadding(sb, ' ', Byte.SIZE).toString();
                TimeRange   actual=TimeRange.valueOf(str);
                assertSame("Mismatched instance for " + str, expected, actual);
            }
        }
    }

    @Test
    public void testCompareStartValue() {
        for (TimeUnit u1 : TimeUnitUtils.VALUES) {
            TimeRange   range=TimeRange.valueOf(u1, 7365L, 3777347L);
            assertEquals("Mismatched self start compare for " + u1, 0, range.compareStartValue(u1, range.getStartValue()));
            assertEquals("Mismatched self end compare for " + u1, 1, ExtendedNumberUtils.signOf(range.compareStartValue(u1, range.getEndValue())));

            for (TimeUnit u2 : TimeUnitUtils.VALUES) {
                int expected=ExtendedNumberUtils.signOf(TimeUnitUtils.BY_DURATION_COMPARATOR.compare(u1, u2));
                int actual=ExtendedComparatorUtils.reverse(ExtendedNumberUtils.signOf(range.compareStartValue(u2, range.getStartValue())));
                assertEquals("Mismatched comparison for " + u1 + " vs " + u2, expected, actual);
            }
        }
    }
    
    @Test
    public void testCompareStartValueWithOpenEndedRange() {
        for (TimeUnit u : TimeUnitUtils.VALUES) {
            TimeRange   range=TimeRange.valueOf(u, 7365L, 3777347L);
            TimeRange   full=TimeRange.fullRangeOf(u);
            assertEquals("Mismatched full-full start comparison", 0, full.compareStartValue(u, full.getStartValue()));
            assertEquals("Mismatched full-range start comparison", 1, ExtendedNumberUtils.signOf(full.compareStartValue(u, range.getStartValue())));
            assertEquals("Mismatched range-full start comparison", -1, ExtendedNumberUtils.signOf(range.compareStartValue(u, full.getStartValue())));
        }
    }

    @Test
    public void testCompareEndValue() {
        for (TimeUnit u1 : TimeUnitUtils.VALUES) {
            TimeRange   range=TimeRange.valueOf(u1, 7365L, 3777347L);
            assertEquals("Mismatched self end compare for " + u1, 0, range.compareEndValue(u1, range.getEndValue()));
            assertEquals("Mismatched self start compare for " + u1, -1, ExtendedNumberUtils.signOf(range.compareEndValue(u1, range.getStartValue())));
            
            for (TimeUnit u2 : TimeUnitUtils.VALUES) {
                int expected=ExtendedNumberUtils.signOf(TimeUnitUtils.BY_DURATION_COMPARATOR.compare(u1, u2));
                int actual=ExtendedComparatorUtils.reverse(ExtendedNumberUtils.signOf(range.compareEndValue(u2, range.getEndValue())));
                assertEquals("Mismatched comparison for " + u1 + " vs " + u2, expected, actual);
            }
        }
    }

    @Test
    public void testCompareEndValueWithOpenEndedRange() {
        for (TimeUnit u : TimeUnitUtils.VALUES) {
            TimeRange   range=TimeRange.valueOf(u, 7365L, 3777347L);
            TimeRange   full=TimeRange.fullRangeOf(u);
            assertEquals("Mismatched full-full start comparison", 0, full.compareEndValue(u, full.getEndValue()));
            assertEquals("Mismatched full-range start comparison", -1, ExtendedNumberUtils.signOf(full.compareEndValue(u, range.getEndValue())));
            assertEquals("Mismatched range-full start comparison", 1, ExtendedNumberUtils.signOf(range.compareEndValue(u, full.getEndValue())));
        }
    }
    
    @Test
    public void testContainsValue() {
        for (TimeUnit u : TimeUnitUtils.VALUES) {
            TimeRange   range=TimeRange.valueOf(u, 7365L, 3777347L);
            long        start=range.getStartValue(), end=range.getEndValue();
            assertTrue("Start value not contained for " + u, range.contains(u, start));
            assertTrue("End value not contained for " + u, range.contains(u, end));

            TimeRange   full=TimeRange.fullRangeOf(u);
            TimeRange   openStart=TimeRange.openStartRange(u, end);
            TimeRange   openEnd=TimeRange.openEndRange(u, start);
            
            int    unitsCount=(int) range.getNumUnits();
            for (int    index=0; index < Long.SIZE; index++) {
                final long offset;
                synchronized(RANDOMIZER) {
                    offset = RANDOMIZER.nextInt(unitsCount);
                }
                
                assertTrue("Value not contained for start offset " + offset + " " + u, range.contains(u, start + offset));
                assertFalse("Value unexpectedly contained for below start offset " + offset + " " + u, range.contains(u, start - offset - 1L));
                assertTrue("Value not contained for end offset " + offset + " " + u, range.contains(u, end - offset));
                assertFalse("Value unexpectedly contained for above end offset " + offset + " " + u, range.contains(u, end + offset + 1L));

                assertTrue("Value not contained for open-start start offset " + offset + " " + u, openStart.contains(u, start + offset));
                assertTrue("Value not contained for open-start below start offset " + offset + " " + u, openStart.contains(u, start - offset - 1L));
                assertTrue("Value not contained for open-start end offset " + offset + " " + u, openStart.contains(u, end - offset));
                assertFalse("Value unexpectedly contained for open-start above end offset " + offset + " " + u, openStart.contains(u, end + offset + 1L));

                assertTrue("Value not contained for open-end start offset " + offset + " " + u, openEnd.contains(u, start + offset));
                assertFalse("Value unexpectedly contained for open-end below start offset " + offset + " " + u, openEnd.contains(u, start - offset - 1L));
                assertTrue("Value not contained for open-end end offset " + offset + " " + u, openEnd.contains(u, end - offset));
                assertTrue("Value not contained for open-end above end offset " + offset + " " + u, openEnd.contains(u, end + offset + 1L));

                assertTrue("Value not contained for full start offset " + offset + " " + u, full.contains(u, start + offset));
                assertTrue("Value not contained for full below start offset " + offset + " " + u, full.contains(u, start - offset - 1L));
                assertTrue("Value not contained for full end offset " + offset + " " + u, full.contains(u, end - offset));
                assertTrue("Value not contained for full above end offset " + offset + " " + u, full.contains(u, end + offset + 1L));
            }
        }
    }
    
    @Test
    public void testContainsRange() {
        for (TimeUnit u : TimeUnitUtils.VALUES) {
            TimeRange   range=TimeRange.valueOf(u, 7365L, 3777347L);
            assertTrue("Self not contained for " + u, range.contains(range));

            TimeRange   full=TimeRange.fullRangeOf(u);
            assertTrue("Range not contained in full for " + u, full.contains(range));
            assertFalse("Full unexpectedly contained in range for " + u, range.contains(full));

            long        start=range.getStartValue(), end=range.getEndValue();
            TimeRange   openStart=TimeRange.openStartRange(u, end);
            assertTrue("Range not contained in open-start for " + u, openStart.contains(range));
            assertFalse("Open-start unexpectedly contained in range for " + u, range.contains(openStart));

            TimeRange   openEnd=TimeRange.openEndRange(u, start);
            assertTrue("Range not contained in open-end for " + u, openEnd.contains(range));
            assertFalse("Open-end unexpectedly contained in range for " + u, range.contains(openEnd));
            
            int    unitsCount=(int) range.getNumUnits() / 2;
            for (int    index=0; index < Long.SIZE; index++) {
                final long offset;
                synchronized(RANDOMIZER) {
                    offset = RANDOMIZER.nextInt(unitsCount);
                }
                
                {
                    TimeRange   subRange=TimeRange.valueOf(u, start + offset, end - offset);
                    assertTrue("Sub-range not contained in range: " + subRange, range.contains(subRange));
                    assertTrue("Sub-range not contained in open-start: " + subRange, openStart.contains(subRange));
                    assertTrue("Sub-range not contained in open-end: " + subRange, openEnd.contains(subRange));
                    assertTrue("Sub-range not contained in full: " + subRange, full.contains(subRange));
                }
                
                {
                    TimeRange   startRange=TimeRange.valueOf(u, start, end - offset);
                    assertTrue("Start-range not contained in range: " + startRange, range.contains(startRange));
                    assertTrue("Start-range not contained in open-start: " + startRange, openStart.contains(startRange));
                    assertTrue("Start-range not contained in open-end: " + startRange, openEnd.contains(startRange));
                    assertTrue("Start-range not contained in full: " + startRange, full.contains(startRange));
                }

                {
                    TimeRange   endRange=TimeRange.valueOf(u, start + offset, end);
                    assertTrue("End-range not contained in range: " + endRange, range.contains(endRange));
                    assertTrue("End-range not contained in open-start: " + endRange, openStart.contains(endRange));
                    assertTrue("End-range not contained in open-end: " + endRange, openEnd.contains(endRange));
                    assertTrue("End-range not contained in full: " + endRange, full.contains(endRange));
                }
                
                {
                    TimeRange   subStartRange=TimeRange.valueOf(u, start - offset, end - offset);
                    assertFalse("Sub-start-range unexpectedly contained in range: " + subStartRange, range.contains(subStartRange));
                    assertTrue("Sub-start-range not contained in open-start: " + subStartRange, openStart.contains(subStartRange));
                    assertFalse("Sub-start-range unexpectedly contained in open-end: " + subStartRange, openEnd.contains(subStartRange));
                    assertTrue("Sub-start-range not contained in full: " + subStartRange, full.contains(subStartRange));
                }

                {
                    TimeRange   aboveEndRange=TimeRange.valueOf(u, start + offset, end + offset);
                    assertFalse("Above-end-range unexpectedly contained in range: " + aboveEndRange, range.contains(aboveEndRange));
                    assertFalse("Above-end-range unexpectedly contained in open-start: " + aboveEndRange, openStart.contains(aboveEndRange));
                    assertTrue("Above-end-range not contained in open-end: " + aboveEndRange, openEnd.contains(aboveEndRange));
                    assertTrue("Above-end-range not contained in full: " + aboveEndRange, full.contains(aboveEndRange));
                }
            }
        }
    }

    @Test
    public void testGetInterval() {
        for (TimeUnit u : TimeUnitUtils.VALUES) {
            TimeRange   range=TimeRange.valueOf(u, 7365L, 3777347L);

            {
                Period      expected=range.getInterval();
                assertEquals("Mismatched interval unit", u, expected.getUnit());
                assertEquals("Mismatched interval size", range.getNumUnits(), expected.getCount());
                
                for (int index=0; index < Byte.SIZE; index++) {
                    Period      actual=range.getInterval();
                    assertSame("Mismatched interval instance for " + u + " at index=" + index, expected, actual);
                }
            }

            {
                TimeRange   openStart=TimeRange.openStartRange(u, range.getEndValue());
                Period      expected=openStart.getInterval();
                assertEquals("Mismatched open-start interval unit", u, expected.getUnit());
                assertTrue("Mismatched open-start interval size: " + expected.getCount(), TimeRange.isPositiveInfinity(expected.getCount()));
            }

            {
                TimeRange   openEnd=TimeRange.openEndRange(u, range.getStartValue());
                Period      expected=openEnd.getInterval();
                assertEquals("Mismatched open-end interval unit", u, expected.getUnit());
                assertTrue("Mismatched open-end interval size: " + expected.getCount(), TimeRange.isPositiveInfinity(expected.getCount()));
            }

            {
                TimeRange   full=TimeRange.fullRangeOf(u);
                Period      expected=full.getInterval();
                assertEquals("Mismatched full interval unit", u, expected.getUnit());
                assertTrue("Mismatched full interval size: " + expected.getCount(), TimeRange.isPositiveInfinity(expected.getCount()));
            }
        }
    }

    @Test
    public void testIntersectsOnSameUnit() {
        for (TimeUnit u : TimeUnitUtils.VALUES) {
            TimeRange   range=TimeRange.valueOf(u, 7365L, 3777347L);
            assertTrue("Self not intersects for " + u, range.intersects(range));

            TimeRange   full=TimeRange.fullRangeOf(u);
            testIntersection("Original", range, "range", full, "full", true);

            long        start=range.getStartValue(), end=range.getEndValue();
            TimeRange   openStart=TimeRange.openStartRange(u, end);
            testIntersection("Original", range, "range", openStart, "open-start", true);

            TimeRange   openEnd=TimeRange.openEndRange(u, start);
            testIntersection("Original", range, "range", openEnd, "open-end", true);
            
            int    unitsCount=(int) range.getNumUnits() / 2;
            for (int    index=0; index < Long.SIZE; index++) {
                final long offset;
                synchronized(RANDOMIZER) {
                    offset = RANDOMIZER.nextInt(unitsCount);
                }
                
                {
                    TimeRange   subRange=TimeRange.valueOf(u, start + offset, end - offset);
                    testIntersection("Sub-range", range, "range", subRange, "sub", true);
                    testIntersection("Sub-range", openStart, "open-start", subRange, "sub", true);
                    testIntersection("Sub-range", openEnd, "open-end", subRange, "sub", true);
                    testIntersection("Sub-range", full, "full", subRange, "sub", true);
                }
                
                {
                    TimeRange   startRange=TimeRange.valueOf(u, start, end - offset);
                    testIntersection("Start-range", range, "range", startRange, "start", true);
                    testIntersection("Start-range", openStart, "open-start", startRange, "start", true);
                    testIntersection("Start-range", openEnd, "open-end", startRange, "start", true);
                    testIntersection("Start-range", full, "full", startRange, "start", true);
                }

                {
                    TimeRange   endRange=TimeRange.valueOf(u, start + offset, end);
                    testIntersection("End-range", range, "range", endRange, "end", true);
                    testIntersection("End-range", openStart, "open-start", endRange, "end", true);
                    testIntersection("End-range", openEnd, "open-end", endRange, "end", true);
                    testIntersection("End-range", full, "full", endRange, "end", true);
                }
                
                {
                    TimeRange   subStartRange=TimeRange.valueOf(u, start - offset, end - offset);
                    testIntersection("Sub-start-range", range, "range", subStartRange, "sub-start", true);
                    testIntersection("Sub-start-range", openStart, "open-start", subStartRange, "sub-start", true);
                    testIntersection("Sub-start-range", openEnd, "open-end", subStartRange, "sub-start", true);
                    testIntersection("Sub-start-range", full, "full", subStartRange, "sub-start", true);
                }

                {
                    TimeRange   aboveEndRange=TimeRange.valueOf(u, start + offset, end + offset);
                    testIntersection("Above-end-range", range, "range", aboveEndRange, "above-end", true);
                    testIntersection("Above-end-range", openStart, "open-start", aboveEndRange, "above-end", true);
                    testIntersection("Above-end-range", openEnd, "open-end", aboveEndRange, "above-end", true);
                    testIntersection("Above-end-range", full, "full", aboveEndRange, "above-end", true);
                }
                
                {
                    TimeRange   belowRange=TimeRange.valueOf(u, start - offset - 2L, start - 1L);
                    testIntersection("Below-range", range, "range", belowRange, "below", false);
                    testIntersection("Below-range", openStart, "open-start", belowRange, "below", true);
                    testIntersection("Below-range", openEnd, "open-end", belowRange, "below", false);
                    testIntersection("Below-range", full, "full", belowRange, "below", true);
                }

                {
                    TimeRange   aboveRange=TimeRange.valueOf(u, end + 1L, end + offset + 2L);
                    testIntersection("Above-range", range, "range", aboveRange, "above", false);
                    testIntersection("Above-range", openStart, "open-start", aboveRange, "above", false);
                    testIntersection("Above-range", openEnd, "open-end", aboveRange, "above", true);
                    testIntersection("Above-range", full, "full", aboveRange, "above", true);
                }
            }
        }
    }

    @Test
    public void testIntersectsOnDifferentUnit() {
        for (TimeUnit u1 : TimeUnitUtils.VALUES) {
            final TimeRange   range=TimeRange.valueOf(u1, 3777347L, 10281713L);
            final long        start=range.getStartValue(), end=range.getEndValue();
            final TimeRange   openStart=TimeRange.openStartRange(u1, end);
            final TimeRange   openEnd=TimeRange.openEndRange(u1, start);
            final TimeRange   full=TimeRange.fullRangeOf(u1);

            for (TimeUnit u2 : TimeUnitUtils.VALUES) {
                if (u1.equals(u2)) {
                    continue;
                }

                final long  startConv=u2.convert(start, u1), endConv=u2.convert(end, u1);
                if (startConv >= endConv) {
                    continue;   // in case of precision issue
                }

                if ((endConv - startConv) > 2L) {   // avoid reversed range
                    TimeRange   subRange=TimeRange.valueOf(u2, startConv + 1L, endConv - 1L);
                    testIntersection("Sub-range", range, "range", subRange, "sub", true);
                    testIntersection("Sub-range", openStart, "open-start", subRange, "sub", true);
                    testIntersection("Sub-range", openEnd, "open-end", subRange, "sub", true);
                    testIntersection("Sub-range", full, "full", subRange, "sub", true);
                }

                {
                    TimeRange   belowRange=TimeRange.valueOf(u2, startConv - 2L, startConv - 1L);
                    testIntersection("Below-range", range, "range", belowRange, "below", false);
                    testIntersection("Below-range", openStart, "open-start", belowRange, "below", true);
                    testIntersection("Below-range", openEnd, "open-end", belowRange, "below", false);
                    testIntersection("Below-range", full, "full", belowRange, "below", true);
                }

                {
                    TimeRange   aboveRange=TimeRange.valueOf(u2, endConv + 1L, endConv + 2L);
                    testIntersection("Above-range", range, "range", aboveRange, "above", false);
                    testIntersection("Above-range", openStart, "open-start", aboveRange, "above", false);
                    testIntersection("Above-range", openEnd, "open-end", aboveRange, "above", true);
                    testIntersection("Above-range", full, "full", aboveRange, "above", true);
                }

                {
                    TimeRange   openStartRange=TimeRange.openStartRange(u2, endConv);
                    testIntersection("Open-start-range", range, "range", openStartRange, "start-open", true);
                    testIntersection("Open-start-range", openStart, "open-start", openStartRange, "start-open", true);
                    testIntersection("Open-start-range", openEnd, "open-end", openStartRange, "start-open", true);
                    testIntersection("Open-start-range", full, "full", openStartRange, "start-open", true);
                }

                {
                    TimeRange   openEndRange=TimeRange.openEndRange(u2, startConv);
                    testIntersection("Open-end-range", range, "range", openEndRange, "end-open", true);
                    testIntersection("Open-end-range", openStart, "open-start", openEndRange, "end-open", true);
                    testIntersection("Open-end-range", openEnd, "open-end", openEndRange, "end-open", true);
                    testIntersection("Open-end-range", full, "full", openEndRange, "end-open", true);
                }

                {
                    TimeRange   fullRange=TimeRange.fullRangeOf(u2);
                    testIntersection("Full-range", range, "range", fullRange, "full-range", true);
                    testIntersection("Full-range", openStart, "open-start", fullRange, "full-range", true);
                    testIntersection("Full-range", openEnd, "open-end", fullRange, "full-range", true);
                    testIntersection("Full-range", full, "full", fullRange, "full-range", true);
                }
            }
        }
    }

    @Test
    public void testRangeIntersection() {
        for (TimeUnit u1 : TimeUnitUtils.VALUES) {
            final TimeRange   range=TimeRange.valueOf(u1, 3777347L, 10281713L);
            assertSame("Mismatched self intersection result", range, range.intersection(range));

            final long        start=range.getStartValue(), end=range.getEndValue();
            final TimeRange   openStart=TimeRange.openStartRange(u1, end);
            testIntersection("Range-open-start", range, "range", openStart, "open-start", range, u1);

            final TimeRange   openEnd=TimeRange.openEndRange(u1, start);
            testIntersection("Range-open-end", range, "range", openEnd, "open-end", range, u1);

            final TimeRange   full=TimeRange.fullRangeOf(u1);
            testIntersection("Range-full", range, "range", full, "full", range, u1);

            for (TimeUnit u2 : TimeUnitUtils.VALUES) {
                final long  startConv=u2.convert(start, u1), endConv=u2.convert(end, u1);
                if (startConv >= endConv) {
                    continue;   // in case of precision issue
                }

                TimeUnit    resultUnit=ExtendedObjectUtils.min(u1, u2, TimeUnitUtils.BY_DURATION_COMPARATOR);
                if ((endConv - startConv) > 2L) {   // avoid reversed range
                    TimeRange   subRange=TimeRange.valueOf(u2, startConv + 1L, endConv - 1L);
                    testIntersection("Sub-range", range, "range", subRange, "sub", subRange, resultUnit);
                    testIntersection("Sub-range", openStart, "open-start", subRange, "sub", subRange, resultUnit);
                    testIntersection("Sub-range", openEnd, "open-end", subRange, "sub", subRange, resultUnit);
                    testIntersection("Sub-range", full, "full", subRange, "sub", subRange, resultUnit);
                }

                {
                    TimeRange   belowRange=TimeRange.valueOf(u2, startConv - 2L, startConv - 1L);
                    testIntersection("Below-range", range, "range", belowRange, "below", null, null);
                }

                {
                    TimeRange   aboveRange=TimeRange.valueOf(u2, endConv + 1L, endConv + 2L);
                    testIntersection("Above-range", range, "range", aboveRange, "above", null, null);
                }
            }
        }
    }

    private static TimeRange testIntersection(String name, TimeRange r1, String r1Name, TimeRange r2, String r2Name, TimeRange expected, TimeUnit resultUnit) {
        TimeRange   direct=r1.intersection(r2);
        assertEquals(name + ": " + r1Name + " " + r1 + " intersection with " + r2Name + " " + r2, expected, direct);
        if (expected != null) {
            validateRangeUnit(name + "[direct]", direct, resultUnit);
        }

        TimeRange   reverse=r2.intersection(r1);
        assertEquals(name + ": " + r2Name + " " + r2 + " intersection with " + r1Name + " " + r1, expected, reverse);
        if (expected != null) {
            validateRangeUnit(name + "[reverse]", reverse, resultUnit);
        }
        
        assertEquals(name + ": " + r1Name + " " + r1 + " / " + r2Name + " " + r2 + " double intersection", direct, reverse);
        return direct;
    }

    private static TimeUnit validateRangeUnit(String name, TimeRange range, TimeUnit expected) {
        assertSame(name + ": " + range + " mismatched unit", expected, range.getUnit());
        return expected;
    }

    private static void testIntersection(String name, TimeRange r1, String r1Name, TimeRange r2, String r2Name, boolean expected) {
        // make sure intersection is commutative
        assertEquals(name + ": " + r1Name + " " + r1 + " " + (expected ? "not" : "unexpectedly") + " intersects with " + r2Name + " " + r2, expected, r1.intersects(r2));
        assertEquals(name + ": " + r2Name + " " + r2 + " " + (expected ? "not" : "unexpectedly") + " intersects with " + r1Name + " " + r1, expected, r2.intersects(r1));
    }
}
