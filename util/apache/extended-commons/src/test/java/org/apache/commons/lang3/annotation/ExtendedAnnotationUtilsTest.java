package org.apache.commons.lang3.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedAnnotationUtilsTest extends AbstractTestSupport {
    public ExtendedAnnotationUtilsTest() {
        super();
    }

    @Test
    public void testGetClassMethodAnnotationsInterfaceA() {
        ClassMethodsAnnotations<A> ann = ExtendedAnnotationUtils.getClassMethodAnnotations(A.class);
        
        assertNotNull("returned value", ann);
        assertEquals("total holders", 2, ann.size());
        
        MethodAnnotationsHolder method1Holder = ann.findByName("method1");
        MethodAnnotationsHolder method2Holder = ann.findByName("method2");
        
        assertNotNull("A#method1 holder", method1Holder);
        assertNotNull("A#method2 holder", method2Holder);
        
        Annotation1 m1a1 = method1Holder.getAnnotation(Annotation1.class);
        Annotation2 m1a2 = method1Holder.getAnnotation(Annotation2.class);
        
        assertNotNull("A#method1 annotation1", m1a1);
        assertNotNull("A#method1 annotation2", m1a2);
        
        Annotation1 m2a1 = method2Holder.getAnnotation(Annotation1.class);
        Annotation2 m2a2 = method2Holder.getAnnotation(Annotation2.class);
        
        assertNull("A#method2 annotation1", m2a1);
        assertNotNull("A#method2 annotation2", m2a2);
    }
    
    @Test
    public void testGetClassMethodAnnotationsClassB() {
        ClassMethodsAnnotations<B> ann = ExtendedAnnotationUtils.getClassMethodAnnotations(B.class);
        
        assertNotNull("returned value", ann);
        assertEquals("total holders", 2, ann.size());
        
        MethodAnnotationsHolder method1Holder = ann.findByName("method1");
        MethodAnnotationsHolder method2Holder = ann.findByName("method2");
        
        assertNotNull("B#method1 holder", method1Holder);
        assertNotNull("B#method2 holder", method2Holder);
        
        Annotation1 m1a1 = method1Holder.getAnnotation(Annotation1.class);
        Annotation2 m1a2 = method1Holder.getAnnotation(Annotation2.class);
        
        assertNotNull("B#method1 annotation1", m1a1);
        assertNotNull("B#method1 annotation2", m1a2);
        assertEquals("B#method1 annotation1 start", 2, m1a1.start());
        assertEquals("B#method1 annotation1 end", 2, m1a1.end());
        
        Annotation1 m2a1 = method2Holder.getAnnotation(Annotation1.class);
        Annotation2 m2a2 = method2Holder.getAnnotation(Annotation2.class);
        
        assertNotNull("B#method2 annotation1", m2a1);
        assertNotNull("B#method2 annotation2", m2a2);
        assertEquals("B#method2 annotation2 start", 3, m2a1.start());
        assertEquals("B#method2 annotation2 end", 3, m2a1.end());
    }

    @Test
    public void testAnnotatedPredicate() {
        Predicate<AnnotatedElement> predicate=ExtendedAnnotationUtils.annotatedPredicate(Annotation2.class);
        for (Method m : A.class.getDeclaredMethods()) {
            String  name=m.getName();
            if (!name.startsWith("method")) {
                continue;
            }
            
            assertTrue(name + ": predicate failed", predicate.evaluate(m));
        }
        
        for (Method m : CharSequence.class.getDeclaredMethods()) {
            assertFalse(m.getName() + ": unexpected predicate success", predicate.evaluate(m));
        }
    }

    @Test
    public void testAnnotatedTransformer() {
        Transformer<AnnotatedElement,Annotation1>   transformer=ExtendedAnnotationUtils.annotatedTransformer(Annotation1.class);
        for (Method m : A.class.getDeclaredMethods()) {
            String      name=m.getName();
            Annotation1 expected=m.getAnnotation(Annotation1.class);
            Annotation1 actual=transformer.transform(m);
            if (expected == null) {
                assertNull(name + ": unexpected result", actual);
            } else {
                assertEquals(name + ": mismatched start value", expected.start(), actual.start());
                assertEquals(name + ": mismatched end value", expected.end(), actual.end());
            }
        }        
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface Annotation1 {
        int start();
        int end();
    }
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface Annotation2 {
        // nothing extra
    }
    
    private static interface A {
        @Annotation1(start = 1, end = 1)
        @Annotation2
        void method1();
        
        @Annotation2
        void method2();
    }
    
    private static class B implements A {

        @Override
        @Annotation1(start = 2, end = 2)
        @Annotation2
        public void method1() {
            // do nothing
        }

        @Override
        @Annotation2
        @Annotation1(start = 3, end = 3)
        public void method2() {
            // do nothing
        }
    }

}
