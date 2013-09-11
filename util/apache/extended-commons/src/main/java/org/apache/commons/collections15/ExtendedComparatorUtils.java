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

import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.ExtendedNumberUtils;

/**
 * @author Lyor G.
 * @since Jul 17, 2013 11:16:49 AM
 */
public class ExtendedComparatorUtils extends ComparatorUtils {
    public ExtendedComparatorUtils() {
        super();
    }

    @SuppressWarnings("rawtypes")
    public static final Comparator<Comparable>	COMPARABLE_COMPARATOR=
    		new Comparator<Comparable>() {
    			@SuppressWarnings("unchecked")
    			@Override
    			public int compare(Comparable o1, Comparable o2) {
    				return ObjectUtils.compare(o1, o2);
    			}
    	};

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final <T extends Comparable<T>> Comparator<T> comparableComparator() {
    	return (Comparator) COMPARABLE_COMPARATOR;
    }

    /**
     * Reverses the result of a comparison
     * @param compResult The original result
     * @return The reversed result
     */
    public static final int reverse(int compResult) {
        return 0 - ExtendedNumberUtils.signOf(compResult);
    }
    /**
     * @param items The {@link Iterable} items - ignored if {@code null}
     * @return The item that comes first in the &quot;natural&quot; order (as
     * defined by the {@link Comparable#compareTo(Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T extends Comparable<T>> T minValue(Iterable<T> items) {
        return minValue(ExtendedIteratorUtils.iteratorOf(items));
    }

    /**
     * @param e The {@link Enumeration} of items - ignored if {@code null}
     * @return The item that comes first in the &quot;natural&quot; order (as
     * defined by the {@link Comparable#compareTo(Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T extends Comparable<T>> T minValue(Enumeration<T> e) {
        return minValue((e == null) ? null : IteratorUtils.asIterator(e));
    }

    /**
     * @param iter The {@link Iterator} of items - ignored if {@code null}
     * @return The item that comes first in the &quot;natural&quot; order (as
     * defined by the {@link Comparable#compareTo(Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T extends Comparable<T>> T minValue(Iterator<T> iter) {
        return minValue(iter, ExtendedComparatorUtils.<T>comparableComparator());
    }

    /**
     * @param e The {@link Enumeration} of items - ignored if {@code null}
     * @param c The {@link Comparator} to use to decide which item comes 1st
     * @return The item that comes first in the &quot;natural&quot; order (as
     * defined by the {@link Comparator#compare(Object,Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T> T minValue(Enumeration<? extends T> e, Comparator<? super T> c) {
        return minValue((e == null) ? null : IteratorUtils.asIterator(e), c);
    }

    /**
     * @param items The {@link Iterable} items - ignored if {@code null}
     * @param c The {@link Comparator} to use to decide which item comes 1st
     * @return The item that comes first in the &quot;natural&quot; order (as
     * defined by the {@link Comparator#compare(Object,Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T> T minValue(Iterable<? extends T> items, Comparator<? super T> c) {
        return minValue(ExtendedIteratorUtils.iteratorOf(items), c);
    }

    /**
     * @param iter The {@link Iterator} of items - ignored if {@code null}
     * @param c The {@link Comparator} to use to decide which item comes 1st
     * @return The item that comes first in the &quot;natural&quot; order (as
     * defined by the {@link Comparator#compare(Object,Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T> T minValue(Iterator<? extends T> iter, Comparator<? super T> c) {
        ExtendedValidate.notNull(c, "No comparator");
        if (iter == null) {
            return null;
        }

        T   candidate=null;
        while(iter.hasNext()) {
            T   value=iter.next();
            // check if can improve
            if (candidate != null) {
                int nRes=c.compare(candidate, value);
                if (nRes <= 0) {
                    continue;
                }
            }
            
            candidate = value;
        }
        
        return candidate;
    }

    /**
     * @param items The {@link Iterable} items - ignored if {@code null}
     * @return The item that comes last in the &quot;natural&quot; order (as
     * defined by the {@link Comparable#compareTo(Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T extends Comparable<T>> T maxValue(Iterable<T> items) {
        return maxValue(ExtendedIteratorUtils.iteratorOf(items));
    }

    /**
     * @param e The {@link Enumeration} of items - ignored if {@code null}
     * @return The item that comes last in the &quot;natural&quot; order (as
     * defined by the {@link Comparable#compareTo(Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T extends Comparable<T>> T maxValue(Enumeration<T> e) {
        return maxValue((e == null) ? null : IteratorUtils.asIterator(e));
    }

    /**
     * @param iter The {@link Iterator} of items - ignored if {@code null}
     * @return The item that comes last in the &quot;natural&quot; order (as
     * defined by the {@link Comparable#compareTo(Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T extends Comparable<T>> T maxValue(Iterator<T> iter) {
        return maxValue(iter, ExtendedComparatorUtils.<T>comparableComparator());
    }

    /**
     * @param e The {@link Enumeration} of items - ignored if {@code null}
     * @param c The {@link Comparator} to use to decide which item comes 1st
     * @return The item that comes last in the &quot;natural&quot; order (as
     * defined by the {@link Comparator#compare(Object,Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T> T maxValue(Enumeration<? extends T> e, Comparator<? super T> c) {
        return maxValue((e == null) ? null : IteratorUtils.asIterator(e), c);
    }

    /**
     * @param items The {@link Iterable} items - ignored if {@code null}
     * @param c The {@link Comparator} to use to decide which item comes 1st
     * @return The item that comes last in the &quot;natural&quot; order (as
     * defined by the {@link Comparator#compare(Object,Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T> T maxValue(Iterable<? extends T> items, Comparator<? super T> c) {
        return maxValue(ExtendedIteratorUtils.iteratorOf(items), c);
    }

    /**
     * @param iter The {@link Iterator} of items - ignored if {@code null}
     * @param c The {@link Comparator} to use to decide which item comes 1st
     * @return The item that comes last in the &quot;natural&quot; order (as
     * defined by the {@link Comparator#compare(Object,Object)} contract) -
     * {@code null} if no items to scan
     */
    public static final <T> T maxValue(Iterator<? extends T> iter, Comparator<? super T> c) {
        ExtendedValidate.notNull(c, "No comparator");
        if (iter == null) {
            return null;
        }

        T   candidate=null;
        while(iter.hasNext()) {
            T   value=iter.next();
            // check if can improve
            if (candidate != null) {
                int nRes=c.compare(candidate, value);
                if (nRes >= 0) {
                    continue;
                }
            }
            
            candidate = value;
        }
        
        return candidate;
    }

    /**
     * Compares 2 objects based on their (full) class name first and then
     * by their {@link System#identityHashCode(Object)} values
     */
    public static final Comparator<Object> BY_IDENTITY_COMPARATOR=
            new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        if (o1 == o2)
                            return 0;
    
                        Class<?>    c1=(o1 == null) ? null : o1.getClass();
                        Class<?>    c2=(o2 == null) ? null : o2.getClass();
                        int         nRes=ExtendedClassUtils.BY_FULL_NAME_COMPARATOR.compare(c1, c2);
                        if (nRes != 0) {
                            return nRes;
                        }
    
                        int h1=System.identityHashCode(o1), h2=System.identityHashCode(o2);
                        if ((nRes=h1 - h2) != 0) {
                            return nRes;
                        }
                        
                        return 0;
                    }
        };
    /**
     * Returns zero ONLY if same instance - CAVEAT EMPTOR: may throw
     * {@link IllegalStateException} in extreme cases...
     */
    public static final Comparator<Object> OBJECT_INSTANCE_COMPARATOR=
            new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    if (o1 == o2)
                        return 0;
                    if (o1 == null) // o2 is not null or o1 == o2
                        return (+1);
                    if (o2 == null)
                        return (-1);
    
                    int  nRes=BY_IDENTITY_COMPARATOR.compare(o1, o2);
                    if (nRes != 0)
                        return nRes;
                    if ((nRes=o1.hashCode() - o2.hashCode()) != 0)
                        return nRes;
    
                    /*
                     * If not same instance, but same hash code, and same 
                     * identity hash code and same class then we are up the
                     * proverbial creek without a paddle
                     */
    
                    throw new IllegalStateException("All instance comparison options exhausted for "
                                  + "(" + o1.getClass().getSimpleName() + "@" + o1.hashCode() + "/" + System.identityHashCode(o1) + ")[" + o1 + "] vs."
                                  + "(" + o2.getClass().getSimpleName() + "@" + o2.hashCode() + "/" + System.identityHashCode(o2) + ")[" + o2 + "]");
                }
            };
    /**
     * Returns zero if {@link Object#equals(Object)} otherwise invokes the {@link #OBJECT_INSTANCE_COMPARATOR}
     */
    public static final Comparator<Object> OBJECT_EQUALITY_COMPARATOR=new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 == o2)
                    return 0;
                if (o1 == null) // o2 is not null or o1 == o2
                    return (+1);
                if (o2 == null)
                    return (-1);
                if (o1.equals(o2)) {
                    return 0;
                } else {
                    return OBJECT_INSTANCE_COMPARATOR.compare(o1, o2);
                }
            }
        };

}
