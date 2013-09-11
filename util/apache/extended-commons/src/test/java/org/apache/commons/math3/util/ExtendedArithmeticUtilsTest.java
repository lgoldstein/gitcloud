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

package org.apache.commons.math3.util;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedArithmeticUtilsTest extends AbstractTestSupport {
    public ExtendedArithmeticUtilsTest() {
        super();
    }

    @Test
    public void testLowestMultipleOf() {
        final int   FACTOR=Byte.SIZE;
        for (int multiplier=1, baseValue=FACTOR; multiplier < Byte.SIZE; multiplier++, baseValue += FACTOR) {
            for (int offset=0, curValue=baseValue, nextValue=baseValue + FACTOR; offset < FACTOR; offset++, curValue++) {
                int actual=ExtendedArithmeticUtils.lowestMultipleOf(curValue, FACTOR);
                if (offset == 0) {
                    assertEquals("Mismatched exact offset result", curValue, actual);
                } else {
                    assertEquals("Mismatched offset=" + offset + " result", nextValue, actual);
                }
            }
        }
    }

    @Test
    public void testHighestMultipleOf() {
        final int   FACTOR=Byte.SIZE;
        for (int multiplier=1, baseValue=FACTOR; multiplier < Byte.SIZE; multiplier++, baseValue += FACTOR) {
            for (int offset=0, curValue=baseValue; offset < FACTOR; offset++, curValue++) {
                int actual=ExtendedArithmeticUtils.highestMultipleOf(curValue, FACTOR);
                if (offset == 0) {
                    assertEquals("Mismatched exact offset result", curValue, actual);
                } else {
                    assertEquals("Mismatched offset=" + offset + " result", baseValue, actual);
                }
            }
        }
    }
}
