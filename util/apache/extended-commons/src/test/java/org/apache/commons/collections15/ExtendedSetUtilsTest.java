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

package org.apache.commons.collections15;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jul 17, 2013 1:06:15 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedSetUtilsTest extends AbstractTestSupport {
    public ExtendedSetUtilsTest() {
        super();
    }

    @Test
    public void testUniqueInstanceSet() {
        List<Object>    expected=Arrays.asList( new Object(), new Object() );
        List<Object>    items=new ArrayList<Object>(expected.size() * Byte.SIZE);
        for (int index=0; index < Byte.SIZE; index++) {
            items.addAll(expected);
        }
        
        for (int    index=0; index < Long.SIZE; index++) {
            Collections.shuffle(items, RANDOMIZER);
            
            List<Object> actual=new ArrayList<Object>(ExtendedSetUtils.uniqueInstanceSet(items));
            if (!CollectionUtils.isEqualCollection(expected, actual)) {
                fail("Mismatched items for " + items + ": expected=" + expected + ", actual=" + actual);
            }
        }
    }
}
