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

package org.apache.commons.codec.digest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 3, 2013 9:52:49 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DigestOutputStreamTest extends AbstractDigestTestSupport {
    private static final byte[]    TEST_DATA;

    // avoid repeated calls to constructor
    static {
        URL  url=ExtendedClassUtils.getClassBytesURL(DigestOutputStreamTest.class);
        assertNotNull("Missing class bytes URL", url);
        try {
            TEST_DATA = IOUtils.toByteArray(url);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DigestOutputStreamTest() {
        super();
    }

    @Test
    public void testDigestCorrectness() throws Exception {
        for (String algorithm : ALGORITHMS) {
            MessageDigest           digest=MessageDigest.getInstance(algorithm);
            byte[]                  expected=digest.digest(TEST_DATA);
            ByteArrayOutputStream   baos=new ByteArrayOutputStream(TEST_DATA.length);
            try {
                DigestOutputStream  output=new DigestOutputStream(algorithm, baos);
                try {
                    output.write(TEST_DATA);
                } finally {
                    output.close();
                }
                
                byte[]  actual=output.getDigestValue();
                assertArrayEquals("Mismatched digest for " + algorithm, expected, actual);
            } finally {
                baos.close();
            }
            
            // also make sure that the output has been written without modification
            byte[]  written=baos.toByteArray();
            assertArrayEquals("Mismatched output for " + algorithm, TEST_DATA, written);
        }
    }
    
    @Test
    public void testDigestOnlyOnClose() throws Exception {
        for (String algorithm : ALGORITHMS) {
            DigestOutputStream  output=new DigestOutputStream(algorithm, NullOutputStream.NULL_OUTPUT_STREAM);
            try {
                assertNull(algorithm + ": unexpected initial digest", output.getDigestValue());
                for (int index=1; index <= Long.SIZE; index++) {
                    output.write(TEST_DATA);
                    assertNull(algorithm + ": unexpected digest at index=" + index, output.getDigestValue());
                }
            } finally {
                output.close();
            }
            
            byte[]  value=output.getDigestValue();
            assertNotNull(algorithm + ": no digest after close", value);
        }
    }
    
    @Test
    public void testSameDigestInstanceAfterClose() throws Exception {
        for (String algorithm : ALGORITHMS) {
            DigestOutputStream  output=new DigestOutputStream(algorithm, NullOutputStream.NULL_OUTPUT_STREAM);
            try {
                output.write(TEST_DATA);
                testSameDigestInstanceAfterClose(output);
            } finally {
                output.close();
            }
        }        
    }
}
