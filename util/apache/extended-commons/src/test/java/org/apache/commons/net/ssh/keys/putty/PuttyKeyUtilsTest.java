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

package org.apache.commons.net.ssh.keys.putty;

import java.net.URL;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

import org.apache.commons.net.ssh.keys.AbstractSSHKeysTestSupport;
import org.apache.commons.net.ssh.keys.DSSKeyDecoder;
import org.apache.commons.net.ssh.keys.KeyUtils;
import org.apache.commons.net.ssh.keys.RSAKeyDecoder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 11:46:56 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PuttyKeyUtilsTest extends AbstractSSHKeysTestSupport {
    public PuttyKeyUtilsTest() {
        super();
    }

    @Test
    public void testLoadUnencryptedPuttyRSAKeyPair() throws Exception {
        testLoadUnencryptedPuttyKeyPair(RSAKeyDecoder.RSA_ALGORITHM);
    }

    @Test
    public void testLoadUnencryptedPuttyDSSKeyPair() throws Exception {
        testLoadUnencryptedPuttyKeyPair(DSSKeyDecoder.DSS_ALGORITHM);
    }

    private KeyPair testLoadUnencryptedPuttyKeyPair(String algorithm) throws Exception {
        return testLoadPuttyKeyPair(getClass().getSimpleName(), algorithm, null);
    }

    @Test
    public void testLoadAES256CBCEncryptedPuttyRSAKeyPair() throws Exception {
        testLoadEncryptedPuttyKeyPair(RSAKeyDecoder.RSA_ALGORITHM, "AES", 256, "CBC");
    }

    @Test
    public void testLoadAES256CBCEncryptedPuttyDSSKeyPair() throws Exception {
        testLoadEncryptedPuttyKeyPair(DSSKeyDecoder.DSS_ALGORITHM, "AES", 256, "CBC");
    }

    private KeyPair testLoadEncryptedPuttyKeyPair(String algorithm, String cipherName, int cipherSize, String cipherType) throws Exception {
        String  xform=cipherName + "/" + cipherType + "/NoPadding";
        int     maxAllowedBits=Cipher.getMaxAllowedKeyLength(xform);
        if (maxAllowedBits < cipherSize) {
            logger.warn("testLoadEncryptedPuttyKeyPair(" + xform + ")"
                    + " required cipher size (" + cipherSize + ")"
                    + " exceeds max. available (" + maxAllowedBits + ")"
                    + " need to download & install the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files");
            return null;
        }

        final String    PASSWORD="super secret passphrase";
        return testLoadPuttyKeyPair(PASSWORD.replace(' ', '-') + "-" + cipherName + "-" + cipherSize + "-" + cipherType, algorithm, PASSWORD);
    }

    private KeyPair testLoadPuttyKeyPair(String prefix, String algorithm, String password) throws Exception {
        URL url=getClassResource(prefix + "-" + algorithm + "-" + KeyPair.class.getSimpleName() + PuttyKeyUtils.PPK_FILE_EXT);
        assertNotNull("Missing test file", url);
        
        KeyPair     kp=validateKeyPair(PuttyKeyUtils.loadPuttyKeyPair(url, password));
        PrivateKey  privateKey=kp.getPrivate();
        PublicKey   expected=kp.getPublic(), actual=KeyUtils.recoverPublicKey(privateKey);
        assertArrayEquals("Mismatched recovered public key contents", expected.getEncoded(), actual.getEncoded());
        return kp;
    }
}
