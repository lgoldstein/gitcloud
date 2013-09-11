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

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullWriter;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 3, 2013 12:07:09 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DigestWriterTest extends AbstractDigestTestSupport {
    private static final File  TEST_FILE;
    private static final char[]    TEST_DATA;
    private static final byte[]    TEST_BYTES;

    // avoid repeated calls to constructor
    static {
        TEST_FILE = getTestJavaSourceFile(DigestWriterTest.class);
        assertNotNull("Cannot locate source file");
        
        try {
            TEST_DATA = ExtendedFileUtils.readFileToCharArray(TEST_FILE);
            TEST_BYTES = FileUtils.readFileToByteArray(TEST_FILE);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DigestWriterTest() {
        super();
    }

    @Test
    public void testDigestCorrectness() throws Exception {
        for (String algorithm : ALGORITHMS) {
            MessageDigest           digest=MessageDigest.getInstance(algorithm);
            byte[]                  expected=digest.digest(TEST_BYTES);
            CharArrayWriter         caw=new CharArrayWriter(TEST_DATA.length);
            try {
                DigestWriter    output=new DigestWriter(algorithm, caw);
                try {
                    output.write(TEST_DATA);
                } finally {
                    output.close();
                }

                byte[]  actual=output.getDigestValue();
                assertArrayEquals("Mismatched digest for " + algorithm, expected, actual);
            } finally {
                caw.close();
            }
            
            char[]  chars=caw.toCharArray();    // make sure writing did not change the data
            assertArrayEquals(algorithm + ": mismatched written contents", TEST_DATA, chars);
        }
    }

    @Test
    public void testDigestOnlyOnClose() throws Exception {
        for (String algorithm : ALGORITHMS) {
            DigestWriter  output=new DigestWriter(algorithm, NullWriter.NULL_WRITER);
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
            DigestWriter  output=new DigestWriter(algorithm, NullWriter.NULL_WRITER);
            try {
                output.write(TEST_DATA);
                testSameDigestInstanceAfterClose(output);
            } finally {
                output.close();
            }
        }        
    }
}
