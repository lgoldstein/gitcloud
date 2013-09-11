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

package org.apache.commons.lang3;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedComparatorUtils;
import org.apache.commons.lang3.math.ExtendedNumberUtils;

/**
 * @author lgoldstein
 */
public class ExtendedArrayUtils extends ArrayUtils {
	public ExtendedArrayUtils() {
		super();
	}

	/**
	 * A &quot;safe&quot; version of {@link Arrays#asList(Object...)}
	 * @param arr The elements array - if {@code null}/empty then {@link Collections#emptyList()}
	 * is returned
	 * @return A {@link List} encapsulating the array (never {@code null})
	 */
	@SafeVarargs
	public static final <T> List<T> asList(T ... arr) {
		if (length(arr) <= 0) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(arr);
		}
	}

	public static final int hashCode(Object ... arr) {
	    return ExtendedCollectionUtils.hashCode(asList(arr));
	}

   public static final int deepHash(Object ... arr) {
        return ExtendedCollectionUtils.deepHash(asList(arr));
    }

    public static final <T> int length(T[] array) {
        return getLength(array);
    }
    
    public static final int length(int ... array) {
        return getLength(array);
    }
    
    public static final int length(long ... array) {
        return getLength(array);
    }
    
    public static final int length(short ... array) {
        return getLength(array);
    }
    
    public static final int length(double ... array) {
        return getLength(array);
    }
    
    public static final int length(float ... array) {
        return getLength(array);
    }
    
    public static final int length(byte ... array) {
        return getLength(array);
    }
    
    public static final int length(char ... array) {
        return getLength(array);
    }
    
    public static final int length(boolean ... array) {
        return getLength(array);
    }

    public static final int hashCode(byte[] buf, int offset, int length) {
        int hashValue=0;
        for (int index=offset, l=0; l < length; index++, l++) {
            hashValue += 31 * hashValue + buf[index];
        }
        
        return hashValue;
    }

    public static final int hashCode(short[] buf, int offset, int length) {
        int hashValue=0;
        for (int index=offset, l=0; l < length; index++, l++) {
            hashValue += 31 * hashValue + buf[index];
        }
        
        return hashValue;
    }

    public static final int hashCode(int[] buf, int offset, int length) {
        int hashValue=0;
        for (int index=offset, l=0; l < length; index++, l++) {
            hashValue += 31 * hashValue + buf[index];
        }
        
        return hashValue;
    }

    public static final int hashCode(long[] buf, int offset, int length) {
        int hashValue=0;
        for (int index=offset, l=0; l < length; index++, l++) {
            hashValue += 31 * hashValue + ExtendedNumberUtils.hashCode(buf[index]);
        }
        
        return hashValue;
    }

    public static final int hashCode(float[] buf, int offset, int length) {
        int hashValue=0;
        for (int index=offset, l=0; l < length; index++, l++) {
            hashValue += 31 * hashValue + ExtendedNumberUtils.hashCode(buf[index]);
        }
        
        return hashValue;
    }

    public static final int hashCode(double[] buf, int offset, int length) {
        int hashValue=0;
        for (int index=offset, l=0; l < length; index++, l++) {
            hashValue += 31 * hashValue + ExtendedNumberUtils.hashCode(buf[index]);
        }
        
        return hashValue;
    }

    public static final int hashCode(char[] buf, int offset, int length) {
        int hashValue=0;
        for (int index=offset, l=0; l < length; index++, l++) {
            hashValue += 31 * hashValue + buf[index];
        }
        
        return hashValue;
    }

    public static final int hashCode(boolean[] buf, int offset, int length) {
        int hashValue=0;
        for (int index=offset, l=0; l < length; index++, l++) {
            if (buf[index]) {
                hashValue += 31 * hashValue + index;
            }
        }
        
        return hashValue;
    }

    public static final int hashCode(Object[] buf, int offset, int length) {
        int hashValue=0;
        for (int index=offset, l=0; l < length; index++, l++) {
            hashValue += 31 * hashCode(buf[index]);
        }
        
        return hashValue;
    }

    /**
     * A {@link Comparator} that compares 2 boolean arrays using {@link #findFirstNonMatchingIndex(boolean[], boolean[])}.
     * If the non-matching index is below the &quot;common&quot; length, then
     * the matching values in the arrays are compared. Otherwise, the shorter
     * array is deemed to come 1st
     */
    public static final Comparator<boolean[]>  BOOLEAN_ARRAYS_COMPARATOR=new Comparator<boolean[]>() {
                @Override
                public int compare(boolean[] a1, boolean[] a2) {
                    int l1=length(a1), l2=length(a2), cmnLen=Math.min(l1, l2);
                    int index=findFirstNonMatchingIndex(a1, a2);
                    if (index < 0) {
                        return 0;
                    }
                    
                    if (index < cmnLen) {
                        return Boolean.valueOf(a1[index]).compareTo(Boolean.valueOf(a2[index]));
                    } else {
                        return ExtendedNumberUtils.signOf(l1 - l2);
                    }
                }
            };

    /**
     * Scans 2 given byte arrays for the 1st non-matching value
     * @param a1 The 1st array (may be {@code null}/empty)
     * @param a2 The 2nd array (may be {@code null}/empty)
     * @return The index of the 1st non-matching value - negative if both
     * arrays of same length and have same values. <B>Note:</B> if arrays are
     * of different length but one is a &quot;prefix&quot; of the other
     * then returns the length of the shorter one as the non-matching index
     */
    public static final int findFirstNonMatchingIndex(boolean[] a1, boolean[] a2) {
        if (a1 == a2) { // take care of the obvious
            return (-1);
        }

        int l1=length(a1), l2=length(a2), cmpLen=Math.min(l1, l2);
        for (int    index=0; index < cmpLen; index++) {
            if (a1[index] != a2[index]) {
                return index;
            }
        }

        if (l1 != l2) {
            return cmpLen;
        } else {
            return (-1);
        }
    }

    /**
     * A {@link Comparator} that compares 2 byte arrays using {@link #findFirstNonMatchingIndex(byte[], byte[])}.
     * If the non-matching index is below the &quot;common&quot; length, then
     * the matching values in the arrays are compared. Otherwise, the shorter
     * array is deemed to come 1st
     */
    public static final Comparator<byte[]>  BYTE_ARRAYS_COMPARATOR=new Comparator<byte[]>() {
                @Override
                public int compare(byte[] a1, byte[] a2) {
                    int l1=length(a1), l2=length(a2), cmnLen=Math.min(l1, l2);
                    int index=findFirstNonMatchingIndex(a1, a2);
                    if (index < 0) {
                        return 0;
                    }
                    
                    if (index < cmnLen) {
                        return ExtendedNumberUtils.signOf(a1[index] - a2[index]);
                    } else {
                        return ExtendedNumberUtils.signOf(l1 - l2);
                    }
                }
            };

    /**
     * Scans 2 given byte arrays for the 1st non-matching value
     * @param a1 The 1st array (may be {@code null}/empty)
     * @param a2 The 2nd array (may be {@code null}/empty)
     * @return The index of the 1st non-matching value - negative if both
     * arrays of same length and have same values. <B>Note:</B> if arrays are
     * of different length but one is a &quot;prefix&quot; of the other
     * then returns the length of the shorter one as the non-matching index
     */
    public static final int findFirstNonMatchingIndex(byte[] a1, byte[] a2) {
        if (a1 == a2) { // take care of the obvious
            return (-1);
        }

        int l1=length(a1), l2=length(a2), cmpLen=Math.min(l1, l2);
        for (int    index=0; index < cmpLen; index++) {
            if (a1[index] != a2[index]) {
                return index;
            }
        }

        if (l1 != l2) {
            return cmpLen;
        } else {
            return (-1);
        }
    }

    /**
     * A {@link Comparator} that compares 2 {@code short} arrays using
     * {@link #findFirstNonMatchingIndex(short[], short[])}. If the
     * non-matching index is below the &quot;common&quot; length, then the
     * matching values in the arrays are compared. Otherwise, the shorter
     * array is deemed to come 1st
     */
    public static final Comparator<short[]>  SHORT_ARRAYS_COMPARATOR=new Comparator<short[]>() {
                @Override
                public int compare(short[] a1, short[] a2) {
                    int l1=length(a1), l2=length(a2), cmnLen=Math.min(l1, l2);
                    int index=findFirstNonMatchingIndex(a1, a2);
                    if (index < 0) {
                        return 0;
                    }
                    
                    if (index < cmnLen) {
                        return ExtendedNumberUtils.signOf(a1[index] - a2[index]);
                    } else {
                        return ExtendedNumberUtils.signOf(l1 - l2);
                    }
                }
            };

    /**
     * Scans 2 given {@code short} arrays for the 1st non-matching value
     * @param a1 The 1st array (may be {@code null}/empty)
     * @param a2 The 2nd array (may be {@code null}/empty)
     * @return The index of the 1st non-matching value - negative if both
     * arrays of same length and have same values. <B>Note:</B> if arrays are
     * of different length but one is a &quot;prefix&quot; of the other
     * then returns the length of the shorter one as the non-matching index
     */
    public static final int findFirstNonMatchingIndex(short[] a1, short[] a2) {
        if (a1 == a2) { // take care of the obvious
            return (-1);
        }

        int l1=length(a1), l2=length(a2), cmpLen=Math.min(l1, l2);
        for (int    index=0; index < cmpLen; index++) {
            if (a1[index] != a2[index]) {
                return index;
            }
        }

        if (l1 != l2) {
            return cmpLen;
        } else {
            return (-1);
        }
    }

    /**
     * A {@link Comparator} that compares 2 {@code int} arrays using
     * {@link #findFirstNonMatchingIndex(int[], int[])}. If the
     * non-matching index is below the &quot;common&quot; length, then the
     * matching values in the arrays are compared. Otherwise, the shorter
     * array is deemed to come 1st
     */
    public static final Comparator<int[]>  INT_ARRAYS_COMPARATOR=new Comparator<int[]>() {
                @Override
                public int compare(int[] a1, int[] a2) {
                    int l1=length(a1), l2=length(a2), cmnLen=Math.min(l1, l2);
                    int index=findFirstNonMatchingIndex(a1, a2);
                    if (index < 0) {
                        return 0;
                    }
                    
                    if (index < cmnLen) {
                        return ExtendedNumberUtils.signOf(a1[index] - a2[index]);
                    } else {
                        return ExtendedNumberUtils.signOf(l1 - l2);
                    }
                }
            };

    /**
     * Scans 2 given {@code int} arrays for the 1st non-matching value
     * @param a1 The 1st array (may be {@code null}/empty)
     * @param a2 The 2nd array (may be {@code null}/empty)
     * @return The index of the 1st non-matching value - negative if both
     * arrays of same length and have same values. <B>Note:</B> if arrays are
     * of different length but one is a &quot;prefix&quot; of the other
     * then returns the length of the shorter one as the non-matching index
     */
    public static final int findFirstNonMatchingIndex(int[] a1, int[] a2) {
        if (a1 == a2) { // take care of the obvious
            return (-1);
        }

        int l1=length(a1), l2=length(a2), cmpLen=Math.min(l1, l2);
        for (int    index=0; index < cmpLen; index++) {
            if (a1[index] != a2[index]) {
                return index;
            }
        }

        if (l1 != l2) {
            return cmpLen;
        } else {
            return (-1);
        }
    }

    /**
     * A {@link Comparator} that compares 2 {@code long} arrays using
     * {@link #findFirstNonMatchingIndex(long[], long[])}. If the
     * non-matching index is below the &quot;common&quot; length, then the
     * matching values in the arrays are compared. Otherwise, the shorter
     * array is deemed to come 1st
     */
    public static final Comparator<long[]>  LONG_ARRAYS_COMPARATOR=new Comparator<long[]>() {
                @Override
                public int compare(long[] a1, long[] a2) {
                    int l1=length(a1), l2=length(a2), cmnLen=Math.min(l1, l2);
                    int index=findFirstNonMatchingIndex(a1, a2);
                    if (index < 0) {
                        return 0;
                    }
                    
                    if (index < cmnLen) {
                        return ExtendedNumberUtils.signOf(a1[index] - a2[index]);
                    } else {
                        return ExtendedNumberUtils.signOf(l1 - l2);
                    }
                }
            };

    /**
     * Scans 2 given {@code long} arrays for the 1st non-matching value
     * @param a1 The 1st array (may be {@code null}/empty)
     * @param a2 The 2nd array (may be {@code null}/empty)
     * @return The index of the 1st non-matching value - negative if both
     * arrays of same length and have same values. <B>Note:</B> if arrays are
     * of different length but one is a &quot;prefix&quot; of the other
     * then returns the length of the shorter one as the non-matching index
     */
    public static final int findFirstNonMatchingIndex(long[] a1, long[] a2) {
        if (a1 == a2) { // take care of the obvious
            return (-1);
        }

        int l1=length(a1), l2=length(a2), cmpLen=Math.min(l1, l2);
        for (int    index=0; index < cmpLen; index++) {
            if (a1[index] != a2[index]) {
                return index;
            }
        }

        if (l1 != l2) {
            return cmpLen;
        } else {
            return (-1);
        }
    }

    /**
     * A {@link Comparator} that compares 2 {@code float} arrays using
     * {@link #findFirstNonMatchingIndex(float[], float[])}. If the
     * non-matching index is below the &quot;common&quot; length, then the
     * matching values in the arrays are compared. Otherwise, the shorter
     * array is deemed to come 1st
     */
    public static final Comparator<float[]>  FLOAT_ARRAYS_COMPARATOR=new Comparator<float[]>() {
                @Override
                public int compare(float[] a1, float[] a2) {
                    int l1=length(a1), l2=length(a2), cmnLen=Math.min(l1, l2);
                    int index=findFirstNonMatchingIndex(a1, a2);
                    if (index < 0) {
                        return 0;
                    }
                    
                    if (index < cmnLen) {
                        return Float.compare(a1[index], a2[index]);
                    } else {
                        return ExtendedNumberUtils.signOf(l1 - l2);
                    }
                }
            };

    /**
     * Scans 2 given {@code float} arrays for the 1st non-matching value.
     * <B>Note:</B> uses {@link ExtendedNumberUtils#compare(float, float)}
     * in order to handle {@code NaN}s correctly
     * @param a1 The 1st array (may be {@code null}/empty)
     * @param a2 The 2nd array (may be {@code null}/empty)
     * @return The index of the 1st non-matching value - negative if both
     * arrays of same length and have same values. <B>Note:</B> if arrays are
     * of different length but one is a &quot;prefix&quot; of the other
     * then returns the length of the shorter one as the non-matching index
     */
    public static final int findFirstNonMatchingIndex(float[] a1, float[] a2) {
        if (a1 == a2) { // take care of the obvious
            return (-1);
        }

        int l1=length(a1), l2=length(a2), cmpLen=Math.min(l1, l2);
        for (int    index=0; index < cmpLen; index++) {
            if (ExtendedNumberUtils.compare(a1[index], a2[index]) != 0) {
                return index;
            }
        }

        if (l1 != l2) {
            return cmpLen;
        } else {
            return (-1);
        }
    }

    /**
     * A {@link Comparator} that compares 2 {@code double} arrays using
     * {@link #findFirstNonMatchingIndex(double[], double[])}. If the
     * non-matching index is below the &quot;common&quot; length, then the
     * matching values in the arrays are compared. Otherwise, the shorter
     * array is deemed to come 1st
     */
    public static final Comparator<double[]>  DOUBLE_ARRAYS_COMPARATOR=new Comparator<double[]>() {
                @Override
                public int compare(double[] a1, double[] a2) {
                    int l1=length(a1), l2=length(a2), cmnLen=Math.min(l1, l2);
                    int index=findFirstNonMatchingIndex(a1, a2);
                    if (index < 0) {
                        return 0;
                    }
                    
                    if (index < cmnLen) {
                        return Double.compare(a1[index], a2[index]);
                    } else {
                        return ExtendedNumberUtils.signOf(l1 - l2);
                    }
                }
            };

    /**
     * Scans 2 given {@code double} arrays for the 1st non-matching value.
     * <B>Note:</B> uses {@link ExtendedNumberUtils#compare(double, double)}
     * in order to handle {@code NaN}s correctly
     * @param a1 The 1st array (may be {@code null}/empty)
     * @param a2 The 2nd array (may be {@code null}/empty)
     * @return The index of the 1st non-matching value - negative if both
     * arrays of same length and have same values. <B>Note:</B> if arrays are
     * of different length but one is a &quot;prefix&quot; of the other
     * then returns the length of the shorter one as the non-matching index
     */
    public static final int findFirstNonMatchingIndex(double[] a1, double[] a2) {
        if (a1 == a2) { // take care of the obvious
            return (-1);
        }

        int l1=length(a1), l2=length(a2), cmpLen=Math.min(l1, l2);
        for (int    index=0; index < cmpLen; index++) {
            if (ExtendedNumberUtils.compare(a1[index], a2[index]) != 0) {
                return index;
            }
        }

        if (l1 != l2) {
            return cmpLen;
        } else {
            return (-1);
        }
    }

    /**
     * @param buf1 First buffer data
     * @param offs1 First buffer offset
     * @param buf2 Second buffer data
     * @param offs2 Second buffer offset
     * @param length Number of elements to compare
     * @return The zero-based offset of the 1st different element - negative
     * if all compared elements are equal
     */
    public static final int diffOffset(byte[] buf1, int offs1, byte[] buf2, int offs2, int length) {
        for (int i1=offs1, i2=offs2, l=0; l < length; i1++, i2++, l++) {
            byte    b1=buf1[i1], b2=buf2[i2];
            if (b1 != b2) {
                return l;
            }
        }
        
        return INDEX_NOT_FOUND;
    }

    /**
     * @param buf1 First buffer data
     * @param offs1 First buffer offset
     * @param buf2 Second buffer data
     * @param offs2 Second buffer offset
     * @param length Number of elements to compare
     * @return The zero-based offset of the 1st different element - negative
     * if all compared elements are equal
     */
    public static final int diffOffset(short[] buf1, int offs1, short[] buf2, int offs2, int length) {
        for (int i1=offs1, i2=offs2, l=0; l < length; i1++, i2++, l++) {
            short    b1=buf1[i1], b2=buf2[i2];
            if (b1 != b2) {
                return l;
            }
        }
        
        return INDEX_NOT_FOUND;
    }

    /**
     * @param buf1 First buffer data
     * @param offs1 First buffer offset
     * @param buf2 Second buffer data
     * @param offs2 Second buffer offset
     * @param length Number of elements to compare
     * @return The zero-based offset of the 1st different element - negative
     * if all compared elements are equal
     */
    public static final int diffOffset(int[] buf1, int offs1, int[] buf2, int offs2, int length) {
        for (int i1=offs1, i2=offs2, l=0; l < length; i1++, i2++, l++) {
            int    b1=buf1[i1], b2=buf2[i2];
            if (b1 != b2) {
                return l;
            }
        }
        
        return INDEX_NOT_FOUND;
    }

    /**
     * @param buf1 First buffer data
     * @param offs1 First buffer offset
     * @param buf2 Second buffer data
     * @param offs2 Second buffer offset
     * @param length Number of elements to compare
     * @return The zero-based offset of the 1st different element - negative
     * if all compared elements are equal
     */
    public static final int diffOffset(long[] buf1, int offs1, long[] buf2, int offs2, int length) {
        for (int i1=offs1, i2=offs2, l=0; l < length; i1++, i2++, l++) {
            long    b1=buf1[i1], b2=buf2[i2];
            if (b1 != b2) {
                return l;
            }
        }
        
        return INDEX_NOT_FOUND;
    }

    /**
     * @param buf1 First buffer data
     * @param offs1 First buffer offset
     * @param buf2 Second buffer data
     * @param offs2 Second buffer offset
     * @param length Number of elements to compare
     * @return The zero-based offset of the 1st different element - negative
     * if all compared elements are equal
     */
    public static final int diffOffset(float[] buf1, int offs1, float[] buf2, int offs2, int length) {
        for (int i1=offs1, i2=offs2, l=0; l < length; i1++, i2++, l++) {
            float    b1=buf1[i1], b2=buf2[i2];
            if (Float.compare(b1, b2) != 0) {
                return l;
            }
        }
        
        return INDEX_NOT_FOUND;
    }

    /**
     * @param buf1 First buffer data
     * @param offs1 First buffer offset
     * @param buf2 Second buffer data
     * @param offs2 Second buffer offset
     * @param length Number of elements to compare
     * @return The zero-based offset of the 1st different element - negative
     * if all compared elements are equal
     */
    public static final int diffOffset(double[] buf1, int offs1, double[] buf2, int offs2, int length) {
        for (int i1=offs1, i2=offs2, l=0; l < length; i1++, i2++, l++) {
            double    b1=buf1[i1], b2=buf2[i2];
            if (Double.compare(b1, b2) != 0) {
                return l;
            }
        }
        
        return INDEX_NOT_FOUND;
    }

    /**
     * @param buf1 First buffer data
     * @param offs1 First buffer offset
     * @param buf2 Second buffer data
     * @param offs2 Second buffer offset
     * @param length Number of elements to compare
     * @return The zero-based offset of the 1st different element - negative
     * if all compared elements are equal
     */
    public static final int diffOffset(char[] buf1, int offs1, char[] buf2, int offs2, int length) {
        for (int i1=offs1, i2=offs2, l=0; l < length; i1++, i2++, l++) {
            char    b1=buf1[i1], b2=buf2[i2];
            if (b1 != b2) {
                return l;
            }
        }
        
        return INDEX_NOT_FOUND;
    }

    /**
     * @param buf1 First buffer data
     * @param offs1 First buffer offset
     * @param buf2 Second buffer data
     * @param offs2 Second buffer offset
     * @param length Number of elements to compare
     * @return The zero-based offset of the 1st different element - negative
     * if all compared elements are equal
     */
    public static final int diffOffset(boolean[] buf1, int offs1, boolean[] buf2, int offs2, int length) {
        for (int i1=offs1, i2=offs2, l=0; l < length; i1++, i2++, l++) {
            boolean    b1=buf1[i1], b2=buf2[i2];
            if (b1 != b2) {
                return l;
            }
        }
        
        return INDEX_NOT_FOUND;
    }

    /**
     * @param buf1 First array of objects
     * @param offs1 First array offset
     * @param buf2 Second array of objects
     * @param offs2 Second array offset
     * @param length Number of elements to compare
     * @return The zero-based offset of the 1st different element - negative
     * if all compared elements are equal
     * @see #diffOffset(Object[], int, Object[], int, Comparator, int)
     */
    public static final <T> int diffOffset(T[] buf1, int offs1, T[] buf2, int offs2, int length) {
        return diffOffset(buf1, offs1, buf2, offs2, null, length);
    }

    /**
     * @param buf1 First array of objects
     * @param offs1 First array offset
     * @param buf2 Second array of objects
     * @param offs2 Second array offset
     * @param length Number of elements to compare using {@link Comparable#compareTo(Object)}
     * @return The zero-based offset of the 1st different element - negative
     * if all compared elements are equal
     * @see #diffOffset(Object[], int, Object[], int, Comparator, int)
     */
    public static final <T extends Comparable<T>> int diffComparableOffset(T[] buf1, int offs1, T[] buf2, int offs2, int length) {
        return diffOffset(buf1, offs1, buf2, offs2, ExtendedComparatorUtils.<T>comparableComparator(), length);
    }

    /**
     * @param buf1 First array of objects
     * @param offs1 First array offset
     * @param buf2 Second array of objects
     * @param offs2 Second array offset
     * @param c A {@link Comparator} to use to check for equality - if
     * {@code null} then {@link ObjectUtils#equals(Object, Object)} is
     * used
     * @param length Number of elements to compare
     * @return The zero-based offset of the 1st different element - negative
     * if all compared elements are equal
     */
    public static final <T> int diffOffset(T[] buf1, int offs1, T[] buf2, int offs2, Comparator<? super T> c, int length) {
        for (int i1=offs1, i2=offs2, l=0; l < length; i1++, i2++, l++) {
            T   v1=buf1[i1], v2=buf2[i2];
            if (c == null) {
                if (!ObjectUtils.equals(v1, v2)) {
                    return l;
                }
            } else {
                if (c.compare(v1, v2) != 0) {
                    return l;
                }
            }
        }
        
        return INDEX_NOT_FOUND;
    }

	public static final List<String> toStringList(Object ... objs) {
		if (length(objs) <= 0) {
			return Collections.emptyList();
		} else {
			return ExtendedStringUtils.toStringList(Arrays.asList(objs));
		}
	}
	
	public static final String toString(CharSequence separator, byte ... data) {
	    return toString(separator, data, 0, length(data));
	}

	public static final String toString(CharSequence separator, byte[] data, int offset, int length) {
	    if (length <= 0) {
	        return "";
	    }
	    
	    try {
	        return append(new StringBuilder(length * (3 + ExtendedCharSequenceUtils.getSafeLength(separator))), separator, data, offset, length).toString();
	    } catch(IOException e) {
	        throw new RuntimeException(e);
	    }
	}

	public static final <A extends Appendable> A append(A sb, CharSequence separator, byte ... data) throws IOException {
	    return append(sb, separator, data, 0, length(data));
	}
	
	public static final <A extends Appendable> A append(A sb, CharSequence separator, byte[] data, int offset, int length) throws IOException {
	    if (length <= 0) {
	        return sb;
	    }
	    
	    for (int   index=0, pos=offset; index < length; index++, pos++) {
	        if ((index > 0) && (ExtendedCharSequenceUtils.getSafeLength(separator) > 0)) {
	            sb.append(separator);
	        }
	        
	        sb.append(String.valueOf(data[pos]));
	    }
	    
	    return sb;
	}
}
