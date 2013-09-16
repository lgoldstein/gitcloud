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

package org.apache.commons.codec.signatures;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.net.ssh.keys.dss.DSSKeyDecoder;
import org.apache.commons.net.ssh.keys.rsa.RSAKeyDecoder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jul 9, 2013 9:44:22 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SignatureUtilsTest extends AbstractSignatureTestSupport {
    public SignatureUtilsTest() {
        super();
    }

    @Test
    public void testSignDataRSA1024() throws Exception {
        testSignData(RSAKeyDecoder.RSA_ALGORITHM, 1024);
    }

    @Test
    public void testSignDataDSS1024() throws Exception {
        testSignData(DSSKeyDecoder.DSS_ALGORITHM, 1024);
    }

    private void testSignData(String algorithm, int numBits)
            throws GeneralSecurityException, IOException {
        KeyPair kp=generateKeyPair(algorithm, numBits);
        URL     file=ExtendedClassUtils.getClassBytesURL(getClass());
        assertNotNull("Cannot locate class bytes", file);

        byte[]  signature=SignatureUtils.signData(kp.getPrivate(), file);
        assertTrue("Signature verification failed", SignatureUtils.verifySignature(kp.getPublic(), file, signature));
    }

    @Test
    public void testSignTextDataRSA1024() throws Exception {
        testSignTextData(RSAKeyDecoder.RSA_ALGORITHM, 1024);
        
    }
    
    private void testSignTextData(String algorithm, int numBits)
            throws GeneralSecurityException, IOException {
        KeyPair     kp=generateKeyPair(algorithm, numBits);
        PrivateKey  prvKey=kp.getPrivate();
        File        file=getTestJavaSourceFile();
        byte[]      expected=SignatureUtils.signData(prvKey, file);
        char[]      chars=ExtendedFileUtils.readFileToCharArray(file);
        byte[]      actual=SignatureUtils.signData(prvKey, chars);
        assertArrayEquals("Mismatched signature contents", expected, actual); 
    }
}
