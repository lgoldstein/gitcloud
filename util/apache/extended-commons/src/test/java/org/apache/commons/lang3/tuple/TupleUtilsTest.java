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

package org.apache.commons.lang3.tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jun 30, 2013 3:01:39 PM
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TupleUtilsTest extends AbstractTestSupport {
    public TupleUtilsTest() {
        super();
    }
    
    @Test
    public void testLeftValueExtractor() {
        Collection<Pair<Number,String>> vals=createPairs(Long.valueOf(Long.MIN_VALUE), Double.valueOf(Math.E));
        Transformer<Pair<Number,?>,Number>  xformer=TupleUtils.leftValueExtractor();
        for (Pair<Number,String> v : vals) {
            Number  expected=v.getLeft(), actual=xformer.transform(v);
            assertSame("Mismatched value for " + v, expected, actual);
        }
    }

    @Test
    public void testRightValueExtractor() {
        Collection<Pair<Number,String>>     vals=createPairs(Long.valueOf(Long.MIN_VALUE), Double.valueOf(Math.E));
        Transformer<Pair<?,String>,String>  xformer=TupleUtils.rightValueExtractor();
        for (Pair<Number,String> v : vals) {
            String  expected=v.getRight(), actual=xformer.transform(v);
            assertSame("Mismatched value for " + v, expected, actual);
        }
    }

    private static final List<Pair<Number,String>> createPairs(Number ... nums) {
        return createPairs(ExtendedArrayUtils.asList(nums));
    }

    private static final List<Pair<Number,String>> createPairs(Collection<? extends Number> nums) {
        if (ExtendedCollectionUtils.isEmpty(nums)) {
            return Collections.emptyList();
        }
        
        List<Pair<Number,String>>   values=new ArrayList<Pair<Number,String>>(nums.size());
        for (Number n : nums) {
            values.add(Pair.of(n, n.toString()));
        }
        return values;
    }

    @Test
    public void testFirstValueExtractor() {
        Collection<Triplet<Class<?>,String,Number>>   vals=createTriplets(
                Long.valueOf(System.nanoTime()),
                Double.valueOf(Math.PI),
                Byte.valueOf(Byte.MAX_VALUE));
        Transformer<Triplet<Class<?>,?,?>,Class<?>>   xformer=TupleUtils.firstValueExtractor();
        for (Triplet<Class<?>,String,Number> v : vals) {
            Class<?>    expected=v.getV1(), actual=xformer.transform(v);
            assertSame("Mismatched value for " + v, expected, actual);
        }
    }

    @Test
    public void testSecondValueExtractor() {
        Collection<Triplet<Class<?>,String,Number>>   vals=createTriplets(
                Long.valueOf(System.currentTimeMillis()),
                Double.valueOf(Math.E),
                Byte.valueOf(Byte.MIN_VALUE));
        Transformer<Triplet<?,String,?>,String>   xformer=TupleUtils.secondValueExtractor();
        for (Triplet<Class<?>,String,Number> v : vals) {
            String    expected=v.getV2(), actual=xformer.transform(v);
            assertSame("Mismatched value for " + v, expected, actual);
        }
    }

    @Test
    public void testThirdValueExtractor() {
        Collection<Triplet<Class<?>,String,Number>>   vals=createTriplets(
                Long.valueOf(Runtime.getRuntime().freeMemory()),
                Double.valueOf(Math.E + Math.PI),
                Byte.valueOf((byte) Byte.SIZE));
        Transformer<Triplet<?,?,Number>,Number>   xformer=TupleUtils.thirdValueExtractor();
        for (Triplet<Class<?>,String,Number> v : vals) {
            Number    expected=v.getV3(), actual=xformer.transform(v);
            assertSame("Mismatched value for " + v, expected, actual);
        }
    }

    private static final List<Triplet<Class<?>,String,Number>> createTriplets(Number ... nums) {
        return createTriplets(ExtendedArrayUtils.asList(nums));
    }

    private static final List<Triplet<Class<?>,String,Number>> createTriplets(Collection<? extends Number> nums) {
        if (ExtendedCollectionUtils.isEmpty(nums)) {
            return Collections.emptyList();
        }
        
        List<Triplet<Class<?>,String,Number>>   vals=new ArrayList<Triplet<Class<?>,String,Number>>(nums.size());
        for (Number n : nums) {
            vals.add(new Triplet<Class<?>,String,Number>(n.getClass(), n.toString(), n));
        }
        return vals;
    }
}
