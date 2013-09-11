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

package org.apache.commons.lang3.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedPredicate;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.AnnotationUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.annotation.ClassAnnotatedElementsAnnotations.AbstractBuilder;
import org.apache.commons.lang3.reflect.ExtendedMethodUtils;

public class ExtendedAnnotationUtils extends AnnotationUtils {
    public ExtendedAnnotationUtils() {
        super();
    }

    public static final Annotation[]    EMPTY_ANNOTATION_ARRAY={ };

    /**
     * An {@link ExtendedTransformer} that returns all the declared annotations
     * of a an {@link AnnotatedElement}
     * @see AnnotatedElement#getDeclaredAnnotations()
     */
    public static final ExtendedTransformer<AnnotatedElement,Annotation[]>  DECLARED_ANNOTATIONS_EXTRACTOR=
            new AbstractExtendedTransformer<AnnotatedElement,Annotation[]>(AnnotatedElement.class, Annotation[].class) {
                @Override
                public Annotation[] transform (AnnotatedElement e) {
                    if (e == null) {
                        return EMPTY_ANNOTATION_ARRAY;
                    } else {
                        return e.getDeclaredAnnotations();
                    }
                }
            };
    /**
     * An {@link ExtendedTransformer} that returns the annotations of a an {@link AnnotatedElement}
     * @see AnnotatedElement#getAnnotations()
     */
    public static final ExtendedTransformer<AnnotatedElement,Annotation[]>  ELEMENT_ANNOTATIONS_EXTRACTOR=
            new AbstractExtendedTransformer<AnnotatedElement,Annotation[]>(AnnotatedElement.class, Annotation[].class) {
                @Override
                public Annotation[] transform (AnnotatedElement e) {
                    if (e == null) {
                        return EMPTY_ANNOTATION_ARRAY;
                    } else {
                        return e.getAnnotations();
                    }
                }
            };

    public static final <T> ClassMethodsAnnotations<T> getClassMethodAnnotations(Class<T> clazz) {
        ClassMethodsAnnotations.Builder<T> builder = new ClassMethodsAnnotations.Builder<T>();
        fillClassAnnotatedElementAnnotations(clazz, builder, ExtendedMethodUtils.DECLARED_METHODS_TRANSFORMER);
        return builder.build(clazz);
    }
    
    /**
     * @param ann An {@link Annotation} class
     * @return An {@link ExtendedPredicate} that returns <code>true</code> if an
     * {@link AnnotatedElement} contains the specified annotation
     * @throws IllegalArgumentException if no annotation class specified
     * @see Class#isAnnotation()
     * @see AnnotatedElement#isAnnotationPresent(Class)
     */
    public static final ExtendedPredicate<AnnotatedElement> annotatedPredicate(final Class<? extends Annotation> ann) {
        ExtendedValidate.notNull(ann, "No annotation class specified");
        ExtendedValidate.isTrue(ann.isAnnotation(), "Not an annotation class: %s", ann.getSimpleName());
        
        return new AbstractExtendedPredicate<AnnotatedElement>(AnnotatedElement.class) {
            @Override
            public boolean evaluate(AnnotatedElement object) {
                if (object == null) {
                    return false;
                } else {
                    return object.isAnnotationPresent(ann);
                }
            }
        };
    }

    /**
     * @param ann An {@link Annotation} class
     * @return A {@link Transformer} that extracts the annotation from
     * the provided {@link AnnotatedElement} (if exists). <B>Note:</B> if
     * {@link Transformer#transform(Object)} returns <code>null</code> it can
     * be either because the input was <code>null</code> or the annotation does
     * not exist
     * @throws IllegalArgumentException if no annotation class specified
     * @see Class#isAnnotation()
     * @see AnnotatedElement#isAnnotationPresent(Class)
     */
    public static final <A extends Annotation> Transformer<AnnotatedElement,A> annotatedTransformer(final Class<? extends A> ann) {
        ExtendedValidate.notNull(ann, "No annotation class specified");
        ExtendedValidate.isTrue(ann.isAnnotation(), "Not an annotation class: %s", ann.getSimpleName());

        return new Transformer<AnnotatedElement,A>() {
            @Override
            public A transform(AnnotatedElement e) {
                if (e == null) {
                    return null;
                } else {
                    return e.getAnnotation(ann);
                }
            }
        };
    }

    private static <T, E extends AnnotatedElement, A extends AnnotatedElementAnnotationsHolder<E>>  
                    void fillClassAnnotatedElementAnnotations(Class<T> clazz, AbstractBuilder<T, E, A> builder, Transformer<Class<?>, E[]> transformer) {
        List<Pair> pairs = new ArrayList<>() ;
        LinkedList<Pair> classesList = new LinkedList<>();
        
        classesList.add(new Pair(Integer.valueOf(0), clazz));
        
        while(!classesList.isEmpty()) {
            Pair p = classesList.removeFirst();
            pairs.add(p);
            
            Class<?> currentClass = p.getValue();
            Integer next = Integer.valueOf(p.getKey().intValue() + 1);
            
            Class<?> superClass = currentClass.getSuperclass();
            if (superClass != null && !Object.class.equals(superClass)) {
                classesList.add(new Pair(next, superClass));
            }
            
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> interfase : interfaces) {
                classesList.add(new Pair(next, interfase));
            }
        }
        
        Collections.sort(pairs);
        
        for(Pair p : pairs) {
            E[] elements = transformer.transform(p.getValue());
            for (E element : elements) {
                Annotation[] annotations = element.getAnnotations();
                builder.withAnnotatedElement(element).withAnnotations(annotations);
            }
        }
    }
  
    private static class Pair extends DefaultKeyValue<Integer, Class<?>> implements Comparable<Pair> {
        public Pair(Integer k, Class<?> v) {
            super(k, v);
        }

        @Override
        public int compareTo(Pair o) {
            return o.key.compareTo(key);
        }
    }
}
