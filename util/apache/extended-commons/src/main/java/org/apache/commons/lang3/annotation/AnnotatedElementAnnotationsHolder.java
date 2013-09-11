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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

abstract class AnnotatedElementAnnotationsHolder<T extends AnnotatedElement> implements Serializable {
    private static final long serialVersionUID = -2328596033327838684L;

    private final T annotatedElement;
    private final Map<Class<? extends Annotation>, Annotation> annotationsMap;
    
    AnnotatedElementAnnotationsHolder(T annObj, Map<Class<? extends Annotation>, Annotation> annMap) {
        this.annotatedElement = annObj;
        this.annotationsMap = Collections.unmodifiableMap(annMap);
    }

    public final T getAnnotatedElement() {
        return annotatedElement;
    }

    public final Map<Class<? extends Annotation>, Annotation> getAnnotationsMap() {
        return annotationsMap;
    }
    
    @SuppressWarnings("unchecked")
    public final <A extends Annotation> A getAnnotation(Class<A> clazz) {
        return (A) annotationsMap.get(clazz);
    }
    
    public abstract String getAnnotatedElementName();
    

    abstract static class AbstractBuilder<T extends AnnotatedElement, E extends AnnotatedElementAnnotationsHolder<T>> {
        final T annotatedObject;
        final Map<Class<? extends Annotation>, Annotation> annotationsMap;

        AbstractBuilder(T annObj) {
            super();
            this.annotatedObject = annObj;
            this.annotationsMap = new HashMap<>();
        }

        public final AbstractBuilder<T, E> withAnnotations(Annotation... annotations) {
            if (annotations == null) {
                throw new IllegalArgumentException("annotations can't be null");
            }
            
            for (Annotation annotation : annotations) {
                if (annotation == null) {
                    throw new IllegalArgumentException("annotation can't be null");
                }
                
                this.annotationsMap.put(annotation.annotationType(), annotation);
            }
            
            return this;
        }
        
        public abstract E build();
    }
}
