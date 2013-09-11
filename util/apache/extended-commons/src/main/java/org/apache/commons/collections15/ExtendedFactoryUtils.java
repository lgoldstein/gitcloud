/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.collections15;

import java.lang.reflect.Constructor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.builder.AbstractExtendedBuilder;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.builder.ExtendedBuilder;
import org.apache.commons.lang3.exception.ExtendedExceptionUtils;
import org.apache.commons.lang3.reflect.ExtendedConstructorUtils;

/**
 * @author Lyor G.
 */
public class ExtendedFactoryUtils extends FactoryUtils {
    public ExtendedFactoryUtils() {
        super();
    }

    /**
     * @param instanceType Type of instance being created
     * @param factory The &quot;regular&quot; factory
     * @return An {@link ExtendedFactory} that delegates to the original one
     * its {@link Factory#create()} invocation
     */
    public static final <T> ExtendedFactory<T> extend(Class<T> instanceType, final Factory<? extends T> factory) {
        ExtendedValidate.notNull(factory, "No factory");
        return new AbstractExtendedFactory<T>(instanceType) {
            @Override
            public T create() {
                return factory.create();
            }
        };
    }

    /**
     * @param factory The {@link Factory} instance to wrap as a {@link Builder}
     * @return A {@link Builder} that invokes the {@link Factory#create()}
     * method on each call to its {@link Builder#build()} method
     */
    public static final <T> Builder<T> asBuilder(final Factory<T> factory) {
        ExtendedValidate.notNull(factory, "No factory");
        return new Builder<T>() {
            @Override
            public T build() {
                return factory.create();
            }
        };
    }

    /**
     * @param factory The {@link ExtendedFactory} instance to wrap as a {@link ExtendedBuilder}
     * @return An {@link ExtendedBuilder} that invokes the {@link Factory#create()}
     * method on each call to its {@link Builder#build()} method
     */
    public static final <T> ExtendedBuilder<T> asBuilder(final ExtendedFactory<T> factory) {
        ExtendedValidate.notNull(factory, "No factory");
        return new AbstractExtendedBuilder<T>(factory.getInstanceType()) {
            @Override
            public T build() {
                return factory.create();
            }
        };
    }
    
    /**
     * @param builder The {@link Builder} instance to wrap as a {@link Factory}
     * @return A {@link Factory} that invokes the {@link Builder#build()} every
     * time its {@link Factory#create()} method is invoked
     */
    public static final <T> Factory<T> asFactory(final Builder<? extends T> builder) {
        ExtendedValidate.notNull(builder, "No builder");
        return new Factory<T>() {
            @Override
            public T create() {
                return builder.build();
            }
        };
    }

    /**
     * @param builder The {@link ExtendedBuilder} instance to wrap as a {@link ExtendedFactory}
     * @return An {@link ExtendedFactory} that invokes the {@link Builder#build()} every
     * time its {@link Factory#create()} method is invoked
     */
    public static final <T> ExtendedFactory<T> asFactory(final ExtendedBuilder<T> builder) {
        ExtendedValidate.notNull(builder, "No builder");
        return new AbstractExtendedFactory<T>(builder.getEntityClass()) {
            @Override
            public T create() {
                return builder.build();
            }
        };
    }
    
    /**
     * @param ctor The {@link Constructor} to invoke
     * @return An {@link ExtendedFactory} that invokes the constructor
     * every time its {@link Factory#create()} method is invoked
     * @throws NullPointerException if no constructor provided
     * @see #instantiateFactory(Constructor, Object...)
     */
    public static final <T> ExtendedFactory<T> instantiateFactory(final Constructor<T> ctor) {
        return instantiateFactory(ctor, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /**
     * @param ctor The {@link Constructor} to invoke
     * @param args The constructor's arguments
     * @return An {@link ExtendedFactory} that invokes the constructor
     * every time its {@link Factory#create()} method is invoked
     * @throws NullPointerException if no constructor provided
     */
    public static final <T> ExtendedFactory<T> instantiateFactory(final Constructor<T> ctor, final Object ... args) {
        return new AbstractExtendedFactory<T>(ExtendedValidate.notNull(ctor, "No constructor").getDeclaringClass()) {
            @Override
            public T create() {
                return ExtendedConstructorUtils.newInstance(ctor, args);
            }
        };
    }
    
    /**
     * @param t A {@link Throwable} to throw
     * @return A {@link Factory} that throws the specified exception
     * every time its {@link Factory#create()} method is invoked
     * @throws NullPointerException if no exception to throw
     * @see ExtendedExceptionUtils#rethrowException(Throwable)
     */
    public static final <T> Factory<T> exceptionFactory(final Throwable t) {
        ExtendedValidate.notNull(t, "No throwable to throw");
        return new Factory<T>() {
            @Override
            public T create() {
                ExtendedExceptionUtils.rethrowException(t);
                throw new UnsupportedOperationException("create(" + t.getClass().getSimpleName() + ")[" + t.getMessage() + "] - unexpected call", t);
            }
        };
    }
}
