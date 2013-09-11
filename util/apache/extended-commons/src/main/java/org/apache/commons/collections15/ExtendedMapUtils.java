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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.ExtendedObjectUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author lgoldstein
 */
public class ExtendedMapUtils extends MapUtils {
	public ExtendedMapUtils() {
		super();
	}

	public static final boolean isEmpty(Map<?,?> map) {
		return (map == null) || map.isEmpty();
	}

	public static final int size(Map<?,?> map) {
		return (map == null) ? 0 : map.size();
	}

	/**
	 * @param map The {@link Map} to be hashed - may be <code>null</code>
	 * @return The result of hashing each entry via {@link #entryHashCode(Entry)}
	 */
	public static final int hashCode(Map<?,?> map) {
	    return entriesHashCode((map == null) ? null : map.entrySet());
	}

	/**
	 * @param eSet The {@link Collection} of {@link java.util.Map.Entry}-ies to hash -
	 * may be <code>null</code>
	 * @return The result of hashing each entry via {@link #entryHashCode(Entry)}
	 */
	public static final int entriesHashCode(Collection<? extends Map.Entry<?,?>> eSet) {
	    if (ExtendedCollectionUtils.isEmpty(eSet)) {
	        return 0;
	    }
	    
	    int hash=1;
	    for (Map.Entry<?,?> e : eSet) {
	        hash = 31 * hash + entryHashCode(e);
	    }
	    
	    return hash;
	}

	/**
	 * @param e The {@link java.util.Map.Entry} to hash - may be <code>null</code>
	 * @return The result of invoking {@link ObjectUtils#hashCode(Object)} on
	 * the key and value respectively
	 */
	public static final int entryHashCode(Map.Entry<?,?> e) {
	    if (e == null) {
	        return 0;
	    } else {
	        return 31 * ObjectUtils.hashCode(e.getKey()) + ObjectUtils.hashCode(e.getValue()); 
	    }
	}

    /**
     * @param map The {@link Map} to be hashed - may be <code>null</code>
     * @return The result of hashing each entry via {@link #entryDeepHash(Entry)}
     */
    public static final int deepHash(Map<?,?> map) {
        return entriesDeepHash((map == null) ? null : map.entrySet());
    }

    /**
     * @param eSet The {@link Collection} of {@link java.util.Map.Entry}-ies to hash -
     * may be <code>null</code>
     * @return The result of hashing each entry via {@link #entryDeepHash(Entry)}
     */
    public static final int entriesDeepHash(Collection<? extends Map.Entry<?,?>> eSet) {
        if (ExtendedCollectionUtils.isEmpty(eSet)) {
            return 0;
        }
        
        int hash=1;
        for (Map.Entry<?,?> e : eSet) {
            hash = 31 * hash + entryDeepHash(e);
        }
        
        return hash;
    }

    /**
     * @param e The {@link java.util.Map.Entry} to hash - may be <code>null</code>
     * @return The result of invoking {@link ExtendedObjectUtils#deepHash(Object)} on
     * the key and value respectively
     */
    public static final int entryDeepHash(Map.Entry<?,?> e) {
        if (e == null) {
            return 0;
        } else {
            return 31 * ExtendedObjectUtils.deepHash(e.getKey()) + ExtendedObjectUtils.deepHash(e.getValue()); 
        }
    }
	
	@SuppressWarnings("unchecked")
	public static final <K,V> SortedMap<K,V> emptySortedMap() {
		return EMPTY_SORTED_MAP;
	}

    /**
     * @param m1 The 1st {@link Map} to compare
     * @param m2 The 2nd {@link Map} to compare
     * @return <code>true</code> if both maps contain the same keys and values
     * @see #compareMaps(Map, Map, Comparator)
     */
    public static final boolean compareMaps (Map<?,?> m1, Map<?,?> m2) {
        return compareMaps(m1, m2, null);
    }

    /**
     * @param m1 The 1st {@link Map} to compare
     * @param m2 The 2nd {@link Map} to compare
     * @param valuesComp The {@link Comparator} to use to check for mapped
     * values equality - if <code>null</code> then {@link Object#equals(Object)}
     * is used
     * @return <code>true</code> if both maps contain the same keys and values
     */
    public static final <K,V> boolean compareMaps(Map<? extends K,? extends V> m1,
                                                  Map<? extends K,? extends V> m2,
                                                  Comparator<? super V> valuesComp) {
        if (m1 == m2)
            return true;
        if ((m1 == null) || (m2 == null))
            return false;
        if (m1.size() != m2.size())
            return false;
        
        return containsAll(m1, m2, valuesComp) && containsAll(m2, m1, valuesComp);
    }

    /**
     * @param m The &quot;master&quot; {@link Map}
     * @param subMap The {@link Map} to check if contained in the master one
     * @return <code>true</code> if the tested map is contained in the master
     * one both keys and value-wise
     * @see #containsAll(Map, Map, Comparator)
     */
    public static final boolean containsAll (Map<?,?> m, Map<?,?> subMap) {
        return containsAll(m, subMap, null);
    }

    /**
     * @param m The &quot;master&quot; {@link Map}
     * @param subMap The {@link Map} to check if contained in the master one
     * @param valuesComp The {@link Comparator} to use to check for mapped
     * values equality - if <code>null</code> then {@link Object#equals(Object)}
     * is used
     * @return <code>true</code> if the tested map is contained in the master
     * one both keys and value-wise
     */
    public static final <K,V> boolean containsAll(Map<? extends K,? extends V> m,
                                                  Map<? extends K,? extends V> subMap,
                                                  Comparator<? super V> valuesComp) {
        if (m == subMap)
            return true;
        if ((subMap == null) || subMap.isEmpty())
            return true;
        if ((m == null) || m.isEmpty())
            return false;

        for (Map.Entry<? extends K,? extends V> subEntry : subMap.entrySet()) {
            K  subKey=subEntry.getKey();
            V   subValue=subEntry.getValue();
            /*
             * If the associated value is null we need to distinguish between
             * it and the fact that the key does not exist in the main map
             */
            if (subValue == null) {
                if (!m.containsKey(subKey)) {
                    return false;
                }
                
                if (m.get(subKey) != null) {
                    return false;
                }
            } else {
                V  value=m.get(subKey);
                if (valuesComp == null) {
                    if (!subValue.equals(value)) {
                        return false;
                    }
                } else {
                    if (valuesComp.compare(value, subValue) != 0) {
                        return false;
                    }
                }
            }
        }
        
        return true;

    }
    /**
     * @param map The {@link Map} to put the value into
     * @param key The mapping key
     * @param value The value
     * @return <code>true</code> if the value is non-<code>null</code> and has been
     * put in the map. <code>false</code> if the value is <code>null</code>, in which
     * case the value is <U>not</U> put in the map.
     */
    public static <K,V> boolean putIfNonNull (Map<K,V> map, K key, V value) {
        if (value == null) {
            return false;
        }
        
        map.put(key, value);
        return true;
    }

	/**
	 * @param comp The {@link Comparator} to use to create the {@link SortedMap}
	 * @return A {@link SortedMap} that uses the provided comparator
	 * @throws IllegalArgumentException If no comparator provided
	 */
	public static final <K,V> SortedMap<K,V> sortedMap(Comparator<? super K> comp) {
		if (comp == null) {
			throw new IllegalArgumentException("No comparator provided"); 
		}

		return new TreeMap<K,V>(comp);
	}

	/**
	 * Creates a {@link SortedMap} from a {@link Collection} of values using a
	 * {@link Transformer} to generate keys for the mapped values, using the
	 * &quot;natural&quot; {@link Comparator} to generate the sorted map
	 * @param ignoreDuplicates If <code>false</code> and a value is mapped to
	 * an existing entry then generates an exception
	 * @param transformer The {@link Transformer} to use to generate keys from
	 * values
	 * @param values The {@link Collection} of {@link Comparable} values to be mapped
	 * - ignored if <code>null</code>/empty (i.e., an empty map is returned)
	 * @return The generated (sorted) map
	 * @throws IllegalArgumentException if no map, transformer or comparator provided
	 * @throws IllegalStateException if a value is mapped to an existing entry
	 * and <code>ignoreDuplicates</code> is <code>false</code>
	 * @see #mapSortedCollectionValues(boolean, Transformer, Comparator, Collection)
	 */
	public static final <K extends Comparable<K>,V> SortedMap<K,V> mapSortedCollectionValues(
			boolean ignoreDuplicates, Transformer<? super V,? extends K> transformer, Collection<? extends V> values) {
		return mapSortedCollectionValues(ignoreDuplicates, transformer, ExtendedComparatorUtils.<K>comparableComparator(), values);
	}

	/**
	 * Creates a {@link SortedMap} from a {@link Collection} of values using a
	 * {@link Transformer} to generate keys for the mapped values and a given
	 * {@link Comparator} to generate the sorted map
	 * @param ignoreDuplicates If <code>false</code> and a value is mapped to
	 * an existing entry then generates an exception
	 * @param transformer The {@link Transformer} to use to generate keys from
	 * values
	 * @param comp The {@link Comparator} to use to generate the sorted map
	 * @param values The {@link Collection} of values to be mapped - ignored
	 * if <code>null</code>/empty (i.e., an empty map is returned)
	 * @return The generated (sorted) map
	 * @throws IllegalArgumentException if no map, transformer or comparator provided
	 * @throws IllegalStateException if a value is mapped to an existing entry
	 * and <code>ignoreDuplicates</code> is <code>false</code>
	 * @see #updateCollectionValuesMap(Map, boolean, Transformer, Collection)
	 */
	public static final <K,V> SortedMap<K,V> mapSortedCollectionValues(
			boolean ignoreDuplicates, Transformer<? super V,? extends K> transformer, Comparator<? super K> comp, Collection<? extends V> values) {
		if (ExtendedCollectionUtils.isEmpty(values)) {
			// need to check this only here since these are checked by the 'else' invoked code
			if ((transformer == null) || (comp == null)) {
				throw new IllegalArgumentException("Missing transformer or comparator");
			}

			return emptySortedMap();
		} else {
			return updateCollectionValuesMap(ExtendedMapUtils.<K,V>sortedMap(comp), ignoreDuplicates, transformer, values);
		}
	}

	/**
	 * Creates a {@link Map} from a {@link Collection} of values using a
	 * {@link Transformer} to generate keys for the mapped values
	 * @param ignoreDuplicates If <code>false</code> and a value is mapped to
	 * an existing entry then generates an exception
	 * @param transformer The {@link Transformer} to use to generate keys from
	 * values 
	 * @param values The {@link Collection} of values to be mapped - ignored
	 * if <code>null</code>/empty (i.e., an empty map is returned)
	 * @return The generated map
	 * @throws IllegalArgumentException if no map or transformer provided
	 * @throws IllegalStateException if a value is mapped to an existing entry
	 * and <code>ignoreDuplicates</code> is <code>false</code>
	 * @see #updateCollectionValuesMap(Map, boolean, Transformer, Collection)
	 */
	public static final <K,V> Map<K,V> mapCollectionValues(
			boolean ignoreDuplicates, Transformer<? super V,? extends K> transformer, Collection<? extends V> values) {
		if (ExtendedCollectionUtils.isEmpty(values)) {
			return Collections.emptyMap();
		} else {
			return updateCollectionValuesMap(new HashMap<K,V>(values.size()), ignoreDuplicates, transformer, values);
		}
	}
	
	/**
	 * @param map The {@link Map} to be updated
	 * @param ignoreDuplicates If <code>false</code> and a value is mapped to
	 * an existing entry then generates an exception
	 * @param transformer The {@link Transformer} to use to generate keys from
	 * values 
	 * @param values The {@link Collection} of values to be mapped - ignored
	 * if <code>null</code>/empty
	 * @return The updated map
	 * @throws IllegalArgumentException if no map or transformer provided
	 * @throws IllegalStateException if a value is mapped to an existing entry
	 * and <code>ignoreDuplicates</code> is <code>false</code>
	 */
	public static final <K,V,M extends Map<K,V>> M updateCollectionValuesMap(
			M map, boolean ignoreDuplicates, Transformer<? super V,? extends K> transformer, Collection<? extends V> values) {
		if ((map == null) || (transformer == null)) {
			throw new IllegalArgumentException("Missing map or transformer");
		}
		
		if (ExtendedCollectionUtils.isEmpty(values)) {
			return map;
		}
		
		for (V v : values) {
			K	k=transformer.transform(v);
			V	prev=map.put(k, v);
			if (prev == null) {
				continue;
			}
			
			if (!ignoreDuplicates) {
				throw new IllegalStateException("Multiple mapped value for key=" + k + ": " + v + ", " + prev);
			}
		}
		
		return map;
	}

    /**
     * @param transformer The {@link Transformer} to use to generate keys from
     * values
     * @param factory The {@link Factory} instance that create an new (empty)
     * {@link Collection} for a mapped value that is encountered for the first
     * time
     * @param values The {@link Collection} of values to be mapped - ignored
     * if <code>null</code>/empty
     * @return A {@link SortedMap} where values mapped to the same key are collected
     * inside a {@link Collection} whose type is determined by the provided
     * factory instance
     * @throws IllegalArgumentException if no map or transformer provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     */
    public static final <K extends Comparable<K>,V,C extends Collection<V>> SortedMap<K,C> mapSortedCollectionMultiValues(
            Transformer<? super V,? extends K> transformer, Factory<? extends C> factory, Collection<? extends V> values) {
        return mapSortedCollectionMultiValues(transformer, ExtendedComparatorUtils.<K>comparableComparator(), factory, values);
    }

    /**
     * @param transformer The {@link Transformer} to use to generate keys from
     * values
     * @param comp The {@link Comparator} used to determine if same key 
     * @param factory The {@link Factory} instance that create an new (empty)
     * {@link Collection} for a mapped value that is encountered for the first
     * time
     * @param values The {@link Collection} of values to be mapped - ignored
     * if <code>null</code>/empty
     * @return A {@link SortedMap} where values mapped to the same key are collected
     * inside a {@link Collection} whose type is determined by the provided
     * factory instance
     * @throws IllegalArgumentException if no map or transformer provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     */
    public static final <K,V,C extends Collection<V>> SortedMap<K,C> mapSortedCollectionMultiValues(
            Transformer<? super V,? extends K> transformer, Comparator<? super K> comp, Factory<? extends C> factory, Collection<? extends V> values) {
        if (ExtendedCollectionUtils.isEmpty(values)) {
            // need to check this only here since these are checked by the 'else' invoked code
            if ((transformer == null) || (comp == null) || (factory == null)) {
                throw new IllegalArgumentException("Missing transformer, comparator or factory");
            }

            return emptySortedMap();
        } else {
            return updateCollectionMultiValuesMap(ExtendedMapUtils.<K,C>sortedMap(comp), transformer, factory, values);
        }
    }

    /**
     * @param transformer The {@link Transformer} to use to generate keys from
     * values 
     * @param factory The {@link Factory} instance that create an new (empty)
     * {@link Collection} for a mapped value that is encountered for the first
     * time
     * @param values The {@link Collection} of values to be mapped - ignored
     * if <code>null</code>/empty
     * @return A {@link Map} where values mapped to the same key are collected
     * inside a {@link Collection} whose type is determined by the provided
     * factory instance
     * @throws IllegalArgumentException if no map or transformer provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     */
	public static final <K,V,C extends Collection<V>> Map<K,C> mapCollectionMultiValues(
            Transformer<? super V,? extends K> transformer, Factory<? extends C> factory, Collection<? extends V> values) {
	    if (ExtendedCollectionUtils.isEmpty(values)) {
            if ((transformer == null) || (factory == null)) {
                throw new IllegalArgumentException("Missing transformer, or factory");
            }
            return Collections.emptyMap();
        } else {
            return updateCollectionMultiValuesMap(new HashMap<K,C>(values.size()), transformer, factory, values);
        }
    }

	/**
     * @param map The {@link Map} to be updated that contains keys mapped to
     * {@link Collection} of values that are mapped to the same key
     * @param transformer The {@link Transformer} to use to generate keys from
     * values 
     * @param factory The {@link Factory} instance that create an new (empty)
     * {@link Collection} for a mapped value that is encountered for the first
     * time
     * @param values The {@link Collection} of values to be mapped - ignored
     * if <code>null</code>/empty
     * @return The updated map
     * @throws IllegalArgumentException if no map or transformer provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     */
    public static final <K,V,C extends Collection<V>,M extends Map<K,C>> M updateCollectionMultiValuesMap(
            M map, Transformer<? super V,? extends K> transformer, Factory<? extends C> factory, Collection<? extends V> values) {
        if ((map == null) || (transformer == null) || (factory == null)) {
            throw new IllegalArgumentException("Missing map, transformer or factory");
        }
        
        if (ExtendedCollectionUtils.isEmpty(values)) {
            return map;
        }
        
        for (V v : values) {
            K   k=transformer.transform(v);
            C   prev=map.get(k);
            if (prev == null) {
                prev = factory.create();
                map.put(k, prev);
            }
            
            prev.add(v);
        }
        
        return map;
    }

    /**
     * @param map The {@link Map} whose entries are to be evaluated
     * @param predicate The {@link Predicate} to use for evaluation
     * @return The <U>first</U> {@link java.util.Map.Entry} whose {@link Predicate#evaluate(Object)}
     * result returned <code>true</code> - <code>null</code> if no match found
     * @see #findFirstEntry(Map, Predicate, boolean) 
     */
	public static final <K,V> Map.Entry<K,V> findFirstAcceptedEntry(Map<K,V> map, Predicate<Map.Entry<K,V>> predicate) {
	    return findFirstEntry(map, predicate, true);
	}

    /**
     * @param map The {@link Map} whose entries are to be evaluated
     * @param predicate The {@link Predicate} to use for evaluation
     * @return The <U>first</U> {@link java.util.Map.Entry} whose {@link Predicate#evaluate(Object)}
     * result returned <code>false</code> - <code>null</code> if no match found
     * @see #findFirstEntry(Map, Predicate, boolean) 
     */
	public static final <K,V> Map.Entry<K,V> findFirstRejectedEntry(Map<K,V> map, Predicate<Map.Entry<K,V>> predicate) {
        return findFirstEntry(map, predicate, false);
	}

    /**
     * @param map The {@link Map} whose entries are to be evaluated
     * @param predicate The {@link Predicate} to use for evaluation
     * @param predicateValue The required evaluation result
     * @return The <U>first</U> {@link java.util.Map.Entry} whose {@link Predicate#evaluate(Object)}
     * result matches the required value - <code>null</code> if no match found 
     */
    public static final <K,V> Map.Entry<K,V> findFirstEntry(Map<K,V> map, Predicate<Map.Entry<K,V>> predicate, boolean predicateValue) {
        if (predicate == null) {
            throw new IllegalArgumentException("No predicate provided");
        }

        if (isEmpty(map)) {
            return null;
        }

        for (Map.Entry<K,V> e : map.entrySet()) {
            if (predicate.evaluate(e) == predicateValue) {
                return e;
            }
        }
        
        return null;    // no match found
    }

    /**
     * Removes all entries for which a {@link Predicate#evaluate(Object)} returned
     * <code>true</code> on the map keys
     * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
     * @param predicate The {@link Predicate} to use to evaluate the keys
     * @return A {@link List} of all the the {@link java.util.Map.Entry}-ies that have been
     * removed
     * @throws IllegalArgumentException if no predicate instance provided
     * @see #filterKeys(Map, Predicate, boolean)
     */
    public static final <K,V,M extends Map<K,V>> List<Map.Entry<K,V>> filterAcceptedValues(M map, Predicate<? super V> predicate) {
        return filterValues(map, predicate, true);
    }

    /**
     * Removes all entries for which a {@link Predicate#evaluate(Object)} returned
     * <code>false</code> on the map keys
     * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
     * @param predicate The {@link Predicate} to use to evaluate the keys
     * @return A {@link List} of all the the {@link java.util.Map.Entry}-ies that have been
     * removed
     * @throws IllegalArgumentException if no predicate instance provided
     * @see #filterKeys(Map, Predicate, boolean)
     */
    public static final <K,V,M extends Map<K,V>> List<Map.Entry<K,V>> filterRejectedValues(M map, Predicate<? super V> predicate) {
        return filterValues(map, predicate, false);
    }

    /**
     * Removes all entries for which a {@link Predicate#evaluate(Object)} returned
     * a required value on the mapped value
     * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
     * @param predicate The {@link Predicate} to use to evaluate the values
     * @param predicateValue The {@link Predicate#evaluate(Object)} result that
     * signals that an entry should be removed - <code>true</code>=remove all
     * entries accepted by the predicate, <code>false</code>=remove the rejected
     * ones
     * @return A {@link List} of all the the {@link java.util.Map.Entry}-ies that have been
     * removed
     * @throws IllegalArgumentException if no predicate instance provided
     */
    public static final <K,V,M extends Map<K,V>> List<Map.Entry<K,V>> filterValues(M map, final Predicate<? super V> predicate, boolean predicateValue) {
        if (predicate == null) {
            throw new IllegalArgumentException("No predicate provided");
        }

        return filterEntries(map, new Predicate<Map.Entry<K,V>>() {
            @Override
            public boolean evaluate(Entry<K, V> e) {
                return predicate.evaluate(e.getValue());
            }
        }, predicateValue);
    }

    /**
     * Removes all entries for which a {@link Predicate#evaluate(Object)} returned
     * <code>true</code> on the map keys
     * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
     * @param predicate The {@link Predicate} to use to evaluate the keys
     * @return A {@link List} of all the the {@link java.util.Map.Entry}-ies that have been
     * removed
     * @throws IllegalArgumentException if no predicate instance provided
     * @see #filterKeys(Map, Predicate, boolean)
     */
    public static final <K,V,M extends Map<K,V>> List<Map.Entry<K,V>> filterAcceptedKeys(M map, Predicate<? super K> predicate) {
        return filterKeys(map, predicate, true);
    }

    /**
     * Removes all entries for which a {@link Predicate#evaluate(Object)} returned
     * <code>false</code> on the map keys
     * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
     * @param predicate The {@link Predicate} to use to evaluate the keys
     * @return A {@link List} of all the the {@link java.util.Map.Entry}-ies that have been
     * removed
     * @throws IllegalArgumentException if no predicate instance provided
     * @see #filterKeys(Map, Predicate, boolean)
     */
    public static final <K,V,M extends Map<K,V>> List<Map.Entry<K,V>> filterRejectedKeys(M map, Predicate<? super K> predicate) {
        return filterKeys(map, predicate, false);
    }

    /**
     * Removes all entries for which a {@link Predicate#evaluate(Object)} returned
     * a required value on the mapped key
     * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
     * @param predicate The {@link Predicate} to use to evaluate the keys
     * @param predicateValue The {@link Predicate#evaluate(Object)} result that
     * signals that an entry should be removed - <code>true</code>=remove all
     * entries accepted by the predicate, <code>false</code>=remove the rejected
     * ones
     * @return A {@link List} of all the the {@link java.util.Map.Entry}-ies that have been
     * removed
     * @throws IllegalArgumentException if no predicate instance provided
     */
    public static final <K,V,M extends Map<K,V>> List<Map.Entry<K,V>> filterKeys(M map, final Predicate<? super K> predicate, boolean predicateValue) {
        if (predicate == null) {
            throw new IllegalArgumentException("No predicate provided");
        }

        return filterEntries(map, new Predicate<Map.Entry<K,V>>() {
            @Override
            public boolean evaluate(Entry<K, V> e) {
                return predicate.evaluate(e.getKey());
            }
        }, predicateValue);
    }

	/**
	 * Removes all entries for which a {@link Predicate#evaluate(Object)} returned
	 * <code>true</code> on the map entries
	 * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
	 * @param predicate The {@link Predicate} to use to evaluate the {@link java.util.Map.Entry}-ies
	 * @return A {@link List} of all the the {@link java.util.Map.Entry}-ies that have been
	 * removed
	 * @throws IllegalArgumentException if no predicate instance provided
	 * @see #filterEntries(Map, Predicate, boolean)
	 */
	public static final <K,V,M extends Map<K,V>> List<Map.Entry<K,V>> filterAcceptedEntries(M map, Predicate<Map.Entry<K,V>> predicate) {
		return filterEntries(map, predicate, true);
	}

	/**
	 * Removes all entries for which a {@link Predicate#evaluate(Object)} returned
	 * <code>false</code> on the map entries
	 * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
	 * @param predicate The {@link Predicate} to use to evaluate the {@link java.util.Map.Entry}-ies
	 * @return A {@link List} of all the the {@link java.util.Map.Entry}-ies that have been
	 * removed
	 * @throws IllegalArgumentException if no predicate instance provided
	 * @see #filterEntries(Map, Predicate, boolean)
	 */
	public static final <K,V,M extends Map<K,V>> List<Map.Entry<K,V>> filterRejectedEntries(M map, Predicate<Map.Entry<K,V>> predicate) {
		return filterEntries(map, predicate, false);
	}

	/**
	 * Removes all entries for which a {@link Predicate#evaluate(Object)} returned
	 * a required value on the mapped entry
	 * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
	 * @param predicate The {@link Predicate} to use to evaluate the {@link java.util.Map.Entry}-ies
	 * @param predicateValue The {@link Predicate#evaluate(Object)} result that
	 * signals that an entry should be removed - <code>true</code>=remove all
	 * entries accepted by the predicate, <code>false</code>=remove the rejected
	 * ones
	 * @return A {@link List} of all the the {@link java.util.Map.Entry}-ies that have been
	 * removed
	 * @throws IllegalArgumentException if no predicate instance provided
	 * @see #removeAll(Map, Collection)
	 */
	public static final <K,V,M extends Map<K,V>> List<Map.Entry<K,V>> filterEntries(M map, Predicate<Map.Entry<K,V>> predicate, boolean predicateValue) {
		if (predicate == null) {
			throw new IllegalArgumentException("No predicated provided");
		}

		if (isEmpty(map)) {
			return Collections.emptyList();
		}
		
		Collection<K>	filteredKeys=null;
		for (Map.Entry<K,V> e : map.entrySet()) {
			if (predicate.evaluate(e) != predicateValue) {
				continue;
			}
			
			if (filteredKeys == null) {
				filteredKeys = new ArrayList<K>(map.size());
			}
			
			filteredKeys.add(e.getKey());
		}
		
		return removeAll(map, filteredKeys);
	}
	
	/**
	 * Removes all entries mapped to the specified keys
	 * @param map The {@link Map} to be updated - ignored if <code>null</code>/empty
	 * @param keys The {@link Collection} of keys to be removed - ignored if
	 * <code>null</code>/empty
	 * @return A {@link List} of all the the {@link java.util.Map.Entry}-ies that have been
	 * removed. <B>Note:</B> if the {@link Map#remove(Object)} call returns
	 * <code>null</code> then it is not reported as a removed entry
	 */
	public static final <K,V> List<Map.Entry<K,V>> removeAll(Map<K,? extends V> map, Collection<? extends K> keys) {
		if (isEmpty(map) || ExtendedCollectionUtils.isEmpty(keys)) {
			return Collections.emptyList();
		}
		
		List<Map.Entry<K,V>>	result=new ArrayList<Map.Entry<K,V>>(keys.size());
		for (K key : keys) {
			V value=map.remove(key);
			if (value == null) {
				continue;
			}
			
			result.add(Pair.of(key, value));
		}
		
		return result;
	}
	
	/**
	 * Invokes a {@link Closure} for each key in a map
	 * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
	 * @param closure The {@link Closure} to be invoked for each {@link java.util.Map.Entry}
	 * @throws IllegalArgumentException if no closure provided
	 */
	public static final <K> void forAllKeysDo(Map<? extends K,?> map, Closure<? super K> closure) {
		if (closure == null) {
			throw new IllegalArgumentException("No closure provided");
		}
		
		if (isEmpty(map)) {
			return;	// debug breakpoint
		}

		CollectionUtils.forAllDo(map.keySet(), closure);
	}

	/**
	 * Invokes a {@link Closure} for each value in a map
	 * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
	 * @param closure The {@link Closure} to be invoked for each value
	 * @throws IllegalArgumentException if no closure provided
	 */
	public static final <V> void forAllValuesDo(Map<?,? extends V> map, Closure<? super V> closure) {
		if (closure == null) {
			throw new IllegalArgumentException("No closure provided");
		}
		
		if (isEmpty(map)) {
			return;	// debug breakpoint
		}

		CollectionUtils.forAllDo(map.values(), closure);
	}

	/**
	 * Invokes a {@link Closure} for each entry in a map
	 * @param map The {@link Map} to be scanned - ignored if <code>null</code>/empty
	 * @param closure The {@link Closure} to be invoked for each key
	 * @throws IllegalArgumentException if no closure provided
	 */
	public static final <K,V> void forAllEntriesDo(Map<K,V> map, Closure<? super Map.Entry<K,V>> closure) {
		if (closure == null) {
			throw new IllegalArgumentException("No closure provided");
		}
		
		if (isEmpty(map)) {
			return;	// debug breakpoint
		}

		CollectionUtils.forAllDo(map.entrySet(), closure);
	}

	private static final Transformer<Map.Entry<?,?>,Object> ENTRY_VALUE_EXTRACTOR=new Transformer<Map.Entry<?,?>,Object>() {
			@Override
			public Object transform(Entry<?, ?> e) {
				if (e == null) {
					return null;
				} else {
					return e.getValue();
				}
			}
		};
	/**
	 * @return A {@link Transformer} that &quot;transforms&quot; a
	 * {@link java.util.Map.Entry} into its {@link java.util.Map.Entry#getValue()} invocation
	 * result
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <K,V> Transformer<Map.Entry<? extends K,? extends V>,V> entryValueExtractor() {
		return (Transformer) ENTRY_VALUE_EXTRACTOR;
	}

	private static final Transformer<Map.Entry<?,?>,Object> ENTRY_KEY_EXTRACTOR=new Transformer<Map.Entry<?,?>,Object>() {
			@Override
			public Object transform(Entry<?, ?> e) {
				if (e == null) {
					return null;
				} else {
					return e.getKey();
				}
			}
		};
	/**
	 * @return A {@link Transformer} that &quot;transforms&quot; a
	 * {@link java.util.Map.Entry} into its {@link java.util.Map.Entry#getKey()} invocation
	 * result
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <K,V> Transformer<Map.Entry<? extends K,? extends V>,K> entryKeyExtractor() {
		return (Transformer) ENTRY_KEY_EXTRACTOR;
	}
	
    /**
     * Reverses the keys and values from one {@link Map} to another - i.e.,
     * the source map values become the keys, and its keys become the values 
     * @param ignoreDuplicates If <code>false</code> and a mapped key/value
     * already exists in the destination {@link Map} then throws an {@link IllegalStateException}
     * @param src The source map
     * @param dst The destination map
     * @return Same instance as the destination map
     */
    public static final <K,V,M extends Map<? super V,? super K>> M flip (
            boolean ignoreDuplicates, final Map<? extends K,? extends V> src, M dst) {
        if (dst == null) {
            throw new IllegalArgumentException("Missing destination map");
        }

        if (isEmpty(src)) {
            return dst;
        }

        for (Map.Entry<? extends K,? extends V> se : src.entrySet()) {
            K       key=se.getKey();
            V       value=se.getValue();
            Object  prev=dst.put(value, key);
            if ((prev != null) && (!ignoreDuplicates)) {
                throw new IllegalStateException("Multiple mapped value for " + value + "=>" + key + " mapping: "
                                              + " current=" + key + ", previous=" + prev);
            }
        }

        return dst;
    }

	/**
	 * Transforms a source map into a destination one
	 * @param ignoreDuplicates If <code>false</code> and a mapped key/value
	 * already exists in the destination {@link Map} then throws an {@link IllegalStateException}
	 * @param src The source {@link Map} - may be <code>null</code>/empty
	 * @param keyTransformer The {@link Transformer} for the keys
	 * @param valueTransformer The {@link Transformer} for the values
	 * @param dst The destination {@link Map}
	 * @return The updated destination map
	 * @throws IllegalArgumentException if no transformers or destination map provided
	 * @throws IllegalStateException if duplicate mapping found in destination
	 * map and <code>ignoreDuplicates</code> is <code>false</code>
	 */
	public static final <K1,V1,M1 extends Map<K1,V1>,K2,V2,M2 extends Map<K2,V2>> M2 map2mapTransform(
	                final boolean ignoreDuplicates,
	                final M1 src,
	                final Transformer<? super K1,? extends K2> keyTransformer,
	                final Transformer<? super V1,? extends V2> valueTransformer,
	                final M2 dst) {
	    if ((keyTransformer == null) || (valueTransformer == null)) {
	        throw new IllegalArgumentException("Missing key/value transformer(s)");
	    }

	    if (dst == null) {
            throw new IllegalArgumentException("Missing destination map");
	    }

	    if (isEmpty(src)) {
	        return dst;
	    }
	    
	    forAllEntriesDo(src, new Closure<Map.Entry<K1,V1>>() {
                @Override
                public void execute(Entry<K1, V1> e) {
                    K1  k1=e.getKey();
                    V1  v1=e.getValue();
                    K2  k2=keyTransformer.transform(k1);
                    V2  v2=valueTransformer.transform(v1);
                    V2  prev=dst.put(k2, v2);
                    if ((prev != null) && (!ignoreDuplicates)) {
                        throw new IllegalStateException("Multiple mapped value for " + k1 + "=>" + k2 + " mapping: "
                                                      + " current=" + v2 + ", previous=" + prev);
                    }
                }
    	    });
	    return dst;
	}

	/**
	 * Creates a {@link Collection} of items out of a {@link Map}
	 * @param ignoreDuplicates If <code>false</code> and the transformed item
	 * already exists in the collection the throw an {@link IllegalStateException}
	 * @param map The source {@link Map} to be transformed - ignored if
	 * <code>null</code>/empty
	 * @param transformer The {@link Transformer} to use in order to transform
     * the map entries into collected items. <B>Note:</B> if returns <code>null</code>
     * for a transformed entry then the item is not collected
	 * @param targetCollection The target collection to the transformed items
	 * are added
	 * @return The (updated) input collection
	 */
	public static final <K,V,E,C extends Collection<? super E>> C collectMapEntries(
	        final boolean ignoreDuplicates,
	        Map<? extends K,? extends V> map,
	        final Transformer<Map.Entry<? extends K,? extends V>,E> transformer,
	        final C targetCollection) {
	    if ((transformer == null) || (targetCollection == null)) {
	        throw new IllegalArgumentException("Missing transformer or target collection");
	    }
	    
	    if (isEmpty(map)) {
	        return targetCollection;
	    }

	    forAllEntriesDo(map, new Closure<Map.Entry<? extends K, ? extends V>>() {
            @Override
            public void execute(Entry<? extends K, ? extends V> entry) {
                E   item=transformer.transform(entry);
                if (item == null) {
                    return;
                }
                
                boolean modified=targetCollection.add(item);
                if (modified) {
                    return;
                }
                
                if (!ignoreDuplicates) {    // collection not modified ==> i.e., duplicate item
                    throw new IllegalStateException("Duplicate item (" + item + ") found for key=" + entry.getKey() + "/value=" + entry.getValue());
                }
            }
	    });
	    return targetCollection;
	}
	
	/**
	 * @param map The {@link Map} to be updated
	 * @param ignoreDuplicates If <code>false</code> and an item is mapped to
	 * an existing value then throws {@link IllegalStateException}
	 * @param transformer The {@link Transformer} to use for converting the
	 * collection items into map entries
	 * @param values The {@link Collection} of values to be mapped
	 * @return The (updated) input map
	 */
	public static final <K,V,M extends Map<K,V>,E> Map<K,V> updateCollectionEntries(
	        M map, boolean ignoreDuplicates, Transformer<? super E,Map.Entry<? extends K, ? extends V>> transformer, Collection<? extends E> values) {
        if ((map == null) || (transformer == null)) {
            throw new IllegalArgumentException("Missing map or transformer");
        }
        
        if (ExtendedCollectionUtils.isEmpty(values)) {
            return map;
        }
        
        for (E item : values) {
            Map.Entry<? extends K,? extends V>  entry=transformer.transform(item);
            if (entry == null) {
                continue;
            }
            
            K   key=entry.getKey();
            V   value=entry.getValue(), prev=map.put(key, value);
            if (prev == null) {
                continue;
            }
            
            if (!ignoreDuplicates) {
                throw new IllegalStateException("Duplicate value for key=" + key + " of item=" + item + ": " + prev + ", " + value);
            }
        }
        
        return map;
	}

    /**
     * Creates a {@link SortedMap} from a {@link Collection} of keys using a
     * {@link Transformer} to generate values for the mapped keys, using the
     * &quot;natural&quot; {@link Comparator} to generate the sorted map
     * @param ignoreDuplicates If <code>false</code> and a value is mapped to
     * an existing entry then generates an exception
     * @param transformer The {@link Transformer} to use to generate values from
     * values
     * @param keys The {@link Collection} of {@link Comparable} keys to be mapped
     * - ignored if <code>null</code>/empty (i.e., an empty map is returned)
     * @return The generated (sorted) map
     * @throws IllegalArgumentException if no map, transformer or comparator provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     * @see #mapSortedCollectionKeys(boolean, Transformer, Comparator, Collection)
     */
    public static final <K extends Comparable<K>,V> SortedMap<K,V> mapSortedCollectionKeys(
            boolean ignoreDuplicates, Transformer<? super K,? extends V> transformer, Collection<? extends K> keys) {
        return mapSortedCollectionKeys(ignoreDuplicates, transformer, ExtendedComparatorUtils.<K>comparableComparator(), keys);
    }

    /**
     * Creates a {@link SortedMap} from a {@link Collection} of keys using a
     * {@link Transformer} to generate values for the mapped keys and a given
     * {@link Comparator} to generate the sorted map
     * @param ignoreDuplicates If <code>false</code> and a value is mapped to
     * an existing entry then generates an exception
     * @param transformer The {@link Transformer} to use to generate values from
     * keys
     * @param comp The {@link Comparator} to use to generate the sorted map
     * @param keys The {@link Collection} of keys to be mapped - ignored
     * if <code>null</code>/empty (i.e., an empty map is returned)
     * @return The generated (sorted) map
     * @throws IllegalArgumentException if no map, transformer or comparator provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     * @see #updateCollectionKeysMap(Map, boolean, Transformer, Collection)
     */
    public static final <K,V> SortedMap<K,V> mapSortedCollectionKeys(
            boolean ignoreDuplicates, Transformer<? super K,? extends V> transformer, Comparator<? super K> comp, Collection<? extends K> keys) {
        if (ExtendedCollectionUtils.isEmpty(keys)) {
            // need to check this only here since these are checked by the 'else' invoked code
            if ((transformer == null) || (comp == null)) {
                throw new IllegalArgumentException("Missing transformer or comparator");
            }

            return emptySortedMap();
        } else {
            return updateCollectionKeysMap(ExtendedMapUtils.<K,V>sortedMap(comp), ignoreDuplicates, transformer, keys);
        }
    }

    /**
     * Creates a {@link Map} from a {@link Collection} of keys using a
     * {@link Transformer} to generate values for the mapped keys
     * @param ignoreDuplicates If <code>false</code> and a value is mapped to
     * an existing entry then generates an exception
     * @param transformer The {@link Transformer} to use to generate values from
     * keys 
     * @param keys The {@link Collection} of keys to be mapped - ignored
     * if <code>null</code>/empty (i.e., an empty map is returned)
     * @return The generated map
     * @throws IllegalArgumentException if no map or transformer provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     * @see #updateCollectionKeysMap(Map, boolean, Transformer, Collection)
     */
    public static final <K,V> Map<K,V> mapCollectionKeys(
            boolean ignoreDuplicates, Transformer<? super K,? extends V> transformer, Collection<? extends K> keys) {
        if (ExtendedCollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        } else {
            return updateCollectionKeysMap(new HashMap<K,V>(keys.size()), ignoreDuplicates, transformer, keys);
        }
    }
    
    /**
     * @param map The {@link Map} to be updated
     * @param ignoreDuplicates If <code>false</code> and a value is mapped to
     * an existing entry then generates an exception
     * @param transformer The {@link Transformer} to use to generate values from
     * keys 
     * @param keys The {@link Collection} of keys to be mapped - ignored
     * if <code>null</code>/empty
     * @return The updated map
     * @throws IllegalArgumentException if no map or transformer provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     */
    public static final <K,V,M extends Map<K,V>> M updateCollectionKeysMap(
            M map, boolean ignoreDuplicates, Transformer<? super K,? extends V> transformer, Collection<? extends K> keys) {
        if ((map == null) || (transformer == null)) {
            throw new IllegalArgumentException("Missing map or transformer");
        }
        
        if (ExtendedCollectionUtils.isEmpty(keys)) {
            return map;
        }
        
        for (K   k : keys) {
            V   v=transformer.transform(k);
            V   prev=map.put(k, v);
            if (prev == null) {
                continue;
            }
            
            if (!ignoreDuplicates) {
                throw new IllegalStateException("Multiple mapped value for key=" + k + ": " + v + ", " + prev);
            }
        }
        
        return map;
    }

    /**
     * @param transformer The {@link Transformer} to use to generate values from
     * keys
     * @param factory The {@link Factory} instance that create an new (empty)
     * {@link Collection} for a mapped value that is encountered for the first
     * time
     * @param keys The {@link Collection} of values to be keys - ignored
     * if <code>null</code>/empty
     * @return A {@link SortedMap} where values mapped to the same key are collected
     * inside a {@link Collection} whose type is determined by the provided
     * factory instance
     * @throws IllegalArgumentException if no map or transformer provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     */
    public static final <K extends Comparable<K>,V,C extends Collection<V>> SortedMap<K,C> mapSortedCollectionMultiKeys(
            Transformer<? super K,? extends V> transformer, Factory<? extends C> factory, Collection<? extends K> keys) {
        return mapSortedCollectionMultiKeys(transformer, ExtendedComparatorUtils.<K>comparableComparator(), factory, keys);
    }

    /**
     * @param transformer The {@link Transformer} to use to generate values from
     * keys
     * @param comp The {@link Comparator} used to determine if same key 
     * @param factory The {@link Factory} instance that create an new (empty)
     * {@link Collection} for a mapped value that is encountered for the first
     * time
     * @param keys The {@link Collection} of keys to be mapped - ignored
     * if <code>null</code>/empty
     * @return A {@link SortedMap} where values mapped to the same key are collected
     * inside a {@link Collection} whose type is determined by the provided
     * factory instance
     * @throws IllegalArgumentException if no map or transformer provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     */
    public static final <K,V,C extends Collection<V>> SortedMap<K,C> mapSortedCollectionMultiKeys(
            Transformer<? super K,? extends V> transformer, Comparator<? super K> comp, Factory<? extends C> factory, Collection<? extends K> keys) {
        if (ExtendedCollectionUtils.isEmpty(keys)) {
            // need to check this only here since these are checked by the 'else' invoked code
            if ((transformer == null) || (comp == null) || (factory == null)) {
                throw new IllegalArgumentException("Missing transformer, comparator or factory");
            }

            return emptySortedMap();
        } else {
            return updateCollectionMultiKeysMap(ExtendedMapUtils.<K,C>sortedMap(comp), transformer, factory, keys);
        }
    }

    /**
     * @param transformer The {@link Transformer} to use to generate values from
     * keys 
     * @param factory The {@link Factory} instance that create an new (empty)
     * {@link Collection} for a mapped value that is encountered for the first
     * time
     * @param keys The {@link Collection} of keys to be mapped - ignored
     * if <code>null</code>/empty
     * @return A {@link Map} where values mapped to the same key are collected
     * inside a {@link Collection} whose type is determined by the provided
     * factory instance
     * @throws IllegalArgumentException if no map or transformer provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     */
    public static final <K,V,C extends Collection<V>> Map<K,C> mapCollectionMultiKeys(
            Transformer<? super K,? extends V> transformer, Factory<? extends C> factory, Collection<? extends K> keys) {
        if (ExtendedCollectionUtils.isEmpty(keys)) {
            if ((transformer == null) || (factory == null)) {
                throw new IllegalArgumentException("Missing transformer, or factory");
            }
            return Collections.emptyMap();
        } else {
            return updateCollectionMultiKeysMap(new HashMap<K,C>(keys.size()), transformer, factory, keys);
        }
    }

    /**
     * @param map The {@link Map} to be updated that contains keys mapped to
     * {@link Collection} of values that are mapped to the same key
     * @param transformer The {@link Transformer} to use to generate values from
     * keys 
     * @param factory The {@link Factory} instance that create an new (empty)
     * {@link Collection} for a mapped value that is encountered for the first
     * time
     * @param keys The {@link Collection} of keys to be mapped - ignored
     * if <code>null</code>/empty
     * @return The updated map
     * @throws IllegalArgumentException if no map or transformer provided
     * @throws IllegalStateException if a value is mapped to an existing entry
     * and <code>ignoreDuplicates</code> is <code>false</code>
     */
    public static final <K,V,C extends Collection<V>,M extends Map<K,C>> M updateCollectionMultiKeysMap(
            M map, Transformer<? super K,? extends V> transformer, Factory<? extends C> factory, Collection<? extends K> keys) {
        if ((map == null) || (transformer == null) || (factory == null)) {
            throw new IllegalArgumentException("Missing map, transformer or factory");
        }
        
        if (ExtendedCollectionUtils.isEmpty(keys)) {
            return map;
        }
        
        for (K k : keys) {
            V   v=transformer.transform(k);
            C   prev=map.get(k);
            if (prev == null) {
                prev = factory.create();
                map.put(k, prev);
            }
            
            prev.add(v);
        }
        
        return map;
    }
}
