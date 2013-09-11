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

package org.apache.commons.net.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Aug 22, 2013 10:08:13 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class URLUtilsTest extends AbstractTestSupport {
    public URLUtilsTest() {
        super();
    }

    @Test
    public void testToStringURL() throws MalformedURLException {
        assertNull("Unexpected null result", URLUtils.toString((URL) null));
        
        URL url=new URL("http://37.77.73.47:7365/royl");
        assertNotNull("Cannot resolve class bytes location", url);      
        assertEquals("Mismatched toString result", url.toExternalForm(), URLUtils.toString(url));
    }

    @Test
    public void testGetURLSource () {
        final URL       containerURL=ExtendedClassUtils.getClassContainerLocationURL(getClass());
        final String    sourceForm=URLUtils.getURLSource(containerURL);
        assertSameSource("With JAR prefix", sourceForm, ExtendedFileUtils.JAR_URL_PREFIX + sourceForm);
        assertSameSource("With sub-resource", sourceForm, sourceForm + String.valueOf(URLUtils.RESOURCE_SUBPATH_SEPARATOR) + "testGetURLSource");
        assertSameSource("All out", sourceForm, ExtendedFileUtils.JAR_URL_PREFIX + sourceForm + String.valueOf(URLUtils.RESOURCE_SUBPATH_SEPARATOR) + "testGetURLSource");
    }

    @Test
    public void testConcat() {
        final String    BASE="http://37.77.34.7:7365/royl", EXTENSION="testConcat", EXPECTED=BASE + "/" + EXTENSION;
        final String[]  extras={ "", "/" };
        for (String baseSuffix : extras) {
            String  baseValue = BASE + baseSuffix;
            assertSame("Mismatched instances for null extension", baseValue, URLUtils.concat(baseValue, null));
            assertSame("Mismatched instances for empty extension", baseValue, URLUtils.concat(baseValue, ""));

            for (String extPrefix : extras) {
                String  extValue=extPrefix + EXTENSION;
                String  actual=URLUtils.concat(baseValue, extValue);
                assertEquals("Mismatched results for base=" + baseValue + ", extension=" + extValue, EXPECTED, actual);
            }
        }
    }

    private static void assertSameSource (String testName, String expected, String url) {
        String  actual=URLUtils.getURLSource(url);
        assertEquals("[" + testName + "] mismatched results", expected, actual);
    }
}
