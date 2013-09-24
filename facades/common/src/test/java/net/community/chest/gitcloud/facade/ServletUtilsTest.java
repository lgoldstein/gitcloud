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
package net.community.chest.gitcloud.facade;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor Goldstein
 * @since Sep 24, 2013 12:16:35 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServletUtilsTest extends AbstractTestSupport {
    public ServletUtilsTest() {
        super();
    }

    @Test
    public void testCapitalizeHttpHeaderNameOnCapitalizedValues() {
        for (String expected : new String[] { "Host", "Content-Type", "Content-Transfer-Encoding" }) {
            String  actual=ServletUtils.capitalizeHttpHeaderName(expected);
            assertSame("Unexpected modification", expected, actual);
        }
    }
    
    @Test
    public void testCapitalizeHttpHeaderNameOnNonCapitalizedValues() {
        String[]    values={
                "host",                         "Host",
                "content-type",                 "Content-Type",
                "content-transfer-encoding",    "Content-Transfer-Encoding",
                "Prefixed-capitalized",         "Prefixed-Capitalized",
                "suffix-Capitalized",           "Suffix-Capitalized",
                "mixed-Capitalized-name-Value", "Mixed-Capitalized-Name-Value"
            };
        for (int    index=0; index < values.length; index += 2) {
            String  hdr=values[index], expected=values[index+1];
            String  actual=ServletUtils.capitalizeHttpHeaderName(expected);
            assertEquals("Mismatched results for hdr=" + hdr, expected, actual);
        }
    }
}
