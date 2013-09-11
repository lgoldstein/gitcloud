/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * @since Sep 3, 2013 10:29:38 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DigestInputStreamTest extends AbstractDigestTestSupport {
    private static final byte[]    TEST_DATA;

    // avoid repeated calls to constructor
    static {
        URL  url=ExtendedClassUtils.getClassBytesURL(DigestInputStreamTest.class);
        assertNotNull("Missing class bytes URL", url);
        try {
            TEST_DATA = IOUtils.toByteArray(url);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DigestInputStreamTest()  {
        super();
    }

    @Test
    public void testDigestCorrectness() throws Exception {
        for (String algorithm : ALGORITHMS) {
            MessageDigest   digest=MessageDigest.getInstance(algorithm);
            byte[]          expected=digest.digest(TEST_DATA);
            InputStream     bais=new ByteArrayInputStream(TEST_DATA);
            try {
                DigestInputStream   input=new DigestInputStream(algorithm, bais);
                try {
                    byte[]  readData=IOUtils.toByteArray(input);
                    // make sure read data has not been modified
                    assertArrayEquals(algorithm + ": mismatched read data", TEST_DATA, readData);
                } finally {
                    input.close();
                }
                
                byte[]  actual=input.getDigestValue();
                assertArrayEquals("Mismatched digest for " + algorithm, expected, actual);
            } finally {
                bais.close();
            }
        }
    }
    
    @Test
    public void testDigestOnlyOnClose() throws Exception {
        byte[]  readBuf=new byte[Byte.MAX_VALUE];
        for (String algorithm : ALGORITHMS) {
            InputStream     bais=new ByteArrayInputStream(TEST_DATA);
            try {
                DigestInputStream   input=new DigestInputStream(algorithm, bais);
                try {
                    assertNull(algorithm + ": unexpected initial digest", input.getDigestValue());
                    for (int remLen=TEST_DATA.length; remLen > 0 ; ) {
                        int toRead=Math.min(remLen, readBuf.length), readLen=input.read(readBuf, 0, toRead);
                        assertEquals(algorithm + ": mismatched read len at remain len=" + remLen, toRead, readLen);
                        assertNull(algorithm + ": unexpected digest at remain len=" + remLen, input.getDigestValue());
                        remLen -= readLen;
                    }
                } finally {
                    input.close();
                }
                byte[]  value=input.getDigestValue();
                assertNotNull(algorithm + ": no digest after close", value);
            } finally {
                bais.close();
            }
            
        }
    }
    
    @Test
    public void testSameDigestInstanceAfterClose() throws Exception {
        for (String algorithm : ALGORITHMS) {
            InputStream bais=new ByteArrayInputStream(TEST_DATA);
            try {
                DigestInputStream   input=new DigestInputStream(algorithm, bais);
                try {
                    int cpyLen=IOUtils.copy(input, NullOutputStream.NULL_OUTPUT_STREAM);
                    assertEquals(algorithm + ": mismatched copy length", TEST_DATA.length, cpyLen);
                    testSameDigestInstanceAfterClose(input);
                } finally {
                    input.close();
                }
            } finally {
                bais.close();
            }
        }        
    }
    
    @Test
    public void testDigestResetOnStreamReset() throws Exception {
        for (String algorithm : ALGORITHMS) {
            MessageDigest   digest=MessageDigest.getInstance(algorithm);
            byte[]          expected=digest.digest(TEST_DATA);
            InputStream     bais=new ByteArrayInputStream(TEST_DATA);
            try {
                DigestInputStream   input=new DigestInputStream(algorithm, bais);
                try {
                    for (int index=0; index < Short.SIZE; index++) {
                        input.reset();

                        int cpyLen=IOUtils.copy(input, NullOutputStream.NULL_OUTPUT_STREAM);
                        assertEquals(algorithm + ": mismatched copy length at index=" + index, TEST_DATA.length, cpyLen);
                    }
                } finally {
                    input.close();
                }

                byte[]  actual=input.getDigestValue();
                assertArrayEquals("Mismatched digest for " + algorithm, expected, actual);
            } finally {
                bais.close();
            }
        }
    }
}
