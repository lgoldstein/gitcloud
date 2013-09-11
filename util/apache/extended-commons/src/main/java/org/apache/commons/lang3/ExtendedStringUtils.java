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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.management.ObjectName;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedTransformer;

/**
 * @author lgoldstein
 */
public class ExtendedStringUtils extends StringUtils {
	public ExtendedStringUtils() {
		super();
	}

	/**
	 * Converts a {@link Collection} of {@link Object}-s to an equivalent
	 * {@link List} of their {@link Object#toString()} values.
	 * @param objs The original collection of objects - may be
	 * <code>null</code>/empty
	 * @return A list of the <U>safe</U> {@link Object#toString()} values
	 * of the objects
	 * @see #SAFE_TOSTRING_XFORMER
	 */
	public static final List<String> toStringList(Collection<?> objs) {
		return ExtendedCollectionUtils.collectToList(objs, SAFE_TOSTRING_XFORMER);
	}

	/**
	 * An {@link ExtendedTransformer} implementation that transforms any
	 * {@link Object} into a {@link String} using the {@link #safeToString(Object)}
	 * method
	 */
	public static final ExtendedTransformer<Object,String>	SAFE_TOSTRING_XFORMER=
			new AbstractExtendedTransformer<Object, String>(Object.class,String.class) {
				@Override
				public String transform(Object input) {
					return safeToString(input);
				}
			};

    /**
     * An {@link ExtendedTransformer} implementation that transforms any
     * {@link Object} into an <U>uppercase</U> {@link String} using the
     * {@link #SAFE_TOSTRING_XFORMER}. <B>Note:</B> returns <code>null</code>
     * if the transformed object is <code>null</code>. 
     */
    public static final ExtendedTransformer<Object,String>  TO_UPPERCASE_XFORMER=
            new AbstractExtendedTransformer<Object, String>(Object.class,String.class) {
                @Override
                public String transform(Object input) {
                    String  s=SAFE_TOSTRING_XFORMER.transform(input);
                    if (s == null) {
                        return null;
                    } else {
                        return s.toUpperCase();
                    }
                }
            };

    /**
     * An {@link ExtendedTransformer} implementation that transforms any
     * {@link Object} into an <U>lowercase</U> {@link String} using the
     * {@link #SAFE_TOSTRING_XFORMER}. <B>Note:</B> returns <code>null</code>
     * if the transformed object is <code>null</code>. 
     */
    public static final ExtendedTransformer<Object,String>  TO_LOWERCASE_XFORMER=
            new AbstractExtendedTransformer<Object, String>(Object.class,String.class) {
                @Override
                public String transform(Object input) {
                    String  s=SAFE_TOSTRING_XFORMER.transform(input);
                    if (s == null) {
                        return null;
                    } else {
                        return s.toLowerCase();
                    }
                }
            };

    /**
     * @param obj The {@link Object} to be checked
     * @return <code>null</code> if the object is <code>null</code>,
     * {@link Object#toString()} otherwise. <B>Note:</B> if the input object
     * is already a {@link String} then it is simply returned.
     */
    public static final String safeToString (Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof String) {
            return (String) obj;
        } else {
            return obj.toString();
        }
    }

    /**
     * @param caseSensitive Whether to return a case sensitive comparator
     * or insensitive one
     * @return The matching {@link Comparator}
     * @see String#CASE_INSENSITIVE_ORDER
     * @see #STRING_SENSITIVE_ORDER
     */
    public static final Comparator<String> stringCaseComparator(boolean caseSensitive) {
    	return caseSensitive ? ExtendedStringUtils.STRING_SENSITIVE_ORDER : String.CASE_INSENSITIVE_ORDER;
    }

    /**
     * The complement of {@link String#CASE_INSENSITIVE_ORDER}
     */
    public static final Comparator<String> STRING_SENSITIVE_ORDER=new Comparator<String>() {
        	@Override
        	public int compare(String s1, String s2) {
        		return safeCompare(s1, s2);
        	}
        };

    /**
     * Compares 2 {@link String}-s allowing for <code>null</code>'s
     * @param s1 1st string
     * @param s2 2nd string
     * @return Same as {@link String#compareTo(String)} except that <code>null</code>
     * takes precedence over non-<code>null</code>
     * @see #safeCompare(String, String, boolean)
     */
    public static final int safeCompare (String s1, String s2) {
        return safeCompare(s1, s2, true);
    }
    
    /**
     * Compares 2 {@link String}-s allowing for <code>null</code>'s
     * @param s1 1st string
     * @param s2 2nd string
     * @param caseSensitive
     * @return Same as {@link String#compareTo(String)} or {@link String#compareToIgnoreCase(String)}
     * (as per the sensitivity flag) except that <code>null</code> takes
     * precedence over non-<code>null</code>
     */
    public static final int safeCompare (String s1, String s2, boolean caseSensitive) {
        if (s1 == s2) {
            return 0;
        } else if (s1 == null) {    // s2 cannot be null or s1 == s2...
            return (-1);
        } else if (s2 == null) {
            return (+1);
        } else if (caseSensitive) {
            return s1.compareTo(s2);
        } else {
            return s1.compareToIgnoreCase(s2);
        }
    }

    /**
     * Checks if a value is quoted, and if so, then un-quotes it using {@link ObjectName#unquote(String)}
     * @param value The input {@link String} value - if <code>null</code> then nothing is done
     * @return The un-quoted result - same as input if already un-quoted
     * @throws IllegalArgumentException If the value is an &quot;imbalanced&quot;
     * quoted value - i.e., starts with a quote but does not end in one or
     * vice versa
     */
    public static String smartUnquoteObjectName (String value) throws IllegalArgumentException {
        int vLen=ExtendedCharSequenceUtils.getSafeLength(value);
        if (vLen <= 0) {
        	return value;	// no quotes
        }

        char    startChar=value.charAt(0), endChar=value.charAt(vLen - 1);
        if ((startChar != '"') && (endChar != '"')) {
        	return value;	// no quotes;
        }

        if (vLen < 2) {
            throw new IllegalArgumentException("Imbalanced quotes[string too small]: " + value);
        }
        if (startChar != '"') {
            throw new IllegalArgumentException("Imbalanced quotes[no start quote]: " + value);
        }
        if (endChar != '"') {
            throw new IllegalArgumentException("Imbalanced quotes[no end quote]: " + value);
        }
            
        return ObjectName.unquote(value);
    }

    /**
     * Checks is a value is already quoted - if so, then does nothing, else
     * invokes {@link ObjectName#quote(String)} on it
     * @param value The input {@link String} value - if <code>null</code> then nothing is done
     * @return The quoted result - same as input if already quoted
     * @throws IllegalArgumentException If the value is an &quot;imbalanced&quot;
     * quoted value - i.e., starts with a quote but does not end in one or
     * vice versa
     */
    public static String smartQuoteObjectName (String value) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }

        int vLen=value.length();
        if (vLen > 0) {
            char    startChar=value.charAt(0), endChar=value.charAt(vLen - 1);
            if ((startChar == '"') || (endChar == '"')) {
                if (vLen < 2)
                    throw new IllegalArgumentException("Imbalanced quotes[string too small]: " + value);
                if (startChar != '"')
                    throw new IllegalArgumentException("Imbalanced quotes[no start quote]: " + value);
                if (endChar != '"')
                    throw new IllegalArgumentException("Imbalanced quotes[no end quote]: " + value);

                return value;   // already quoted
            }            
        }
        
        return ObjectName.quote(value);
    }
    

    /**
     * @param s The {@link String} value to calculate the hash code on - may
     * be <code>null</code>/empty in which case a value of zero is returned
     * @return The calculated hash code
     * @see #hashCode(String, Boolean)
     */
    public static final int hashCode (String s) {
    	return hashCode(s, null);
    }

    /**
     * @param s The {@link String} value to calculate the hash code on - may
     * be <code>null</code>/empty in which case a value of zero is returned
     * @param useUppercase Whether to convert the string to uppercase, lowercase
     * or not at all:
     * <UL>
     * 		<LI><code>null</code> - no conversion</LI>
     * 		<LI>{@link Boolean#TRUE} - get hash code of uppercase</LI>
     * 		<LI>{@link Boolean#FALSE} - get hash code of lowercase</LI>
     * </UL>
     * @return The calculated hash code
     */
    public static final int hashCode (String s, Boolean useUppercase) {
        if (isEmpty(s)) {
            return 0;
        } else if (useUppercase == null) {
        	return s.hashCode();
        } else if (useUppercase.booleanValue()) {
            return s.toUpperCase().hashCode();
        } else {
            return s.toLowerCase().hashCode();
        }
    }

    /**
     * @param s1 1st {@link CharSequence} to compare
     * @param s2 2nd {@link CharSequence} to compare
     * @return Index of 1st non-matching character - negative if all match
     */
    public static int findFirstNonMatchingCharacterIndex (CharSequence s1, CharSequence s2) {
    	int	l1=ExtendedCharSequenceUtils.getSafeLength(s1), l2=ExtendedCharSequenceUtils.getSafeLength(s2);
    	return findFirstNonMatchingCharacterIndex(s1, s2, 0, Math.max(l1, l2));
    }

    /**
     * @param s1 1st {@link CharSequence} to compare
     * @param s2 2nd {@link CharSequence} to compare
     * @param startIndex Index to start comparing the character
     * @param maxLen Max. number of characters to compare
     * @return Index of 1st non-matching character - negative if all match.
     * <B>Note:</B> if one sequence is longer than the other and the shorter
     * one is a prefix of the longer, then the return value is the 1st
     * index after the shorter one
     */
    public static int findFirstNonMatchingCharacterIndex (CharSequence s1, CharSequence s2, int startIndex, int maxLen) {
    	int	l1=ExtendedCharSequenceUtils.getSafeLength(s1), l2=ExtendedCharSequenceUtils.getSafeLength(s2);
    	int	minLen=Math.min(l1, l2), cmpLen=Math.min(minLen,maxLen);
    	int	offset=findFirstNonMatchingCharacterOffset(s1, startIndex, s2, startIndex, cmpLen);
    	if (offset >= 0) {
    		return startIndex + offset;
    	}

    	if (cmpLen == maxLen) {
    		return (-1);	// exhausted all characters and they match
    	}

    	return startIndex + cmpLen;	// the 1st character after the compared available ones
    }

    public static int findFirstNonMatchingCharacterOffset (CharSequence s1, CharSequence s2, int length) {
    	return findFirstNonMatchingCharacterOffset(s1, s2, 0, length);
    }

    public static int findFirstNonMatchingCharacterOffset (CharSequence s1, CharSequence s2, int startIndex, int length) {
    	return findFirstNonMatchingCharacterOffset(s1, startIndex, s2, startIndex, length);
    }

    public static int findFirstNonMatchingCharacterOffset (CharSequence s1, int s1Start, CharSequence s2, int s2Start, int length) {
    	for (int	i1=s1Start, i2=s2Start, len=0; len < length; i1++, i2++, len++) {
    		char	c1=s1.charAt(i1), c2=s2.charAt(i2);
    		if (c1 != c2) {
    			return len;
    		}
    	}

    	return (-1);
    }

    /**
     * Converts a sequence of <U>Unicode</U> values into a {@link String}
     * @param uniVals The Unicode character values - if <code>null</code>
     * or empty then an empty string is returned
     * @return The {@link String} represented by these values
     */
    public static final String toString (final int ... uniVals) {
    	if (ExtendedArrayUtils.length(uniVals) <= 0) {
    		return "";
    	}

        final char[]  chars=new char[uniVals.length];
        for (int    index=0; index < uniVals.length; index++) {
        	final int	uv=uniVals[index];
        	if ((uv < Character.MIN_VALUE) || (uv > Character.MAX_VALUE)) {
        		throw new IllegalArgumentException("Bad unicode value (" + uv + ") at index=" + index);
        	}

            chars[index] = (char) (uv & 0x00FFFF);
        }

        return new String(chars);
    }
    
    public static final int[] toUnicode(final CharSequence cs) {
    	return toUnicode(cs, 0, ExtendedCharSequenceUtils.getSafeLength(cs));
    }

    public static final int[] toUnicode(final CharSequence cs, final int startPos, final int length) {
    	if (length <= 0) {
    		return ArrayUtils.EMPTY_INT_ARRAY;
    	}
    	
    	int[]	vals=new int[length];
    	for (int vIndex=0, curPos=startPos; vIndex < length; vIndex++, curPos++) {
    		vals[vIndex] = cs.charAt(curPos) & 0x00FFFF;
    	}
    	
    	return vals;
    }
    
    /**
     * @param s Original {@link String}
     * @return The result of splitting the string by camel-case and joining
     * back the components using space
     * @see #joinCamelCase(String, char)
     */
    public static final String joinCamelCase(String s) {
        return joinCamelCase(s, ' ');
    }
    
    /**
     * @param s Original {@link String}
     * @param separator Separator to use when joining the camel-case components
     * @return The result of splitting the string by camel-case and joining
     * back the components using the specified character
     * @see #splitByCharacterTypeCamelCase(String)
     * @see #join(Iterable, char)
     */
    public static final String joinCamelCase(String s, char separator) {
        return join(splitByCharacterTypeCamelCase(s), separator);
    }

    /**
     * @param s The input {@link String}
     * @return The string's characters or an empty array
     * if {@link #isEmpty(CharSequence)}
     */
    public static final char[] getChars(String s) {
        if (isEmpty(s)) {
            return ArrayUtils.EMPTY_CHAR_ARRAY;
        } else {
            return s.toCharArray();
        }
    }
	/**
	 * @param cs Original {@link String}
	 * @param length Max. allowed length
	 * @return The original string if below or equals the max. length,
	 * a substring of the first <code>length</code> characters
	 */
	public static final String trimToSize (final String cs, final int length)
	{
		final int	csLen=length(cs);
		if (csLen <= length)
			return cs;
		else
			return cs.substring(0, length);
	}
}
