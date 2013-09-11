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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.apache.commons.beanutils.ExtendedBeanUtils;
import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.ExtendedPredicate;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.functors.AllPredicate;
import org.apache.commons.lang3.ArrayUtils;

public class ClassMethodsAnnotations<T> extends ClassAnnotatedElementsAnnotations<T, Method, MethodAnnotationsHolder> {
    private static final long serialVersionUID = -430843940944579873L;

    private ClassMethodsAnnotations(Class<T> srcClass, Map<String, MethodAnnotationsHolder> map) {
        super(srcClass, map);
    }
    
    public static final ExtendedPredicate<MethodAnnotationsHolder> PUBLIC_METHODS_PREDICATE =
            new AbstractExtendedPredicate<MethodAnnotationsHolder>(MethodAnnotationsHolder.class) {
        @Override
        public boolean evaluate(MethodAnnotationsHolder object) {
            Method m = object.getAnnotatedElement();
            return Modifier.isPublic(m.getModifiers());
        }
    };
    
    public static final ExtendedPredicate<MethodAnnotationsHolder> STATIC_METHODS_PREDICATE =
            new AbstractExtendedPredicate<MethodAnnotationsHolder>(MethodAnnotationsHolder.class) {
        @Override
        public boolean evaluate(MethodAnnotationsHolder object) {
            Method m = object.getAnnotatedElement();
            return Modifier.isStatic(m.getModifiers());
        }
    };
    
    public static final ExtendedPredicate<MethodAnnotationsHolder> GETTER_OR_SETTER_PREDICATE =
            new AbstractExtendedPredicate<MethodAnnotationsHolder>(MethodAnnotationsHolder.class) {
        @Override
        public boolean evaluate(MethodAnnotationsHolder object) {
            Method m = object.getAnnotatedElement();
            return ExtendedBeanUtils.isGetter(m) || ExtendedBeanUtils.isSetter(m);
        }
    };
    
    public static final ExtendedPredicate<MethodAnnotationsHolder> NOT_STATIC_METHODS_PREDICATE =
            new AbstractExtendedPredicate<MethodAnnotationsHolder>(MethodAnnotationsHolder.class) {
        @Override
        public boolean evaluate(MethodAnnotationsHolder object) {
            return !STATIC_METHODS_PREDICATE.evaluate(object);
        }
    };
    
    @SuppressWarnings("unchecked")
    public static final Predicate<MethodAnnotationsHolder> PUBLIC_NOT_STATIC_PREDICATE = 
                    new AllPredicate<>(ArrayUtils.toArray(PUBLIC_METHODS_PREDICATE, NOT_STATIC_METHODS_PREDICATE));
    
    public static final class Builder<T> extends AbstractBuilder<T, Method, MethodAnnotationsHolder> {
        @Override
        protected MethodAnnotationsHolder.Builder createHolderBuilder(Method element) {
            return new MethodAnnotationsHolder.Builder(element);
        }

        @SuppressWarnings("synthetic-access")
		@Override
        protected ClassMethodsAnnotations<T> build(Class<T> srcClass, Map<String, MethodAnnotationsHolder> holdersMap) {
            return new ClassMethodsAnnotations<T>(srcClass, holdersMap);
        }
    }
}
