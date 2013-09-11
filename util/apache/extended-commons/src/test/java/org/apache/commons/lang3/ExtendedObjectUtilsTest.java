/**
 * 
 */
package org.apache.commons.lang3;

import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.concurrent.TimeUnitUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedObjectUtilsTest extends AbstractTestSupport {
    public ExtendedObjectUtilsTest() {
        super();
    }

    @Test
    public void testParseIdentity() {
        Pair<String,Integer>    identity=ExtendedObjectUtils.parseIdentity(ObjectUtils.identityToString(this));
        assertNotNull("No identity recovered", identity);
        assertEquals("Mismatched class name", getClass().getName(), identity.getLeft());
        assertEquals("Mismatched hash code", Integer.valueOf(System.identityHashCode(this)), identity.getRight());
    }

    @Test
    public void testParseBadIdentity() {
        for (String value : new String[] {
                "NoSeparator",
                "NoHashCode" + String.valueOf(ExtendedObjectUtils.IDENTITY_STRING_SEPARATOR),
                String.valueOf(ExtendedObjectUtils.IDENTITY_STRING_SEPARATOR) + "7365",
                "NonNumberHashCode" + String.valueOf(ExtendedObjectUtils.IDENTITY_STRING_SEPARATOR) + "Hello"
            }) {
            try {
                Pair<String,Integer> identity=ExtendedObjectUtils.parseIdentity(value);
                fail(value + ": unexpected success: " + identity);
            } catch(IllegalArgumentException e) {
                // expected - ignored
            }
        }
    }

    @Test
    public void testDeepHashSimpleArrays() {
        {
            byte[]  a={ 7, 3, 6, 5 };
            assertEquals("Mismatched byte[] hash code", Arrays.hashCode(a), ExtendedObjectUtils.deepHash(a));
        }

        {
            short[]  a={ 1, 0, 2, 8, 1, 7, 1, 3 };
            assertEquals("Mismatched short[] hash code", Arrays.hashCode(a), ExtendedObjectUtils.deepHash(a));
        }

        {
            int[]  a={ 3, 7, 7, 7, 3, 4, 7 };
            assertEquals("Mismatched int[] hash code", Arrays.hashCode(a), ExtendedObjectUtils.deepHash(a));
        }

        {
            Runtime r=Runtime.getRuntime();
            long[]  a={ System.currentTimeMillis(), System.nanoTime(), r.freeMemory(), r.maxMemory(), r.totalMemory() };
            assertEquals("Mismatched long[] hash code for " + Arrays.toString(a), Arrays.hashCode(a), ExtendedObjectUtils.deepHash(a));
        }

        {
            float[]  a={ Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN, Float.MIN_NORMAL, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY };
            assertEquals("Mismatched float[] hash code", Arrays.hashCode(a), ExtendedObjectUtils.deepHash(a));
        }

        {
            double[]  a={ Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN, Double.MIN_NORMAL, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Math.PI, Math.E };
            assertEquals("Mismatched double[] hash code", Arrays.hashCode(a), ExtendedObjectUtils.deepHash(a));
        }
        
        {
            char[]  a=getClass().getSimpleName().toCharArray();
            assertEquals("Mismatched char[] hash code", Arrays.hashCode(a), ExtendedObjectUtils.deepHash(a));
        }
        
        {
            boolean[]   a={ true, false };
            assertEquals("Mismatched boolean[] hash code", Arrays.hashCode(a), ExtendedObjectUtils.deepHash(a));
        }
    }
    
    @Test
    public void testBaseObjectDeepHash() {
        for (Object o : new Object[] {
                getClass().getSimpleName(),
                TimeUnit.MILLISECONDS,
                Byte.valueOf(Byte.MIN_VALUE),
                Short.valueOf((short) 7365),
                Integer.valueOf(3777347),
                Long.valueOf(System.nanoTime()),
                Float.valueOf(102.81713f),
                Double.valueOf(Math.PI),
                Character.valueOf('l'),
                Boolean.TRUE
        }) {
            Class<?>    c=o.getClass();
            assertTrue(c.getName() + ": not a base class", ExtendedClassUtils.isBaseType(c));
            assertEquals(c.getName() + ": mismatched hash value for " + o, ObjectUtils.hashCode(o), ExtendedObjectUtils.deepHash(o));
        }
    }

    @Test
    public void testCompoundObjectDeepHash() {
        testCompoundObjectDeepHash(Calendar.getInstance());
        testCompoundObjectDeepHash(System.getProperties());
    }
    
    @Test
    public void testComparatorMinNullValue() {
        final String    VALUE="testComparatorMinNullValue";
        assertNull("Unexpected value all null", ExtendedObjectUtils.min(null, null, String.CASE_INSENSITIVE_ORDER));
        assertSame("Unexpected value 1st null", VALUE, ExtendedObjectUtils.min(null, VALUE, String.CASE_INSENSITIVE_ORDER));
        assertSame("Unexpected value 2nd null", VALUE, ExtendedObjectUtils.min(VALUE, null, String.CASE_INSENSITIVE_ORDER));
    }

    @Test
    public void testComparatorMinValue() {
        for (TimeUnit u1 : TimeUnitUtils.VALUES) {
            for (TimeUnit u2 : TimeUnitUtils.VALUES) {
                int         nRes=TimeUnitUtils.BY_DURATION_COMPARATOR.compare(u1, u2);
                TimeUnit    expected=(nRes <= 0) ? u1 : u2;
                TimeUnit    actual=ExtendedObjectUtils.min(u1, u2, TimeUnitUtils.BY_DURATION_COMPARATOR);
                assertSame("Mismatched " + u1 + " vs. " + u2 + " result", expected, actual);

                actual = ExtendedObjectUtils.min(u2, u1, TimeUnitUtils.BY_DURATION_COMPARATOR);
                assertSame("Mismatched " + u2 + " vs. " + u1 + " result", expected, actual);
            }
        }
    }

    @Test
    public void testComparatorMaxNullValue() {
        final String    VALUE="testComparatorMaxNullValue";
        assertNull("Unexpected value all null", ExtendedObjectUtils.max(null, null, String.CASE_INSENSITIVE_ORDER));
        assertSame("Unexpected value 1st null", VALUE, ExtendedObjectUtils.max(null, VALUE, String.CASE_INSENSITIVE_ORDER));
        assertSame("Unexpected value 2nd null", VALUE, ExtendedObjectUtils.max(VALUE, null, String.CASE_INSENSITIVE_ORDER));
    }

    @Test
    public void testComparatorMaxValue() {
        for (TimeUnit u1 : TimeUnitUtils.VALUES) {
            for (TimeUnit u2 : TimeUnitUtils.VALUES) {
                int         nRes=TimeUnitUtils.BY_DURATION_COMPARATOR.compare(u1, u2);
                TimeUnit    expected=(nRes > 0) ? u1 : u2;
                TimeUnit    actual=ExtendedObjectUtils.max(u1, u2, TimeUnitUtils.BY_DURATION_COMPARATOR);
                assertSame("Mismatched " + u1 + " vs. " + u2 + " result", expected, actual);

                actual = ExtendedObjectUtils.max(u2, u1, TimeUnitUtils.BY_DURATION_COMPARATOR);
                assertSame("Mismatched " + u2 + " vs. " + u1 + " result", expected, actual);
            }
        }
    }

    private int testCompoundObjectDeepHash(Object o) {
        int hashValue=ExtendedObjectUtils.deepHash(o);
        System.out.append(ToStringBuilder.reflectionToString(o, ToStringStyle.SHORT_PREFIX_STYLE, false)).append(": ").println(hashValue);
        return hashValue;
    }
}
