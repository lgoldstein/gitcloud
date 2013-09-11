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

import java.util.Comparator;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.lang3.math.ExtendedNumberUtils;

/**
 * @author lgoldstein
 *
 */
public class ExtendedCharSequenceUtils extends CharSequenceUtils {
	public ExtendedCharSequenceUtils() {
		super();
	}

	/**
	 * @param o The {@link Object} to examine
	 * @return One of the following:</BR>
	 * <UL>
	 * 		<LI>
	 * 		If the object is already a {@link CharSequence} then the object
	 * 		itself (cast accordingly)
	 * 		</LI>
	 * 
	 * 		<LI>
	 * 		If the reference is <code>null</code> then returns <code>null</code>
	 * 		</LI>
	 * 
	 * 		<LI>
	 * 		Otherwise, return {@link Object#toString()}
	 * 		</LI>
	 * </UL>
	 * @see ExtendedStringUtils#safeToString(Object)
	 */
	public static final CharSequence safeToCharSequence(Object o) {
		if (o instanceof CharSequence) {
			return (CharSequence) o;
		} else {
			return ExtendedStringUtils.safeToString(o);
		}
	}

    /**
     * Compares 2 {@link CharSequence}s case <U>sensitive</U>
     * @param s1 The 1st {@link CharSequence}
     * @param s2 The 2nd {@link CharSequence}
     * @return Negative if 1st sequence comes before the 2nd one in lexicographical
     * order, positive if after, zero if same order
     * @see #compareSequences(CharSequence, CharSequence, boolean)
     */
    public static final int compareSequences(CharSequence s1, CharSequence s2) {
        return compareSequences(s1, s2, true);
    }

    /**
     * Compares 2 {@link CharSequence}s
     * @param s1 The 1st {@link CharSequence}
     * @param s2 The 2nd {@link CharSequence}
     * @param caseSensitive If <code>false</code> then characters are converted
     * to lowercase before being compared
     * @return Negative if 1st sequence comes before the 2nd one in lexicographical
     * order, positive if after, zero if same order
     * @see #compareSequences(CharSequence, CharSequence, boolean)
     */
    public static final int compareSequences(CharSequence s1, CharSequence s2, boolean caseSensitive) {
        if (s1 == s2) {
            return 0;
        }

        int l1=ExtendedCharSequenceUtils.getSafeLength(s1), l2=ExtendedCharSequenceUtils.getSafeLength(s2), maxComp=Math.min(l1, l2);
        for (int index=0; index < maxComp; index++) {
            char    c1=s1.charAt(index), c2=s2.charAt(index);
            if (!caseSensitive) {
                c1 = Character.toLowerCase(c1);
                c2 = Character.toLowerCase(c2);
            }
            
            int diff=c1 - c2;
            if (diff != 0) {
                return ExtendedNumberUtils.signOf(diff);
            }
        }
        
        if (l1 != l2) {
            return ExtendedNumberUtils.signOf(l1 - l2);
        } else {
            return 0;
        }
    }

    public static final Comparator<CharSequence> SEQ_SENSITIVE_ORDER=new Comparator<CharSequence>() {
            @Override
            public int compare(CharSequence s1, CharSequence s2) {
                return compareSequences(s1, s2, true);
            }
        };

    /**
     * The {@link CharSequence} equivalent of {@link String#CASE_INSENSITIVE_ORDER}
     */
    public static final Comparator<CharSequence> SEQ_INSENSITIVE_ORDER=new Comparator<CharSequence>() {
            @Override
            public int compare(CharSequence s1, CharSequence s2) {
                return compareSequences(s1, s2, false);
            }
        };

    /**
     * @param cs The &quot;main&quot; {@link CharSequence}
     * @param sub The {@link CharSequence} to check if prefix
     * @param caseSensitive <code>true</code> whether the comparison is case sensitive
     * @return <code>true</code> if main sequence contains the sub-sequence and
     * is <U>longer</U> than it.
     * @see StringUtils#startsWith(CharSequence, CharSequence)
     * @see StringUtils#startsWithIgnoreCase(CharSequence, CharSequence)
     */
    public static final boolean isProperPrefix(CharSequence cs, CharSequence sub, boolean caseSensitive) {
        if (caseSensitive) {
            if (!StringUtils.startsWith(cs, sub)) {
                return false;
            }
        } else {
            if (!StringUtils.startsWithIgnoreCase(cs, sub)) {
                return false;
            }
        }
        
        int cLen=ExtendedCharSequenceUtils.getSafeLength(cs), sLen=ExtendedCharSequenceUtils.getSafeLength(sub);
        if (cLen <= sLen) {
            return false;
        } else {
            return true;
        }
    }

	// An improved version of StringUtils#capitalize
    public static final String capitalize(CharSequence cs) {
        int csLength=ExtendedCharSequenceUtils.getSafeLength(cs);
        if (csLength <= 0) {
            return ExtendedStringUtils.safeToString(cs);
        }

        char    ch=cs.charAt(0), tch=Character.toTitleCase(ch);
        if (ch == tch) {
            return cs.toString();
        }
        
        if (csLength == 1) {
            return String.valueOf(tch);
        }

        return new StringBuilder(csLength)
                    .append(tch)
                    .append(cs.subSequence(1, csLength))
                    .toString()
                    ;
    }

    /**
     * An {@link ExtendedTransformer} that converts any {@link Object}
     * to a capitalized {@link String} using the {@link ExtendedStringUtils#SAFE_TOSTRING_XFORMER}
     * and the {@link #capitalize(CharSequence)} method. <B>Note</B>:
     * returns <code>null</code> if input is <code>null</code>
     */
    public static final ExtendedTransformer<Object,String>  CAPITALIZE_XFORMER=
            new AbstractExtendedTransformer<Object, String>(Object.class,String.class) {
                @Override
                public String transform(Object input) {
                    return capitalize(ExtendedStringUtils.SAFE_TOSTRING_XFORMER.transform(input));
                }
            };

    // An improved version of StringUtils#uncapitalize
    public static final String uncapitalize(CharSequence cs) {
        int csLength=ExtendedCharSequenceUtils.getSafeLength(cs);
        if (csLength <= 0) {
            return ExtendedStringUtils.safeToString(cs);
        }

        char    ch=cs.charAt(0), tch=Character.toLowerCase(ch);
        if (ch == tch) {
            return cs.toString();
        }
        
        if (csLength == 1) {
            return String.valueOf(tch);
        }

        return new StringBuilder(csLength)
                    .append(tch)
                    .append(cs.subSequence(1, csLength))
                    .toString()
                    ;
    }

    /**
     * An {@link ExtendedTransformer} that converts any {@link Object}
     * to a uncapitalized {@link String} using the {@link ExtendedStringUtils#SAFE_TOSTRING_XFORMER}
     * and the {@link #uncapitalize(CharSequence)} method. <B>Note</B>:
     * returns <code>null</code> if input is <code>null</code>
     */
    public static final ExtendedTransformer<Object,String>  UNCAPITALIZE_XFORMER=
            new AbstractExtendedTransformer<Object, String>(Object.class,String.class) {
                @Override
                public String transform(Object input) {
                    return uncapitalize(ExtendedStringUtils.SAFE_TOSTRING_XFORMER.transform(input));
                }
            };

	/**
	 * @param cs Original {@link CharSequence}
	 * @param length Max. allowed length
	 * @return The original sequence if below or equals the max. length,
	 * a subsequence of the first <code>length</code> characters
	 */
	public static final CharSequence trimToSize (final CharSequence cs, final int length) {
		final int	csLen=ExtendedCharSequenceUtils.getSafeLength(cs);
		if (csLen <= length)
			return cs;
		else
			return cs.subSequence(0, length);
	}

    /**
     * Strips any enclosing quotes or double-quotes (if existing)
     * @param value The original {@link CharSequence}
     * @return The value without any enclosing quotes or double-quotes - same
     * as input if not quotes to begin with
     * @throws IllegalArgumentException If imbalanced quotes
     */
    public static CharSequence stripQuotes (CharSequence value) throws IllegalArgumentException {
        int vLen=ExtendedCharSequenceUtils.getSafeLength(value);
        if (vLen <= 0) {
            return value;
        }
        
        char    delim=value.charAt(0);
        if ((delim != '\'') && (delim != '"')) {
            delim = value.charAt(vLen - 1);
    
            if ((delim == '\'') || (delim == '"')) {
                throw new IllegalArgumentException("Imbalanced end quote: " + value);
            } else {
                return value;
            }
        }
        
        if (vLen < 2) {
            throw new IllegalArgumentException("String too short for quoting: " + value);
        }
        
        if (value.charAt(vLen - 1) != delim) {
            throw new IllegalArgumentException("Imbalanced start quote: " + value);
        }
        
        return value.subSequence(1, vLen - 1);
    }

    /**
     * @param seq Input {@link CharSequence}
     * @return The {@link CharSequence#length()} or zero if <code>null</code>
     */
    public static final int getSafeLength (CharSequence seq) {
        if (seq == null) {
            return 0;
        } else {
            return seq.length();
        }
    }
}
