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

import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author Lyor G.
 * @since Jul 17, 2013 12:23:18 PM
 */
public class ExtendedSetUtils extends SetUtils {
    public ExtendedSetUtils() {
        super();
    }

    @SuppressWarnings("unchecked")
    public static final <V> SortedSet<V> emptySortedSet() {
    	return EMPTY_SORTED_SET;
    }

    public static final <V> SortedSet<V> sortedSet(Comparator<? super V> comp) {
    	return new TreeSet<V>(Validate.notNull(comp, "No comparator", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    @SuppressWarnings("rawtypes")
    private static final Factory    hashSetFactory=new Factory() {
            @Override
            public Set create() {
                return new HashSet();
            }
        };
    /**
     * @return A {@link Factory} that returns a new {@link HashSet} every
     * time the {@link Factory#create()} method is invoked
     */
    @SuppressWarnings("unchecked")
    public static final <V> Factory<Set<V>> hashSetFactory() {
        return hashSetFactory;
    }


    /**
     * @param comp The {@link Comparator} to use for the {@link SortedSet}
     * @return A {@link Factory} that returns a new {@link SortedSet} every
     * time the {@link Factory#create()} method is invoked using the provided
     * comparator
     * @throws IllegalArgumentException if no comparator provided
     */
    public static final <V> Factory<SortedSet<V>> sortedSetFactory(final Comparator<? super V> comp) {
        if (comp == null) {
            throw new IllegalArgumentException("No comparator provided");
        }

        return new Factory<SortedSet<V>>() {
            @Override
            public SortedSet<V> create() {
                return ExtendedSetUtils.sortedSet(comp);
            }
        };
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final Factory  sortedSetFactory=
            sortedSetFactory(ExtendedComparatorUtils.<Comparable>comparableComparator());

    /**
     * @return A {@link Factory} that returns a new {@link SortedSet} every
     * time the {@link Factory#create()} method is invoked using the &quot;natural&quot;
     * {@link Comparable#compareTo(Object)} order
     * @see #sortedSetFactory(Comparator)
     * @throws IllegalArgumentException if no comparator provided
     */
    @SuppressWarnings("unchecked")
    public static final <V extends Comparable<V>> Factory<SortedSet<V>> sortedSetFactory() {
        return sortedSetFactory;
    }

    /**
     * @return A {@link Set} that uses the {@link ExtendedComparatorUtils#OBJECT_INSTANCE_COMPARATOR}
     * in order to decide whether a member can be added
     */
    public static final <T> Set<T> uniqueInstanceSet() {
        return new TreeSet<T>(ExtendedComparatorUtils.OBJECT_INSTANCE_COMPARATOR);
    }
    
    /**
     * @param items The items to be included in the set - ignored if {@code null}/empty
     * @return A {@link Set} that uses the {@link ExtendedComparatorUtils#OBJECT_INSTANCE_COMPARATOR}
     * in order to decide whether a member can be added with the items in it
     */
    @SafeVarargs
    public static final <V> Set<V> uniqueInstanceSet (V ... items) {
        return uniqueInstanceSet(ExtendedArrayUtils.asList(items));
    }

    /**
     * @param items The items to be included in the set - ignored if {@code null}/empty
     * @return A {@link Set} that uses the {@link ExtendedComparatorUtils#OBJECT_INSTANCE_COMPARATOR}
     * in order to decide whether a member can be added with the items in it
     */
    public static final <V> Set<V> uniqueInstanceSet (Iterable<? extends V> items) {
        return uniqueInstanceSet(ExtendedIteratorUtils.iteratorOf(items));
    }

    /**
     * @param e The {@link Enumeration} of items to be included in the set - ignored if {@code null}/empty
     * @return A {@link Set} that uses the {@link ExtendedComparatorUtils#OBJECT_INSTANCE_COMPARATOR}
     * in order to decide whether a member can be added with the items in it
     */
    public static final <V> Set<V> uniqueInstanceSet (Enumeration<? extends V> e) {
        return uniqueInstanceSet((e == null) ? null : IteratorUtils.asIterator(e));
    }

    /**
     * @param iter The {@link Iterator} of items to be included in the set - ignored if {@code null}/empty
     * @return A {@link Set} that uses the {@link ExtendedComparatorUtils#OBJECT_INSTANCE_COMPARATOR}
     * in order to decide whether a member can be added with the items in it
     */
    public static final <V> Set<V> uniqueInstanceSet (Iterator<? extends V> iter) {
        Set<V>  result=uniqueInstanceSet();
        ExtendedCollectionUtils.addAllItems(result, iter);
        return result;
    }
}
