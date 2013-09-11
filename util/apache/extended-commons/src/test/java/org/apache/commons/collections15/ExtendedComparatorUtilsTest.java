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

package org.apache.commons.collections15;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jul 17, 2013 11:18:55 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedComparatorUtilsTest extends AbstractTestSupport {
    public ExtendedComparatorUtilsTest() {
        super();
    }

    @Test
    public void testObjectInstanceComparator() {
        Comparator<Object>  c=ExtendedComparatorUtils.OBJECT_INSTANCE_COMPARATOR;
        Date    d1=new Date(System.currentTimeMillis()), d2=(Date) d1.clone();
        assertEquals("Mismatched same instance result", 0, c.compare(d1, d1));
        assertFalse("Bad instance result for equal objects", c.compare(d1, d2) == 0);

        ConstantHashCode o1=new ConstantHashCode(), o2=new ConstantHashCode();
        assertFalse("Bad instance result for same hash code objects", c.compare(o1, o2) == 0);
    }

    @Test
    public void testMinValue() {
        List<Integer>   items=new ArrayList<Integer>(Long.SIZE);
        for (int    index=0; index < Long.SIZE; index++) {
            items.add(Integer.valueOf(index));
        }
        
        Integer expected=Integer.valueOf(0);
        for (int    index=0; index < Long.SIZE; index++) {
            Collections.shuffle(items, RANDOMIZER);
            Integer actual=ExtendedComparatorUtils.minValue(items);
            assertEquals("Mismatched result for " + items, expected, actual);
        }
    }

    @Test
    public void testMaxValue() {
        List<Integer>   items=new ArrayList<Integer>(Long.SIZE);
        for (int    index=0; index < Long.SIZE; index++) {
            items.add(Integer.valueOf(index));
        }
        
        Integer expected=Integer.valueOf(Long.SIZE - 1);
        for (int    index=0; index < Long.SIZE; index++) {
            Collections.shuffle(items, RANDOMIZER);
            Integer actual=ExtendedComparatorUtils.maxValue(items);
            assertEquals("Mismatched result for " + items, expected, actual);
        }
    }

    static final class ConstantHashCode {
        ConstantHashCode() {
            super();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof ConstantHashCode);
        }
    }
}
