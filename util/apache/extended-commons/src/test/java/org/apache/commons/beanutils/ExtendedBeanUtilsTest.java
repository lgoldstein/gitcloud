/**
 * 
 */
package org.apache.commons.beanutils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.FactoryUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.reflect.ProxyUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedBeanUtilsTest extends AbstractTestSupport {
    private static final SortedMap<String,Pair<Method,Method>>    FULL_MAP=introspectTestInterfaces();

    public ExtendedBeanUtilsTest() {
        super();
    }

    /**
     * Makes sure that {@link ExtendedBeanUtils#describeBean(java.beans.BeanInfo)}
     * handles an interface that has no other super-interfaces correctly
     * @throws IntrospectionException if failed to introspect the bean class
     */
    @Test
    public void testDescribeImmediateInterfaceInfo() throws IntrospectionException {
        Map<String,Pair<Method,Method>> expected=ExtendedBeanUtils.describeBean(Introspector.getBeanInfo(GetterInterface.class));
        assertFalse("No expected attributes retrieved", ExtendedMapUtils.isEmpty(expected));

        Map<String,Pair<Method,Method>> actual=ExtendedBeanUtils.describeBean(GetterInterface.class);
        assertBeanInfo(expected, actual, true, true);
    }

    /**
     * Makes sure that {@link ExtendedBeanUtils#describeBean(java.beans.BeanInfo)}
     * handles an interface that has other super-interfaces correctly
     */
    @Test
    public void testDescribeExtendingInterfaceInfo() {
        Map<String,Pair<Method,Method>> actual=ExtendedBeanUtils.describeBean(SetterInterface.class);
        assertBeanInfo(FULL_MAP, actual, true, true);
    }

    /**
     * Makes sure that {@link ExtendedBeanUtils#describeBean(java.beans.BeanInfo)}
     * handles an abstract class by checking the implemented interfaces
     */
    @Test
    public void testDescribeAbstractClass()  {
        Map<String,Pair<Method,Method>> actual=ExtendedBeanUtils.describeBean(AbstractImpl.class);
        /*
         * All non-interfaces declare "Object#getClass" as a "class" property
         */
        Pair<Method,Method> classProp=actual.remove("class");
        assertNotNull("No property descriptor for Object#getClass", classProp);
        assertNotNull("No method accessor for Object#getClass", classProp.getLeft());
        assertBeanInfo(FULL_MAP, actual, true, true);
    }

    @Test
    public void testDescribeNonPublicAccessors() {
        Map<String,Pair<Method,Method>> actual=ExtendedBeanUtils.describeBean(MixedAccessAccessors.class, false, true);
        /*
         * All non-interfaces declare "Object#getClass" as a "class" property
         */
        Pair<Method,Method> classProp=actual.remove("class");
        assertNotNull("No property descriptor for Object#getClass", classProp);
        assertNotNull("No method accessor for Object#getClass", classProp.getLeft());

        List<String>    accessTypes=Collections.unmodifiableList(Arrays.asList("public", "protected", "private", "package"));
        assertEquals("Mismatched attributes count", accessTypes.size(), actual.size());
        
        for (String access : accessTypes) {
            String              name=access + "Getter";
            Pair<Method,Method> accPair=actual.remove(name);
            assertNotNull(name + ": No accessors pair", accPair);
            
            Method  getter=accPair.getLeft(), setter=accPair.getRight();
            assertNotNull(name + ": no getter", getter);
            assertTrue(name + ": getter not accessible", getter.isAccessible());
            assertTrue(name + ": not recognized as getter", ExtendedBeanUtils.isGetter(getter));
            assertFalse(name + ": recognized as setter", ExtendedBeanUtils.isSetter(getter));

            assertNotNull(name + ": no setter", setter);
            assertTrue(name + ": setter not accessible", setter.isAccessible());
            assertTrue(name + ": not recognized as setter", ExtendedBeanUtils.isSetter(setter));
            assertFalse(name + ": recognized as getter", ExtendedBeanUtils.isGetter(setter));
        }
    }

    @Test
    public void testDescribeCoVariantAccessors() {
        Map<String,Pair<Method,Method>> actual=ExtendedBeanUtils.describeBean(DerivedMixedAccessAccessors.class, false, true);
        Pair<Method,Method>             pair=actual.get("packageGetter");
        assertNotNull("No mapping found", pair);
        
        Method  getter=pair.getLeft();
        assertNotNull("No getter found", getter);
        assertInstanceof("Mismatched getter type", CharSequence.class, getter.getReturnType());
    }

    @Test
    public void testDescribeProxiedDirectInterfaces() {
        Map<String,Pair<Method,Method>> expected=ExtendedBeanUtils.describeBean(GetterInterface.class);
        GetterInterface                 getter=
                ProxyUtils.newProxyInstance(GetterInterface.class, getDefaultClassLoader(), Mockito.mock(InvocationHandler.class), GetterInterface.class);
        Map<String,Pair<Method,Method>> actual=ExtendedBeanUtils.describeBean(getter.getClass());
        assertBeanInfo(expected, actual, true, true);
    }

    @Test
    public void testDescribeProxiedExtensionInterfaces() {
        SetterInterface                 setter=
                ProxyUtils.newProxyInstance(SetterInterface.class, getDefaultClassLoader(), Mockito.mock(InvocationHandler.class), SetterInterface.class);
        Map<String,Pair<Method,Method>> actual=ExtendedBeanUtils.describeBean(setter.getClass());
        assertBeanInfo(FULL_MAP, actual, true, true);
    }

    @Test
    public void testPropertiesToBeanTransformer() {
        DummyBean expected=new DummyBean();
        expected.setStringValue("testPropertiesToBeanTransformer");
        expected.setLongValue(System.nanoTime());
        expected.setDateValue(new Date(System.currentTimeMillis()));

        Map<String,Pair<Method,Method>>                 attrsMap=ExtendedBeanUtils.describeBean(DummyBean.class);
        Factory<DummyBean>                              beanFactory=FactoryUtils.instantiateFactory(DummyBean.class);
        Transformer<DummyBean,SortedMap<String,Object>> beanXformer=ExtendedBeanUtils.beanToPropertiesTransformer(DummyBean.class, attrsMap);
        Map<String,Object>                              valuesMap=beanXformer.transform(expected);
        Transformer<Map<String,?>,DummyBean>            attrsXformer=ExtendedBeanUtils.propertiesToBeanTransformer(beanFactory, attrsMap);
        DummyBean                                       actual=attrsXformer.transform(valuesMap);
        assertEquals("Mismatched pure values map re-constructed bean", expected, actual);
        
        // make sure that any properties that do not refer to existing attributes are ignored
        for (int index=0; index < Byte.SIZE; index++) {
            valuesMap.put("dummyProp#" + index, Integer.valueOf(index));
        }

        actual = attrsXformer.transform(valuesMap);
        assertEquals("Mismatched dirty values map re-constructed bean", expected, actual);
    }

    private static SortedMap<String,Pair<Method,Method>> introspectTestInterfaces() {
        try {
            Map<String,Pair<Method,Method>> gettersMap=ExtendedBeanUtils.describeBean(Introspector.getBeanInfo(GetterInterface.class));
            assertFalse("No getters retrieved", ExtendedMapUtils.isEmpty(gettersMap));
    
            Map<String,Pair<Method,Method>> settersMap=ExtendedBeanUtils.describeBean(Introspector.getBeanInfo(SetterInterface.class));
            assertEquals("Mismatched merged maps size", ExtendedMapUtils.size(gettersMap), ExtendedMapUtils.size(settersMap));
    
            SortedMap<String,Pair<Method,Method>> expected=new TreeMap<String,Pair<Method,Method>>(String.CASE_INSENSITIVE_ORDER);
            for (Map.Entry<String,Pair<Method,Method>> ge : gettersMap.entrySet()) {
                String              name=ge.getKey();
                Pair<Method,Method> gPair=ge.getValue(), sPair=settersMap.get(name);
                assertNotNull(name + ": no setter pair found", sPair);
                assertNotNull(name + ": missing getter", gPair.getLeft());
                assertNull(name + ": unexpected setter method", gPair.getRight());
                assertNull(name + ": unexpected getter method", sPair.getLeft());
                assertNotNull(name + ": missing setter", sPair.getRight());
                
                Pair<Method,Method> prev=expected.put(name, Pair.of(gPair.getLeft(), sPair.getRight()));
                assertNull(name + ": multiple mappings", prev);
            }
            
            return expected;
        } catch(IntrospectionException e) {
            throw new IllegalStateException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private static void assertBeanInfo (Map<String,Pair<Method,Method>> expected, Map<String,Pair<Method,Method>> actual, boolean checkGetters, boolean checkSetters) {
        assertTrue("Must compare either getters or setters or both", checkGetters || checkSetters);
        assertEquals("Mismatched info maps size", ExtendedMapUtils.size(expected), ExtendedMapUtils.size(actual));
        
        for (Map.Entry<String,Pair<Method,Method>> ee : expected.entrySet()) {
            String              name=ee.getKey();
            Pair<Method,Method> expPair=ee.getValue(), actPair=actual.get(name);
            assertNotNull(name + ": No mapping", actPair);
            
            if (checkGetters) {
                assertEquals(name + ": mismatched getters", expPair.getLeft(), actPair.getLeft());
            }

            if (checkSetters) {
                assertEquals(name + ": mismatched setters", expPair.getRight(), actPair.getRight());
            }
        }
    }

    public static interface GetterInterface {
        static final String ATTR_NAME="value";

        String getValue();
    }
    
    public static interface SetterInterface extends GetterInterface {
        void setValue(String v);
    }
    
    public static abstract class AbstractImpl implements SetterInterface {
        protected AbstractImpl() {
            super();
        }
        
        @Override
        public String getValue() {
            return getClass().getSimpleName();
        }
    }
    
    public static class MixedAccessAccessors {
        public boolean isPublicGetter() { return getPackageGetter() != null; }
        void setPublicGetter(boolean v) { setPackageGetter(String.valueOf(v)); }
        
        CharSequence getPackageGetter() { return "aaa"; }
        private void setPackageGetter(CharSequence v) { if (v ==  null) return; }
        
        protected long getProtectedGetter() { return isPrivateGetter() ? System.currentTimeMillis() : 0L; }
        void setProtectedGetter(long v) { setPublicGetter(v > 0L); }
        
        private boolean isPrivateGetter() { return true; }
        public void setPrivateGetter(boolean v) { setProtectedGetter(v ? 1L : 0L); }
    }
    
    public static class DerivedMixedAccessAccessors extends MixedAccessAccessors {
        @Override
        public String getPackageGetter() {
            return String.valueOf(super.getPackageGetter());
        }
    }
    
    public static class DummyBean extends AbstractSimpleJavaBean {
        private String  stringValue;
        private long longValue;
        private Date dateValue;

        public DummyBean() {
            super();
        }

        public String getStringValue() {
            return stringValue;
        }
        public void setStringValue(String v) {
            this.stringValue = v;
        }
        public Date getDateValue() {
            return dateValue;
        }
        public void setDateValue(Date v) {
            this.dateValue = v;
        }
        public long getLongValue() {
            return longValue;
        }
        public void setLongValue(long v) {
            this.longValue = v;
        }
    }
}