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
 * @since Jun 5, 2013 3:01:29 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CloseShieldReaderTest extends AbstractTestSupport {
    public CloseShieldReaderTest () {
        super();
    }

    @Test
    public void testCloseShielding() throws IOException {
        CharSequence                data=getClass().getName() + "#testCloseShielding";
        final int                   MAX_SHIELD_READ=data.length() / 2;
        ExtendedCharSequenceReader  proxy=new ExtendedCharSequenceReader(data);
        try {
            CloseShieldReader   r=new CloseShieldReader(proxy);
            try {
                for (int    index=0; index < MAX_SHIELD_READ; index++) {
                    char    expected=data.charAt(index), actual=(char) r.read();
                    assertEquals("Mismatched character at shielded position=" + index, expected, actual);
                }
            } finally {
                r.close();
            }

            assertFalse("Shield unexpectedly reported as open", r.isOpen());
            assertTrue("Proxy not reported as open", proxy.isOpen());
            assertEquals("Mismatched single shielded read result", (-1), r.read());
            
            char[]  cbuf=new char[Byte.SIZE];
            assertEquals("Unexpected shielded char[] read", (-1), r.read(cbuf));
            assertEquals("Unexpected shielded CharBuffer read", (-1), r.read(CharBuffer.wrap(cbuf)));
            
            for (int index=MAX_SHIELD_READ; index < data.length(); index++) {
                char    expected=data.charAt(index), actual=(char) proxy.read();
                assertEquals("Mismatched character at proxy position=" + index, expected, actual);
            }
        } finally {
            proxy.close();
        }
    }
}
