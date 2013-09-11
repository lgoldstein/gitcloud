/*
 * 
 */
package org.apache.commons.cli;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 2:46:39 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EnvironmentUtilsTest extends AbstractTestSupport {
    public EnvironmentUtilsTest () {
        super();
    }

    @Test
    public void testSysenvReader() {
    	Map<String,String>	env=System.getenv();
        ExtendedMapUtils.forAllEntriesDo(env, new Closure<Map.Entry<String,String>>() {
            @Override
            public void execute(Entry<String, String> e) {
                String  key=e.getKey(), expected=e.getValue(), actual=EnvironmentUtils.SYSENV_READER.get(key);
                assertEquals(key + ": mismatched environment values", expected, actual);
            }
        });
        
        Collection<String>	expKeys=env.keySet(), actKeys=EnvironmentUtils.SYSENV_READER.getKeys();
        if (!CollectionUtils.isEqualCollection(expKeys, actKeys)) {
        	fail("Mismatched environment key set: expected=" + expKeys + ", actual=" + actKeys);
        }
    }

    @Test
    public void testSyspropReader() {
    	Collection<String>	expKeys=System.getProperties().stringPropertyNames();
    	CollectionUtils.forAllDo(expKeys, new Closure<String>() {
			@Override
			public void execute (String key) {
				String	expected=System.getProperty(key), actual=EnvironmentUtils.SYSPROPS_READER.get(key);
                assertEquals(key + ": mismatched property values", expected, actual);
			}
    	});

    	Collection<String>	actKeys=EnvironmentUtils.SYSPROPS_READER.getKeys();
    	if (!CollectionUtils.isEqualCollection(expKeys, actKeys)) {
        	fail("Mismatched properties set: expected=" + expKeys + ", actual=" + actKeys);
        }
    }
}
