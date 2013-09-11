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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 3, 2013 10:06:25 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedDigestUtilsTest extends AbstractDigestTestSupport {
    private static final File  TEST_FILE;
    private static final byte[]    TEST_BYTES;

    // avoid repeated calls to constructor
    static {
        TEST_FILE = getTestJavaSourceFile(ExtendedDigestUtilsTest.class);
        assertNotNull("Cannot locate source file");
        
        try {
            TEST_BYTES = FileUtils.readFileToByteArray(TEST_FILE);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ExtendedDigestUtilsTest() {
        super();
    }

    @Test
    public void testGetDigestAlgorithms() {
        for (String algorithm : ALGORITHMS) {
            try {
                MessageDigest   digest=MessageDigest.getInstance(algorithm);
                assertNotNull("No digest instance created for " + algorithm, digest);
            } catch(NoSuchAlgorithmException e) {
                fail("No digest found for " + algorithm);
            }
        }
    }
    
    @Test
    public void testDigestFile() throws IOException {
        for (String algorithm : ALGORITHMS) {
            MessageDigest   digest=DigestUtils.getDigest(algorithm);
            byte[]          expected=digest.digest(TEST_BYTES);
            digest.reset();    // make sure

            byte[]  actual=ExtendedDigestUtils.digest(digest, TEST_FILE);
            assertArrayEquals(algorithm + ": mismatched digest value", expected, actual);
        }
    }
    
    @Test
    public void testDigestURL() throws IOException {
        final URL   url=TEST_FILE.toURI().toURL();
        for (String algorithm : ALGORITHMS) {
            MessageDigest   digest=DigestUtils.getDigest(algorithm);
            byte[]          expected=digest.digest(TEST_BYTES);
            digest.reset();    // make sure

            byte[]  actual=ExtendedDigestUtils.digest(digest, url);
            assertArrayEquals(algorithm + ": mismatched digest value", expected, actual);
        }
    }

    @Test
    public void testDigestInputStream() throws IOException {
        for (String algorithm : ALGORITHMS) {
            MessageDigest   digest=DigestUtils.getDigest(algorithm);
            byte[]          expected=digest.digest(TEST_BYTES);
            digest.reset();    // make sure

            InputStream input=new FileInputStream(TEST_FILE);
            try {
                byte[]  actual=ExtendedDigestUtils.digest(digest, input);
                assertArrayEquals(algorithm + ": mismatched digest value", expected, actual);
            } finally {
                input.close();
            }
        }
    }

    @Test
    public void testDigestReader() throws IOException {
        for (String algorithm : ALGORITHMS) {
            MessageDigest   digest=DigestUtils.getDigest(algorithm);
            byte[]          expected=digest.digest(TEST_BYTES);
            digest.reset();    // make sure

            Reader input=new FileReader(TEST_FILE);
            try {
                byte[]  actual=ExtendedDigestUtils.digest(digest, input);
                assertArrayEquals(algorithm + ": mismatched digest value", expected, actual);
            } finally {
                input.close();
            }
        }
    }

    @Test
    public void testDigestCharactersAndCharSequence() throws IOException {
        char[]  chars=ExtendedFileUtils.readFileToCharArray(TEST_FILE);
        for (String algorithm : ALGORITHMS) {
            MessageDigest   digest=DigestUtils.getDigest(algorithm);
            byte[]          expected=digest.digest(TEST_BYTES);
            digest.reset();    // make sure
            
            byte[]  actual=ExtendedDigestUtils.digest(digest, chars);
            assertArrayEquals(algorithm + ": mismatched char[] digest value", expected, actual);

            actual = ExtendedDigestUtils.digest(digest, CharBuffer.wrap(chars));
            assertArrayEquals(algorithm + ": mismatched CharSequence digest value", expected, actual);
        }
    }
}
