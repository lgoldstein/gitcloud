/**
 * 
 */
package org.apache.commons.lang3.exception;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author lgoldstein
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedExceptionUtilsTest extends AbstractTestSupport {
	public ExtendedExceptionUtilsTest() {
		super();
	}


    /**
     * Makes sure that if the original exception is already a {@link RuntimeException}
     * then no wrapping occurs - i.e., the original instance is returned (after casting)
     */
    @Test
    public void testUnwrappedException () {
        Throwable   th=new IllegalArgumentException("testUnwrappedException");
        assertTrue("Test un-wrapped exception not runtime", th instanceof RuntimeException);

        RuntimeException    re=ExtendedExceptionUtils.toRuntimeException(th);
        assertSame("Mismatched converted instances", th, re);
    }

    /**
     * Makes sure that if the original exception is not a {@link RuntimeException}
     * then it is wrapped into one using the {@link RuntimeException#RuntimeException(Throwable)}
     * constructor, thus making it it's cause value
     */
    @Test
    public void testWrappedException () {
        Throwable   th=new NoSuchFieldException("testWrappedException");
        assertFalse("Test wrapped exception already runtime", th instanceof RuntimeException);

        RuntimeException    re=ExtendedExceptionUtils.toRuntimeException(th);
        assertNotSame("Unconverted instances", th, re);
        assertSame("Mismatched wrapped instances", th, re.getCause());
    }
    
    @Test
    public void testPeeledException () {
        RuntimeException    ex=new IllegalArgumentException("peeled");
        Throwable           th=new InvocationTargetException(ex, "wrapper");
        RuntimeException    re=ExtendedExceptionUtils.toRuntimeException(th, true);
        assertSame("Mismatched peeled instance", ex, re);
    }

    @Test
    public void testRethrowException () {
        final String    msg="testRethrowException";
        for (Throwable expected : new Throwable[] {  // mixed checked and unchecked exception, errors and exceptions
                new SQLException(msg),
                new IllegalArgumentException(msg),
                new NoSuchMethodError(msg),
                new UnsupportedOperationException(msg),
                new LinkageError(msg),
                new ClassCastException(msg)
            }) {
            try {
            	ExtendedExceptionUtils.rethrowException(expected);
                fail("Unexpected success for " + expected.getClass().getSimpleName());
            } catch(Throwable actual) {
                assertSame("Mismatched caught exception for " + expected.getClass().getSimpleName()
                         + ": " + actual.getClass().getSimpleName() + "[" + actual.getMessage() + "]",
                           expected, actual);
            }
        }
    }
    
    @Test
    public void testGetCause() {
    	Throwable	withoutCause=new IllegalArgumentException("testGetCause");
    	assertNull("Unexpected initial cause", ExtendedExceptionUtils.getSafeCause(withoutCause));

    	Throwable	withCause=new IllegalStateException(withoutCause.getMessage(), withoutCause);
    	assertSame("Mismatched resolved cause", withoutCause, ExtendedExceptionUtils.getSafeCause(withCause));
    }
}
