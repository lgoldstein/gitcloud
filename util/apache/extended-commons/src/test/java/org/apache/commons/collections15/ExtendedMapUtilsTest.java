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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author lgoldstein
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedMapUtilsTest extends AbstractTestSupport {
	public ExtendedMapUtilsTest() {
		super();
	}

    @Test
    public void testCompareMaps () {
        final List<? extends Number> values=makeCountingList(Long.SIZE);
        Map<String,Map<?,?>>    comparedMaps=
                Collections.unmodifiableMap(new TreeMap<String, Map<?,?>>(String.CASE_INSENSITIVE_ORDER) {
                        private static final long serialVersionUID = 2410007810257486072L;
                        {
                            put(HashMap.class.getSimpleName(), populateMap(new HashMap<Number,Number>(values.size()), values));
                            put(TreeMap.class.getSimpleName(), populateMap(new TreeMap<Number,Number>(), values));
                            put(LinkedHashMap.class.getSimpleName(), populateMap(new LinkedHashMap<Number,Number>(values.size()), values));
                        }
                    });

        for (int    index=0; index < Byte.SIZE; index++) {
            Collections.shuffle(values);
            // the only map whose order we can influence is the LinkedHashMap
            Map<?,?>  shuffledMap=populateMap(new LinkedHashMap<Number,Number>(values.size()), values);
            for (Map.Entry<String,Map<?,?>> ce : comparedMaps.entrySet()) {
                String      type=ce.getKey();
                Map<?,?>    orgMap=ce.getValue();

                assertTrue(type + ": Mismatched contents", ExtendedMapUtils.compareMaps(orgMap, shuffledMap));
                assertTrue(type + ": Mismatched reversed contents", ExtendedMapUtils.compareMaps(shuffledMap, orgMap));

                assertTrue(type + ": Mismatched containment", ExtendedMapUtils.containsAll(orgMap, shuffledMap));
                assertTrue(type + ": Mismatched reversed containment", ExtendedMapUtils.containsAll(shuffledMap, orgMap));
            }
        }
    }

    @Test
    public void testSize () {
        assertEquals("Mismatched null size", 0, ExtendedMapUtils.size(null));
        assertEquals("Mismatched empty list size", 0, ExtendedMapUtils.size(Collections.<Object,Object>emptyMap()));

        List<? extends Number> values=makeCountingList(Short.SIZE);
        Random        rnd=new Random(System.currentTimeMillis());
        for (int index=0; index < Long.SIZE; index++) {
            Collections.shuffle(values, rnd);
            int numItems=rnd.nextInt(values.size());
            if (numItems == 0)
                numItems = 1;
            List<? extends Number>   items=values.subList(0, numItems);
            assertEquals("Mismatched size for " + items,
                         numItems, ExtendedMapUtils.size(populateMap(new HashMap<Number,Number>(numItems), items)));
        }
    }

    @Test
    public void testPutIfNonNull () {
        Map<String,String>  map=new HashMap<String, String>() {
                private static final long serialVersionUID = -2377934973620640979L;
                {
                    put("key", "value");
                }
            };
       assertFalse("Null value mapped", ExtendedMapUtils.putIfNonNull(map, "key", null));
       assertEquals("Map size changed after null", 1, map.size());
       assertEquals("Mapped value changed after null", "value", map.get("key"));
       
       assertTrue("Non-null value not mapped", ExtendedMapUtils.putIfNonNull(map, "key", "anotherValue"));
       assertEquals("Map size changed after non-null", 1, map.size());
       assertEquals("Mapped value not changed after non-null", "anotherValue", map.get("key"));

       assertFalse("Null new key mapped", ExtendedMapUtils.putIfNonNull(map, "new-key", null));
       assertEquals("Map size changed after null new key", 1, map.size());
       assertNull("New key value exists", map.get("new-key"));

       assertTrue("New key value not mapped", ExtendedMapUtils.putIfNonNull(map, "new-key", "new-value"));
       assertEquals("Map size not changed after new key", 2, map.size());
       assertEquals("New key value mismatch", "new-value", map.get("new-key"));
    }

    @Test
    public void testRemoveAll () {
    	List<String>	values=Arrays.asList("foo", "bar", "baz",
    								String.valueOf(Math.random()), String.valueOf(System.currentTimeMillis()),
    								getClass().getSimpleName(), new Date().toString());
    	Map<String,String>	map=new TreeMap<String, String>();
    	for (String v : values) {
    		map.put(v, v);
    	}

    	List<String> 					toRemove=values.subList(0, values.size() / 2);
    	List<String>					toLeave=values.subList(toRemove.size(), values.size());
    	List<Map.Entry<String,String>>	removedEntries=ExtendedMapUtils.removeAll(map, toRemove);
    	assertEquals("Mismatched removed value size: " + removedEntries, toRemove.size(), ExtendedCollectionUtils.size(removedEntries));

    	List<String>	removed=ExtendedCollectionUtils.collectToList(removedEntries, ExtendedMapUtils.<String,String>entryValueExtractor());
    	if (!CollectionUtils.isEqualCollection(toRemove, removed)) {
    		fail("Mismatched removed values - expected: " + toRemove + ", actual: " + removed);
    	}

    	for (String key : toRemove) {
    		assertFalse("Key not removed: " + key, map.containsKey(key));
    	}

    	for (String key : toLeave) {
    		String	value=map.get(key);
    		assertEquals("Mismatched left key value", key, value);
    	}
    }

	@Test
    @SuppressWarnings("unchecked")
    public void testRemoveAllNoKeys () {
    	Map<String,String>	map=Collections.unmodifiableMap(new HashMap<String,String>());
    	@SuppressWarnings("rawtypes")
		Collection[]		colls={ null, Collections.<String>emptyList() };
    	for (Collection<String> toRemove : colls) {
    		List<Map.Entry<String,String>>	removedEntries=ExtendedMapUtils.removeAll(map, toRemove);
    		assertEquals("Mismatched result for " + toRemove, 0, ExtendedCollectionUtils.size(removedEntries));
    	}
    }

	@Test
    @SuppressWarnings("unchecked")
    public void testRemoveAllNoEntries () {
		Collection<String>	toRemove=Arrays.asList(getClass().getSimpleName(), "testRemoveAllNoEntries");
    	@SuppressWarnings("rawtypes")
		Map[]				maps={ null, Collections.<String,String>emptyMap() };
    	for (Map<String,String> m : maps) {
    		List<Map.Entry<String,String>>	removedEntries=ExtendedMapUtils.removeAll(m, toRemove);
    		assertEquals("Mismatched result for " + m, 0, ExtendedCollectionUtils.size(removedEntries));
    	}
	}

	@Test
	public void testFindFirstEntry() {
        final Map<Integer,Integer>  map=populateMap(new TreeMap<Integer,Integer>(), makeCountingList(Byte.SIZE));
        final Predicate<Map.Entry<Integer,Integer>> predicate=new Predicate<Map.Entry<Integer,Integer>>() {
                @Override
                public boolean evaluate(Entry<Integer, Integer> e) {
                    Number  v=e.getKey();
                    if ((v.intValue() & 0x01) == 0) {
                        return false;
                    } else {
                        return true;
                    }
                }
            };
        map.remove(Integer.valueOf(0));     // zero will make trouble for this test
        
        for (boolean    acceptOdd : new boolean[] { true, false }) {
            Map.Entry<Integer,Integer>  e=ExtendedMapUtils.findFirstEntry(map, predicate, acceptOdd);
            assertNotNull("No match found for predicate value=" + acceptOdd);
            
            Integer expected=acceptOdd ? Integer.valueOf(1) : Integer.valueOf(2);
            Integer actual=e.getKey();
            assertEquals("Mismatched results for predicate value=" + acceptOdd, expected, actual);
        }
	}

	@Test
	public void testMapSortedCollectionMultiValues() {
	    List<Integer>  values=makeCountingList(Short.SIZE);
	    Transformer<Integer,Boolean>   transformer=new Transformer<Integer, Boolean>() {
                @Override
                public Boolean transform(Integer n) {
                    if ((n.intValue() & 0x01) == 0) {
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                }
            };
       Map<Boolean,Set<Integer>>   result=
               ExtendedMapUtils.mapCollectionMultiValues(transformer, ExtendedSetUtils.<Integer>hashSetFactory(), values);
       assertEquals("Mismatched result size", 2, ExtendedMapUtils.size(result));
       for (Integer v : values) {
           Boolean      key=transformer.transform(v);
           Set<Integer> vSet=result.get(key);
           assertNotNull(v + " no mapped result found", vSet);
           assertTrue(v + " not found in mapping of key" + key + ": " + vSet, vSet.contains(v));
       }
	}

	@Test
	public void testMapSortedCollectionKeys() {
        List<Integer>  keys=makeCountingList(Short.SIZE);
        Transformer<Integer,Boolean>   transformer=new Transformer<Integer, Boolean>() {
            @Override
            public Boolean transform(Integer n) {
                if ((n.intValue() & 0x01) == 0) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            }
        };

        Collections.shuffle(keys);  // shuffle to ensure not in order to begin with
        SortedMap<Integer,Boolean>  result=ExtendedMapUtils.mapSortedCollectionKeys(false, transformer, keys);   
        assertEquals("Mismatched result size", keys.size(), ExtendedMapUtils.size(result));
        
        Collections.sort(keys); // the expected order of the sorted result
        for (Map.Entry<Integer,Boolean> re : result.entrySet()) {
            Integer expKey=keys.remove(0), actKey=re.getKey();
            assertSame("Mismatched key order", expKey, actKey);
            
            Boolean expValue=transformer.transform(expKey), actValue=re.getValue();
            assertEquals("Mismatched value for key=" + expKey, expValue, actValue);
        }
	}

	@Test
	public void testMap2MapTransform() {
	    Map<String,String> src=new HashMap<String,String>() {
                private static final long serialVersionUID = 1L;
    
                {
                    put("class", getClass().getName());
                    put("test", "testMap2mapTransform");
                    put("time", String.valueOf(new Date(System.currentTimeMillis())));
                }
    	    };
        final Map<String,String>  dst=
                ExtendedMapUtils.map2mapTransform(
                    false, src, ExtendedStringUtils.TO_UPPERCASE_XFORMER, ExtendedStringUtils.TO_LOWERCASE_XFORMER, new HashMap<String,String>(src.size()));
        ExtendedMapUtils.forAllEntriesDo(src, new Closure<Map.Entry<String,String>>() {
            @Override
            public void execute(Entry<String, String> input) {
                String  key=input.getKey(), value=input.getValue();
                String  expKey=ExtendedStringUtils.TO_UPPERCASE_XFORMER.transform(key);
                String  expValue=ExtendedStringUtils.TO_LOWERCASE_XFORMER.transform(value);
                String  actValue=dst.get(expKey);
                assertEquals(key + "/" + expKey + ": mismatched values", expValue, actValue);
            }
        });
	}

	@Test
	public void testCollectMapEntries() {
        Map<String,String> src=new HashMap<String,String>() {
            private static final long serialVersionUID = 1L;

            {
                put("class", getClass().getName());
                put("test", "testCollectMapEntries");
                put("time", String.valueOf(new Date(System.currentTimeMillis())));
            }
        };
        Collection<String>  expKeys=src.keySet();
        Collection<String>  actKeys=
                ExtendedMapUtils.collectMapEntries(false, src, ExtendedMapUtils.<String,String>entryKeyExtractor(), new ArrayList<String>(expKeys.size()));
        if (!CollectionUtils.isEqualCollection(expKeys, actKeys)) {
            fail("Mismatched key set: expected=" + expKeys + ", actual=" + actKeys);
        }

        Collection<String>  expValues=src.values();
        Collection<String>  actValues=
                ExtendedMapUtils.collectMapEntries(false, src, ExtendedMapUtils.<String,String>entryValueExtractor(), new ArrayList<String>(expKeys.size()));
        if (!CollectionUtils.isEqualCollection(expValues, actValues)) {
            fail("Mismatched values: expected=" + expValues + ", actual=" + actValues);
        }
	}

    @Test
    public void testFlip () {
        Map<String,Long>    src=new TreeMap<String, Long>() {
            private static final long serialVersionUID = -3686693573082540693L;

            {
                put("sysTime", Long.valueOf(7365L));
                put("nanoTime", Long.valueOf(3777347L));
            }
        };
        
        Map<Number,CharSequence>    dst=ExtendedMapUtils.flip(false, src, new HashMap<Number,CharSequence>(src.size()));
        assertEquals("Mismatched size", src.size(), dst.size());
        for (Map.Entry<String,Long> se : src.entrySet()) {
            String          expected=se.getKey();
            Long            value=se.getValue();
            CharSequence    actual=dst.remove(value);
            assertSame("Mismatched key for value=" + value, expected, actual);
        }
    }

    @Test
    public void testFlipNullOrEmpty () {
        Map<Object,Object>  dst=Collections.unmodifiableMap(new HashMap<Object,Object>());
        assertSame("Mismatached instance for null source", dst, ExtendedMapUtils.flip(false, null, dst));
        assertSame("Mismatached instance for empty source", dst, ExtendedMapUtils.flip(false, Collections.<Object,Object>emptyMap(), dst));
    }

    static final List<Integer> makeCountingList(int numMembers) {
    	assertTrue("Non-positive members count: " + numMembers, numMembers > 0);
    	List<Integer>	list=new ArrayList<Integer>(numMembers);
    	for (int index=0; index < numMembers; index++) {
    		list.add(Integer.valueOf(index));
    	}
    	
    	return list;
    }

    static final <N extends Number, M extends Map<N,N>> M populateMap (M map, Collection<? extends N> values) {
        for (N n : values) {
            assertNull("Multiple mapped values for " + n, map.put(n, n));
        }

        assertTrue(map.getClass().getSimpleName() + " not equal to itself", ExtendedMapUtils.compareMaps(map, map));
        assertTrue(map.getClass().getSimpleName() + " does not contain itself", ExtendedMapUtils.containsAll(map, map));
        return map;
    }
}
