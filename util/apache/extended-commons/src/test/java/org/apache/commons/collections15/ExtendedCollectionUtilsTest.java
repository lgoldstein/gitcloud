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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.AsymmetricComparator;
import org.apache.commons.lang3.concurrent.TimeUnitUtils;
import org.apache.commons.lang3.math.ExtendedNumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Triplet;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author lgoldstein
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedCollectionUtilsTest extends AbstractTestSupport {
	private static final AsymmetricComparator<Map.Entry<? extends CharSequence,? extends Number>,Number>	TESTCOMP=
			new AsymmetricComparator<Map.Entry<? extends CharSequence,? extends Number>,Number>() {
				@Override
				public int compare (Map.Entry<? extends CharSequence,? extends Number> v1, Number v2)
				{
					final Number	n1=(v1 == null) ? null : v1.getValue();
					final int		i1=(n1 == null) ? (-1) : n1.intValue(),
									i2=(v2 == null) ? (-2) : v2.intValue();
					return ExtendedNumberUtils.signOf(i1 - i2);
				}
		};

	public ExtendedCollectionUtilsTest() {
		super();
	}

    @Test
    public void testSize () {
        assertEquals("Mismatched null size", 0, ExtendedCollectionUtils.size(null));
        assertEquals("Mismatched empty list size", 0, ExtendedCollectionUtils.size(Collections.<Object>emptyList()));
        assertEquals("Mismatched empty set size", 0, ExtendedCollectionUtils.size(Collections.<Object>emptySet()));
        
        Random  rnd=new Random(System.currentTimeMillis());
        for (int index=0; index < Long.SIZE; index++) {
            int numItems=1 + rnd.nextInt(Long.SIZE);
            assertEquals("Mismatched collection size", numItems, ExtendedCollectionUtils.size(Collections.nCopies(numItems, Integer.valueOf(index))));
        }
    }

    @Test
    public void testGetFirstMemberOnNullOrEmpty () {
    	Collection<?>[]	colls={ null, Collections.<Object>emptyList() };
    	for (Collection<?> c : colls) {
    		Object	result=ExtendedCollectionUtils.getFirstMember(c);
    		assertNull("Unexpected result for " + c, result);
    	}
    }

    @Test
    public void testGetFirstMemberOnVariousCollections () {
    	Object	value=Long.valueOf(System.currentTimeMillis());
    	Collection<?>[]	colls={ Collections.singleton(value), Collections.singletonList(value), Arrays.asList(value) };
    	for (Collection<?> c : colls) {
    		Object		result=ExtendedCollectionUtils.getFirstMember(c);
    		Class<?>	cc=c.getClass();
    		assertSame("Mismatched result for " + cc.getSimpleName(), value, result);
    	}
    }

    @Test
    public void testFindFirstDifference() {
        List<Integer>   source=new ArrayList<Integer>();
        for (int index=0; index < Byte.SIZE; index++) {
            source.add(Integer.valueOf(index));
        }
        
        assertNull("Unexpected self-difference", ExtendedCollectionUtils.findFirstComparableDifference(source, source));
        assertFirstDifference("1st value", source, source.subList(1, source.size()), source.get(0), source.get(1), 0);
        assertFirstDifference("last value", source, source.subList(0, source.size() - 1), source.get(source.size() - 1), null, source.size() - 1);
        
        List<Integer>   diff=new ArrayList<Integer>(source);
        Integer         diffValue=Integer.valueOf(7365);
        for (int index=0; index < source.size(); index++) {
            Integer orgValue=source.get(index);
            diff.set(index, diffValue);         // set a known non-matching value
            assertFirstDifference("index=" + index, source, diff, orgValue, diffValue, index);
            diff.set(index, orgValue); // restore original value
        }
    }

    @Test
    public void testAggregate() {
        Set<Integer>    expected=new HashSet<Integer>();
        for (int index=0; index < Byte.SIZE; index++) {
            expected.add(Integer.valueOf(index));
        }
        
        Set<Integer>    actual=ExtendedCollectionUtils.aggregateToSet(expected, new Transformer<Integer,List<Integer>>() {
                @Override
                public List<Integer> transform(Integer input) {
                    return Arrays.asList(input, input, input);
                }
            });
        if (!CollectionUtils.isEqualCollection(expected, actual)) {
            fail("Mismatched aggregated result: expected=" + expected + ", actual=" + actual);
        }
    }

	@Test
	public void testIndexAndMatchOf ()
	{
		final List<ImmutablePair<String,Integer>>	list=new ArrayList<ImmutablePair<String,Integer>>();
		final Set<Integer>							valSet=new TreeSet<Integer>();
		for (int	index=0; index < Long.SIZE; index++)
		{
			// NOTE: we create duplicates on purpose
			final Integer	value=Integer.valueOf(RANDOMIZER.nextInt(Short.SIZE));
			list.add(ImmutablePair.of(value.toString(), value));
			valSet.add(value);
		}

		for (final Number value : valSet)
		{
			final int	index=ExtendedCollectionUtils.indexOf(list, value, TESTCOMP);
			assertTrue("Value not found: " + value, index >= 0);
			
			final Map.Entry<? extends CharSequence,? extends Number>	entry=list.get(index),
									match=ExtendedCollectionUtils.matchOf(list, value, TESTCOMP);
			assertSame("Mismatched entries for " + value, entry, match);
		}
	}

	@Test
	public void testSortedAndBinaryIndexOf ()
	{
		final List<ImmutablePair<String,Integer>>	list=new ArrayList<ImmutablePair<String,Integer>>();
		for (int	index=0; index < Long.SIZE; index++)
			list.add(ImmutablePair.of(String.valueOf(index), Integer.valueOf(index)));

		for (int index=0; index < list.size(); index++)
		{
			final Number	value=Integer.valueOf(index);
			assertEquals("Mismatched comparison for " + value, 0, TESTCOMP.compare(list.get(index), value));
			assertEquals("Mismatched sorted index for " + value, index, ExtendedCollectionUtils.sortedIndexOf(list, value, TESTCOMP));
			assertEquals("Mismatched binary index for " + value, index, ExtendedCollectionUtils.binaryIndexOf(list, value, TESTCOMP));
		}
	}

    @Test
    public void testAccumulate() {
        Set<Integer>    expected=new HashSet<Integer>();
        for (int index=0; index < Byte.SIZE; index++) {
            expected.add(Integer.valueOf(index));
        }
        
        Set<Integer>    actual=null;
        for (int    index=0; index < Byte.SIZE; index++) {
            Set<Integer>    prev=actual;
            actual = ExtendedCollectionUtils.accumulate(prev, expected, ExtendedSetUtils.<Integer>hashSetFactory());
            assertNotSame("No new instance created at iteration " + index, expected, actual);

            if (index > 0) {
                assertSame("Unexpected new instance at iteration " + index, prev, actual);
            }
            if (!CollectionUtils.isEqualCollection(expected, actual)) {
                fail("Mismatched accumulated result at iteration " + index + ": expected=" + expected + ", actual=" + actual);
            }
        }
    }

    @Test
    public void testMinValueIndex() {
        List<Integer>   items=new ArrayList<Integer>(Long.SIZE);
        for (int    index=0; index < Long.SIZE; index++) {
            items.add(Integer.valueOf(index));
        }
        
        Integer expValue=Integer.valueOf(0);
        for (int    index=0; index < Long.SIZE; index++) {
            Collections.shuffle(items, RANDOMIZER);
            int expected=items.indexOf(expValue), actual=ExtendedCollectionUtils.minValueIndex(items);
            assertEquals("Mismatched result for " + items, expected, actual);
        }
    }

    @Test
    public void testMaxValueIndex() {
        List<Integer>   items=new ArrayList<Integer>(Long.SIZE);
        for (int    index=0; index < Long.SIZE; index++) {
            items.add(Integer.valueOf(index));
        }
        
        Integer expValue=Integer.valueOf(Long.SIZE - 1);
        for (int    index=0; index < Long.SIZE; index++) {
            Collections.shuffle(items, RANDOMIZER);
            int expected=items.indexOf(expValue), actual=ExtendedCollectionUtils.maxValueIndex(items);
            assertEquals("Mismatched result for " + items, expected, actual);
        }
    }

    @Test
    public void testMinValueOnNullEmpty() {
        assertNull("Unexpected null result", ExtendedCollectionUtils.minValue(String.CASE_INSENSITIVE_ORDER, null));
        assertNull("Unexpected empty result", ExtendedCollectionUtils.minValue(String.CASE_INSENSITIVE_ORDER, Collections.<String>emptyList()));
        assertNull("Unexpected all null(s)", ExtendedCollectionUtils.minValue(String.CASE_INSENSITIVE_ORDER, Arrays.<String>asList(null, null, null)));
    }

    @Test
    public void testMinValue() {
        List<TimeUnit>  list=new ArrayList<TimeUnit>(TimeUnitUtils.VALUES);
        for (int index=0; index < TimeUnitUtils.VALUES.size(); index++) {
            list.add(null);
        }
        
        for (int index=0; index < Long.SIZE; index++) {
            synchronized(RANDOMIZER) {
                Collections.shuffle(list, RANDOMIZER);
            }
            
            TimeUnit    actual=ExtendedCollectionUtils.minValue(TimeUnitUtils.BY_DURATION_COMPARATOR, list);
            assertSame("Mismatched result for " + list, TimeUnit.NANOSECONDS, actual);
        }
    }

    @Test
    public void testMaxValueOnNullEmpty() {
        assertNull("Unexpected null result", ExtendedCollectionUtils.maxValue(String.CASE_INSENSITIVE_ORDER, null));
        assertNull("Unexpected empty result", ExtendedCollectionUtils.maxValue(String.CASE_INSENSITIVE_ORDER, Collections.<String>emptyList()));
        assertNull("Unexpected all null(s)", ExtendedCollectionUtils.maxValue(String.CASE_INSENSITIVE_ORDER, Arrays.<String>asList(null, null, null)));
    }

    @Test
    public void testMaxValue() {
        List<TimeUnit>  list=new ArrayList<TimeUnit>(TimeUnitUtils.VALUES);
        for (int index=0; index < TimeUnitUtils.VALUES.size(); index++) {
            list.add(null);
        }
        
        for (int index=0; index < Long.SIZE; index++) {
            synchronized(RANDOMIZER) {
                Collections.shuffle(list, RANDOMIZER);
            }
            
            TimeUnit    actual=ExtendedCollectionUtils.maxValue(TimeUnitUtils.BY_DURATION_COMPARATOR, list);
            assertSame("Mismatched result for " + list, TimeUnit.DAYS, actual);
        }
    }

    private static Triplet<Integer,Integer,Integer> assertFirstDifference(
            String test, List<Integer> l1, List<Integer> l2, Integer v1, Integer v2, int offset) {
        Triplet<Integer,Integer,Integer>    result=ExtendedCollectionUtils.findFirstComparableDifference(l1, l2);
        assertNotNull(test + ": unexpected equality for " + l1 + "/" + l2, result);
        assertEquals(test + ": mismatched V1 for " + l1 + "/" + l2, v1, result.getV1());
        assertEquals(test + ": mismatched V2 for " + l1 + "/" + l2, v2, result.getV2());
        assertEquals(test + ": mismatched offset for " + l1 + "/" + l2, Integer.valueOf(offset), result.getV3());
        return result;
    }
}
