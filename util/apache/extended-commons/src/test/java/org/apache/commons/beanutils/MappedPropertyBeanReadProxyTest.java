/**
 * 
 */
package org.apache.commons.beanutils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.map.UnmodifiableSortedMap;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.ExtendedMethodUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MappedPropertyBeanReadProxyTest extends AbstractTestSupport {
    private static final SortedMap<String,Method> readersMap=
            UnmodifiableSortedMap.decorate(MappedPropertyBeanReadProxy.getProxyPropertyReaders(MappedTestInterface.class));

    public MappedPropertyBeanReadProxyTest() {
        super();
    }

    /**
     * Makes sure that if all the values are provided, then invoking the proxy
     * yields the expected values
     */
    @Test
    public void testFullValuesProxy() {
        assertProxiedValues(createValuesMap(readersMap.keySet()));
    }

    /**
     * Makes sure that if we have partial values mapping then invoking the
     * proxy succeeds on mapped ones and fails on the un-mapped
     */
    @Test
    public void testPartialValuesProxy() {
        Set<String>  allProps=readersMap.keySet();
        Set<String>  names=ExtendedCollectionUtils.selectToSortedSet(allProps, new Predicate<String>() {
                private final AtomicBoolean flag=new AtomicBoolean(true);

                @Override
                public boolean evaluate(String object) {
                    return flag.getAndSet(!flag.get()); // toggle on every evaluation
                }
            });
        assertFalse("No names selected", ExtendedCollectionUtils.isEmpty(names));
        assertTrue("All names selected", allProps.size() > names.size());
        
        Map<String,?>       valuesMap=createValuesMap(names);
        MappedTestInterface proxy=createPropertyBeanProxy(valuesMap);
        for (String propName : allProps) {
            boolean mappedValue=names.contains(propName);
            try {
                Object  actual=getValue(proxy, propName);
                if (mappedValue) {
                    assertSame(propName + ": mismatched value", valuesMap.get(propName), actual);
                } else {
                    fail(propName + ": unexpected value: " + actual);
                }
            } catch(NoSuchElementException e) {
                if (mappedValue) {
                    fail(propName + ": cannot retrieve mapped value: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Makes sure that <code>null</code> can be signaled by an {@link org.apache.commons.lang3.ObjectUtils.Null}
     * mapped value
     */
    @Test
    public void testNullValuesProxy() {
        Set<String>  allProps=readersMap.keySet();
        Map<String,Object>  valuesMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String n : allProps) {
            valuesMap.put(n, ObjectUtils.NULL);
        }
        MappedTestInterface proxy=createPropertyBeanProxy(valuesMap);
        for (String propName : allProps) {
            assertNull(propName + ": non-null value", getValue(proxy, propName));
        }
    }

    private MappedTestInterface assertProxiedValues(Map<String,?> valuesMap) {
        MappedTestInterface proxy=createPropertyBeanProxy(valuesMap);
        for (Map.Entry<String, ?> ve : valuesMap.entrySet()) {
            String  propName=ve.getKey();
            
            Object  expected=ve.getValue(), actual=getValue(proxy, propName);
            assertSame(propName + ": Mismatched values", expected, actual);
        }

        return proxy;
    }

    private static Object getValue(MappedTestInterface proxy, String propName) {
        Method  m=readersMap.get(propName);
        assertNotNull(propName + ": no getter", m);
        return ExtendedMethodUtils.invoke(m, proxy);
    }

    private MappedTestInterface createPropertyBeanProxy(Map<String,?> valuesMap) {
        return MappedPropertyBeanReadProxy.createPropertyBeanProxy(valuesMap, ExtendedClassUtils.getDefaultClassLoader(getClass()), MappedTestInterface.class);
    }

    private static SortedMap<String,Object> createValuesMap(Collection<String> propsNames) {
        SortedMap<String,Object>    valuesMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String n : propsNames) {
            valuesMap.put(n, createMappedValue(n));
        }
        
        return valuesMap;
    }
    
    private static Object createMappedValue(String n) {
        Method  m=readersMap.get(n);
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
        TimeUnit getUnit();
        String getString();
        Date getDate();
    }
}
