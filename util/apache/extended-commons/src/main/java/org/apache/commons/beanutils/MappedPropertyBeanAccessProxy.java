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

package org.apache.commons.beanutils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.collections15.keyvalue.KeyedAccessor;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ProxyUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Provides an {@link InvocationHandler} whose {@link #invoke(Object, Method, Object[])}
 * interprets getter/setter access as manipulating an underlying {@link KeyedAccessor}
 * whose keys are the accessed methods names
 * @author Lyor G.
 */
public class MappedPropertyBeanAccessProxy extends MappedPropertyBeanReadProxy {
    private final KeyedAccessor<String,Object>   accessor;

    public MappedPropertyBeanAccessProxy(KeyedAccessor<String,Object> acc) {
        super(acc);
        
        if ((this.accessor=acc) == null) {
            throw new IllegalStateException("No accessor");
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (ExtendedArrayUtils.length(args) == 1) { // assume any 1 argument invocation is a setter
            return accessor.put(method.getName(), args[0]);
        } else {    // otherwise assume it is a getter
            return super.invoke(proxy, method, args);
        }
    }

    /**
     * @param valuesMap The backing {@link Map} of values - key=the &quot;pure&quot;
     * attribute name
     * @param proxyType The result proxy type
     * @return A proxy instance backed by the values
     * @see #createPropertyBeanAccessor(Map, ClassLoader, Class)
     */
    public static final <T> T createPropertyBeanAccessor(Map<String,Object> valuesMap, Class<T> proxyType) {
        return createPropertyBeanAccessor(valuesMap, ExtendedClassUtils.getDefaultClassLoader(proxyType), proxyType);
    }

    /**
     * @param valuesMap The backing {@link Map} of values - key=the &quot;pure&quot;
     * attribute name
     * @param cl The {@link ClassLoader} to use to generate the proxy
     * @param proxyType The result proxy type
     * @return A proxy instance backed by the values
     * @see #createPropertyBeanAccessor(KeyedAccessor, ClassLoader, Class)
     */
    public static final <T> T createPropertyBeanAccessor(Map<String,Object> valuesMap, ClassLoader cl, Class<T> proxyType) {
        return createPropertyBeanAccessor(createMethodsAccessor(valuesMap, proxyType), cl, proxyType);
    }

    /**
     * @param accessor The {@link KeyedAccessor} to use to access the mapped values.
     * <B>Note:</B> the key must match the getter/setter method name (i.e.,
     * <code>getX, setX</code> and not just <code>X</code>)
     * @param proxyType The result proxy type
     * @return A proxy instance backed by the values
     * @see #createPropertyBeanAccessor(KeyedAccessor, ClassLoader, Class)
     */
    public static final <T> T createPropertyBeanAccessor(KeyedAccessor<String,Object> accessor, Class<T> proxyType) {
        return createPropertyBeanAccessor(accessor, ExtendedClassUtils.getDefaultClassLoader(proxyType), proxyType);
    }

    /**
     * @param accessor The {@link KeyedAccessor} to use to access the mapped values.
     * <B>Note:</B> the key must match the getter/setter method name (i.e.,
     * <code>getX, setX</code> and not just <code>X</code>)
     * @param cl The {@link ClassLoader} to use to generate the proxy
     * @param proxyType The result proxy type
     * @return A proxy instance backed by the values
     * @see ProxyUtils#newProxyInstance(Class, ClassLoader, InvocationHandler, Class...)
     */
    public static final <T> T createPropertyBeanAccessor(KeyedAccessor<String,Object> accessor, ClassLoader cl, Class<T> proxyType) {
        return ProxyUtils.newProxyInstance(proxyType, cl, new MappedPropertyBeanAccessProxy(accessor), proxyType);
    }

    public static final KeyedAccessor<String,Object> createMethodsAccessor(Map<String,Object> valuesMap, Class<?> proxyType) {
        return createMethodsAccessor(valuesMap, ExtendedBeanUtils.describeBean(proxyType));
    }

    public static final KeyedAccessor<String,Object> createMethodsAccessor(KeyedAccessor<String,Object> accessor, Class<?> proxyType) {
        return createMethodsAccessor(accessor, calculateRoutingMap(proxyType));
    }

    public static final KeyedAccessor<String,Object> createMethodsAccessor(Map<String,Object> valuesMap, Map<String,? extends Pair<Method,Method>> methodsMap) {
        return createMethodsAccessor(createValuesAccessor(valuesMap), calculateRoutingMap(methodsMap));
    }

    /**
     * @param valuesMap A {@link Map} of values where key=the <U>pure</U>
     * attribute name, value=its assigned value
     * @return A {@link KeyedAccessor} that interprets <code>null</code>
     * values as follows:</BR>
     * <UL>
     *      <LI>
     *      If the wrapped {@link Map#get(Object)} returns <code>null</code>
     *      then it substitutes {@link ObjectUtils#NULL}, otherwise returns
     *      the original value.
     *      </LI>
     *      
     *      </LI>
     *      If the {@link Map#put(Object, Object)} is about to be mapped with
     *      a <code>null</code> or {@link org.apache.commons.lang3.ObjectUtils.Null} value, then the key
     *      is <U>removed</U> from the map. Otherwise the original value is
     *      mapped.
     * </UL>
     */
    public static final KeyedAccessor<String,Object> createValuesAccessor(final Map<String,Object> valuesMap) {
        return new KeyedAccessor<String,Object>() {
            @Override
            public Object put(String key, Object value) {
                if ((value == null) || (value instanceof ObjectUtils.Null)) {
                    return valuesMap.remove(key);
                } else {
                    return valuesMap.put(key, value);
                }
            }

            @Override
            public Object get(String key) {
                Object  value=valuesMap.get(key);
                if (value == null) {
                    return ObjectUtils.NULL;
                } else {
                    return value;
                }
            }
        };        
    }

    /**
     * @param valuesAccessor The {@link KeyedAccessor} used to read/write the
     * backing values. <B>Note:</B> the accessor must use {@link org.apache.commons.lang3.ObjectUtils.Null}
     * values to represent <code>null</code>s
     * @param routingMap A {@link Map} whose key=the method name, value=the property
     * it accesses.
     * @return The {@link KeyedAccessor} to be used for the {@link MappedPropertyBeanAccessProxy#MappedPropertyBeanAccessProxy(KeyedAccessor)}
     * constructor
     * @see #calculateRoutingMap(Class)
     */
    public static final KeyedAccessor<String,Object> createMethodsAccessor(final KeyedAccessor<String,Object> valuesAccessor, final Map<String,String> routingMap) {
        if (ExtendedMapUtils.isEmpty(routingMap)) {
            return null;
        }
        
        return new KeyedAccessor<String,Object>() {
            @Override
            public Object put(String key, Object value) {
                String  propName=routingMap.get(key);
                if (StringUtils.isEmpty(propName)) {
                    throw new NoSuchElementException("Unknown property mapping for put(" + key + ")[" + value + "]");
                }
                
                if (value == null) {
                    return valuesAccessor.put(propName, ObjectUtils.NULL);
                } else {
                    return valuesAccessor.put(propName, value);
                }
            }

            @Override
            public Object get(String key) {
                String  propName=routingMap.get(key);
                if (StringUtils.isEmpty(propName)) {
                    throw new NoSuchElementException("Unknown property mapping for get(" + key + ")");
                }
                return valuesAccessor.get(propName);
            }
        };
    }
    
    /**
     * @param proxyType The proxy target {@link Class}
     * @return A {@link SortedMap} whose key=the method name, value=the property
     * it accesses.
     * @see #calculateRoutingMap(Map)
     */
    public static final SortedMap<String,String> calculateRoutingMap(Class<?> proxyType) {
        if (proxyType == null) {
            return ExtendedMapUtils.emptySortedMap();
        }
        
        if (!proxyType.isInterface()) {
            throw new IllegalArgumentException("Proxy is not an interface: " + proxyType.getSimpleName());
        }

        return calculateRoutingMap(ExtendedBeanUtils.describeBean(proxyType));
    }
    /**
     * @param methodsMap A {@link Map} of the bean properties and its getter/setter
     * methods where key=the property name, value=the getter/setter {@link Method}s
     * represented as a {@link Pair} whose left side is the getter and right side
     * the setter (either of which may be <code>null</code>
     * @return A {@link SortedMap} whose key=the method name, value=the property
     * it accesses.
     */
    public static final SortedMap<String,String> calculateRoutingMap(Map<String,? extends Pair<Method,Method>> methodsMap) {
        if (ExtendedMapUtils.isEmpty(methodsMap)) {
            return ExtendedMapUtils.emptySortedMap();
        }

        final SortedMap<String,String>    routingMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String,? extends Pair<Method,Method>> pe : methodsMap.entrySet()) {
            String              propName=pe.getKey();
            Pair<Method,Method> p=pe.getValue();
            Method              getter=ExtendedBeanUtils.GETTER_VALUE.transform(p);
            Method              setter=ExtendedBeanUtils.SETTER_VALUE.transform(p);
            if (getter != null) {
                Object  prev=routingMap.put(getter.getName(), propName);
                if (prev != null) {
                    throw new IllegalStateException("Multiple getters for " + propName);
                }
            }
            
            if (setter != null) {
                Object  prev=routingMap.put(setter.getName(), propName);
                if (prev != null) {
                    throw new IllegalStateException("Multiple setters for " + propName);
                }
            }
        }
        
        return routingMap;
    }
}
