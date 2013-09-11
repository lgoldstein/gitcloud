/**
 * 
 */
package org.apache.commons.lang3.reflect;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProxyUtilsTest extends AbstractTestSupport {
    public ProxyUtilsTest() {
        super();
    }

    @Test
    public void testNewProxyInstance () throws Exception {
        final String    TEST_DATA="testNewProxyInstance";
        StringBuilder   sb=new StringBuilder();
        Appendable      result=ProxyUtils.newProxyInstance(Appendable.class, createDelegatingInvocationHandler(sb), Appendable.class);
        // this must be false since we only specified Appendable as the proxy interface
        assertFalse("Result is a " + CharSequence.class.getSimpleName(), result instanceof CharSequence);
        
        Appendable  actual=result.append(TEST_DATA);
        assertSame("Mismatched appended instance result", sb, actual);
        assertEquals("Mismatched builder contents", TEST_DATA, sb.toString());
    }

    @Test
    public void testNewProxyInstanceFailures () {
        final Object            dummy=new Object();
        final InvocationHandler h=createDelegatingInvocationHandler(dummy);
        for (Class<?>[] interfaces : new Class<?>[][] {
                null,   // null array
                new Class[] { },    // empty array
                new Class[] { CharSequence.class, StringBuilder.class /* not an interface */, Appendable.class },
                new Class[] { CharSequence.class, null /* blank spot */, Appendable.class }
        }) {
            try {
                Object  result=ProxyUtils.newProxyInstance(Object.class, h, interfaces);
                fail("Unexpected proxy success for " + Arrays.toString(interfaces) + ": " + result);
            } catch(IllegalArgumentException e) {
                // expected - ignored
            }
        }
    }

    @Test
    public void testUnimplementedHandler() throws IOException {
        Appendable      result=ProxyUtils.newProxyInstance(Appendable.class, ProxyUtils.UNIMPLEMENTED_HANDLER, Appendable.class);
        try {
            result.append('x');
            fail("Unexpected char append success");
        } catch(UnsupportedOperationException e) {
            // expected - ignored
        }
        
        try {
            result.append("testUnimplementedHandler");
            fail("Unexpected full sequence append success");
        } catch(UnsupportedOperationException e) {
            // expected - ignored
        }

        try {
            result.append("testUnimplementedHandler", 0, 1);
            fail("Unexpected partial sequence append success");
        } catch(UnsupportedOperationException e) {
            // expected - ignored
        }
    }

    private InvocationHandler createDelegatingInvocationHandler (final Object target) {
        return new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                return method.invoke(target, args);
            }
        };
    }
}
