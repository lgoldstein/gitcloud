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
import java.util.Iterator;

import org.apache.commons.lang3.ExtendedObjectUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triplet;

/**
 * @author Lyor G.
 */
public class ExtendedIteratorUtils extends IteratorUtils {
    private static final Transformer<Iterable<?>,Iterator<?>> iteratorExtractor=
            new Transformer<Iterable<?>,Iterator<?>>() {
                @Override
                public Iterator<?> transform(Iterable<?> input) {
                    return iteratorOf(input);
                }
        };

    public ExtendedIteratorUtils() {
        super();
    }

    /**
     * @param iter The {@link Iterable} instance - ignored if <code>null</code>
     * @return The associated {@link Iterator} - or <code>null</code>
     */
    public static final <E> Iterator<E> iteratorOf(Iterable<E> iter) {
        if (iter == null) {
            return null;
        } else {
            return iter.iterator();
        }
    }

    /**
     * @return A {@link Transformer} that returns the {@link Iterable#iterator()}
     * call result
     */
    @SuppressWarnings({ "cast", "unchecked", "rawtypes" })
    public static final <E> Transformer<Iterable<E>,Iterator<E>> iteratorOf() {
        return (Transformer<Iterable<E>,Iterator<E>>) (Transformer) iteratorExtractor;
    }
    
    public static final <E> Triplet<E, E, Integer> findFirstDifference(Iterator<? extends E> iter1, Iterator<? extends E> iter2) {
        return findFirstDifference(iter1, iter2, ExtendedComparatorUtils.OBJECT_EQUALITY_COMPARATOR);
    }

    public static final <E extends Comparable<E>> Triplet<E, E, Integer> findFirstComparableDifference(Iterator<? extends E> iter1, Iterator<? extends E> iter2) {
        return findFirstDifference(iter1, iter2, ExtendedComparatorUtils.<E>comparableComparator());
    }

    /**
     * @param iter1 1st {@link Iterator} - may be <code>null</code>
     * @param iter2 2nd {@link Iterator} - may be <code>null</code>
     * @param comp The {@link Comparator} to use to check if 2 members are equal
     * @return A {@link Triplet} representing the 1st difference - where:</BR>
     * <UL>
     *      <LI>
     *      V1 - the value from the 1st iterator that did not match (<B>Note:</B>
     *      may be <code>null</code> if the iterator ran out of values but the
     *      other one still has values)
     *      </LI>
     *      
     *      <LI>
     *      V2 - the value from the 2nd iterator that did not match (<B>Note:</B>
     *      may be <code>null</code> if the iterator ran out of values but the
     *      other one still has values)
     *      </LI>
     *      
     *      <LI>
     *      V3 - The {@link Integer} &quot;offset&quot; of the difference - i.e.,
     *      how many successful {@link Iterator#next()} calls where executed on
     *      <U>both</U> iterators until the difference was found
     *      </LI>
     * </UL>
     * If no difference then returns <code>null</code>
     */
    public static final <E> Triplet<E, E, Integer> findFirstDifference(Iterator<? extends E> iter1, Iterator<? extends E> iter2, Comparator<? super E> comp) {
        if (comp == null) {
            throw new IllegalArgumentException("No comparator provided");
        }

        int offset=0;
        for ( ; (iter1 != null) && iter1.hasNext() && (iter2 != null) && iter2.hasNext(); offset++) {
            E   v1=iter1.next(), v2=iter2.next();
            int nRes=comp.compare(v1, v2);
            if (nRes != 0) {
                return new Triplet<E,E,Integer>(v1, v2, Integer.valueOf(offset));
            }
        }
        
        // this point is reached if either iterator is null or has reached its end of values
        if ((iter1 == null) || (!iter1.hasNext())) {
            if ((iter2 == null) || (!iter2.hasNext())) {
                return null;
            } else {
                return new Triplet<E,E,Integer>(null, iter2.next(), Integer.valueOf(offset));
            }
        } else if ((iter2 == null) || (!iter2.hasNext())) {
            if ((iter1 == null) || (!iter1.hasNext())) {
                return null;
            } else {
                return new Triplet<E,E,Integer>(iter1.next(), null, Integer.valueOf(offset));
            }
        }
        
        throw new IllegalStateException("Unresolved iterator exit loop state");
    }

    /**
     * Iterates over all members and invokes {@link ObjectUtils#hashCode(Object)}
     * on each
     * @param e The {@link Enumeration} of items to be hashed - may be <code>null</code>
     * @return The calculated hash code
     */
    public static final int hashCode(Enumeration<?> e) {
        return hashCode((e == null) ? null : asIterator(e));
    }

    /**
     * Iterates over all members and invokes {@link ObjectUtils#hashCode(Object)}
     * on each
     * @param items The {@link Iterable} items to be hashed - may be <code>null</code>
     * @return The calculated hash code
     */
    public static final int hashCode(Iterable<?> items) {
        return hashCode(iteratorOf(items));
    }

    /**
     * Iterates over all members and invokes {@link ObjectUtils#hashCode(Object)}
     * on each
     * @param iter The {@link Iterator} of items to be hashed - may be <code>null</code>
     * @return The calculated hash code
     */
    public static final int hashCode(Iterator<?> iter) {
        if (iter == null) {
            return 0;
        }
        
        int hash=1;
        while(iter.hasNext()) {
            hash = hash * 31 + ObjectUtils.hashCode(iter.next());
        }
        
        return hash;
    }
    
    /**
     * Iterates over all members and invokes {@link ExtendedObjectUtils#deepHash(Object)}
     * on each
     * @param items The {@link Iterable} items to be hashed - may be <code>null</code>
     * @return The calculated hash code
     */
    public static final int deepHash(Iterable<?> items) {
        return deepHash((items == null) ? null : items.iterator());
    }

    /**
     * Iterates over all members and invokes {@link ExtendedObjectUtils#deepHash(Object)}
     * on each
     * @param e The {@link Enumeration} of items to be hashed - may be <code>null</code>
     * @return The calculated hash code
     */
    public static final int deepHash(Enumeration<?> e) {
        return deepHash((e == null) ? null : asIterator(e));
    }

    /**
     * Iterates over all members and invokes {@link ExtendedObjectUtils#deepHash(Object)}
     * on each
     * @param iter The {@link Iterator} of items to be hashed - may be <code>null</code>
     * @return The calculated hash code
     */
    public static final int deepHash(Iterator<?> iter) {
        if (iter == null) {
            return 0;
        }
        
        int hash=1;
        while(iter.hasNext()) {
            hash = hash * 31 + ExtendedObjectUtils.deepHash(iter.next());
        }
        
        return hash;
    }
}
