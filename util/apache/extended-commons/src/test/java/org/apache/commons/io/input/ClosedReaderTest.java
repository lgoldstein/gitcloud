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
import java.nio.CharBuffer;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jun 5, 2013 2:57:37 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClosedReaderTest extends AbstractTestSupport {
    public ClosedReaderTest () {
        super();
    }

    @Test
    public void testReader() throws IOException {
        ClosedReader  r=new ClosedReader();
        try {
            assertFalse("Unexpectedly reported as open", r.isOpen());
            assertEquals("Mismatched single read result", (-1), r.read());
            
            char[]  cbuf=new char[Byte.SIZE];
            assertEquals("Unexpected char[] read", (-1), r.read(cbuf));
            assertEquals("Unexpected CharBuffer read", (-1), r.read(CharBuffer.wrap(cbuf)));
        } finally {
            r.close();
        }
    }
}
