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

package org.apache.commons.lang3.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedClassUtils;

/**
 * @author Lyor G.
 */
public final class ProxyUtils {
    private ProxyUtils() {
        throw new UnsupportedOperationException("No instance");
    }
    
    /**
     * Convenience <I>varargs</I> method for {@link Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}
     * using the {@link ExtendedClassUtils#getDefaultClassLoader()} result as the proxy loader
     * @param resultType The proxy result {@link Class} to be cast to
     * @param h The target {@link InvocationHandler}
     * @param interfaces The interface classes being proxy-ied
     * @return The create proxy
     * @see #newProxyInstance(Class, ClassLoader, InvocationHandler, Class...)
     */
    public static final <T> T newProxyInstance(Class<T> resultType,InvocationHandler h, Class<?> ... interfaces) {
        return newProxyInstance(resultType, ExtendedClassUtils.getDefaultClassLoader(h.getClass()), h, interfaces);
    }

    /**
     * Convenience <I>varargs</I> method for {@link Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}
     * @param resultType The proxy result {@link Class} to be cast to
     * @param loader The {@link ClassLoader} to use
     * @param h The target {@link InvocationHandler}
     * @param interfaces The interface classes being proxy-ied
     * @return The create proxy
     * @see Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)
     */
    public static final <T> T newProxyInstance(Class<T> resultType, ClassLoader loader, InvocationHandler h, Class<?> ... interfaces) {
        if (ExtendedArrayUtils.length(interfaces) <= 0) {
            throw new IllegalArgumentException("No interfaces specified");
        }

        for (Class<?> i : interfaces) {
            if (i == null) {
                throw new IllegalArgumentException("Blank spot in " + Arrays.toString(interfaces));
            }
            if (!i.isInterface()) {
                throw new IllegalArgumentException("Not an interface: " + i.getSimpleName());
            }
        }

        return resultType.cast(Proxy.newProxyInstance(loader, interfaces, h));
    }

    /**
     * An {@link InvocationHandler} that throws {@link UnsupportedOperationException}
     * on any attempt to call its <code>invoke</code> method
     */
    public static final InvocationHandler   UNIMPLEMENTED_HANDLER=new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                Class<?>    clazz=method.getDeclaringClass();
                throw new UnsupportedOperationException(clazz.getName() + "#" + method.getName() + "() - not implementd");
            }
        };
}
