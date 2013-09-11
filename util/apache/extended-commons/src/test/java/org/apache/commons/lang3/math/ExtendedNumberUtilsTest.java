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

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.ExtendedCharSequenceUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author lgoldstein
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedNumberUtilsTest extends AbstractTestSupport {
	public ExtendedNumberUtilsTest() {
		super();
	}

	@Test
	public void testSafeDivInt() {
		final int	ZERO=0, NON_ZERO=7365;
		assertEquals("Mismatched result for zero/zero", 0, ExtendedNumberUtils.safeDiv(ZERO, ZERO));
		assertEquals("Mismatched result for zero/non-zero", 0, ExtendedNumberUtils.safeDiv(ZERO, NON_ZERO));
		assertEquals("Mismatched result for non-zero/zero", 0, ExtendedNumberUtils.safeDiv(NON_ZERO, ZERO));
	}

	@Test
	public void testSafeDivLong() {
		final long	ZERO=0L, NON_ZERO=System.currentTimeMillis();
		assertEquals("Mismatched result for zero/zero", 0L, ExtendedNumberUtils.safeDiv(ZERO, ZERO));
		assertEquals("Mismatched result for zero/non-zero", 0L, ExtendedNumberUtils.safeDiv(ZERO, NON_ZERO));
		assertEquals("Mismatched result for non-zero/zero", 0L, ExtendedNumberUtils.safeDiv(NON_ZERO, ZERO));
	}

    @Test
    public void testIntSignof () {
        Random  rnd=new Random(System.currentTimeMillis());
        for (int    index=0; index < Byte.MAX_VALUE; index++) {
            int value=rnd.nextInt();
            if (value < 0)
                assertEquals("Mismatched result for negative=" + value, (-1), ExtendedNumberUtils.signOf(value));
            else if (value > 0)
                assertEquals("Mismatched result for positive" + value, 1, ExtendedNumberUtils.signOf(value));
            else
                assertEquals("Mismatched result for zero", 0, ExtendedNumberUtils.signOf(value));
        }
    }

    @Test
    public void testLongSignof () {
        Random  rnd=new Random(System.currentTimeMillis());
        for (int    index=0; index < Byte.MAX_VALUE; index++) {
            long value=rnd.nextLong();
            if (value < 0L)
                assertEquals("Mismatched result for negative=" + value, (-1), ExtendedNumberUtils.signOf(value));
            else if (value > 0L)
                assertEquals("Mismatched result for positive" + value, 1, ExtendedNumberUtils.signOf(value));
            else
                assertEquals("Mismatched result for zero", 0, ExtendedNumberUtils.signOf(value));
        }
    }

    @Test
    public void testIsIntegralNumber () {
        for (Number n : new Number[] {        
                    Byte.valueOf(Byte.MIN_VALUE),
                    Short.valueOf(Short.MAX_VALUE),
                    Integer.valueOf(Integer.SIZE),
                    Long.valueOf(System.nanoTime()) }) {
            assertTrue("Number not integral: " + n.getClass(), ExtendedNumberUtils.isIntegralNumber(n));
            assertFalse("Number is precision: " + n.getClass(), ExtendedNumberUtils.isPrecisionNumber(n));
        }
    }

    @Test
    public void testIsPrecisionNumber () {
        for (Number n : new Number[] {        
                    Float.valueOf((float) Integer.SIZE / Byte.MAX_VALUE),
                    Double.valueOf(Math.random())}) {
            assertTrue("Number not precision: " + n.getClass(), ExtendedNumberUtils.isPrecisionNumber(n));
            assertFalse("Number is integral: " + n.getClass(), ExtendedNumberUtils.isIntegralNumber(n));
        }
    }

    @Test
    public void testClassifyNonPrimitiveWrappers () {
        for (Number n : new Number[] {
                new AtomicInteger(Short.SIZE),
                new AtomicLong(System.nanoTime()),
                new BigDecimal(Math.random()),
                new BigInteger(String.valueOf(Long.SIZE)),
            })  {
            assertFalse("Number is integral: " + n.getClass(), ExtendedNumberUtils.isIntegralNumber(n));
            assertFalse("Number is precision: " + n.getClass(), ExtendedNumberUtils.isPrecisionNumber(n));
        }
    }

    @Test
    public void testComparePrimitiveWrappers () throws Exception {
        final int   v1=Short.SIZE, v2=Long.SIZE, expected=ExtendedNumberUtils.signOf(v1 - v2);
        for (Class<?> t1 : ExtendedNumberUtils.INTEGRAL_TYPES) {
            Constructor<?>  c1=t1.getDeclaredConstructor(String.class);
            Number          n1=(Number) c1.newInstance(String.valueOf(v1));

            for (Class<?> t2 : ExtendedNumberUtils.INTEGRAL_TYPES) {
                Constructor<?>  c2=t2.getDeclaredConstructor(String.class);
                Number          n2=(Number) c2.newInstance(String.valueOf(v2));
                int             actual=ExtendedNumberUtils.signOf(ExtendedNumberUtils.compareNumbers(n1, n2));
                assertEquals("Mismatched result for n1=" + n1.getClass() + "/n2=" + n2.getClass(),
                             expected, actual);
            }
        }
    }

    @Test
    public void testCompareNonPrimitiveWrappers () throws Exception {
        final int   v1=Short.SIZE, v2=Long.SIZE, expected=ExtendedNumberUtils.signOf(v1 - v2);
        for (Class<?> t1 : ExtendedNumberUtils.INTEGRAL_TYPES) {
            Constructor<?>  c1=t1.getDeclaredConstructor(String.class);
            Number          n1=(Number) c1.newInstance(String.valueOf(v1));

            for (Class<?> t2 : ExtendedNumberUtils.PRECISION_TYPES) {
                Constructor<?>  c2=t2.getDeclaredConstructor(String.class);
                Number          n2=(Number) c2.newInstance(String.valueOf(v2));
                int             actual=ExtendedNumberUtils.signOf(ExtendedNumberUtils.compareNumbers(n1, n2));
                assertEquals("Mismatched result for n1=" + n1.getClass() + "/n2=" + n2.getClass(),
                             expected, actual);
            }
        }        
    }

    @Test
    public void testIsIntegerNumber () {
        Object[]    pairs={
                null,       Boolean.FALSE,
                "",         Boolean.FALSE,
                " ",        Boolean.FALSE,
                "-",        Boolean.FALSE,
                "+",        Boolean.FALSE,
                "1234",     Boolean.TRUE,
                "12 34",    Boolean.FALSE,
                "-7365",    Boolean.TRUE,
                "3777347",  Boolean.TRUE,
                "+7365",    Boolean.TRUE,
                String.valueOf(Math.PI),   Boolean.FALSE,
                "-" + String.valueOf(Math.E),    Boolean.FALSE,
                "abcd",     Boolean.FALSE,
                "1E+6",     Boolean.FALSE
            };
        for (int index=0; index < pairs.length; index += 2) {
            String  value=(String) pairs[index];
            Boolean expected=(Boolean) pairs[index+1];
            Boolean actual=Boolean.valueOf(ExtendedNumberUtils.isIntegerNumber(value));
            assertSame("Mismatched result for value=" + value, expected, actual);

            try {
            	final long	lValue;
            	// for some reason, Long#parseLong cannot handle a leading '+' only a '-'
                if ((ExtendedCharSequenceUtils.getSafeLength(value) > 1) && (value.charAt(0) == '+')) {
                	lValue = Long.parseLong(value.substring(1));
                } else {
                	lValue = Long.parseLong(value);
                }
            	if (!expected.booleanValue()) {
            		fail("Unexpected success to parse " + value + ": " + lValue);
            	}
            } catch(NumberFormatException e) {
            	if (expected.booleanValue()) {
            		fail("Cannot parse " + value + " though declared integer: " + e.getMessage());
            	}
            }
        }
    }

    @Test
    public void testIsFloatingPoint () {
        Object[]    pairs={
                null,       Boolean.FALSE,
                "",         Boolean.FALSE,
                " ",        Boolean.FALSE,
                "-",        Boolean.FALSE,
                "+",        Boolean.FALSE,
                ExtendedNumberUtils.NAN_VALUE, Boolean.TRUE,
                "+" + ExtendedNumberUtils.NAN_VALUE, Boolean.TRUE,
                "-" + ExtendedNumberUtils.NAN_VALUE, Boolean.TRUE,
                ExtendedNumberUtils.INFINITY_VALUE, Boolean.TRUE,
                "+" + ExtendedNumberUtils.INFINITY_VALUE, Boolean.TRUE,
                "-" + ExtendedNumberUtils.INFINITY_VALUE, Boolean.TRUE,
                "1234",     Boolean.FALSE,
                "12.3.4",   Boolean.FALSE,
                "12 34",    Boolean.FALSE,
                "-7365",    Boolean.FALSE,
                "3777347",  Boolean.FALSE,
                "+7365",    Boolean.FALSE,
                String.valueOf(Math.PI),   	Boolean.TRUE,
                "-" + String.valueOf(Math.E), 	Boolean.TRUE,
                "abcd",     Boolean.FALSE,
                "1E+6",     Boolean.TRUE,
                ".736e-5",  Boolean.TRUE,
                ".e-5",  	Boolean.FALSE,
                ".E3",  	Boolean.FALSE,
                ".e",  		Boolean.FALSE,
                ".",  		Boolean.FALSE,
                "-0.01e-7", Boolean.TRUE
        	};
        for (int index=0; index < pairs.length; index += 2) {
            String  value=(String) pairs[index];
            Boolean expected=(Boolean) pairs[index+1];
            Boolean actual=Boolean.valueOf(ExtendedNumberUtils.isFloatingPoint(value));
            assertSame("Mismatched result for value=" + value, expected, actual);
        	// Unlike Long#parseLong, Double#parseDouble does not throw NumberFormatException for null-s
            if (value == null) {
            	continue;
            }

            // Double#parseDouble succeeds on them
            if (ExtendedNumberUtils.isIntegerNumber(value)) {
            	continue;
            }

            try {
            	double	dValue=Double.parseDouble(value);
            	if (!expected.booleanValue()) {
            		fail("Unexpected success to parse " + value + ": " + dValue);
            	}
            } catch(NumberFormatException e) {
            	if (expected.booleanValue()) {
            		fail("Cannot parse " + value + " though declared floating point: " + e.getMessage());
            	}
            }
        }
    }

    @Test
    public void testShortToBytesAndBack() {
        byte[]  bytes=new byte[2];
        for (short expected : new short[] { 0, (-1), 7365, 3777, 347, Short.MIN_VALUE, Short.MAX_VALUE }) {
            int usedLen=ExtendedNumberUtils.toBytes(expected, bytes);
            assertEquals("Mismatched used length for value=" + expected, 2, usedLen);
            
            short   actual=ExtendedNumberUtils.toShortValue(bytes);
            assertEquals("Mismatched re-constructed value", expected, actual);
        }
    }
    
    @Test
    public void testReverseShortValue() {
        final short[] VALUES={
                        0x0000, 0x0000,
                (short) 0xFFFF, (short) 0xFFFF,
                (short) 0xA1B2, (short) 0xB2A1,
                (short) 0x7FFF, (short) 0xFFF7F
            };
        for (int index=0; index < VALUES.length; index += 2) {
            short v1=VALUES[index], v2=VALUES[index+1];
            short v1Rev=ExtendedNumberUtils.reverseBytes(v1);
            assertEquals("Mismatched v1 -> v2 result", Integer.toHexString(v2 & 0xFFFF), Integer.toHexString(v1Rev & 0xFFFF));

            short v2Rev=ExtendedNumberUtils.reverseBytes(v2);
            assertEquals("Mismatched v2 -> v1 result", Integer.toHexString(v1 & 0xFFFF), Integer.toHexString(v2Rev & 0xFFFF));
        }
    }

    @Test
    public void testIntegerToBytesAndBack() {
        byte[]  bytes=new byte[4];
        for (int expected : new int[] { 0, (-1), 7365, 3777347, Integer.MIN_VALUE, Integer.MAX_VALUE }) {
            int usedLen=ExtendedNumberUtils.toBytes(expected, bytes);
            assertEquals("Mismatched used length for value=" + expected, 4, usedLen);
            
            int actual=ExtendedNumberUtils.toIntegerValue(bytes);
            assertEquals("Mismatched re-constructed value", expected, actual);
        }
    }
    
    @Test
    public void testReverseIntValue() {
        final int[] VALUES={
                0x00000000, 0x00000000,
                0xFFFFFFFF, 0xFFFFFFFF,
                0xA1B2C3D4, 0xD4C3B2A1,
                0x7FFFFFFF, 0xFFFFFF7F
            };
        for (int index=0; index < VALUES.length; index += 2) {
            int v1=VALUES[index], v2=VALUES[index+1];
            int v1Rev=ExtendedNumberUtils.reverseBytes(v1);
            assertEquals("Mismatched v1 -> v2 result", Integer.toHexString(v2), Integer.toHexString(v1Rev));

            int v2Rev=ExtendedNumberUtils.reverseBytes(v2);
            assertEquals("Mismatched v2 -> v1 result", Integer.toHexString(v1), Integer.toHexString(v2Rev));
        }
    }

    @Test
    public void testLongToBytesAndBack() {
        byte[]  bytes=new byte[8];
        for (long expected : new long[] {
                0L, (-1L),
                7365L, 3777347L,
                System.currentTimeMillis(), System.nanoTime(),
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Long.MAX_VALUE, Long.MIN_VALUE }) {
            int usedLen=ExtendedNumberUtils.toBytes(expected, bytes);
            assertEquals("Mismatched used length for value=" + expected, 8, usedLen);
            
            long actual=ExtendedNumberUtils.toLongValue(bytes);
            assertEquals("Mismatched re-constructed value", expected, actual);
        }
    }
    
    @Test
    public void testReverseLongValue() {
        final long[] VALUES={
                0x0000000000000000L, 0x0000000000000000L,
                0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL,
                0x6C796F72676F6C64L, 0x646C6F67726F796CL,
                0x7FFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFF7FL
            };
        for (int index=0; index < VALUES.length; index += 2) {
            long v1=VALUES[index], v2=VALUES[index+1];
            long v1Rev=ExtendedNumberUtils.reverseBytes(v1);
            assertEquals("Mismatched v1 -> v2 result", Long.toHexString(v2), Long.toHexString(v1Rev));

            long v2Rev=ExtendedNumberUtils.reverseBytes(v2);
            assertEquals("Mismatched v2 -> v1 result", Long.toHexString(v1), Long.toHexString(v2Rev));
        }
    }
    
    @Test
    public void testCompareDoubles() {
        assertEquals("NaN not equals to itself", 0, ExtendedNumberUtils.compare(Double.NaN, Double.NaN));
        assertTrue("NaN not greater than not-Nan", ExtendedNumberUtils.compare(Double.NaN, Math.PI) > 0);
        assertTrue("not-Nan not smaller than NaN", ExtendedNumberUtils.compare(Math.E, Double.NaN) < 0);
        assertEquals("+Infinity not equals to itself", 0, ExtendedNumberUtils.compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertEquals("-Infinity not equals to itself", 0, ExtendedNumberUtils.compare(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
        assertTrue("+Infinity not greater than not-Nan", ExtendedNumberUtils.compare(Double.POSITIVE_INFINITY, Math.PI) > 0);
        assertTrue("-Infinity not smaller than not-Nan", ExtendedNumberUtils.compare(Double.NEGATIVE_INFINITY, Math.E) < 0);
    }
}
