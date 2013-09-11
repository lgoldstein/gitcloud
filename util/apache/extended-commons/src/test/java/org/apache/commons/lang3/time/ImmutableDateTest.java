/**
 * 
 */
package org.apache.commons.lang3.time;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.apache.commons.beanutils.ExtendedBeanUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ImmutableDateTest extends AbstractTestSupport {
    public ImmutableDateTest() {
        super();
    }

    @Test
    public void testImmutability () {
        final Date      EXPECTED=new Date(731965L), ACTUAL=new ImmutableDate(EXPECTED.getTime());
        for (Method mthd : Date.class.getDeclaredMethods()) {
            if (!ExtendedBeanUtils.isSetter(mthd)) {
                continue;   // interested only in setters
            }
            
            final String  name=mthd.getName();
            Class<?>[]    params=mthd.getParameterTypes();
            Class<?>      paramType=params[0];
            final Object   invocationArg;
            if (Integer.class.isAssignableFrom(paramType) || Integer.TYPE.isAssignableFrom(paramType)) {
                invocationArg = Integer.valueOf(17041690);
            } else if (Long.class.isAssignableFrom(paramType) || Long.TYPE.isAssignableFrom(paramType)) {
                invocationArg = Long.valueOf(17041690);
            } else {
                throw new IllegalArgumentException("Unknown parameter type (" + paramType.getName() + ") for method=" + name);
            }
            
            try {
                mthd.invoke(ACTUAL, invocationArg);
                fail("Unexpected success in invocation of " + name);
            } catch(Exception e) {
                assertTrue("Unkown exception on invocation of " + name + ": " + e.getClass().getName(), (e instanceof InvocationTargetException));

                Throwable   t=((InvocationTargetException) e).getTargetException();
                assertTrue("Unkown target exception on invocation of " + name + ": " + e.getClass().getName(), (t instanceof UnsupportedOperationException));
            }
            
            assertEquals("Mismatched value after invocation of " + name, EXPECTED, ACTUAL);
        }
    }
}
