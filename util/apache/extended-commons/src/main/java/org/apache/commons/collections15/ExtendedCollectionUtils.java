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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.collections15.collection.IndexedReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.AsymmetricComparator;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedObjectUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triplet;

/**
 * @author lgoldstein
 */
public class ExtendedCollectionUtils extends CollectionUtils {
	public ExtendedCollectionUtils() {
		super();
	}
	
	@SuppressWarnings("unchecked")
    public static final <V> Collection<V> emptyCollection() {
	    return EMPTY_COLLECTION;
	}
	
	/**
     * @param c The {@link Collection} to be accessed
     * @return The 1st member in the collection - if not empty, or <code>null</code> otherwise
     */
    @SuppressWarnings("unchecked")
	public static final <T> T getFirstMember (Collection<? extends T> c) {
    	if (isEmpty(c)) {
    		return null;
    	} else {
    		return (T) get(c, 0);
    	}
    }

    public static final <T> T getFirstMember (IndexedReader<T> reader) {
        if (reader == null) {
            return null;
        } else {
            return reader.get(0);
        }
    }

	// for some reason it is not provided in the generic-collections artifact
	public static final boolean isEmpty(Collection<?> c) {
		return (c == null) || c.isEmpty();
	}

    public static final int size (Collection<?> c) {
        return (c == null) ? 0 : c.size();
    }

    /**
     * @param coll The {@linkplain Collection} to add the item to
     * @param item The item to be added
     * @return The collection after item added
     */
    public static final <E,C extends Collection<E>> C append(C coll, E item) {
        coll.add(item);
        return coll;
    }

    /**
     * @param coll The {@linkplain Collection} to add the items to
     * @param items The items to be added - ignored if {@code null}/empty
     * @return The collection after all items added
     */
    @SafeVarargs
    public static final <E,C extends Collection<E>> C append(C coll, E ... items) {
        return append(coll, ExtendedArrayUtils.asList(items));
    }

    /**
     * @param coll The {@linkplain Collection} to add the items to
     * @param items The items to be added - ignored if {@code null}/empty
     * @return The collection after all items added
     */
    public static final <E,C extends Collection<E>> C append(C coll, Collection<? extends E> items) {
        if (isEmpty(items)) {
            return coll;
        }
        
        coll.addAll(items);
        return coll;
    }
    
    /**
     * @param coll The {@linkplain Collection} to add the items to
     * @param items The {@link Iterable} items to be added - ignored if {@code null}
     * @return The collection after all items added
     */
    public static final <E,C extends Collection<E>> C append(C coll, Iterable<? extends E> items) {
        return append(coll, (items == null) ? null : items.iterator());
    }
    
    /**
     * @param coll The {@linkplain Collection} to add the items to
     * @param items The {@link Iterator} containing the items to be added - ignored if {@code null}
     * @return The collection after all items added
     */
    public static final <E,C extends Collection<E>> C append(C coll, Iterator<? extends E> items) {
        if (items == null) {
            return coll;
        }

        while (items.hasNext()) {
            coll.add(items.next());
        }

        return coll;
    }

    /**
     * Iterates over all members and invokes {@link ObjectUtils#hashCode(Object)}
     * on each
     * @param coll The {@link Collection} to be hashed - may be <code>null</code>
     * @return The calculated hash code
     */
    public static final int hashCode(Collection<?> coll) {
        return ExtendedIteratorUtils.hashCode(ExtendedIteratorUtils.iteratorOf(coll));
    }
    
    /**
     * Iterates over all members and invokes {@link ExtendedObjectUtils#deepHash(Object)}
     * on each
     * @param coll The {@link Collection} of items to be hashed - may be <code>null</code>
     * @return The calculated hash code
     */
    public static final int deepHash(Collection<?> coll) {
        return ExtendedIteratorUtils.deepHash((coll == null) ? null : coll.iterator());
    }
    
    /**
     * Adds all elements in the {@link Iterable} to the given collection.
     * @param coll the collection to add to
     * @param items {@link Iterable} elements to add, ignored if {@code null}
     * @return Number of items for which {@link Collection#add(Object)} returned {@code true}
     * @throws NullPointerException if the collection is {@code null}
     */
    public static final <E> int addAllItems(Collection<? super E> coll, Iterable<? extends E> items) {
        return addAllItems(coll, ExtendedIteratorUtils.iteratorOf(items));
    }

    /**
     * Adds all elements in the {@link Enumeration} to the given collection.
     * @param coll the collection to add to
     * @param e {@link Enumeration} of elements to add, ignored if {@code null}
     * @return Number of items for which {@link Collection#add(Object)} returned {@code true}
     * @throws NullPointerException if the collection is {@code null}
     */
    public static final <E> int addAllItems(Collection<? super E> coll, Enumeration<? extends E> e) {
        return addAllItems(coll, (e == null) ? null : IteratorUtils.asIterator(e));
    }

    /**
     * Adds all elements in the {@link Iterator} to the given collection.
     * @param coll the collection to add to
     * @param iter {@link Iterator} of elements to add, ignored if {@code null}
     * @return Number of items for which {@link Collection#add(Object)} returned {@code true}
     * @throws NullPointerException if the collection is {@code null}
     */
    public static final <E> int addAllItems(Collection<? super E> coll, Iterator<? extends E> iter) {
        ExtendedValidate.notNull(coll, "No target collection");
        if (iter == null) {
            return 0;
        }

        int numAdded=0;
        while(iter.hasNext()) {
            E   item=iter.next();
            if (coll.add(item)) {
                numAdded++;
            }
        }
        
        return numAdded;
    }

	public static final <I,O> List<O> collectToList(Collection<? extends I> inputCollection, Transformer<? super I, ? extends O> transformer) {
		if (isEmpty(inputCollection)) {
			return Collections.emptyList();
		} else {
			return collect(inputCollection, transformer, new ArrayList<O>(inputCollection.size()));
		}
	}
	
	public static final <I,O> Set<O> collectToSet(Collection<? extends I> inputCollection, Transformer<? super I, ? extends O> transformer) {
		if (isEmpty(inputCollection)) {
			return Collections.emptySet();
		} else {
			return collect(inputCollection, transformer, new HashSet<O>(inputCollection.size()));
		}
	}

	public static final <I,O,C extends Collection<O>> C collect(
	        Collection<? extends I> inputCollection, Transformer<? super I, ? extends O> transformer, Factory<? extends C> factory) {
	    C  result=factory.create();
	    if (isEmpty(inputCollection)) {
	        return result;
	    } else {
	        return collect(inputCollection, transformer, result);
	    }
	}

	public static final <I,O extends Comparable<O>> SortedSet<O> collectToSortedSet(Collection<? extends I> inputCollection, Transformer<? super I, ? extends O> transformer) {
		return collectToSortedSet(inputCollection, transformer, ExtendedComparatorUtils.<O>comparableComparator());
	}

	public static final <I,O> SortedSet<O> collectToSortedSet(Collection<? extends I> inputCollection, Transformer<? super I, ? extends O> transformer, Comparator<? super O> comp) {
		if (isEmpty(inputCollection)) {
			return ExtendedSetUtils.emptySortedSet();
		} else {
			return collect(inputCollection, transformer, ExtendedSetUtils.sortedSet(comp));
		}
	}

    public static final <E> List<E> accumulateToList(List<E> current, Collection<? extends E> delta) {
        return accumulate(current, delta, ExtendedCollectionUtils.<E>arrayListFactory());
    }

    public static final <E> Set<E> accumulateToSet(Set<E> current, Collection<? extends E> delta) {
        return accumulate(current, delta, ExtendedSetUtils.<E>hashSetFactory());
    }

    public static final <E extends Comparable<E>> SortedSet<E> accumulateToSortedSet(SortedSet<E> current, Collection<? extends E> delta) {
        return accumulate(current, delta, ExtendedSetUtils.<E>sortedSetFactory());
    }

    public static final <E> Collection<E> accumulateToCollection(Collection<E> current, Collection<? extends E> delta) {
        return accumulate(current, delta, ExtendedCollectionUtils.<E>linkedListFactory());
    }

    /**
     * <P>Adds a collection of items to an existing one, allocating a new collection
     * if necessary:</P></BR>
     * <UL>
     *     <LI>
     *     If the collection of items to be added is {@code null}/empty then
     *     it is ignored and the current collection reference is returned as-is
     *     </LI>
     *     
     *     <LI>
     *     If the current collection of items is non-{@code null} then the
     *     delta is added to it.
     *     </LI>
     *     
     *     <LI>
     *     Otherwise, a <U>new</U> collection is created (via the factory)
     *     and initialized with the items to be added
     *     </LI>
     * </UL>
     * @param current The current collection of items
     * @param delta The items to add - ignored if {@code null}/empty
     * @param factory A {@link Factory} to create the current collection of
     * items if necessary
     * @return The allocated/updated collection of items
     */
    public static final <E, C extends Collection<E>> C accumulate(C current, Collection<? extends E> delta, Factory<? extends C> factory) {
        if (isEmpty(delta)) {
            return current;
        }
        
        if (current != null) {
            current.addAll(delta);
            return current;
        }
        
        C  allocated=factory.create();
        allocated.addAll(delta);
        return allocated;
    }

    public static final <I,O> Set<O> aggregateToSortedSet(
            Collection<? extends I> inputCollection, Transformer<? super I, ? extends Collection<? extends O>> transformer, Comparator<? super O> comp) {
        if (isEmpty(inputCollection)) {
            return ExtendedSetUtils.emptySortedSet();
        } else {
            return aggregate(inputCollection, transformer, ExtendedSetUtils.sortedSet(comp));
        }
    }

    public static final <I,O> Set<O> aggregateToSet(
            Collection<? extends I> inputCollection, Transformer<? super I, ? extends Collection<? extends O>> transformer) {
        if (isEmpty(inputCollection)) {
            return Collections.emptySet();
        } else {
            return aggregate(inputCollection, transformer, new HashSet<O>(inputCollection.size() * 2));
        }
    }

    public static final <I,O> List<O> aggregateToList(
            Collection<? extends I> inputCollection, Transformer<? super I, ? extends Collection<? extends O>> transformer) {
        if (isEmpty(inputCollection)) {
            return Collections.emptyList();
        } else {
            return aggregate(inputCollection, transformer, new ArrayList<O>(inputCollection.size() * 2));
        }
    }

    /**
     * @param inputCollection The input {@link Collection} to be scanned
     * @param transformer The {@link Transformer} to use on the scanned input
     * items to generate a collection of output values
     * @param factory The {@link Factory} to use to generate the output collection
     * @return The result of adding all intermediate collections generated by
     * the transformer to the output
     */
    public static final <I,O,C extends Collection<O>> C aggregate(
                Collection<? extends I> inputCollection, Transformer<? super I, ? extends Collection<? extends O>> transformer, Factory<? extends C> factory) {
        return aggregate(inputCollection, transformer, factory.create());
    }

    public static final <I,O,C extends Collection<O>> C aggregate(
                Collection<? extends I> inputCollection, Transformer<? super I, ? extends Collection<? extends O>> transformer, C result) {
        ExtendedValidate.notNull(transformer, "No transformer provided");
        if (isEmpty(inputCollection)) {
            return result;
        }
        
        for (I item : inputCollection) {
            Collection<? extends O>    values=transformer.transform(item);
            if (isEmpty(values)) {
                continue;
            }
            
            if (!result.addAll(values)) {
                continue;   // debug breakpoint
            }
        }
        
        return result;
    }

	public static final <E> List<E> selectToList(Collection<? extends E> inputCollection, Predicate<? super E> selector) {
		if (isEmpty(inputCollection)) {
			return Collections.emptyList();
		} else {
			return select(inputCollection, selector, new ArrayList<E>(inputCollection.size()));
		}
	}

	public static final <E> Set<E> selectToSet(Collection<? extends E> inputCollection, Predicate<? super E> selector) {
		if (isEmpty(inputCollection)) {
			return Collections.emptySet();
		} else {
			return select(inputCollection, selector, new HashSet<E>(inputCollection.size()));
		}
	}

	public static final <E extends Comparable<E>> SortedSet<E> selectToSortedSet(Collection<? extends E> inputCollection, Predicate<? super E> selector) {
		return selectToSortedSet(inputCollection, selector, ExtendedComparatorUtils.<E>comparableComparator());
	}
	
	public static final <E> SortedSet<E> selectToSortedSet(Collection<? extends E> inputCollection, Predicate<? super E> selector, Comparator<? super E> comp) {
		if (isEmpty(inputCollection)) {
			return ExtendedSetUtils.emptySortedSet();
		} else {
			return select(inputCollection, selector, ExtendedSetUtils.sortedSet(comp));
		}
	}
	
	public static final <E,C extends Collection<E>> C select(Collection<? extends E> inputCollection, Predicate<? super E> selector, Factory<? extends C> factory) {
        C  result=ExtendedValidate.notNull(factory, "No factory").create();
	    if (isEmpty(inputCollection)) {
	        return result;
	    } else {
	        return select(inputCollection, selector, result);
	    }
	}

	public static final <E> void filterAccepted(Iterable<E> iterable, Predicate<? super E> predicate) {
		filter(iterable, PredicateUtils.notPredicate(predicate));
	}
	
	@SafeVarargs
	public static final <E> Set<E> asSet(E ... items) {
		if (ExtendedArrayUtils.length(items) <= 0) {
			return Collections.emptySet();
		}
		
		Set<E>	itemSet=new HashSet<E>(items.length);
		addAll(itemSet, items);
		return itemSet;
	}
	
	@SafeVarargs
	public static final <E extends Comparable<E>> SortedSet<E> asSortedSet(E ... items) {
		return asSortedSet(ExtendedComparatorUtils.<E>comparableComparator(), items);
	}
	
	@SafeVarargs
	public static final <E> SortedSet<E> asSortedSet(Comparator<? super E> comp, E ... items) {
	    return asSortedSet(comp, ExtendedArrayUtils.asList(items));
	}

	public static final <E extends Comparable<E>> SortedSet<E> asSortedSet(Collection<? extends E> items) {
	    return asSortedSet(ExtendedComparatorUtils.<E>comparableComparator(), items);
	}

	public static final <E> SortedSet<E> asSortedSet(Comparator<? super E> comp, Collection<? extends E> items) {
	    if (isEmpty(items)) {
	        return ExtendedSetUtils.emptySortedSet();
	    }

        SortedSet<E>    itemSet=ExtendedSetUtils.sortedSet(comp);
        itemSet.addAll(items);
        return itemSet;
	}

    @SuppressWarnings("rawtypes")
    private static final Factory    linkedListFactory=new Factory() {
            @Override
            public Collection create() {
                return new LinkedList();
            }
        };
    /**
     * @return A {@link Factory} that returns a new {@link LinkedList} every
     * time the {@link Factory#create()} method is invoked
     */
    @SuppressWarnings("unchecked")
    public static final <V> Factory<Collection<V>> linkedListFactory() {
        return linkedListFactory;
    }

    @SuppressWarnings("rawtypes")
    private static final Factory    arrayListFactory=new Factory() {
            @Override
            public List create() {
                return new ArrayList();
            }
        };
    /**
     * @return A {@link Factory} that returns a new {@link ArrayList} every
     * time the {@link Factory#create()} method is invoked
     */
    @SuppressWarnings("unchecked")
    public static final <V> Factory<List<V>> arrayListFactory() {
        return arrayListFactory;
    }
    
    /**
     * Locates a matching entry using a comparator
     * @param iter The {@link Iterable} instance to scan
     * @param value The value to compare with
     * @param comp The {@link Comparator} to use
     * @return <code>true</code> if a match was found
     * @see #find(Iterable, Object, Comparator)
     */
    public static final <E> boolean exists(Iterable<? extends E> iter, E value, Comparator<? super E> comp) {
        E   match=find(iter, value, comp);
        if (match == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Locates a matching entry using a comparator
     * @param iter The {@link Iterable} instance to scan
     * @param value The value to compare with
     * @param comp The {@link Comparator} to use
     * @return The matching entry - <code>null</code> if not found
     */
    public static final <E> E find(Iterable<? extends E> iter, E value, Comparator<? super E> comp) {
        return find(iter, ExtendedPredicateUtils.equals(value, comp));
    }
    
    /**
     * Checks if 2 collections contain the same members in the same <U>order</U>
     * @param iter1 1st {@link Iterable} - may be <code>null</code>
     * @param iter2 2nd {@link Iterable} - may be <code>null</code>
     * @return <code>true</code> if both collections contain the same members
     * in the same order
     * @see #findFirstDifference(Iterable, Iterable)
     */
    public static final <E> boolean isSameOrderCollection(Iterable<? extends E> iter1, Iterable<? extends E> iter2) {
        return isSameOrderCollection(iter1, iter2, ExtendedComparatorUtils.OBJECT_EQUALITY_COMPARATOR);
    }

    public static final <E extends Comparable<E>> boolean isSameComparableOrderCollection(Iterable<? extends E> iter1, Iterable<? extends E> iter2) {
        return isSameOrderCollection(iter1, iter2, ExtendedComparatorUtils.<E>comparableComparator());
    }

    public static final <E> boolean isSameOrderCollection(Iterable<? extends E> iter1, Iterable<? extends E> iter2, Comparator<? super E> comp) {
        return findFirstDifference(iter1, iter2, comp) == null;
    }

    public static final <E> Triplet<E, E, Integer> findFirstDifference(Iterable<? extends E> iter1, Iterable<? extends E> iter2) {
        return findFirstDifference(iter1, iter2, ExtendedComparatorUtils.OBJECT_EQUALITY_COMPARATOR);
    }

    public static final <E extends Comparable<E>> Triplet<E, E, Integer> findFirstComparableDifference(Iterable<? extends E> iter1, Iterable<? extends E> iter2) {
        return findFirstDifference(iter1, iter2, ExtendedComparatorUtils.<E>comparableComparator());
    }

    /**
     * @param iter1 1st {@link Iterable} - may be <code>null</code>
     * @param iter2 2nd {@link Iterable} - may be <code>null</code>
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
     * @see ExtendedIteratorUtils#findFirstDifference(Iterator, Iterator, Comparator)
     */
    public static final <E> Triplet<E, E, Integer> findFirstDifference(Iterable<? extends E> iter1, Iterable<? extends E> iter2, Comparator<? super E> comp) {
        return ExtendedIteratorUtils.findFirstDifference(ExtendedIteratorUtils.iteratorOf(iter1), ExtendedIteratorUtils.iteratorOf(iter2), comp);
    }

    /**
     * Locates an element in a {@link List} of elements
     * @param <T> Type of elements in the {@link List}
     * @param v Element to be located using {@link Object#equals(Object)} - <code>if null</code>, then failure result returned
     * @param vals The {@link List} values
     * @return Index of element in the {@link List} - negative if not found
     */
    public static final <T> int indexOf (T v, List<? extends T> vals) {
        if ((v == null) || isEmpty(vals)) {
            return (-1);
        } else {
            return vals.indexOf(v);
        }
    }

    /**
     * Locates an element in a {@link List} of elements
     * @param <T> Type of {@link List} elements
     * @param v Element to be located - if {@code null}, then failure result returned
     * @param c The {@link Comparator} to use to check if elements are equal
     * to the sought value. If {@code null}, then only reference equality and the
     * {@link Object#equals(Object)} method are used. 
     * @param vals The {@link List} values
     * @return Index of element in the {@link List} - negative if not found
     */
    public static final <T> int indexOf (T v, Comparator<? super T> c, List<? extends T> vals) {
        return indexOf(v, c, 0, vals);
    }

    /**
     * Locates an element in a {@link List} of elements
     * @param <T> Type of {@link List} elements
     * @param v Element to be located - if {@code null}, then failure result returned
     * @param c The {@link Comparator} to use to check if elements are equal
     * to the sought value. If {@code null}, then only reference equality and the
     * {@link Object#equals(Object)} method are used. 
     * @param startIndex Index to start looking for the element (inclusive)
     * @param vals The {@link List} values
     * @return Index of element in the {@link List} - negative if not found
     */
    public static final <T> int indexOf (T v, Comparator<? super T> c, int startIndex, List<? extends T> vals) {
        return indexOf(v, c, startIndex, size(vals), vals);
    }

    /**
     * Locates an element in a {@link List} of elements
     * @param <T> Type of {@link List} elements
     * @param v Element to be located - ignored (i.e., not-found) if {@code null}
     * @param c The {@link Comparator} to use to check if elements are equal
     * to the sought value. If <code>null</code>, then only reference equality and the
     * {@link Object#equals(Object)} method are used. 
     * @param startIndex Index to start looking for the element (inclusive)
     * @param endIndex Index to stop looking for the element (exclusive)
     * @param vals The {@link List} values
     * @return Index of element in the {@link List} - negative if not found
     */
    public static final <T> int indexOf (
            T v, Comparator<? super T> c, int startIndex, int endIndex, List<? extends T> vals) {
        if ((v == null) || isEmpty(vals)) {
            return (-1);
        }

        for (int    vIndex=startIndex; vIndex < endIndex; vIndex++) {
            T cv=vals.get(vIndex);
            if (c != null) {
                if (0 == c.compare(cv, v)) {
                    return vIndex;
                }
            } else {
                 if (ObjectUtils.equals(v, cv)) {
                     return vIndex;
                 }
            }
        }

        return (-1);
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @return The index of the item that comes first in the &quot;natural&quot
     * order imposed by the {@link Comparable#compareTo(Object)} contract - negative
     * if no items evaluated
     */
    public static final <T extends Comparable<T>> int minValueIndex(List<? extends T> items) {
        return minValueIndex(items, 0);
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @param startIndex Index to start evaluation (inclusive)
     * @return The index of the item that comes first in the &quot;natural&quot
     * order imposed by the {@link Comparable#compareTo(Object)} contract - negative
     * if no items evaluated
     */
    public static final <T extends Comparable<T>> int minValueIndex(List<? extends T> items, int startIndex) {
        return minValueIndex(items, startIndex, size(items));
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @param startIndex Index to start evaluation (inclusive)
     * @param endIndex Index to end evaluation (exclusive)
     * @return The index of the item that comes first in the &quot;natural&quot
     * order imposed by the {@link Comparable#compareTo(Object)} contract - negative
     * if no items evaluated
     */
    public static final <T extends Comparable<T>> int minValueIndex(List<? extends T> items, int startIndex, int endIndex) {
        return minValueIndex(items, startIndex, endIndex, ExtendedComparatorUtils.<T>comparableComparator());
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @param c The {@link Comparator} to use to decide which item comes first
     * @return The index of the item that comes first in the order imposed
     * by the {@link Comparator#compare(Object, Object)} contract - negative
     * if no items evaluated
     */
    public static final <T> int minValueIndex(List<? extends T> items, Comparator<? super T> c) {
        return minValueIndex(items, 0, c);
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @param startIndex Index to start evaluation (inclusive)
     * @param c The {@link Comparator} to use to decide which item comes first
     * @return The index of the item that comes first in the order imposed
     * by the {@link Comparator#compare(Object, Object)} contract - negative
     * if no items evaluated
     */
    public static final <T> int minValueIndex(List<? extends T> items, int startIndex, Comparator<? super T> c) {
        return minValueIndex(items, startIndex, size(items), c);
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @param startIndex Index to start evaluation (inclusive)
     * @param endIndex Index to end evaluation (exclusive)
     * @param c The {@link Comparator} to use to decide which item comes first
     * @return The index of the item that comes first in the order imposed
     * by the {@link Comparator#compare(Object, Object)} contract - negative
     * if no items evaluated
     */
    public static final <T> int minValueIndex(List<? extends T> items, int startIndex, int endIndex, Comparator<? super T> c) {
        ExtendedValidate.notNull(c, "No comparator");
        if (isEmpty(items)) {
            return (-1);
        }
        
        int cIndex=(-1);
        T   candidate=null;
        for (int    vIndex=startIndex; vIndex < endIndex; vIndex++) {
            T value=items.get(vIndex);
            if (candidate != null) {    // check if can improve
                int nRes=c.compare(candidate, value);
                if (nRes <= 0) {
                    continue;
                }
            }
            
            candidate = value;
            cIndex = vIndex;
        }
        
        return cIndex;
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @return The index of the item that comes last in the &quot;natural&quot
     * order imposed by the {@link Comparable#compareTo(Object)} contract - negative
     * if no items evaluated
     */
    public static final <T extends Comparable<T>> int maxValueIndex(List<? extends T> items) {
        return maxValueIndex(items, 0);
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @param startIndex Index to start evaluation (inclusive)
     * @return The index of the item that comes last in the &quot;natural&quot
     * order imposed by the {@link Comparable#compareTo(Object)} contract - negative
     * if no items evaluated
     */
    public static final <T extends Comparable<T>> int maxValueIndex(List<? extends T> items, int startIndex) {
        return maxValueIndex(items, startIndex, size(items));
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @param startIndex Index to start evaluation (inclusive)
     * @param endIndex Index to end evaluation (exclusive)
     * @return The index of the item that comes last in the &quot;natural&quot
     * order imposed by the {@link Comparable#compareTo(Object)} contract - negative
     * if no items evaluated
     */
    public static final <T extends Comparable<T>> int maxValueIndex(List<? extends T> items, int startIndex, int endIndex) {
        return maxValueIndex(items, startIndex, endIndex, ExtendedComparatorUtils.<T>comparableComparator());
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @param c The {@link Comparator} to use to decide which item comes first
     * @return The index of the item that comes last in the order imposed
     * by the {@link Comparator#compare(Object, Object)} contract - negative
     * if no items evaluated
     */
    public static final <T> int maxValueIndex(List<? extends T> items, Comparator<? super T> c) {
        return maxValueIndex(items, 0, c);
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @param startIndex Index to start evaluation (inclusive)
     * @param c The {@link Comparator} to use to decide which item comes first
     * @return The index of the item that comes last in the order imposed
     * by the {@link Comparator#compare(Object, Object)} contract - negative
     * if no items evaluated
     */
    public static final <T> int maxValueIndex(List<? extends T> items, int startIndex, Comparator<? super T> c) {
        return maxValueIndex(items, startIndex, size(items), c);
    }

    /**
     * @param items The {@link List} of items to evaluate
     * @param startIndex Index to start evaluation (inclusive)
     * @param endIndex Index to end evaluation (exclusive)
     * @param c The {@link Comparator} to use to decide which item comes first
     * @return The index of the item that comes last in the order imposed
     * by the {@link Comparator#compare(Object, Object)} contract - negative
     * if no items evaluated
     */
    public static final <T> int maxValueIndex(List<? extends T> items, int startIndex, int endIndex, Comparator<? super T> c) {
        ExtendedValidate.notNull(c, "No comparator");
        if (isEmpty(items)) {
            return (-1);
        }
        
        int cIndex=(-1);
        T   candidate=null;
        for (int    vIndex=startIndex; vIndex < endIndex; vIndex++) {
            T value=items.get(vIndex);
            if (candidate != null) {    // check if can improve
                int nRes=c.compare(candidate, value);
                if (nRes >= 0) {
                    continue;
                }
            }
            
            candidate = value;
            cIndex = vIndex;
        }
        
        return cIndex;
    }

    /**
     * Uses null safe comparison of objects
     * @param <T> type of the values processed by this method
     * @param values the values to be processed, may be {@code null}/empty and contain {@code null}s
     * @param comp the {@link Comparator} to use - <B>Note:</B> must be provided
     * even if no values or no non-{@code null} values exist
     * @return
     *  <ul>
     *   <li>If no objects provided then {@code null} is returned
     *   <li>If any objects are non-{@code null} and unequal, the lesser object.
     *   <li>If all objects are non-{@code null} and equal, the first.
     *   <li>If any of the objects are {@code null}, the lesser of the non-null objects.
     *   <li>If all the objects are {@code null}, {@code null} is returned.
     *  </ul>
     */
    public static <T extends Comparable<? super T>> T minValue(Comparator<? super T> comp, Collection<? extends T> values) {
        Validate.notNull(comp, "No comparator", ArrayUtils.EMPTY_OBJECT_ARRAY);
        if (isEmpty(values)) {
            return null;
        }

        T result = null;
        for (T value : values) {
            if (ExtendedObjectUtils.compare(value, result, comp, true) < 0) {
                result = value;
            }
        }

        return result;
    }

    /**
     * Uses null safe comparison of objects
     * @param <T> type of the values processed by this method
     * @param values the values to be processed, may be {@code null}/empty and contain {@code null}s
     * @param comp the {@link Comparator} to use - <B>Note:</B> must be provided
     * even if no values or no non-{@code null} values exist
     * @return
     *  <ul>
     *   <li>If no objects provided then {@code null} is returned</li>
     *   <li>If any objects are non-null and unequal, the greater object.</li>
     *   <li>If all objects are non-null and equal, the first.</li>
     *   <li>If any of the objects are null, the greater of the non-null objects.</li>
     *   <li>If all the objects are null, null is returned.</li>
     *  </ul>
     */
    public static <T extends Comparable<? super T>> T maxValue(Comparator<? super T> comp, Collection<? extends T> values) {
        Validate.notNull(comp, "No comparator", ArrayUtils.EMPTY_OBJECT_ARRAY);
        if (isEmpty(values)) {
            return null;
        }

        T result = null;
        for (T value : values) {
            if (ExtendedObjectUtils.compare(value, result, comp, false) > 0) {
                result = value;
            }
        }

        return result;
    }

	public static final <T,V> int indexOf (
			final List<? extends T>							list,
			final V											value,
			final AsymmetricComparator<? super T,? super V>	c)
	{
		final int	listSize=size(list);
		if (listSize <= 0)
			return (-1);
		if (c == null)
			return (-1);

		for (int	index=0; index < listSize; index++)
		{
			final T member=list.get(index);
			if (c.compare(member, value) == 0)
				return index;
		}

		return (-1);
	}

    public static final <T,V> T matchOf (
            final List<? extends T>                         list,
            final V                                         value,
            final AsymmetricComparator<? super T,? super V> c)
    {
        final int   index=indexOf(list, value, c);
        if (index < 0)
            return null;
        else
            return list.get(index);
    }

	public static final <T,V> T sortedMatchOf (
			final List<? extends T>							list,
			final V											value,
			final AsymmetricComparator<? super T,? super V>	c)
	{
		final int	index=sortedIndexOf(list, value, c);
		if (index < 0)
			return null;
		else
			return list.get(index);
	}

	// stops if member > value
	public static final <T,V> int sortedIndexOf (
			final List<? extends T>							list,
			final V											value,
			final AsymmetricComparator<? super T,? super V>	c)
	{
		final int	listSize=size(list);
		if (listSize <= 0)
			return (-1);
		if (c == null)
			return (-1);

		for (int	index=0; index < listSize; index++)
		{
			final T 	member=list.get(index);
			final int	nRes=c.compare(member, value);
			if (nRes == 0)
				return index;
			if (nRes > 0)	// if member beyond value then stop
				return (-1);
		}

		return (-1);
	}

	public static final <T,V> T binaryMatchOf (
			final List<? extends T>							list,
			final V											value,
			final AsymmetricComparator<? super T,? super V>	c)
	{
		final int	listSize=size(list);
		if (listSize <= 0)
			return null;

		return binaryMatchOf(list, value, 0, listSize, c);
	}

	public static final <T,V> T binaryMatchOf (
			final List<? extends T>							list,
			final V											value,
			final int /* inclusive */						fromIndex,
			final int /* exclusive */						toIndex,
			final AsymmetricComparator<? super T,? super V>	c)
	{
		final int	index=binaryIndexOf(list, value, fromIndex, toIndex, c);
		if (index < 0)
			return null;
		else
			return list.get(index);
	}

	public static final <T,V> int binaryIndexOf (
			final List<? extends T>							list,
			final V											value,
			final AsymmetricComparator<? super T,? super V>	c)
	{
		final int	listSize=size(list);
		if (listSize <= 0)
			return (-1);

		return binaryIndexOf(list, value, 0, listSize, c);
	}
	
	public static final <T,V> int binaryIndexOf (
			final List<? extends T>							list,
			final V											value,
			final int /* inclusive */						fromIndex,
			final int /* exclusive */						toIndex,
			final AsymmetricComparator<? super T,? super V>	c)
	{
		final int	listSize=size(list);
		if ((fromIndex < 0) || (toIndex > listSize))
			throw new ArrayIndexOutOfBoundsException("binaryIndexOf(" + fromIndex + "-" + toIndex + ")"
												   + " indices out of available range: 0-" + listSize);
		if (fromIndex >= toIndex)
			throw new IllegalArgumentException("binaryIndexOf(" + fromIndex + "-" + toIndex + ") inversion N/A");

		// see Arrays.sort code...
		int	low=fromIndex, high=toIndex - 1;
		while (low <= high)
		{
		    final int	mid=(low + high) >>> 1;
		    final T		midVal=list.get(mid);
		    final int	cmp=c.compare(midVal, value);

		    if (cmp < 0)
		    	low = mid + 1;
		    else if (cmp > 0)
		    	high = mid - 1;
		    else
		    	return mid; // key found
		}

		return 0 - (low + 1);  // key not found but show where it should have been
	}
} 
