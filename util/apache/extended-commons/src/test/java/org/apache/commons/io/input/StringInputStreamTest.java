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

package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringInputStreamTest extends AbstractTestSupport {
    public StringInputStreamTest() {
        super();
    }
    
    @Test
    public void testSimpleString() throws IOException {
        String      expected=getClass().getName() + "#testSimpleString()";
        InputStream s=new StringInputStream(expected);
        try {
            String  actual=IOUtils.toString(s);
            assertEquals("Mismatched read string", expected, actual);
        } finally {
            s.close();
        }
    }
}
