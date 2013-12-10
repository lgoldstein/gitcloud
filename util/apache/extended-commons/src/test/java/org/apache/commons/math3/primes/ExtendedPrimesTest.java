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

package org.apache.commons.math3.primes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Nov 11, 2013 11:44:53 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedPrimesTest extends AbstractTestSupport {
    public ExtendedPrimesTest() {
        super();
    }

    @Test
    public void testFactorsMap() {
        int[][] TEST_VALUES={
                { 5, 5, 1 },
                { 8, 2, 3 },
                { 9, 3, 2 },
                { 24, 2, 3, 3, 1 },
                { 56, 7, 1, 2, 3 },
                { 100, 2, 2, 5, 2 }
            };
        SortedMap<Integer,Integer>  expected=new TreeMap<Integer,Integer>();
        for (int[]  values : TEST_VALUES) {
            final int n=values[0];
            expected.clear();
            for (int    index=1; index < values.length; index += 2) {
                int factor=values[index], power=values[index+1];
                assertNull(n + ": duplicate factor: " + factor, expected.put(Integer.valueOf(factor), Integer.valueOf(power)));
            }
            
            SortedMap<Integer,? extends Number> actual=ExtendedPrimes.factorsMap(n);
            assertEquals(n + ": mismatched factors count", expected.size(), actual.size());
            for (Map.Entry<Integer,? extends Number> ee : expected.entrySet()) {
                Integer factor=ee.getKey();
                Number  expPower=ee.getValue(), actPower=actual.get(factor);
                assertNotNull(n + ": no power for factor=" + factor, actPower);
                assertEquals(n + ": mismatched power value for factor=" + factor, expPower.intValue(), actPower.intValue());
            }
        }
    }

    @Test
    public void testNonIntegersFactorsMap() {
        final int           TEST_VALUE=32765;
        Collection<Integer> factors=Primes.primeFactors(TEST_VALUE);
        SortedMap<Integer,? extends Number> expected=ExtendedPrimes.factorsMap(TEST_VALUE);

        Collection<Long>    converted=new ArrayList<Long>(factors.size());
        for (Integer    fac : factors) {
            converted.add(Long.valueOf(fac.longValue()));
        }

        SortedMap<Integer,? extends Number> actual=ExtendedPrimes.factorsMap(converted);
        assertEquals("Mismatched factors count", expected.size(), actual.size());
        for (Map.Entry<Integer,? extends Number> ee : expected.entrySet()) {
            Integer factor=ee.getKey();
            Number  expPower=ee.getValue(), actPower=actual.get(factor);
            assertNotNull("No power for factor=" + factor, actPower);
            assertEquals("Mismatched power value for factor=" + factor, expPower.intValue(), actPower.intValue());
        }
    }
}
