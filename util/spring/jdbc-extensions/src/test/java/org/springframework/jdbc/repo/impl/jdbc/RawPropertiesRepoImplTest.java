/* Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.jdbc.repo.impl.jdbc;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.commons.beanutils.AbstractSimpleJavaBean;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.reflect.ProxyUtils;
import org.apache.commons.lang3.time.Period;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.AbstractExtendedConverter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.ExtendedConverter;
import org.springframework.core.convert.converter.ExtendedConverterRegistryUtils;
import org.springframework.jdbc.repo.MutableIdentity;
import org.springframework.jdbc.repo.RawPropertiesRepo;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.ExtendedAbstractJUnit4SpringContextTests;
import org.springframework.validation.Validator;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 12:36:43 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(locations={ "classpath:org/springframework/jdbc/repo/impl/jdbc/RawPropertiesRepoImplTest.xml" })
public class RawPropertiesRepoImplTest extends ExtendedAbstractJUnit4SpringContextTests {
    @Inject private DataSource    dataSource;
    @Inject private ConversionService conversionService;

    protected static final List<TimeUnit>    units=Collections.unmodifiableList(Arrays.asList(TimeUnit.values()));
    protected static final List<Class<?>> classes=Collections.unmodifiableList(
            Arrays.asList(Date.class, Calendar.class, Period.class, Validator.class));

    private TestEntryRepo   repo;
    public static final String PROP_NAME = "randomValue";

    public RawPropertiesRepoImplTest() {
        super();
    }

    @Before
    public void setUp() {
        if (repo != null) {
            repo.removeAll();
        }
        repo = new TestEntryRepo(dataSource, conversionService);
    }

    @After
    public void tearDown() {
        repo.removeAll();
    }

    @Test
    public void testListEntities() {
        Set<String> expected=new TreeSet<String>();
        for (int index=0; index < Byte.SIZE; index++) {
            TestEntry e=new TestEntry();
            repo.setProperties(e, new Transformer<TestEntry, Map<String,?>>() {
                @Override
                public Map<String,?> transform(TestEntry input) {
                    return Collections.<String,Serializable>singletonMap(PROP_NAME, Double.valueOf(Math.random()));
                }
            });
            expected.add(e.getId());
        }
        
        List<String>   idsList=repo.listEntities();
        assertEquals("Mismatched entities list count", expected.size(), ExtendedCollectionUtils.size(idsList));
        
        Set<String>     actual=new TreeSet<>(idsList);
        assertEquals("Mismatched unique entities count", idsList.size(), actual.size());
        
        if (!CollectionUtils.isEqualCollection(expected, actual)) {
            fail("Mismatched ID set: expected=" + expected + ", actual=" + actual);
        }
    }

    /**
     * Makes sure that {@link RawPropertiesRepo#entityExists(String)}
     * and/or {@link RawPropertiesRepo#getProperties(String)} return
     * correct result for non-existing entities
     */
    @Test
    public void testGetNonExistingEntityProperties() {
        for (int index=0; index < Byte.SIZE; index++) {
            String      id=String.valueOf(Math.random());
            assertFalse("Unexpected existence for ID=" + id, repo.entityExists(id));

            Map<?,?>    props=repo.getProperties(id);
            if (ExtendedMapUtils.size(props) > 0) {
                fail("Unexpected proeprties for ID=" + id + ": " + props);
            }
        }
    }

    /**
     * Makes sure that {@link RawPropertiesRepo#entityExists(String)}
     * behaves as expected on entity properties removal and also that
     * {@link RawPropertiesRepo#removeProperties(String)} returns the
     * original properties for the removed entity
     */
    @Test
    public void testRemoveExistingEntityProperties() {
        final String                ID=getClass().getSimpleName() + "#testRemoveEntityProperties";
        Map<String,Serializable>    expected=assertSimplePropertiesUpdate(ID, createEntityProperties(ID));
        assertTrue("Candidate entity not found", repo.entityExists(ID));

        Map<String,Serializable>    actual=repo.removeProperties(ID);
        assertFalse("Removed entity still reported as existing", repo.entityExists(ID));
        if (!ExtendedMapUtils.compareMaps(expected, actual)) {
            fail("Mismatched properties: expected=" + expected + ", actual=" + actual);
        }
    }

    /**
     * Makes sure that {@link RawPropertiesRepo#removeProperties(String)}
     * returns no properties for a non-existing entity
     */
    @Test
    public void testRemoveNonExistingEntityProperties() {
        final String ID=getClass().getSimpleName() + "#testRemoveNonExistingEntityProperties";
        assertSimplePropertiesUpdate(ID, createEntityProperties(ID));
        repo.removeProperties(ID);
        
        for (int index=0; index < Byte.SIZE; index++) {
            Map<?,?>    actual = repo.removeProperties(ID);
            if (ExtendedMapUtils.size(actual) > 0) {
                fail("Unexpected properties values at index=" + index + ": " + actual);
            }
        }
    }

    @Test
    public void testSimpleSetProperties() {
        final String ID=getClass().getSimpleName() + "#testSimpleSetProperties";
        assertSimplePropertiesUpdate(ID, createEntityProperties(ID));
    }

    /**
     * Make sure that repeated calls to {@link RawPropertiesRepo#setProperties(String, Map)}
     * with same ID and different properties replaces and previous ones
     */
    @Test
    public void testSetPropertiesReplacePrevious() {
        final String                ID=getClass().getSimpleName() + "#testSetPropertiesReplacePrevious";
        Map<String,Serializable>    expected=createEntityProperties(ID);
        List<Serializable>          values=new ArrayList<Serializable>(expected.keySet());
        Set<String>                 names=new TreeSet<String>(expected.keySet());
        for (int index=0; index < Byte.SIZE; index++) {
            expected.clear();

            for (String key : names) {
                int             valPos=RANDOMIZER.nextInt(values.size());
                Serializable    value=values.get(valPos);
                expected.put(key, value);
            }
    
            repo.setProperties(ID, expected);
        }
    }

    @Test
    public void testFindEntitiesByPropertyValue() {
        final String                BASE_ID="testFindEntitiesByPropertyValue";
        Map<String,Serializable>    props=createEntityProperties(BASE_ID);
        List<String>                expected=new ArrayList<>();
        for (int index=0; index < Byte.SIZE; index++) {
            String  id=BASE_ID + index;
            repo.setProperties(id, props);
            expected.add(id);
        }
        
        for (Map.Entry<String,Serializable> pe : props.entrySet()) {
            String          propName=pe.getKey();
            Serializable    propValue=pe.getValue();
            List<String>    actual=repo.findEntities(propName, propValue);
            if (!CollectionUtils.isEqualCollection(expected, actual)) {
                fail("Mismatched results for " + propName + "=" + propValue + ": expected=" + expected + ", actual=" + actual);
            }
            
            // change the type so we know it won't matcj
            if (propValue instanceof String) {
                propValue = Integer.valueOf(propValue.hashCode());
            } else {
                propValue = propValue.toString();
            }
            
            actual = repo.findEntities(propName, propValue);
            if (ExtendedCollectionUtils.size(actual) > 0) {
                fail("Unexpected results for " + propName + "=" + propValue + ": " + actual);
            }
        }
    }

    @Test
    public void testValidateProxyClass() {
        assertValidationFails("testValidateProxyClass",
                ProxyUtils.newProxyInstance(Appendable.class, ExtendedClassUtils.getDefaultClassLoader(), Mockito.mock(InvocationHandler.class), Appendable.class));
    }

    @Test
    public void testValidateNonPublicClass() {
        assertValidationFails("testValidateNonPublicClass", TestEntryRepo.class);
    }

    @Test
    public void testValidateAnonymousClass() {
        Object  annon=new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    return null;
                }
            };
        assertValidationFails("testValidateAbstractClass", annon.getClass());
    }

    @Test
    public void testLongValue() {
        BigInteger num = new BigInteger(500000, RANDOMIZER);
        final String longString = num.toString(8);

        TestEntry e=new TestEntry();
        repo.setProperties(e, new Transformer<TestEntry, Map<String,?>>() {
                @Override
                public Map<String,?> transform(TestEntry input) {
                    return Collections.<String,Serializable>singletonMap(PROP_NAME, longString);
                }
            });

        Map<String, Serializable> props = repo.getProperties(e.getId());
        assertEquals("Mismatched long value", longString, props.get(PROP_NAME));
    }

    @Test
    public void testListMatchingIdentifiers() {
        final String                COMMON_PART="testListMatchingIdentifiers";
        // NOTE: we chose a separator that has SQL meaning on purpose
        final String                COMMON_PREFIX=getClass().getSimpleName() + "#" + COMMON_PART + "%";
        Map<String,Serializable>    props=createEntityProperties(COMMON_PART);
        List<String>                expected=new ArrayList<String>();
        for (int index=0; index < Byte.SIZE; index++) {
            String  id=COMMON_PREFIX + index;
            repo.setProperties(id, props);
            expected.add(id);
        }

        // create some extra values
        for (int index=0; index < Byte.SIZE; index++) {
            repo.setProperties(String.valueOf(Math.random()), props);
        }

        List<String>    actual=repo.listMatchingIdentifiers("*" + COMMON_PART + "*");
        if (!CollectionUtils.isEqualCollection(expected, actual)) {
            fail("Mismatched common part wildcard results: expected=" + expected + ", actual=" + actual);
        }
        
        actual = repo.listMatchingIdentifiers(COMMON_PREFIX + "*");
        if (!CollectionUtils.isEqualCollection(expected, actual)) {
            fail("Mismatched common prefix wildcard results: expected=" + expected + ", actual=" + actual);
        }

        for (String id : expected) {
            actual = repo.listMatchingIdentifiers(id);
            assertEquals(id + ": Mismatched results count", 1, ExtendedCollectionUtils.size(actual));
            assertEquals("Mismatched matched identifiers", id, actual.get(0));
        }
    }

    @Test
    public void testDefaultToStringConversion() {
        TestMutable original=new TestMutable(System.nanoTime());
        assertTrue("Cannot default convert to string", conversionService.canConvert(original.getClass(), String.class));
        String  actual=conversionService.convert(original, String.class);
        assertEquals("Mismatched default to-string conversion result", original.toString(), actual);
        
        assertObjectInstanceof("Conversion service not a converters registry", ConverterRegistry.class, conversionService);

        ConverterRegistry   registry=(ConverterRegistry) conversionService;
        ExtendedConverter<String, TestMutable>  fromStringConverter=
                new AbstractExtendedConverter<String, TestMutable>(String.class, TestMutable.class) {
                    @Override
                    public TestMutable convert(String source) {
                        return new TestMutable(Long.parseLong(source) * 2L);
                    }
                };
        ExtendedConverter<TestMutable,String>   toStringConverter=
                new AbstractExtendedConverter<TestMutable,String>(TestMutable.class, String.class) {
                    @Override
                    public String convert(TestMutable source) {
                        return String.valueOf(0L - source.longValue());
                    }
                };
        ExtendedConverterRegistryUtils.addConverters(registry, fromStringConverter);

        {
            final String    ID="testDefaultToStringConversion-simple";
            repo.setProperties(ID, Collections.singletonMap(ID, original));
    
            Map<String,? extends Serializable>  props=repo.getProperties(ID);
            Serializable                        propValue=props.get(ID);
            assertObjectInstanceof("Incompatible simple property value", TestMutable.class, propValue);
            assertEquals("Mismatched re-constructed simple property value", fromStringConverter.convert(original.toString()), propValue);
        }

        ExtendedConverterRegistryUtils.addConverters(registry, toStringConverter);

        actual = conversionService.convert(original, String.class);
        assertEquals("Mismatched registered converter to-string result", toStringConverter.convert(original), actual.toString());
        
        {
            final String    ID="testDefaultToStringConversion-converted";
            repo.setProperties(ID, Collections.singletonMap(ID, original));
            
            Map<String,? extends Serializable>  props=repo.getProperties(ID);
            Serializable                        propValue=props.get(ID);
            assertObjectInstanceof("Incompatible converted property value", TestMutable.class, propValue);
            assertEquals("Mismatched re-constructed converted property value", fromStringConverter.convert(toStringConverter.convert(original)), propValue);
        }
    }

    public static class TestMutable extends MutableLong {
        private static final long serialVersionUID = -7590735440544562487L;

        public TestMutable(long value) {
            super(value);
        }
    }

    private void assertValidationFails(String testName, Object propValue) {
        try {
            repo.validatePropertyValues(testName, Collections.singletonMap(testName, propValue));
            fail(testName + ": Unexpected valid property value: " + propValue);
        } catch(IllegalStateException e) {
            // expected - ignored
        }
    }

    private Map<String,Serializable> assertSimplePropertiesUpdate(String id, Map<String,Serializable> expected) {
        repo.setProperties(id, expected);
        
        Map<String,Serializable>    actual=repo.getProperties(id);
        if (!ExtendedMapUtils.compareMaps(expected, actual)) {
            fail("Mismatched properties: expected=" + expected + ", actual=" + actual);
        }
        
        return actual;
    }

    private Map<String,Serializable> createEntityProperties(final String seed) {
        final int   unitIndex=RANDOMIZER.nextInt(units.size());
        final int   classIndex=RANDOMIZER.nextInt(classes.size());
        return new TreeMap<String,Serializable>(String.CASE_INSENSITIVE_ORDER) {
            private static final long serialVersionUID = 1L;

            {
                put("testName", seed);
                put("testHash", Integer.valueOf(seed.hashCode()));
                put("nowTime", Long.valueOf(System.nanoTime()));
                put("nowDate", new Date(System.currentTimeMillis()));
                put("nowCalendar", Calendar.getInstance());
                put("randomValue", Double.valueOf(Math.random()));
                put("enumValue", units.get(unitIndex));
                put("periodValue", Period.valueOf(units.get(unitIndex), Math.abs(RANDOMIZER.nextLong())));
                put("classValue", classes.get(classIndex));
            }
        };
    }

    static class TestEntryRepo extends RawPropertiesRepoImpl<TestEntry> {
        public TestEntryRepo(DataSource ds, ConversionService converter) {
            super(TestEntry.class, ds, converter);
        }
    }

    public static class TestEntry extends AbstractSimpleJavaBean implements MutableIdentity, Serializable {
        private static final long serialVersionUID = -2255416708772090715L;

        private String  id;
        public TestEntry() {
            this(UUID.randomUUID().toString());
        }
        
        public TestEntry(String identifier) {
            id = identifier;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String identifier) {
            id = identifier;
        }
    }
}
