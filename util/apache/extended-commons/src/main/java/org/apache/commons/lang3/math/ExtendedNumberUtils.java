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

package org.apache.commons.lang3.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.ExtendedPredicate;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedCharSequenceUtils;
import org.apache.commons.lang3.ExtendedStringUtils;

/**
 * @author lgoldstein
 */
public class ExtendedNumberUtils extends NumberUtils {
    /** Reusable {@link Float} constant for NaN. */
    public static final Float FLOAT_NAN = Float.valueOf(Float.NaN);
    /**
     * An {@link ExtendedPredicate} that returns <code>true</code> if the evaluated
     * {@link Number} represent NaN <U><code>float</code></U>
     * @see Float#isNaN(float)
     */
    public static final ExtendedPredicate<Number> FLOAT_NAN_PREDICATE=
        new AbstractExtendedPredicate<Number>(Number.class) {
            @Override
            public boolean evaluate(Number n) {
                if (n == null) {
                    return false;
                } else {
                    return Float.isNaN(n.floatValue());
                }
            }
        };

    /** Reusable {@link Float} constant for positive infinity. */
    public static final Float FLOAT_POSITIVE_INFINITY = Float.valueOf(Float.POSITIVE_INFINITY);
    /** Reusable {@link Float} constant for negative infinity. */
    public static final Float FLOAT_NEGATIVE_INFINITY = Float.valueOf(Float.NEGATIVE_INFINITY);
    /**
     * An {@link ExtendedPredicate} that returns <code>true</code> if the evaluated
     * {@link Number} represent a positive or negative infinity <U><code>float</code></U>
     * @see Float#isInfinite(float)
     */
    public static final ExtendedPredicate<Number> FLOAT_INFINITE_PREDICATE=
        new AbstractExtendedPredicate<Number>(Number.class) {
            @Override
            public boolean evaluate(Number n) {
                if (n == null) {
                    return false;
                } else {
                    return Float.isInfinite(n.floatValue());
                }
            }
        };

    /** Reusable {@link Double} constant for NaN. */
    public static final Double DOUBLE_NAN = Double.valueOf(Double.NaN);
    /**
     * An {@link ExtendedPredicate} that returns <code>true</code> if the evaluated
     * {@link Number} represent NaN <U><code>double</code></U>
     * @see Double#isNaN(double)
     */
    public static final ExtendedPredicate<Number> DOUBLE_NAN_PREDICATE=
        new AbstractExtendedPredicate<Number>(Number.class) {
            @Override
            public boolean evaluate(Number n) {
                if (n == null) {
                    return false;
                } else {
                    return Double.isNaN(n.doubleValue());
                }
            }
        };

    /** Reusable {@link Double} constant for positive infinity. */
    public static final Double DOUBLE_POSITIVE_INFINITY = Double.valueOf(Double.POSITIVE_INFINITY);
    /** Reusable {@link Double} constant for negative infinity. */
    public static final Double DOUBLE_NEGATIVE_INFINITY = Double.valueOf(Double.NEGATIVE_INFINITY);
    /**
     * An {@link ExtendedPredicate} that returns <code>true</code> if the evaluated
     * {@link Number} represent a positive or negative infinity <U><code>double</code></U>
     * @see Double#isInfinite(double)
     */
    public static final ExtendedPredicate<Number> DOUBLE_INFINITE_PREDICATE=
        new AbstractExtendedPredicate<Number>(Number.class) {
            @Override
            public boolean evaluate(Number n) {
                if (n == null) {
                    return false;
                } else {
                    return Double.isInfinite(n.doubleValue());
                }
            }
        };
    
    /**
     * An (unmodifiable) {@link List} of all the &quot;integral&quot; primitive wrappers
     */
    public static final List<Class<?>>   INTEGRAL_TYPES=
            Collections.unmodifiableList(Arrays.asList((Class<?>) Long.class, Integer.class, Short.class, Byte.class));
    /**
     * An (unmodifiable) {@link List} of all the non-&quot;integral&quot; primitive wrappers
     */
    public static final List<Class<?>>   PRECISION_TYPES=
            Collections.unmodifiableList(Arrays.asList((Class<?>) Double.class, Float.class));
    /**
     * Used to display a (N)ot-(A)-(N)umber for {@link Float} or
     * {@link Double} values
     */
    public static final String	NAN_VALUE="NaN";

    /**
     * Used to display (positive/negative) infinity for {@link Float} or
     * {@link Double} values
     */
    public static final String	INFINITY_VALUE="Infinity";

    public ExtendedNumberUtils() {
		super();
	}

    public static final int hashCode(float value) {
        return Float.floatToIntBits(value);
    }

	public static final int hashCode (final double value) {
		return hashCode(Double.doubleToLongBits(value));
	}

	public static final int hashCode (final long value) {
		if (value == 0L) {
			return 0;
		}

		int	hiValue=(int) ((value >> 32) & 0xFFFFFFFF), loValue=(int) (value & 0xFFFFFFFF);
		if (hiValue == 0) {
			return loValue;
		} else if (loValue == 0) {
			return hiValue;
		} else {
			return loValue ^ hiValue;
		}
	}

    /**
     * @param value Value to be tested
     * @return (-1) if value is negative, (+1) if positive, 0 if zero
     */
    public static final int signOf (final int value) {
        if (value == 0)
            return 0;
        else if (value < 0)
            return (-1);
        else
            return (+1);
    }

    // NOTE: in JDK 1.7 one can use Long.compare(v1, v2)
    public static final int compare(long v1, long v2) {
        return signOf(v1 - v2);
    }
    
    // Takes care of NaN(s) since NaN != NaN by default - pushes NaN(s) to end (i.e. NaN > not-NaN)
    public static final int compare(double v1, double v2) {
        // special comparison for NaN since NaN != NaN
        if (Double.isNaN(v1)) {
            if (Double.isNaN(v2)) {
                return 0;
            } else {
                return (+1);    // push NaN(s) to end
            }
        } else if (Double.isNaN(v2)) {
            return (-1);    // push NaN(s) to end
        } else {
            return Double.compare(v1, v2);
        }
    }
    
    // Takes care of NaN(s) since NaN != NaN by default - pushes NaN(s) to end (i.e. NaN > not-NaN)
    public static final int compare(float v1, float v2) {
        // special comparison for NaN since NaN != NaN
        if (Float.isNaN(v1)) {
            if (Float.isNaN(v2)) {
                return 0;
            } else {
                return (+1);    // push NaN(s) to end
            }
        } else if (Float.isNaN(v2)) {
            return (-1);    // push NaN(s) to end
        } else {
            return Float.compare(v1, v2);
        }
    }

    /**
     * @param value Value to be tested
     * @return (-1) if value is negative, (+1) if positive, 0 if zero
     */
    public static final int signOf (final long value) {
        if (value == 0L)
            return 0;
        else if (value < 0L)
            return (-1);
        else
            return (+1);
    }

    /**
     * Compares 2 {@link Number}s by checking if they are &quot;integral&quot;
     * ones. If so, then it compares their {@link Number#longValue()}-s,
     * otherwise it compares their {@link Number#doubleValue()}-s
     * @param n1 1st {@link Number} to compare
     * @param n2 2nd {@link Number} to compare
     * @return negative if 1st number is smaller, positive if greater and
     * zero if equal
     * @see #isIntegralNumber(Number)
     */
    public static final int compareNumbers (Number n1, Number n2) {
        if (isIntegralNumber(n1) && isIntegralNumber(n2)) {
            return compare(n1.longValue(), n2.longValue());
        } else {
            return Double.compare(n1.doubleValue(), n2.doubleValue());
        }
    }

	/**
	 * @param value Value to divide
	 * @param divisor Divisor
	 * @return Division result or zero if any of the 2 arguments is zero
	 */
	public static final long safeDiv (final long value, final long divisor) {
		if ((value == 0L) || (divisor == 0L))
			return 0L;
		else
			return value / divisor;
	}

	/**
	 * @param value Value to divide
	 * @param divisor Divisor
	 * @return Division result or zero if any of the 2 arguments is zero
	 */
	public static final int safeDiv (final int value, final int divisor) {
		if ((value == 0) || (divisor == 0))
			return 0;
		else
			return value / divisor;
	}

	/**
	 * @param n A {@link Number} to test
	 * @return <code>true</code> if it is a wrapper for <code>long/int/short/byte</code>
	 * or a <code>double/float</code>
	 * @see #isNumberWrapperType(Class)
	 */
	public static final boolean isNumberWrapper(Number n) {
	    return isNumberWrapperType((n == null) ? null : n.getClass());
	}

    /**
     * An {@link ExtendedPredicate} that returns <code>true</code> if the evaluated
     * {@link Number} is a wrapper for <code>long/int/short/byte</code>
     * or a <code>double/float</code>
     * @see #isNumberWrapper(Number)
     */
    public static final ExtendedPredicate<Number>  NUMBER_WRAPPER_PREDICATE=
        new AbstractExtendedPredicate<Number>(Number.class) {
           @Override
           public boolean evaluate(Number n) {
               return isNumberWrapper(n);
           }
       };

	/**
	 * @param numType The {@link Class} to test
     * @return <code>true</code> if it is a wrapper for <code>long/int/short/byte</code>
     * or a <code>double/float</code>
     * @see #isIntegralType(Class)
     * @see #isPrecisionType(Class)
	 */
	public static final boolean isNumberWrapperType(Class<?> numType) {
	    if (numType == null) {
	        return false;
	    } else {
	        return isIntegralType(numType) || isPrecisionType(numType);
	    }
	}

    /**
     * A {@link Predicate} that returns <code>true</code> if the evaluated
     * {@link Class} is a wrapper for <code>long/int/short/byte</code>
     * or a <code>double/float</code>
     * @see #isNumberWrapperType(Class)
     */
    public static final Predicate<Class<?>>  NUMBER_WRAPPER_TYPE_PREDICATE=new Predicate<Class<?>>() {
           @Override
           public boolean evaluate(Class<?> numType) {
               return isNumberWrapperType(numType);
           }
       };

    /**
     * @param n A {@link Number} to test
     * @return <code>true</true> if it is a <code>long/int/short/byte</code>
     * primitive wrapper
     * @see #isIntegralType(Class)
     */
    public static final boolean isIntegralNumber (Number n) {
        return isIntegralType((n == null) ? null : n.getClass());
    }

    /**
     * A {@link ExtendedPredicate} that returns <code>true</true> if the evaluated
     * {@link Number} is a <code>long/int/short/byte</code> primitive wrapper
     * @see #isIntegralNumber(Number)
     */
    public static final ExtendedPredicate<Number>  INTEGRAL_NUMBER_PREDICATE=
        new AbstractExtendedPredicate<Number>(Number.class) {
            @Override
            public boolean evaluate(Number n) {
                return isIntegralNumber(n);
            }
        };

    /**
     * @param numType The {@link Class} to test
     * @return <code>true</true> if it is a <code>long/int/short/byte</code>
     * primitive wrapper
     * @see #INTEGRAL_TYPES
     */
    public static final boolean isIntegralType (Class<?> numType) {
        return (numType != null) && INTEGRAL_TYPES.contains(numType);
    }

    /**
     * A {@link Predicate} that returns <code>true</true> if 
     * the evaluated {@link Class} is a <code>long/int/short/byte</code>
     * primitive wrapper
     * @see #isIntegralType(Class)
     */
    public static final Predicate<Class<?>>  INTEGRAL_TYPE_PREDICATE=new Predicate<Class<?>>() {
            @Override
            public boolean evaluate(Class<?> numType) {
                return isIntegralType(numType);
            }
        };

    /**
     * @param n A {@link Number} to test
     * @return <code>true</true> if it is a <code>double/float</code>
     * primitive wrapper
     * @see #isPrecisionType(Class)
     */
    public static final boolean isPrecisionNumber (Number n) {
        return isPrecisionType((n == null) ? null : n.getClass());
    }

    /**
     * An {@link ExtendedPredicate} that returns <code>true</true> if it
     * the evaluated {@link Number} is a <code>double/float</code>
     * primitive wrapper
     * @see #isPrecisionNumber(Number)
     */
    public static final ExtendedPredicate<Number>  PRECISION_NUMBER_PREDICATE=
        new AbstractExtendedPredicate<Number>(Number.class) {
            @Override
            public boolean evaluate(Number n) {
                return isPrecisionNumber(n);
            }
        };

    /**
     * @param numType The {@link Class} to test
     * @return <code>true</true> if it is a <code>double/float</code>
     * primitive wrapper
     * @see #PRECISION_TYPES
     */
    public static final boolean isPrecisionType(Class<?> numType) {
        return (numType != null) && PRECISION_TYPES.contains(numType);
    }

    /**
     * A {@link Predicate} that returns <code>true</true> if 
     * the evaluated {@link Class} is a <code>double/float</code>
     * primitive wrapper
     * @see #isPrecisionType(Class)
     */
    public static final Predicate<Class<?>>  PRECISION_TYPE_PREDICATE=new Predicate<Class<?>>() {
            @Override
            public boolean evaluate(Class<?> numType) {
                return isPrecisionType(numType);
            }
        };

	/**
	 * Checks if a given {@link CharSequence} represents a non-floating
	 * point number regardless of whether is can fit into a <code>byte,
	 * short, int, long, {@link java.math.BigInteger}, etc.</code>
	 * @param s The {@link CharSequence} to be checked - <B>Note:</B> if
	 * <code>null</code>/empty then result is <code>false</code>
	 * @return <code>true</code> if the input parameter represents a
	 * positive/negative non-floating point number - e.g., <code>-73965,
	 * 3777347</code> are <code>true</code> whereas <code>3.14, abcd,
	 * 12 34</code> are <code>false</code>
	 * @see #isIntegerNumber(CharSequence, int, int)
	 */
	public static final boolean isIntegerNumber (CharSequence s) {
		return isIntegerNumber(s, 0, ExtendedCharSequenceUtils.getSafeLength(s));
	}
	
	/**
	 * An {@link ExtendedPredicate} that returns <code>true</code> if the
	 * evaluated {@link CharSequence} represents an integer number
	 * @see #isIntegerNumber(CharSequence)
	 */
	public static final ExtendedPredicate<CharSequence>    INTNUM_CHARSEQ_PREDICATE=
	        new AbstractExtendedPredicate<CharSequence>(CharSequence.class) {
                @Override
                public boolean evaluate (CharSequence cs) {
                    return isIntegerNumber(cs);
                }
            };

	/**
	 * Checks if a given {@link CharSequence} represents a non-floating
	 * point number regardless of whether is can fit into a <code>byte,
	 * short, int, long, {@link java.math.BigInteger}, etc.</code>
	 * @param s The {@link CharSequence} to be checked
	 * @param startIndex - character position to start checking
	 * @param length - Number of characters to check - <B>Note:</B> if
	 * non-positive then result is <code>false</code>
	 * @return <code>true</code> if the input parameter represents a
	 * positive/negative non-floating point number - e.g., <code>-73965,
	 * 3777347</code> are <code>true</code> whereas <code>3.14, abcd,
	 * 12 34</code> are <code>false</code>
	 */
	public static final boolean isIntegerNumber (CharSequence s, int startIndex, int length) {
		if (length <= 0) {
			return false;
		}

	    for (int    index=startIndex, maxIndex=startIndex + length; index < maxIndex; index++) {
	        final char  ch=s.charAt(index);
	        if ((ch == '+') || (ch == '-')) {
	            if ((index != startIndex) || (length <= 1)) {
	                return false;
	            }
	        } else if ((ch < '0') || (ch > '9')) {
	            return false;
	        }
	    }
	
	    return true;
	}

	/**
	 * Checks if a given {@link CharSequence} represents a floating
	 * point number regardless of whether it can fit into a <code>float,
	 * double, {@link java.math.BigDecimal}, etc.</code>
	 * @param s The {@link CharSequence} to be checked - <B>Note:</B> if
	 * <code>null</code>/empty then result is <code>false</code>
	 * @return <code>true</code> if the input parameter represents a
	 * positive/negative floating point number - including <code>NaN</code>
	 * and infinity
	 * @see #isFloatingPoint(CharSequence, int, int)
	 */
	public static final boolean isFloatingPoint (CharSequence s) {
		return isFloatingPoint(s, 0, ExtendedCharSequenceUtils.getSafeLength(s));
	}

    /**
     * An {@link ExtendedPredicate} that returns <code>true</code> if the
     * evaluated {@link CharSequence} represents a floating point number
     * @see #isFloatingPoint(CharSequence)
     */
    public static final ExtendedPredicate<CharSequence>    FLTNUM_CHARSEQ_PREDICATE=
            new AbstractExtendedPredicate<CharSequence>(CharSequence.class) {
                @Override
                public boolean evaluate (CharSequence cs) {
                    return isFloatingPoint(cs);
                }
            };

	/**
	 * Checks if a given {@link CharSequence} represents a floating
	 * point number regardless of whether it can fit into a <code>float,
	 * double, {@link java.math.BigDecimal}, etc.</code>
	 * @param s The {@link CharSequence} to be checked
	 * @param startIndex - character position to start checking
	 * @param length - Number of characters to check - <B>Note:</B> if
	 * non-positive then result is <code>false</code>
	 * @return <code>true</code> if the input parameter represents a
	 * positive/negative floating point number - including <code>NaN</code>
	 * and infinity
	 * @see #isKnownValueFloatingPoint(CharSequence, int, int)
	 */
	public static final boolean isFloatingPoint (CharSequence s, int startIndex, int length) {
		if (length <= 0) {
			return false;
		}

		if (isKnownValueFloatingPoint(s, startIndex, length)) {
			return true;
		}

		int	dotPos=(-1);
	    for (int    index=startIndex, maxIndex=startIndex + length; index < maxIndex; index++) {
	        char  ch=s.charAt(index);
	        switch(ch) {
	        	case 'E'	:	// exponent
	        	case 'e'	:
	        		// cannot start with exponent or have only exponent
	        		if ((index == startIndex) || (length <= 1)) {
	        			return false;
	        		}

	        		// cannot have exponent following dot without some number in-between
	        		if ((dotPos >= 0) && (index == (dotPos + 1))) {
	        			return false;
	        		}

	        		index++;	// skip exponent
	        		if (index >= maxIndex) {
	        			return false;
	        		}
	        
	        		ch = s.charAt(index);
	        		// exponent must be followed by a '+' or '-' sign
	        		if ((ch != '+') && (ch != '-')) {
	        			return false;
	        		}

	        		// exponent must contain an integer value
	        		return isIntegerNumber(s, index, maxIndex - index);
	        
	        	case '+'	:
	        	case '-'	:
	        		// if there is a sign, then it must be 1st
		            if ((index != startIndex) || (length <= 1)) {
		                return false;
		            }
		            
		            // check if it is a signed known value - e.g., +NaN, -Infinity
		            if (isKnownValueFloatingPoint(s, startIndex+1, length - 1)) {
		            	return true;
		            }

		            break;

	        	case '.'	:
	        		if ((dotPos >= 0) || (length <= 1)) {
	        			return false;	// already have a dot or all we have is a dot
	        		}
	        		
	        		dotPos = index;
	        		break;

	        	default		:
	        		if ((ch < '0') || (ch > '9')) {
	    	            return false;
	        		}
	        }
	    }

	    // if reached this point then no exponent found - so must have a dot
	    if (dotPos < 0) {
	    	return false;
	    }

	    return true;
	}
	
	/**
	 * @param s The {@link CharSequence} to check
	 * @param startIndex - character position to start checking
	 * @param length - Number of characters to check - <B>Note:</B> if
	 * @return <code> If the sequence represents a &quot;known&quot; floating
	 * point value - e.g., {@link #NAN_VALUE}, {@link #INFINITY_VALUE}
	 */
	public static final boolean isKnownValueFloatingPoint (CharSequence s, int startIndex, int length) {
		if ((NAN_VALUE.length() == length)
		 && (ExtendedStringUtils.findFirstNonMatchingCharacterOffset(s, startIndex, NAN_VALUE, 0, length) < 0)) {
			 return true;
		 }

		if ((INFINITY_VALUE.length() == length)
		 && (ExtendedStringUtils.findFirstNonMatchingCharacterOffset(s, startIndex, INFINITY_VALUE, 0, length) < 0)) {
			 return true;
		}

		return false;
	}

    public static final short reverseBytes(short value) {
        byte[] buf=toBytes(value);
        ArrayUtils.reverse(buf);
        return toShortValue(buf);
    }
    
    public static final short toShortValue(byte ... bytes) {
        return toShortValue(bytes, 0, bytes.length);
    }

    public static final short toShortValue(byte[] buf, int offset, int len) {
        if (len < 2) {
            throw new NumberFormatException("Not enough data for value: " + len);
        }

        short    result=0;
        for (int index=0, pos=offset; index < 2; index++, pos++) {
            result = (short) (((result << Byte.SIZE) & 0xFF00) | (buf[pos] & 0x00FF));
        }

        return result;
    }

    // returns number of used bytes
    public static final byte[] toBytes(short value) {
        byte[] buf=new byte[2];
        toBytes(value, buf);
        return buf;
    }
    
    // returns number of used bytes
    public static final int toBytes(short value, byte[] buf) {
        return toBytes(value, buf, 0, buf.length);
    }

    // returns number of used bytes
    public static final int toBytes(short value, byte[] buf, int offset, int availableLen) {
        if (availableLen < 2) {
            throw new IllegalArgumentException("Insufficient available length: " + availableLen);
        }

        for (int   index=0, mask=value, pos=offset; index < 2; index++, pos++, mask <<= Byte.SIZE) {
            buf[pos] = (byte) ((mask >> 8) & 0xFF);
        }
        
        return 2;
    }

	public static final int reverseBytes(int value) {
	    byte[] buf=toBytes(value);
	    ArrayUtils.reverse(buf);
	    return toIntegerValue(buf);
	}
	
	public static final int toIntegerValue(byte ... bytes) {
	    return toIntegerValue(bytes, 0, bytes.length);
	}

	public static final int toIntegerValue(byte[] buf, int offset, int len) {
	    if (len < 4) {
	        throw new NumberFormatException("Not enough data for value: " + len);
	    }

	    int    result=0;
	    for (int index=0, pos=offset; index < 4; index++, pos++) {
	        result = ((result << Byte.SIZE) & 0xFFFFFF00) | (buf[pos] & 0x00FF);
	    }

	    return result;
	}

    // returns number of used bytes
	public static final byte[] toBytes(int value) {
	    byte[] buf=new byte[4];
	    toBytes(value, buf);
	    return buf;
	}
	
    // returns number of used bytes
	public static final int toBytes(int value, byte[] buf) {
	    return toBytes(value, buf, 0, buf.length);
	}

    // returns number of used bytes
	public static final int toBytes(int value, byte[] buf, int offset, int availableLen) {
	    if (availableLen < 4) {
	        throw new IllegalArgumentException("Insufficient available length: " + availableLen);
	    }

	    for (int   index=0, mask=value, pos=offset; index < 4; index++, pos++, mask <<= Byte.SIZE) {
	        buf[pos] = (byte) ((mask >> 24) & 0xFF);
	    }
	    
	    return 4;
	}

	public static final long reverseBytes(long value) {
        byte[] buf=toBytes(value);
        ArrayUtils.reverse(buf);
        return toLongValue(buf);
    }
    
    public static final long toLongValue(byte ... bytes) {
        return toLongValue(bytes, 0, bytes.length);
    }

    public static final long toLongValue(byte[] buf, int offset, int len) {
        if (len < 8) {
            throw new NumberFormatException("Not enough data for value: " + len);
        }

        long    result=0L;
        for (int index=0, pos=offset; index < 8; index++, pos++) {
            result = ((result << Byte.SIZE) & 0xFFFFFFFFFFFFFF00L) | (buf[pos] & 0x00FF);
        }

        return result;
    }

    public static final byte[] toBytes(long value) {
        byte[] buf=new byte[8];
        toBytes(value, buf);
        return buf;
    }
    
    // returns number of used bytes
    public static final int toBytes(long value, byte[] buf) {
        return toBytes(value, buf, 0, buf.length);
    }

    // returns number of used bytes
    public static final int toBytes(long value, byte[] buf, int offset, int availableLen) {
        if (availableLen < 8) {
            throw new IllegalArgumentException("Insufficient available length: " + availableLen);
        }

        long    mask=value;
        for (int   index=0, pos=offset; index < 8; index++, pos++, mask <<= Byte.SIZE) {
            buf[pos] = (byte) ((mask >> 56) & 0xFF);
        }
        
        return 8;
    }
}
