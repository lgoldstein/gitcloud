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

package org.apache.commons.lang3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedIteratorUtils;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author lgoldstein
 */
public class ExtendedObjectUtils extends ObjectUtils {
    /**
     * The separator used by the various <code>identityToString</code> or
     * <code>appendIdentity</code> methods to separate the class name from
     * the hash code
     */
    public static final char    IDENTITY_STRING_SEPARATOR='@';

	public ExtendedObjectUtils() {
		super();
	}

	public static final boolean isBaseType(Object o) {
		if (o == null) {
			return false;
		} else {
			return ExtendedClassUtils.isBaseType(o.getClass());
		}
	}

    public static final boolean isPrimitiveOrWrapper(Object o) {
        if (o == null) {
            return false;
        } else {
        	return ClassUtils.isPrimitiveOrWrapper(o.getClass());
        }
    }

    public static final boolean isPrimitiveWrapper(Object o) {
    	if (o == null) {
    		return false;
    	} else {
    		return ClassUtils.isPrimitiveWrapper(o.getClass());
    	}
    }
    
    public static final int hashCode(boolean v) {
    	return v ? 1 : 0;
    }

    @SuppressWarnings("rawtypes")
    private static final Transformer    cloningTransformer=
            new Transformer() {
                @Override
                public Object transform(Object input) {
                    return ObjectUtils.clone(input);
                }
            };
    /**
     * @return A {@link Transformer} that invokes {@link ObjectUtils#clone(Object)}
     */
    @SuppressWarnings("unchecked")
    public static final <T> Transformer<T,T> cloneTransformer() {
        return cloningTransformer;
    }

    @SafeVarargs
    public static final <V> List<V> cloneObjects (final V ... objList) {
        if (ArrayUtils.isEmpty(objList))
            return Collections.emptyList();
        else if (objList.length == 1)
            return Collections.singletonList(clone(objList[0]));
        else
            return cloneObjects(Arrays.asList(objList));
    }

    public static final <V> List<V> cloneObjects (final Collection<? extends V> objList) {
        if (ExtendedCollectionUtils.isEmpty(objList)) {
            return Collections.emptyList();
        }

        final List<V>   result=new ArrayList<V>(objList.size());
        for (final V obj : objList) {
            final V cpy=clone(obj);
            if (cpy != null) {
                result.add(cpy);
            }
        }

        return result;
    }

    /**
     * Executes a &quot;deep&quot; hash of the object - i.e., if it is an
     * array, {@link Iterable}, {@link Iterator}, {@link Enumeration} or
     * a {@link Map} container then its members are deep-hashed recursively
     * @param o The {@link Object} to be deep-hashed
     * @return The hash value
     */
    public static final int deepHash(Object o) {
        if (o == null) {
            return 0;
        }

        Class<?>    objType=o.getClass();
        if (objType.isArray()) {
            if (o instanceof long[]) {
                return Arrays.hashCode((long[]) o);
            } else if (o instanceof int[]) {
                return Arrays.hashCode((int[]) o);
            } else if (o instanceof short[]) {
                return Arrays.hashCode((short[]) o);
            } else if (o instanceof char[]) {
                return Arrays.hashCode((char[]) o);
            } else if (o instanceof byte[]) {
                return Arrays.hashCode((byte[]) o);
            } else if (o instanceof double[]) {
                return Arrays.hashCode((double[]) o);
            } else if (o instanceof float[]) {
                return Arrays.hashCode((float[]) o);
            } else if (o instanceof boolean[]) {
                return Arrays.hashCode((boolean[]) o);
            } else {
                // Not an array of primitives
                return ExtendedArrayUtils.deepHash((Object[]) o);
            }
        } else if (Map.class.isAssignableFrom(objType)) {
            return ExtendedMapUtils.deepHash((Map<?,?>) o);
        } else if (Iterable.class.isAssignableFrom(objType)) {
            return ExtendedIteratorUtils.deepHash((Iterable<?>) o);
        } else if (Iterator.class.isAssignableFrom(objType)) {
            return ExtendedIteratorUtils.deepHash((Iterator<?>) o);
        } else if (Enumeration.class.isAssignableFrom(objType)) {
            return ExtendedIteratorUtils.deepHash((Enumeration<?>) o);
        } else if (ExtendedClassUtils.isBaseType(objType)) {
            return hashCode(o);
        } else {
            return HashCodeBuilder.reflectionHashCode(o, false);
        }
    }
    
    /**
     * <p>Appends the result that would be produced by {@link Object#toString()}
     * if a class did not override method itself.
     * @param sb The {@link StringBuilder} to append to
     * @param o The {@link Object} whose identity is to be appended
     * @return Same appendable instance as the input
     * @throws NullPointerException if either the builder instance or the object
     * are <code>null</code>
     * @see #appendIdentity(Appendable, Object)
     */
    public static final StringBuilder identityToString(StringBuilder sb, Object o) {
        try {
            return appendIdentity(sb, o);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>Appends the result that would be produced by {@link Object#toString()}
     * if a class did not override method itself.
     * @param sb The {@link Appendable} to append to
     * @param o The {@link Object} whose identity is to be appended
     * @return Same appendable instance as the input
     * @throws NullPointerException if either the {@link Appendable} instance
     * or the object are <code>null</code>
     * @throws IOException If failed to invoke the {@link Appendable#append(CharSequence)} API(s)
     * @see #identityToString(Object)
     * @see #identityToString(StringBuffer, Object)
     */
    public static final <A extends Appendable> A appendIdentity(A sb, Object o) throws IOException {
        if (o == null) {
            throw new NullPointerException("Cannot append a null identity");
        }

        sb.append(o.getClass().getName())
          .append(IDENTITY_STRING_SEPARATOR)
          .append(Integer.toHexString(System.identityHashCode(o)));
        return sb;
    }

    /**
     * Parses the result of one of the <code>identityToString</code> or
     * <code>appendIdentity</code> methods
     * @param id The identity value - ignored if <code>null</code>/empty
     * @return A {@link Pair} whose key/left is the class name and value/right
     * is the hash code (<code>null</code> if no initial identity value)
     * @throws IllegalArgumentException If malformed identifier
     */
    public static final Pair<String,Integer> parseIdentity(CharSequence id)
            throws IllegalArgumentException {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        
        int sepPos=StringUtils.indexOf(id, IDENTITY_STRING_SEPARATOR);
        if ((sepPos <= 0) || (sepPos >= (id.length() - 1))) {
            throw new IllegalArgumentException("parseIdentity(" + id + ") malformed identity - misplaced/missing separator");
        }
        
        CharSequence    className=id.subSequence(0, sepPos), hashCode=id.subSequence(sepPos + 1, id.length());
        try {
            return Pair.of(className.toString(), Integer.valueOf(hashCode.toString(), 16));
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("parseIdentity(" + id + ") malformed hash value: " + hashCode);
        }
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
     *   <li>If any objects are non-{@code null} and unequal, the lesser object.</li>
     *   <li>If all objects are non-{@code null} and equal, the first.</li>
     *   <li>If any of the objects are {@code null}, the lesser of the non-null objects.</li>
     *   <li>If all the objects are {@code null}, {@code null} is returned.</li>
     *  </ul>
     */
    public static <T extends Comparable<? super T>> T min(Comparator<? super T> comp, @SuppressWarnings("unchecked") T... values) {
        return ExtendedCollectionUtils.minValue(comp, ExtendedArrayUtils.asList(values));
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
    public static <T extends Comparable<? super T>> T max(Comparator<? super T> comp, @SuppressWarnings("unchecked") T... values) {
        return ExtendedCollectionUtils.maxValue(comp, ExtendedArrayUtils.asList(values));
    }

    /**
     * Null safe minimum value calculator - where {@code null} is considered
     * greater than non-{@code null}
     * @param c1 1st value - may be {@code null}
     * @param c2 2nd value - may be {@code null}
     * @param comp The {@link Comparator} to use
     * @return According to {@link #compare(Object, Object, Comparator, boolean)}
     */
    public static <T> T min(T c1, T c2, Comparator<? super T> comp) {
        return min(c1, c2, comp, true);
    }

    /**
     * Null safe minimum value calculator
     * @param c1 1st value - may be {@code null}
     * @param c2 2nd value - may be {@code null}
     * @param comp The {@link Comparator} to use
     * @param nullGreater if true {@code null} is considered greater
     * than a non-{@code null} value or if false {@code null} is
     * considered less than a Non-{@code null} value
     * @return According to {@link #compare(Object, Object, Comparator, boolean)}
     */
    public static <T> T min(T c1, T c2, Comparator<? super T> comp, boolean nullGreater) {
        if (compare(c1, c2, comp, nullGreater) <= 0) {
            return c1;
        } else {
            return c2;
        }
    }

    /**
     * Null safe maximum value calculator - where {@code null} is considered
     * smaller than non-{@code null}
     * @param c1 1st value - may be {@code null}
     * @param c2 2nd value - may be {@code null}
     * @param comp The {@link Comparator} to use
     * @return According to {@link #compare(Object, Object, Comparator, boolean)}
     */
    public static <T> T max(T c1, T c2, Comparator<? super T> comp) {
        return max(c1, c2, comp, false);
    }

    /**
     * Null safe minimum value calculator
     * @param c1 1st value - may be {@code null}
     * @param c2 2nd value - may be {@code null}
     * @param comp The {@link Comparator} to use
     * @param nullGreater if true {@code null} is considered greater
     * than a non-{@code null} value or if false {@code null} is
     * considered less than a Non-{@code null} value
     * @return According to {@link #compare(Object, Object, Comparator, boolean)}
     */
    public static <T> T max(T c1, T c2, Comparator<? super T> comp, boolean nullGreater) {
        if (compare(c1, c2, comp, nullGreater) >= 0) {
            return c1;
        } else {
            return c2;
        }
    }

    /**
     * <p>
     * Null safe comparison of objects using a {@link Comparator}.
     * <B>Note:</B> {@code null} is assumed to be less than a
     * non-{@code null} value.
     * </p>
     * @param <T> type of the values processed by this method
     * @param c1  the first object, may be {@code null}
     * @param c2  the second object, may be {@code null}
     * @param comp the {@link Comparator} to use. <B>Note:</B> if any of the
     * compared objects is {@code null} the comparator is not invoked (though
     * it must be provided anyway)
     * @return a negative value if c1 < c2, zero if c1 = c2
     * and a positive value if c1 > c2
     */
    public static <T> int compare(T c1, T c2, Comparator<? super T> comp) {
        return compare(c1, c2, comp, false);
    }

    /**
     * Null safe comparison of objects using a {@link Comparator}.
     * @param <T> type of the values processed by this method
     * @param c1  the first object, may be {@code null}
     * @param c2  the second object, may be {@code null}
     * @param comp the {@link Comparator} to use. <B>Note:</B> if any of the
     * compared objects is {@code null} the comparator is not invoked (though
     * it must be provided anyway)
     * @param nullGreater if true {@code null} is considered greater
     *  than a non-{@code null} value or if false {@code null} is
     *  considered less than a Non-{@code null} value
     * @return a negative value if c1 < c2, zero if c1 = c2
     *  and a positive value if c1 > c2
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T> int compare(T c1, T c2, Comparator<? super T> comp, boolean nullGreater) {
        Validate.notNull(comp, "No comparator", ArrayUtils.EMPTY_OBJECT_ARRAY);

        if (c1 == c2) {
            return 0;
        } else if (c1 == null) {
            return nullGreater ? 1 : -1;
        } else if (c2 == null) {
            return nullGreater ? -1 : 1;
        } else {
            return comp.compare(c1, c2);
        }
    }
}
