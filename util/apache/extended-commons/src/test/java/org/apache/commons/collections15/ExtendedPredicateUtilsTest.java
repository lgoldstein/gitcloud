/**
 * 
 */
package org.apache.commons.collections15;

import java.nio.CharBuffer;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedPredicateUtilsTest extends AbstractTestSupport {
    public ExtendedPredicateUtilsTest() {
        super();
    }

    @Test
    public void testCanConvert() {
        ExtendedPredicate<CharSequence> predicate=
                new AbstractExtendedPredicate<CharSequence>(CharSequence.class) {
                    @Override
                    public boolean evaluate(CharSequence object) {
                        return StringUtils.isEmpty(object);
                    }
            };
        for (CharSequence item : new CharSequence[] {
                     getClass().getSimpleName(),
                     new StringBuilder("testCanConvert"),
                     CharBuffer.wrap(getClass().getPackage().getName())
                }) {
            Class<?>    itemClass=item.getClass();
            assertTrue(itemClass.getSimpleName() + ": cannot evaluate", ExtendedPredicateUtils.canEvaluate(predicate, item));
        }
        
        for (Object item : new Object[] { new Date(), Long.valueOf(System.currentTimeMillis()), Double.valueOf(Math.random()) }) {
            Class<?>    itemClass=item.getClass();
            assertFalse(itemClass.getSimpleName() + ": unexpected ability to evaluate", ExtendedPredicateUtils.canEvaluate(predicate, item));
        }
    }
    
    @Test
    public void testSafeEvaluate() {
        ExtendedPredicate<CharSequence> predicate=
                new AbstractExtendedPredicate<CharSequence>(CharSequence.class) {
                    @Override
                    public boolean evaluate(CharSequence object) {
                        return StringUtils.isEmpty(object);
                    }
            };
        for (CharSequence item : new CharSequence[] {
                     getClass().getSimpleName(),
                     new StringBuilder("testCanConvert"),
                     CharBuffer.wrap(getClass().getPackage().getName())
                }) {
            Class<?>    itemClass=item.getClass();
            assertNull(itemClass.getSimpleName() + ": unexpected evaluation result (no predicate)", ExtendedPredicateUtils.safeEvaluate(null, item));
            assertNull(itemClass.getSimpleName() + ": unexpected evaluation result (no item)", ExtendedPredicateUtils.safeEvaluate(predicate, null));
            assertEquals(itemClass.getSimpleName() + ": mismatched evaluation result", Boolean.FALSE, ExtendedPredicateUtils.safeEvaluate(predicate, item));
        }
        
        for (Object item : new Object[] { new Date(), Long.valueOf(System.currentTimeMillis()), Double.valueOf(Math.random()) }) {
            Class<?>    itemClass=item.getClass();
            assertNull(itemClass.getSimpleName() + ": unexpected evaluation result (no predicate)", ExtendedPredicateUtils.safeEvaluate(null, item));
            assertNull(itemClass.getSimpleName() + ": unexpected evaluation result (no item)", ExtendedPredicateUtils.safeEvaluate(predicate, null));
            assertNull(itemClass.getSimpleName() + ": unexpected evaluation result", ExtendedPredicateUtils.safeEvaluate(predicate, item));
        }
    }
}
