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

package org.junit;

import java.util.Comparator;

/**
 * @author lgoldstein
 *
 */
public class ExtendedAssert extends Assert {
	public ExtendedAssert ()
	{
		super();
	}

    public static final void assertEquals (final char expected, final char actual) {
    	assertEquals(null, expected, actual);
    }

    public static final void assertEquals (final String message, final char expected, final char actual) {
    	assertEquals(message, Character.valueOf(expected), Character.valueOf(actual));
    }

	public static final void assertEquals (boolean expected, boolean actual)
    {
		assertEquals(null, expected, actual);
    }

    public static final void assertEquals (String message, boolean expected, boolean actual)
    {
    	assertEquals(message, Boolean.valueOf(expected), Boolean.valueOf(actual));
    }

    /**
     * Checks equality using an optional {@link Comparator}
     * @param message The message to display if comparison fails
     * @param expected Expected value
     * @param actual Actual value
     * @param comp The {@link Comparator} to use - if <code>null</code> then
     * the default {@link #assertEquals(String, Object, Object)} is invoked
     */
    public static final <T> void assertEquals (String message, T expected, T actual, Comparator<? super T> comp) {
    	if (comp == null) {
    		assertEquals(message, expected, actual);
    	} else {
    		int	nRes=comp.compare(expected, actual);
    		if (nRes != 0) {
    			fail(message + ": failed comparison (" + nRes + ") - expected=" + expected + ", actual=" + actual);
    		}
    	}
    }

    public static final void assertObjectInstanceof (String message, Class<?> expected, Object actual)
    {
    	assertInstanceof(message, expected, (actual == null) ? null : actual.getClass());
    }

    public static final void assertInstanceof (String message, Class<?> expected, Class<?> actual)
    {
    	if ((actual == null) || (!expected.isAssignableFrom(actual)))
    		assertEquals(message, expected.getName(), (actual == null) ? null : actual.getName());
    }

    public static final void assertNotMatches (boolean v1, boolean v2)
    {
    	assertNotMatches(null, v1, v2);
    }

    public static final void assertNotMatches (String message, boolean v1, boolean v2)
    {
		if (v1 == v2)
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

    public static final void assertNotEquals (float v1, float v2)
    {
    	assertNotEquals(null, v1, v2);
    }

    public static final void assertNotEquals (String message, float v1, float v2)
    {
		if (Float.compare(v1, v2) == 0)
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

    public static final void assertNotEquals (double v1, double v2)
    {
    	assertNotEquals(null, v1, v2);
    }

    public static final void assertNotEquals (String message, double v1, double v2)
    {
		if (Double.compare(v1, v2) == 0)
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

    public static final void assertNotEquals (int v1, int v2)
    {
    	assertNotEquals(null, v1, v2);
    }

    public static final void assertNotEquals (String message, int v1, int v2)
    {
		if (v1 == v2)
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

    public static final void assertNotEquals (long v1, long v2)
    {
    	assertNotEquals(null, v1, v2);
    }

    public static final void assertNotEquals (String message, long v1, long v2)
    {
		if (v1 == v2)
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

    public static final void assertNotEquals (Object v1, Object v2)
    {
    	assertNotEquals(null, v1, v2);
    }

    public static final void assertNotEquals (String message, Object v1, Object v2)
    {
		if ((v1 == v2)
		 ||	((v1 != null) && v1.equals(v2))
		 || ((v2 != null) && v2.equals(v1)))
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

    public static final void assertNotNan (final String message, final float value) {
        if (Float.isNaN(value))
            fail(message + ": expected: " + value + ", actual: NaN");
    }

    public static final void assertNan (final String message, final float value)
    {
        if (!Float.isNaN(value))
            fail(message + ": expected: NaN, actual: " + value);
    }

	public static final void assertNotNan (final String message, final double value) {
		if (Double.isNaN(value))
			fail(message + ": expected: " + value + ", actual: NaN");
	}

	public static final void assertNan (final String message, final double value)
	{
		if (!Double.isNaN(value))
			fail(message + ": expected: NaN, actual: " + value);
	}

	public static final void unexpectedSuccess(final Object unexpected, final Class<? extends Throwable> expectedClass) {
        fail("Unexpected success: " + unexpected + ". Expected: " + expectedClass.getSimpleName());
    }

	public static final void assertEmpty(String message, CharSequence data) {
	    if ((data != null) && (data.length() > 0)) {
	        fail(message + ": " + data);
	    }
	}

	public static final void assertNotEmpty(String message, CharSequence data) {
	    if ((data == null) || (data.length() <= 0)) {
	        fail(message + ": " + ((data == null) ? null : "\"\""));
	    }
	}
}
