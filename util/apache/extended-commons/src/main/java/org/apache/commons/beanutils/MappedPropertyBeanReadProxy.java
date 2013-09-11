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

package org.apache.commons.beanutils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.collections15.keyvalue.KeyedAccessUtils;
import org.apache.commons.collections15.keyvalue.KeyedReader;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.ProxyUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Provides an {@link InvocationHandler} whose {@link #invoke(Object, Method, Object[])}
 * interprets the accessed {@link Method} as a bean <I>getter</I> whose
 * result should be returned from an associated {@link KeyedReader} whose
 * keys are the accessed methods names
 * @author Lyor G.
 */
public class MappedPropertyBeanReadProxy implements InvocationHandler {
    private final KeyedReader<String,?> valuesMap;
    /**
     * @param methodValuesMap A {@link Map} where key=the invoked {@link Method}
     * name, value=the {@link Object} to return. <B>Note:</B> use the {@link ObjectUtils#NULL}
     * constant (or any other {@link org.apache.commons.lang3.ObjectUtils.Null}) value to indicate
     * values for which <code>null</code> should be returned
     */
    public MappedPropertyBeanReadProxy(Map<String,?> methodValuesMap) {
        this(KeyedAccessUtils.keyedAccessor(methodValuesMap));
    }
    
    public MappedPropertyBeanReadProxy(KeyedReader<String,?> reader) {
        if ((valuesMap=reader) == null) {
            throw new IllegalStateException("No values reader provided");
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        String  name=method.getName();
        if (ExtendedArrayUtils.length(args) > 0) {
            throw new NoSuchMethodException("The invoked method is not a getter: " + name);
        }

        Object  value=valuesMap.get(name);
        if (value == null) {
            throw new NoSuchElementException("No value found for " + name);
        }
        
        if (value instanceof ObjectUtils.Null) {
            return null;
        } else {
            return value;
        }
    }

    /**
     * @param valuesMap The {@link Map} of values assigned to the properties
     * where key=the property name (case <U>insensitive</U>), value=the
     * assigned value. <B>Note:</B> use the {@link ObjectUtils#NULL}
     * constant (or any other {@link org.apache.commons.lang3.ObjectUtils.Null}) value to indicate
     * values for which <code>null</code> should be returned
     * @param proxyType The result proxy type
     * @return A proxy instance backed by the values
     * @see #createPropertyBeanProxy(Map, ClassLoader, Class)
     */
    public static final <T> T createPropertyBeanProxy(Map<String,?> valuesMap, Class<T> proxyType) {
        return createPropertyBeanProxy(valuesMap, ExtendedClassUtils.getDefaultClassLoader(proxyType), proxyType);
    }

    /**
     * @param valuesMap The {@link Map} of values assigned to the properties
     * where key=the property name (case <U>insensitive</U>), value=the
     * assigned value. <B>Note:</B> use the {@link ObjectUtils#NULL}
     * constant (or any other {@link org.apache.commons.lang3.ObjectUtils.Null}) value to indicate
     * values for which <code>null</code> should be returned
     * @param cl The {@link ClassLoader} to use to generate the proxy
     * @param proxyType The result proxy type
     * @return A proxy instance backed by the values
     * @see #getProxyPropertyReaders(Class)
     * @see #createPropertyBeanProxy(Map, Map, ClassLoader, Class)
     */
    public static final <T> T createPropertyBeanProxy(Map<String,?> valuesMap, ClassLoader cl, Class<T> proxyType) {
        return createPropertyBeanProxy(getProxyPropertyReaders(proxyType), valuesMap, cl, proxyType);
    }

    /**
     * @param propsMap The properties {@link Map} where key=property name
     * (case <U>insensitive</U>), value=the {@link Method} to be invoked to
     * retrieve the specified property
     * @param valuesMap The {@link Map} of values assigned to the properties
     * where key=the property name (case <U>insensitive</U>), value=the
     * assigned value. <B>Note:</B> use the {@link ObjectUtils#NULL}
     * constant (or any other {@link org.apache.commons.lang3.ObjectUtils.Null}) value to indicate
     * values for which <code>null</code> should be returned
     * @param proxyType The result proxy type
     * @return A proxy instance backed by the values
     * @see #createPropertyBeanProxy(Map, ClassLoader, Class)
     */
    public static final <T> T createPropertyBeanProxy(Map<String,Method> propsMap, Map<String,?> valuesMap, Class<T> proxyType) {
        return createPropertyBeanProxy(propsMap, valuesMap, ExtendedClassUtils.getDefaultClassLoader(proxyType), proxyType);
    }

    /**
     * @param propsMap The properties {@link Map} where key=property name
     * (case <U>insensitive</U>), value=the {@link Method} to be invoked to
     * retrieve the specified property
     * @param valuesMap The {@link Map} of values assigned to the properties
     * where key=the property name (case <U>insensitive</U>), value=the
     * assigned value. <B>Note:</B> use the {@link ObjectUtils#NULL}
     * constant (or any other {@link org.apache.commons.lang3.ObjectUtils.Null}) value to indicate
     * values for which <code>null</code> should be returned
     * @param cl The {@link ClassLoader} to use to generate the proxy
     * @param proxyType The result proxy type
     * @return A proxy instance backed by the values
     * @see #calculateMethodValuesMap(Map, Map)
     * @see #createPropertyBeanProxy(KeyedReader, ClassLoader, Class) 
     */
    public static final <T> T createPropertyBeanProxy(Map<String,Method> propsMap, Map<String,?> valuesMap, ClassLoader cl, Class<T> proxyType) {
        Map<String,Object>  methodValuesMap=calculateMethodValuesMap(propsMap, valuesMap);
        return createPropertyBeanProxy(KeyedAccessUtils.keyedAccessor(methodValuesMap), cl, proxyType);
    }

    /**
     * @param reader The {@link KeyedReader} to use to access the mapped values.
     * <B>Note:</B> the key must match the getter method name (i.e., <code>getX</code>
     * and not just <code>X</code>)
     * @param proxyType The result proxy type
     * @return A proxy instance backed by the values
     * @see #createPropertyBeanProxy(KeyedReader, ClassLoader, Class) 
     */
    public static final <T> T createPropertyBeanProxy(KeyedReader<String,?> reader,Class<T> proxyType) {
        return createPropertyBeanProxy(reader, ExtendedClassUtils.getDefaultClassLoader(proxyType), proxyType);
    }

    /**
     * @param reader The {@link KeyedReader} to use to access the mapped values.
     * <B>Note:</B> the key must match the getter method name (i.e., <code>getX</code>
     * and not just <code>X</code>)
     * @param cl The {@link ClassLoader} to use to generate the proxy
     * @param proxyType The result proxy type
     * @return A proxy instance backed by the values
     * @see ProxyUtils#newProxyInstance(Class, ClassLoader, InvocationHandler, Class...)
     */
    public static final <T> T createPropertyBeanProxy(KeyedReader<String,?> reader, ClassLoader cl, Class<T> proxyType) {
        return ProxyUtils.newProxyInstance(proxyType, cl, new MappedPropertyBeanReadProxy(reader), proxyType);
    }
    /**
     * @param propsMap The {@link Map} of properties where key=property name
     * (case <U>insensitive</U>), value=the {@link Method} to be invoked to
     * retrieve the specified property
     * @param valuesMap The {@link Map} of values assigned to the properties
     * where key=the property name (case <U>insensitive</U>), value=the
     * assigned value. <B>Note:</B> use the {@link ObjectUtils#NULL}
     * constant (or any other {@link org.apache.commons.lang3.ObjectUtils.Null}) value to indicate
     * values for which <code>null</code> should be returned
     * @return A {@link SortedMap} of the values to be returned for each
     * property getter method where key=the method name, value=the assigned
     * value for it. <B>Note:</B> if a getter method represents a property
     * for which there is no value in the value map, then it is <U>skipped</U>.
     * This means that if that specific method is ever invoked, an exception
     * will be thrown
     * @see #invoke(Object, Method, Object[])
     */
    public static final SortedMap<String,Object> calculateMethodValuesMap(Map<String,Method> propsMap, Map<String,?> valuesMap) {
        if (ExtendedMapUtils.isEmpty(propsMap) || ExtendedMapUtils.isEmpty(valuesMap)) {
            return ExtendedMapUtils.emptySortedMap();
        }
        
        SortedMap<String,Object>    methodValuesMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, Method> pe : propsMap.entrySet()) {
            String  name=pe.getKey();
            Object  value=valuesMap.get(name);
            if (value == null) {
                continue;
            }
            
            Method  m=pe.getValue();
            Object  prev=methodValuesMap.put(m.getName(), value);
            if (prev != null) {
                throw new IllegalStateException("Multiple mapped values for method=" + m.getName() + " (possible case issues)");
            }
        }
        
        return methodValuesMap;
    }

    /**
     * @param proxyType The type of <U>interface</U> to be proxied - ignored if <code>null</code>
     * @return A {@link SortedMap} of the property getters where key=the
     * pure property name (case insensitive), value=the getter {@link Method}
     * that can be used to retrieve the specified property
     * @throws IllegalArgumentException if non-interface to proxy
     * @throws IllegalStateException if several getters with same name but
     * different case found
     */
    public static final SortedMap<String,Method> getProxyPropertyReaders(Class<?> proxyType) {
        if (proxyType == null) {
            return ExtendedMapUtils.emptySortedMap();
        }
        
        /*
         * Not really necessary since any attempt to proxy it will throw an
         * exception, but we want to emphasize that we only allow this type
         * of mapped property bean proxy on interfaces
         */
        if (!proxyType.isInterface()) {
            throw new IllegalArgumentException("Non-interface to proxy: " + proxyType.getSimpleName());
        }

        Map<String,? extends Pair<Method,Method>>   attrsMap=ExtendedBeanUtils.describeBean(proxyType);
        SortedMap<String,Method>                    methodsMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String,? extends Pair<Method,Method>> ae : attrsMap.entrySet()) {
            String              name=ae.getKey();
            Pair<Method,Method> p=ae.getValue();
            Method  getter=ExtendedBeanUtils.GETTER_VALUE.transform(p);
            if (getter == null) {
                continue;   // ignore non-readable attributes
            }
            
            Method  prev=methodsMap.put(name, getter);
            if (prev != null) {
                throw new IllegalStateException("Multiple properties named " + name + " (probably a case issue)");
            }
        }
        
        return methodsMap;
    }
}
