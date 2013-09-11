/**
 * 
 */
package org.apache.commons.lang3;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang3.math.ExtendedNumberUtils;
import org.apache.commons.lang3.reflect.ExtendedMethodUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author lgoldstein
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedArrayUtilsTest extends AbstractTestSupport {
	public ExtendedArrayUtilsTest() {
		super();
	}

    @Test
    public void testLengthTArray() {
        String[] arr1 = new String[3];
        Object[] arr2 = null;
        Integer[] arr3 = new Integer[0];
        
        assertEquals("Mismatched String array length", arr1.length, ExtendedArrayUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ExtendedArrayUtils.length(arr2));
        assertEquals("Mismatched empty array length", 0, ExtendedArrayUtils.length(arr3));
    }

    @Test
    public void testLengthIntArray() {
        int[] arr1 = { 7, 3, 6, 5 }, arr2 = null, arr3 = { };
        
        assertEquals("Mismatched array length", arr1.length, ExtendedArrayUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ExtendedArrayUtils.length(arr2));
        assertEquals("Mismatched empty array length", 0, ExtendedArrayUtils.length(arr3));
    }

    @Test
    public void testLengthLongArray() {
        long[] arr1 = { 3, 7, 7, 7, 3, 4, 7 }, arr2 = null, arr3 = { };
        
        assertEquals("Mismatched array length", arr1.length, ExtendedArrayUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ExtendedArrayUtils.length(arr2));
        assertEquals("Mismatched empty array length", 0, ExtendedArrayUtils.length(arr3));
    }

    @Test
    public void testLengthShortArray() {
        short[] arr1 = { 0, 0, 0 }, arr2 = null, arr3 = { };
        
        assertEquals("Mismatched array length", arr1.length, ExtendedArrayUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ExtendedArrayUtils.length(arr2));
        assertEquals("Mismatched empty array length", 0, ExtendedArrayUtils.length(arr3));
    }

    @Test
    public void testLengthDoubleArray() {
        double[] arr1 = { 7d, 3d, 6d, 5d }, arr2 = null, arr3 = { };
        
        assertEquals("Mismatched array length", arr1.length, ExtendedArrayUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ExtendedArrayUtils.length(arr2));
        assertEquals("Mismatched empty array length", 0, ExtendedArrayUtils.length(arr3));
    }

    @Test
    public void testLengthFloatArray() {
        float[] arr1 = { 3f, 7f, 7f, 7f, 3f, 4f, 7f }, arr2 = null, arr3 = { };
        
        assertEquals("Mismatched array length", arr1.length, ExtendedArrayUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ExtendedArrayUtils.length(arr2));
        assertEquals("Mismatched empty array length", 0, ExtendedArrayUtils.length(arr3));
    }

    @Test
    public void testLengthByteArray() {
        byte[] arr1 = { 0, 0, 0 }, arr2 = null, arr3 = { };
        
        assertEquals("Mismatched array length", arr1.length, ExtendedArrayUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ExtendedArrayUtils.length(arr2));
        assertEquals("Mismatched empty array length", 0, ExtendedArrayUtils.length(arr3));
    }

    @Test
    public void testLengthCharArray() {
        char[] arr1 = { 'r', 'o', 'y', 'l' }, arr2 = null, arr3 = { };
        
        assertEquals("Mismatched array length", arr1.length, ExtendedArrayUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ExtendedArrayUtils.length(arr2));
        assertEquals("Mismatched empty array length", 0, ExtendedArrayUtils.length(arr3));
    }

    @Test
    public void testLengthBooleanArray() {
        boolean[] arr1 = { true, false }, arr2 = null, arr3 = { };
        
        assertEquals("Mismatched array length", arr1.length, ExtendedArrayUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ExtendedArrayUtils.length(arr2));
        assertEquals("Mismatched empty array length", 0, ExtendedArrayUtils.length(arr3));
    }

    @Test
    public void testDiffsOffsetBytes() {
        testDiffsOffsets(Byte.valueOf((byte) -1), new byte[] { 0, Byte.SIZE, Byte.MAX_VALUE, Byte.MIN_VALUE });
    }

    @Test
    public void testDiffsOffsetShorts() {
        testDiffsOffsets(Short.valueOf((short) -1), new short[] { 0, Short.SIZE, Short.MAX_VALUE, Short.MIN_VALUE });
    }

    @Test
    public void testDiffsOffsetInts() {
        testDiffsOffsets(Integer.valueOf(-1), new int[] { 3, 7, 7, 3, 4, 7 });
    }

    @Test
    public void testDiffsOffsetLongs() {
        testDiffsOffsets(Long.valueOf(-1L), new long[] { 7, 3, 6, 5 });
    }

    @Test
    public void testDiffsOffsetFloats() {
        testDiffsOffsets(Float.valueOf(73.65f),
                new float[] { 0, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NaN });
    }

    @Test
    public void testDiffsOffsetDoubles() {
        testDiffsOffsets(Double.valueOf(3777.347d),
                new double[] { 0, Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN });
    }

    @Test
    public void testDiffsOffsetChars() {
        testDiffsOffsets(Character.valueOf('\0'), new char[] { 'r', 'o', 'y', 'l' });
    }

    @Test
    public void testDiffsOffsetObjects() {
        String[]    seed={ getClass().getPackage().getName(), getClass().getSimpleName(), "testDiffsOffsetObjects" };
        assertTrue("Mismatched self diff offset result", ExtendedArrayUtils.diffOffset(seed, 0, seed, 0, String.CASE_INSENSITIVE_ORDER, seed.length) < 0);
        
        String[]    copyVals=seed.clone();
        for (int    index=0; index < seed.length; index++) {
            copyVals[index] = "lgold";
            assertEquals("Mismatched indexed ofset value", index, ExtendedArrayUtils.diffOffset(seed, 0, copyVals, 0, String.CASE_INSENSITIVE_ORDER, seed.length));
            assertEquals("Mismatched zero-based ofset value", 0, ExtendedArrayUtils.diffOffset(seed, index, copyVals, index, String.CASE_INSENSITIVE_ORDER, seed.length - index));
            copyVals[index] = seed[index];  // restore original value
        }
    }

    @Test     // NOTE: this test represents the boolean/short/int/long/float/double as well since same logic
    public void testFindFirstNonMatchingIndexByte() {
        byte[]  a1=new byte[Long.SIZE], a2=new byte[a1.length];
        byte[]  lt=new byte[a1.length - Byte.SIZE], gt=new byte[a1.length + Byte.SIZE];
        for (int index=0; index < Long.SIZE; index++) {
            synchronized(RANDOMIZER) {
                RANDOMIZER.nextBytes(gt);
            }
            
            System.arraycopy(gt, 0, a1, 0, a1.length);
            final String  v1=Arrays.toString(a1);
            assertEquals("Mismatched self check result: " + v1, -1, ExtendedNumberUtils.signOf(ExtendedArrayUtils.findFirstNonMatchingIndex(a1, a1)));

            System.arraycopy(a1, 0, a2, 0, a1.length);
            assertEquals("Mismatched equals check result: " + v1, -1, ExtendedNumberUtils.signOf(ExtendedArrayUtils.findFirstNonMatchingIndex(a1, a2)));
            
            System.arraycopy(a1, 0, lt, 0, lt.length);
            assertEquals("Mismatched prefix-last result: " + v1, lt.length, ExtendedArrayUtils.findFirstNonMatchingIndex(a1, lt));
            assertEquals("Mismatched prefix-first result: " + v1, lt.length, ExtendedArrayUtils.findFirstNonMatchingIndex(lt, a1));
            
            assertEquals("Mismatched suffix-last result: " + v1, a1.length, ExtendedArrayUtils.findFirstNonMatchingIndex(a1, gt));
            assertEquals("Mismatched suffix-first result: " + v1, a1.length, ExtendedArrayUtils.findFirstNonMatchingIndex(gt, a1));

            for (int pos=0; pos < a1.length; pos++) {
                a1[pos]++;
                try {
                    assertEquals("Mismatched result for position=" + pos + ": " + v1, pos, ExtendedArrayUtils.findFirstNonMatchingIndex(a1, a2));
                    assertEquals("Mismatched reversed result for position=" + pos + ": " + v1, pos, ExtendedArrayUtils.findFirstNonMatchingIndex(a2, a1));
                } finally {
                    a1[pos] = a2[pos];  // restore original value
                }
            }
        }
    }

    @Test
    public void testFindFirstNonMatchingIndexNanDoublesAndInfinity() {
        double[]    a1=new double[Long.SIZE], a2=new double[a1.length];
        for (int index=0; index < Long.SIZE; index++) {
            for (int pos=0; pos < a1.length; pos++) {
                final double    value;
                synchronized(RANDOMIZER) {
                    value = RANDOMIZER.nextDouble();
                }
                
                a1[pos] = value;
            }

            final String  v1=Arrays.toString(a1);
            assertEquals("Mismatched self check result: " + v1, -1, ExtendedNumberUtils.signOf(ExtendedArrayUtils.findFirstNonMatchingIndex(a1, a1)));

            System.arraycopy(a1, 0, a2, 0, a1.length);
            assertEquals("Mismatched equals check result: " + v1, -1, ExtendedNumberUtils.signOf(ExtendedArrayUtils.findFirstNonMatchingIndex(a1, a2)));
            
            for (int pos=0; pos < a1.length; pos++) {
                final double    value=a1[pos];
                try {
                    a1[pos] = Double.NaN;
                    assertEquals("Mismatched NaN result for position=" + pos + ": " + v1, pos, ExtendedArrayUtils.findFirstNonMatchingIndex(a1, a2));
                    assertEquals("Mismatched reversed NaN result for position=" + pos + ": " + v1, pos, ExtendedArrayUtils.findFirstNonMatchingIndex(a2, a1));

                    a2[pos] = Double.NaN;
                    assertEquals("Mismatched NaN equals check result for pos=" + pos + ": " + v1,
                                 -1, ExtendedNumberUtils.signOf(ExtendedArrayUtils.findFirstNonMatchingIndex(a1, a2)));
                } finally {
                    a1[pos] = value;
                    a2[pos] = value;
                }
            }
        }
    }

    private void testDiffsOffsets(Object nonExistingValue, Object seed) {
        Class<?>    seedClass=seed.getClass();
        assertTrue(seedClass.getSimpleName() + ": not an array", seedClass.isArray());
        
        Method  diffMethod=MethodUtils.getAccessibleMethod(
                ExtendedArrayUtils.class, "diffOffset", seedClass, Integer.TYPE, seedClass, Integer.TYPE, Integer.TYPE);
        assertNotNull(seedClass.getSimpleName() + ": no matching method found", diffMethod);
        
        Integer seedLength=Integer.valueOf(ArrayUtils.getLength(seed));
        Integer result=ExtendedMethodUtils.retrieveTypedStaticValue(
                            diffMethod, Integer.class, seed, Integer.valueOf(0), seed, Integer.valueOf(0), seedLength);
        assertEquals(seedClass.getSimpleName() + ": mismatched self diff result", (-1), result.intValue());

        Class<?>    compType=seedClass.getComponentType();
        Object   copyVals=Array.newInstance(compType,seedLength.intValue());
        System.arraycopy(seed, 0, copyVals, 0, seedLength.intValue());
        
        for (int index=0; index < seedLength.intValue(); index++) {
            Array.set(copyVals, index, nonExistingValue);
                
            result = ExtendedMethodUtils.retrieveTypedStaticValue(
                        diffMethod, Integer.class, seed, Integer.valueOf(0), copyVals, Integer.valueOf(0), seedLength);
            assertEquals(seedClass.getSimpleName() + ": Mismatched index-based offset", index, result.intValue());

            result = ExtendedMethodUtils.retrieveTypedStaticValue(
                    diffMethod, Integer.class, seed, Integer.valueOf(index), copyVals, Integer.valueOf(index), Integer.valueOf(seedLength.intValue() - index));
            assertEquals(seedClass.getSimpleName() + ": Mismatched zero-based offset", 0, result.intValue());

            // restore original
            Object  orgVal=Array.get(seed, index);
            Array.set(copyVals, index, orgVal);
        }
    }
}
