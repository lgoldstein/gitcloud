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

package org.apache.commons.math3.util;

import java.util.Arrays;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Aug 21, 2013 11:14:04 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedMathUtilsTest extends AbstractTestSupport {
    public ExtendedMathUtilsTest() {
        super();
    }

    @Test
    public void testAbsInt() {
        int[]   specialValues={
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE
            };
        for (int index=0; index < specialValues.length; index += 2) {
            int value=specialValues[index], expected=specialValues[index+1];
            int actual=ExtendedMathUtils.safeAbs(value);
            assertEquals("Mismatched result for " + value, expected, actual);
        }
        
        for (int index=0; index < Byte.MAX_VALUE; index++) {
            final int value;
            synchronized(RANDOMIZER) {
                value = RANDOMIZER.nextInt();
            }
            
            if (Arrays.binarySearch(specialValues, value) >= 0) {
                continue;   // skip special values since tested
            }
            
            int expected=Math.abs(value), actual=ExtendedMathUtils.safeAbs(value);
            assertEquals("Mismatched result for " + value, expected, actual);
        }
    }

    @Test
    public void testAbsLong() {
        long[]   specialValues={
                Long.MIN_VALUE, Long.MAX_VALUE,
                Long.MAX_VALUE, Long.MAX_VALUE
            };
        for (int index=0; index < specialValues.length; index += 2) {
            long value=specialValues[index], expected=specialValues[index+1];
            long actual=ExtendedMathUtils.safeAbs(value);
            assertEquals("Mismatched result for " + value, expected, actual);
        }
        
        for (int index=0; index < Byte.MAX_VALUE; index++) {
            final long value;
            synchronized(RANDOMIZER) {
                value = RANDOMIZER.nextLong();
            }
            
            if (Arrays.binarySearch(specialValues, value) >= 0) {
                continue;   // skip special values since tested
            }
            
            long expected=Math.abs(value), actual=ExtendedMathUtils.safeAbs(value);
            assertEquals("Mismatched result for " + value, expected, actual);
        }
    }
}
