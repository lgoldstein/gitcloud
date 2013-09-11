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

package org.apache.commons.lang3.builder;

import java.lang.reflect.Constructor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.exception.ExtendedExceptionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.ExtendedConstructorUtils;

/**
 * @author Lyor G.
 */
public class BuilderUtils {
    public BuilderUtils() {
        super();
    }

    @SuppressWarnings("rawtypes")
    private static final Builder nullBuilderInstance=new Builder() {
            @Override
            public Object build() {
                return null;
            }
        };
    /**
     * @return A {@link Builder} that always returns {@code null}
     * on each call to {@code build} method
     */
    @SuppressWarnings("unchecked")
    public static final <T> Builder<T> nullBuilder() {
        return nullBuilderInstance;
    }
    
    /**
     * @param constValue The constant value to return
     * @return A {@link Builder} that always returns the constant value
     * on each call to {@code build} method
     */
    public static final <T> Builder<T> constantBuilder(final T constValue) {
        return new Builder<T>() {
            @Override
            public T build() {
                return constValue;
            }
        };
    }
    
    /**
     * @param instanceType The type of instance to create
     * @return An {@link ExtendedBuilder} that returns a new instance of
     * the specified type on each call to {@code build} method
     * @see #instantiateBuilder(Class, Class[], Object[])
     */
    public static final <T> ExtendedBuilder<T> instantiateBuilder(final Class<T> instanceType) {
        return instantiateBuilder(instanceType, ArrayUtils.EMPTY_CLASS_ARRAY, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }
    
    /**
     * @param ctor The {@link Constructor} to use to create instances
     * @return An {@link ExtendedBuilder} that returns a new instance
     * using the specified constructor on each call to {@code build} method
     * @throws NullPointerException if no constructor instance provided
     * @see #instantiateBuilder(Constructor, Object...)
     */
    public static final <T> ExtendedBuilder<T> instantiateBuilder(final Constructor<T> ctor) {
        return instantiateBuilder(ctor, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /**
     * @param instanceType The instance to type to create
     * @param argTypes The constructor's arguments types
     * @param args The constructor's invocation arguments
     * @return An {@link ExtendedBuilder} that invokes the constructor with
     * the given arguments on each call to {@code build} method
     * @throws NullPointerException if no matching constructor found
     * @see #instantiateBuilder(Constructor, Object...)
     * @see ConstructorUtils#getMatchingAccessibleConstructor(Class, Class...)
     */
    public static final <T> ExtendedBuilder<T> instantiateBuilder(
            final Class<T> instanceType, final Class<?>[] argTypes, final Object[] args) {
        return instantiateBuilder(ConstructorUtils.getMatchingAccessibleConstructor(instanceType, argTypes), args);
    }

    /**
     * @param ctor The {@link Constructor} to invoke
     * @param args The invocation arguments
     * @return An {@link ExtendedBuilder} that invokes the constructor with
     * the given arguments on each call to {@code build} method
     * @throws NullPointerException if no constructor instance provided
     */
    public static final <T> ExtendedBuilder<T> instantiateBuilder(final Constructor<T> ctor, final Object ... args) {
        return new AbstractExtendedBuilder<T>(ExtendedValidate.notNull(ctor, "No constructor provided").getDeclaringClass()) {
            @Override
            public T build() {
                return ExtendedConstructorUtils.newInstance(ctor, args);
            }
        };
    }
    
    /**
     * @param t The {@link Throwable} to throw
     * @return A {@link Builder} that throws the specified exception
     * whenever invoked
     * @see ExtendedExceptionUtils#rethrowException(Throwable)
     */
    public static final <T> Builder<T> exceptionBuilder(final Throwable t) {
        ExtendedValidate.notNull(t, "No throwable to throw");
        return new Builder<T>() {
            @Override
            public T build() {
                ExtendedExceptionUtils.rethrowException(t);
                throw new UnsupportedOperationException("build(" + t.getClass().getSimpleName() + ")[" + t.getMessage() + "] - unexpected call", t);
            }
        };
    }
}
