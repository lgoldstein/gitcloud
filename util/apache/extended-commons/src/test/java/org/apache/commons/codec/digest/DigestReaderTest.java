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

import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.security.MessageDigest;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullWriter;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 3, 2013 12:44:49 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DigestReaderTest extends AbstractDigestTestSupport {
    private static final File  TEST_FILE;
    private static final char[]    TEST_DATA;
    private static final byte[]    TEST_BYTES;

    // avoid repeated calls to constructor
    static {
        TEST_FILE = getTestJavaSourceFile(DigestReaderTest.class);
        assertNotNull("Cannot locate source file");
        
        try {
            TEST_DATA = ExtendedFileUtils.readFileToCharArray(TEST_FILE);
            TEST_BYTES = FileUtils.readFileToByteArray(TEST_FILE);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DigestReaderTest() {
        super();
    }


    @Test
    public void testDigestCorrectness() throws Exception {
        for (String algorithm : ALGORITHMS) {
            MessageDigest   digest=MessageDigest.getInstance(algorithm);
            byte[]          expected=digest.digest(TEST_BYTES);
            Reader          car=new CharArrayReader(TEST_DATA);
            try {
                DigestReader   input=new DigestReader(algorithm, car);
                try {
                    char[]  readData=IOUtils.toCharArray(input);
                    // make sure read data has not been modified
                    assertArrayEquals(algorithm + ": mismatched read data", TEST_DATA, readData);
                } finally {
                    input.close();
                }
                
                byte[]  actual=input.getDigestValue();
                assertArrayEquals("Mismatched digest for " + algorithm, expected, actual);
            } finally {
                car.close();
            }
        }
    }
    
    @Test
    public void testDigestOnlyOnClose() throws Exception {
        char[]  readBuf=new char[Byte.MAX_VALUE];
        for (String algorithm : ALGORITHMS) {
            Reader  car=new CharArrayReader(TEST_DATA);
            try {
                DigestReader   input=new DigestReader(algorithm, car);
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
                car.close();
            }
        }
    }
    
    @Test
    public void testSameDigestInstanceAfterClose() throws Exception {
        for (String algorithm : ALGORITHMS) {
            Reader  car=new CharArrayReader(TEST_DATA);
            try {
                DigestReader   input=new DigestReader(algorithm, car);
                try {
                    int cpyLen=IOUtils.copy(input, NullWriter.NULL_WRITER);
                    assertEquals(algorithm + ": mismatched copy length", TEST_DATA.length, cpyLen);
                    testSameDigestInstanceAfterClose(input);
                } finally {
                    input.close();
                }
            } finally {
                car.close();
            }
        }        
    }
    
    @Test
    public void testDigestResetOnStreamReset() throws Exception {
        for (String algorithm : ALGORITHMS) {
            MessageDigest   digest=MessageDigest.getInstance(algorithm);
            byte[]          expected=digest.digest(TEST_BYTES);
            Reader          car=new CharArrayReader(TEST_DATA);
            try {
                DigestReader   input=new DigestReader(algorithm, car);
                try {
                    for (int index=0; index < Short.SIZE; index++) {
                        input.reset();

                        int cpyLen=IOUtils.copy(input, NullWriter.NULL_WRITER);
                        assertEquals(algorithm + ": mismatched copy length at index=" + index, TEST_DATA.length, cpyLen);
                    }
                } finally {
                    input.close();
                }
                
                byte[]  actual=input.getDigestValue();
                assertArrayEquals("Mismatched digest for " + algorithm, expected, actual);
            } finally {
                car.close();
            }
        }
    }
}
