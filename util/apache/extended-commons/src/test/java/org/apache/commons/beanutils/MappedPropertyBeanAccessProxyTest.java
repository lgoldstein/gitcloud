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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.reflect.ExtendedMethodUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MappedPropertyBeanAccessProxyTest extends AbstractTestSupport {
    private static final Map<String,? extends Pair<Method,Method>>  methodsMap=ExtendedBeanUtils.describeBean(MappedTestInterface.class);

    public MappedPropertyBeanAccessProxyTest() {
        super();
    }

    /**
     * Makes sure that if all the values are provided, then invoking the proxy
     * yields the expected values
     */
    @Test
    public void testFullValuesProxy() {
        assertProxiedValues(createValuesMap(methodsMap.keySet()));
    }

    @Test
    public void testNullValuesHandling() {
        Collection<String>  names=methodsMap.keySet();
        MappedTestInterface proxy=createPropertyBeanProxy(createValuesMap(names));
        for (String propName : names) {
            assertNotNull(propName + ": no current value", getValue(proxy, propName));
            setValue(proxy, propName, null);
            assertNull(propName + ": not null-ified", getValue(proxy, propName));
        }
    }

    private MappedTestInterface assertProxiedValues(Map<String,Object> valuesMap) {
        MappedTestInterface proxy=createPropertyBeanProxy(valuesMap);
        for (Map.Entry<String, ?> ve : valuesMap.entrySet()) {
            String  propName=ve.getKey();
            
            Object  expected=ve.getValue(), actual=getValue(proxy, propName);
            assertEquals(propName + ": Mismatched read values", expected, actual);
        }

        Collection<String>  names=new TreeSet<>(valuesMap.keySet());
        for (String propName : names) {
            Object  expected=createMappedValue(propName), prev=valuesMap.get(propName);
            assertNotEquals(propName + ": No new value generated", expected, prev);
            setValue(proxy, propName, expected);
            
            Object  actual=getValue(proxy, propName);
            assertEquals(propName + ": Mismatched updated values (prev=" + prev + ")", expected, actual);
        }

        return proxy;
    }

    private static Object getValue(MappedTestInterface proxy, String propName) {
        Pair<Method,Method> p=methodsMap.get(propName);
        Method              m=p.getLeft();
        assertNotNull(propName + ": no getter", m);
        return ExtendedMethodUtils.invoke(m, proxy);
    }

    private static void setValue(MappedTestInterface proxy, String propName, Object value) {
        Pair<Method,Method> p=methodsMap.get(propName);
        Method              m=p.getRight();
        assertNotNull(propName + ": no setter", m);
        ExtendedMethodUtils.invoke(m, proxy, value);
    }

    private MappedTestInterface createPropertyBeanProxy(Map<String,Object> valuesMap) {
        return MappedPropertyBeanAccessProxy.createPropertyBeanAccessor(valuesMap, ExtendedClassUtils.getDefaultClassLoader(getClass()), MappedTestInterface.class);
    }

    private static SortedMap<String,Object> createValuesMap(Collection<String> propsNames) {
        SortedMap<String,Object>    valuesMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String n : propsNames) {
            valuesMap.put(n, createMappedValue(n));
        }
        
        return valuesMap;
    }
    
    private static Object createMappedValue(String n) {
        Pair<Method,Method> p=methodsMap.get(n);
        Method              m=p.getLeft();
        assertNotNull(n + ": No getter", m);
        return createMappedValue(n, m);
    }

    private static Object createMappedValue(String name, Method m) {
        return createMappedValue(name, m.getReturnType());
    }

    private static Object createMappedValue(String name, Class<?> pType) {
        return createTestPropertyValue(name, pType);
    }

    public static interface MappedTestInterface {
        Double getUnit();
        void setUnit(Double d);

        String getString();
        void setString(String s);

        Date getDate();
        void setDate(Date d);
    }
}
