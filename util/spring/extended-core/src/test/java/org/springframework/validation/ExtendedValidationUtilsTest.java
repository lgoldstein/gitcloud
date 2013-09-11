/**
 * 
 */
package org.springframework.validation;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.springframework.test.AbstractSpringTestSupport;

/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedValidationUtilsTest extends AbstractSpringTestSupport {
    public ExtendedValidationUtilsTest() {
        super();
    }

    @Test
    public void testToValidatorProxyDelegation() {
        final AtomicBoolean callGet=new AtomicBoolean(false);
        final AtomicBoolean callValidate=new AtomicBoolean(false);
        final AtomicReference<Object>   validateArg=new AtomicReference<Object>(null);
        TypedValidator<CharSequence>   expected=new TypedValidator<CharSequence>() {
                @Override
                public Class<CharSequence> getEntityClass() {
                    assertFalse("Multiple entity get calls", callGet.getAndSet(true));
                    return CharSequence.class;
                }
    
                @Override
                public void validate(CharSequence target, Errors errors) {
                    assertFalse("Multiple validate calls", callValidate.getAndSet(true));
                    assertNull("Multiple validate objects", validateArg.getAndSet(target));
                }
            };
        Validator   actual=ExtendedValidationUtils.toValidator(expected);
        assertNotNull("No proxy created", actual);
        
        assertTrue("Mismatched supporting class", actual.supports(StringBuilder.class));
        assertTrue("Entity class getter not invokled", callGet.getAndSet(false));
        
        final String    expArg="testToValidatorProxy";
        actual.validate(expArg, Mockito.mock(Errors.class));
        assertTrue("Validation method not invokled", callValidate.get());
        assertSame("Mismatched validation argument", expArg, validateArg.get());
    }
    
    @Test
    public void testToValidatorProxySupportResult() {
        TypedValidator<CharSequence>   expected=new TypedValidator<CharSequence>() {
                @Override
                public Class<CharSequence> getEntityClass() {
                    return CharSequence.class;
                }
    
                @Override
                @SuppressWarnings("synthetic-access")
                public void validate(CharSequence target, Errors errors) {
                    logger.info("validate(" + target.getClass().getSimpleName() + "): " + target);
                }
            };
        Validator   actual=ExtendedValidationUtils.toValidator(expected);
        for (Class<?> c : Arrays.asList(CharSequence.class, String.class, StringBuilder.class)) {
            assertTrue(c.getSimpleName() + ": not supported", actual.supports(c));
        }
        for (Class<?> c : Arrays.asList(Date.class, Long.class, getClass())) {
            assertFalse(c.getSimpleName() + ": unexpectedly supported", actual.supports(c));
        }
    }

}
