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
package net.community.chest.gitcloud.facade.frontend.git;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.lib.Constants;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.AbstractSpringTestSupport;

/**
 * @author Lyor Goldstein
 * @since Sep 12, 2013 2:52:37 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitControllerTest extends AbstractSpringTestSupport {
    public GitControllerTest() {
        super();
    }

    @Test
    public void testExtractRepositoryNameFromValidPaths() {
        final String    expected="testExtractRepositoryName" + Constants.DOT_GIT_EXT;
        List<String>    prefixes=Collections.unmodifiableList(Arrays.asList("", "/", "/l/y/o/r/"));
        List<String>    suffixes=Collections.unmodifiableList(Arrays.asList("", "/", "/r/o/y/l"));
        for (String prfx : prefixes) {
            for (String sfx : suffixes) {
                String  uriPath=prfx + expected + sfx;
                String  actual=GitController.extractRepositoryName(uriPath);
                assertEquals("Mismatched name for path=" + uriPath, expected, actual);
            }
        }
    }
    
    @Test
    public void testExtractRepositoryNameFromInvalidPaths() {
        for (String uriPath : new String[] { null, "", Constants.DOT_GIT_EXT, "/" + Constants.DOT_GIT_EXT, "/no/dot/git" }) {
            assertNull("Unexpected name for " + uriPath, GitController.extractRepositoryName(uriPath));
        }
    }

    @Test
    public void testCapitalizeHttpHeaderNameOnCapitalizedValues() {
        for (String expected : new String[] { "Host", "Content-Type", "Content-Transfer-Encoding" }) {
            String  actual=GitController.capitalizeHttpHeaderName(expected);
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
            String  actual=GitController.capitalizeHttpHeaderName(expected);
            assertEquals("Mismatched results for hdr=" + hdr, expected, actual);
        }
    }
}
