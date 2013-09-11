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

package org.springframework.validation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.reflect.ExtendedMethodUtils;
import org.apache.commons.lang3.reflect.ProxyUtils;
import org.springframework.util.Assert;

/**
 * @author Lyor G.
 */
public abstract class ExtendedValidationUtils extends ValidationUtils {
    public static final Predicate<Method> SUPPORTS_METHOD_SIGNATURE=
            ExtendedMethodUtils.matchingSignaturePredicate("supports", Boolean.TYPE, Class.class);
    public static final Predicate<Method> VALIDATE_METHOD_SIGNATURE=
            ExtendedMethodUtils.matchingSignaturePredicate("validate", Void.TYPE, Object.class, Errors.class);

    protected ExtendedValidationUtils() {
        super();
    }

    /**
     * @param validator The {@link TypedValidator} 
     * @return A proxy {@link Validator} that delegates the calls to the typed one
     * @see #toValidator(TypedValidator, ClassLoader)
     */
    public static final Validator toValidator(TypedValidator<?> validator) {
        Assert.notNull(validator, "No validator instance");
        return toValidator(validator, ExtendedClassUtils.getDefaultClassLoader(validator.getClass()));
    }

    /**
     * @param validator The {@link TypedValidator} 
     * @param cl The {@link ClassLoader} to use for the proxy
     * @return A proxy {@link Validator} that delegates the calls to the typed one
     */
    public static final <E> Validator toValidator(final TypedValidator<E> validator, ClassLoader cl) {
        Assert.notNull(validator, "No validator instance");
        Assert.notNull(cl, "No class loader");

        return ProxyUtils.newProxyInstance(Validator.class, cl,
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                if (SUPPORTS_METHOD_SIGNATURE.evaluate(method)) {
                                    Class<E>    expected=validator.getEntityClass();
                                    Class<?>    actual=(Class<?>) args[0];
                                    return Boolean.valueOf(expected.isAssignableFrom(actual));
                                } else if (VALIDATE_METHOD_SIGNATURE.evaluate(method)) {
                                    Class<E>    expected=validator.getEntityClass();
                                    validator.validate(expected.cast(args[0]), (Errors) args[1]);
                                    return null;
                                } else {
                                    return method.invoke(validator, args);
                                }
                            }
                        },
                        Validator.class);
    }
}
