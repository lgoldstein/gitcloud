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

package org.apache.commons.net.ssh.keys;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.net.ssh.keys.dss.DSSKeyDecoder;
import org.apache.commons.net.ssh.keys.rsa.RSAKeyDecoder;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 10:43:44 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KeyUtilsTest extends AbstractSSHKeysTestSupport {
    private final File  targetFolder;

    public KeyUtilsTest() {
        targetFolder = ensureFolderExists(new File(Validate.notNull(detectTargetFolder(), "No target folder", ArrayUtils.EMPTY_OBJECT_ARRAY), getClass().getSimpleName()));
    }
    
    @BeforeClass
    public static final void listAllProvidersProperties() throws IOException {
        final Class<?>  ANCHOR=KeyUtilsTest.class;
        File    targetFolder=ensureFolderExists(
                new File(Validate.notNull(detectTargetFolder(ANCHOR), "No target folder", ArrayUtils.EMPTY_OBJECT_ARRAY), ANCHOR.getSimpleName()));    
        for (Provider provider: Security.getProviders()) {
            String      providerName=provider.getName();
            File        providerFile=new File(targetFolder, providerName + ".properties");
            PrintStream out=new PrintStream(providerFile);
            try {
                System.out.append('\t').append(providerFile.getName());
                for (String key: provider.stringPropertyNames()) {
                    out.append(key).append(": ").println(provider.getProperty(key));
                    System.out.append('.');
                }
            } finally {
                System.out.println();
                out.close();
            }
        }
    }

    @Test
    public void testLoadPEMPrivateKeyRSA() throws Exception {
        testLoadPEMPrivateKey(RSAKeyDecoder.RSA_ALGORITHM);
    }

    @Test
    public void testLoadPEMPrivateKeyDSS() throws Exception {
        testLoadPEMPrivateKey(DSSKeyDecoder.DSS_ALGORITHM);
    }

    @Test
    public void testDecodeRSAPublicKeyString() throws Exception {
        final String    KEY_DATA="AAAAB3NzaC1yc2EAAAABJQAAAIEAiPWx6WM4lhHNedGfBpPJNPpZ7yKu+dnn1SJejgt4596k6YjzGGphH2TUxwKzxcKDKKezwkpfnxPkSMkuEspGRt/aZZ9wa++Oi7Qkr8prgHc4soW6NUlfDzpvZK2H5E7eQaSeP3SAwGmQKUFHCddNaP0L+hM7zhFNzjFvpaMgJw0=";
        PublicKey       expected=KeyUtils.decodeOpenSSHPublicKey(KEY_DATA);
        PublicKey       actual=KeyUtils.decodeOpenSSHPublicKey(RSAKeyDecoder.SSH_RSA + " " + KEY_DATA + "  dummy@dummy");
        assertEquals("Mismatched decoded key algorithm", expected.getAlgorithm(), actual.getAlgorithm());
        assertArrayEquals("Mismatched encoded key data", expected.getEncoded(), actual.getEncoded());
        assertEquals("Mismatched key comparator result", 0, KeyUtils.BY_ALGORITHM_ENCODED_FORM_KEY_COMPARATOR.compare(expected, actual));
    }

    private PrivateKey testLoadPEMPrivateKey(String algorithm) throws Exception {
        URL url=getClassResource(getClass().getSimpleName() + "-" + algorithm + KeyUtils.PEM_KEYFILE_EXT);
        assertNotNull("Missing test file", url);
        
        PrivateKey  key=KeyUtils.loadPEMPrivateKey(url, null);
        assertNotNull("No key extracted", key);
        
        String  baseName="testLoadPEMPrivateKey-" + algorithm + ".asn1";
        byte[]  data=key.getEncoded();
        FileUtils.writeByteArrayToFile(new File(targetFolder, baseName), data);
        FileUtils.writeStringToFile(new File(targetFolder, baseName + ".b64.txt"), Base64.encodeBase64String(data));
        return key;
    }

    @Test
    public void testRecoverRSAPublicKey() throws Exception {
        testRecoverPublicKey(RSAKeyDecoder.RSA_ALGORITHM);
    }

    @Test
    public void testRecoverDSSPublicKey() throws Exception {
        testRecoverPublicKey(DSSKeyDecoder.DSS_ALGORITHM);
    }

    private KeyPair testRecoverPublicKey(String algorithm) throws Exception {
        URL url=getClassResource(getClass().getSimpleName() + "-" + algorithm + "-" + KeyPair.class.getSimpleName());
        assertNotNull("Missing test file", url);
        
        KeyPair     kp=KeyUtils.loadOpenSSHKeyPair(url, null);
        PrivateKey  privateKey=kp.getPrivate();
        PublicKey   expected=kp.getPublic(), actual=KeyUtils.recoverPublicKey(privateKey);
        assertArrayEquals("Mismatched recovered public key contents", expected.getEncoded(), actual.getEncoded());
        assertEquals("Mismatched key comparator result", 0, KeyUtils.BY_ALGORITHM_ENCODED_FORM_KEY_COMPARATOR.compare(expected, actual));
        return validateKeyPair(kp);
    }

    @Test
    public void testLoadRSAKeyPair() throws Exception {
        testLoadKeyPair(RSAKeyDecoder.RSA_ALGORITHM);
    }

    @Test
    public void testLoadDSSKeyPair() throws Exception {
        testLoadKeyPair(DSSKeyDecoder.DSS_ALGORITHM);
    }

    private KeyPair testLoadKeyPair(String algorithm) throws Exception {
        URL url=getClassResource(getClass().getSimpleName() + "-" + algorithm + "-" + KeyPair.class.getSimpleName());
        assertNotNull("Missing test file", url);
        return validateKeyPair(KeyUtils.loadOpenSSHKeyPair(url, null));
    }

    @Test
    public void testLoadDefaultKeyPair() throws Exception {
        File    keysFolder=KeyUtils.getDefaultKeysFolder();
        if (!keysFolder.exists()) {
            logger.info("testLoadDefaultKeyPair: skip - not found " + keysFolder.getAbsolutePath());
            return;
        }
        
        assertTrue("Not a folder: " + keysFolder.getAbsolutePath(), keysFolder.isDirectory());
        for (String algorithm : new String[] { RSAKeyDecoder.RSA_ALGORITHM, DSSKeyDecoder.DSS_ALGORITHM }) {
            File    pubFile=new File(keysFolder, KeyUtils.getStandardOpenSSHPublicKeyFilename(algorithm));
            File    prvFile=new File(keysFolder, KeyUtils.getStandardOpenSSHPrivateKeyFilename(algorithm));
            if (!pubFile.exists()) {
                logger.info("testLoadDefaultKeyPair[" + algorithm + "]: skip - public key file not found " + pubFile.getAbsolutePath());
                continue;
            }

            if (!prvFile.exists()) {
                logger.info("testLoadDefaultKeyPair[" + algorithm + "]: skip - private key file not found " + prvFile.getAbsolutePath());
                continue;
            }
            
            logger.info("testLoadDefaultKeyPair[" + algorithm + "] validating");
            validateKeyPair(KeyUtils.loadDefaultKeyPair(algorithm, null));
        }
    }

    @Test
    public void testLoadSSH2RSAPublicKey() throws Exception {
        testLoadSSH2PublicKey(RSAKeyDecoder.RSA_ALGORITHM);
    }

    @Test
    public void testLoadSSH2DSSPublicKey() throws Exception {
        testLoadSSH2PublicKey(DSSKeyDecoder.DSS_ALGORITHM);
    }

    private PublicKey testLoadSSH2PublicKey(String algorithm) throws Exception {
        URL url=getClassResource(getClass().getSimpleName() + "-SSH2-" + algorithm + ".txt"); 
        assertNotNull("Missing test file", url);

        PublicKey   key=KeyUtils.loadSSH2PublicKey(url);
        assertNotNull("No key decoded", key);
        assertEquals("Mismatched decoded key algorithm", algorithm, key.getAlgorithm());
        return key;
    }
    
    @Test
    public void testLoadDESEDE3PasswordProtectedRSAPrivateKey() throws Exception {
        testLoadPasswordProtectedPrivateKey(RSAKeyDecoder.RSA_ALGORITHM, "DES-EDE3");
    }

    @Test
    public void testLoadAES128PasswordProtectedRSAPrivateKey() throws Exception {
        testLoadPasswordProtectedPrivateKey(RSAKeyDecoder.RSA_ALGORITHM, "AES-128");
    }

    private KeyPair testLoadPasswordProtectedPrivateKey(String algorithm, String encryption) throws Exception {
        final String    PASSWORD="super secret passphrase";
        URL             url=getClassResource(PASSWORD.replace(' ', '-') + "-" + algorithm + "-" + encryption + "-key");
        assertNotNull("Missing test file", url);
  
        return validateKeyPair(KeyUtils.loadOpenSSHKeyPair(url, PASSWORD));
    }
    
    @Test
    public void testAppendOpenSSHRSAPublicKey() throws Exception {
        for (int keySize : new int[] { 1024, 2048, 3072, 4096 }) {
            KeyPairGenerator    gen=KeyPairGenerator.getInstance(RSAKeyDecoder.RSA_ALGORITHM);
            gen.initialize(keySize);

            KeyPair kp=gen.generateKeyPair();
            testAppendOpenSSHPublicKey(RSAKeyDecoder.RSA_ALGORITHM + "-" + keySize, kp.getPublic());
        }
    }

    @Test
    public void testAppendOpenSSHDSSPublicKey() throws Exception {
        for (int keySize : new int[] { 512, 768, 1024 }) {
            KeyPairGenerator    gen=KeyPairGenerator.getInstance(DSSKeyDecoder.DSS_ALGORITHM);
            gen.initialize(keySize);

            KeyPair kp=gen.generateKeyPair();
            testAppendOpenSSHPublicKey(DSSKeyDecoder.DSS_ALGORITHM + "-" + keySize, kp.getPublic());
        }
    }

    private PublicKey testAppendOpenSSHPublicKey(String msg, PublicKey expected) throws IOException {
        logger.info("testAppendOpenSSHPublicKey(" + msg + ")");

        byte[]      expBytes=expected.getEncoded();
        String      keyData=KeyUtils.appendOpenSSHPublicKey(
                new StringBuilder(expBytes.length), expected, "testAppendOpenSSHPublicKey@" + getClass().getSimpleName()).toString();
        PublicKey   actual=KeyUtils.decodeOpenSSHPublicKey(keyData);
        byte[]      actBytes=actual.getEncoded();
        assertEquals(msg + ": mismatched algorithms", expected.getAlgorithm(), actual.getAlgorithm());
        assertArrayEquals(msg + ": mismatched encodings", expBytes, actBytes);
        return actual;
    }

    @Test
    public void testAppendPEMPrivateKeyRSA() throws Exception {
        for (int keySize : new int[] { 1024, 2048, 3072, 4096 }) {
            KeyPairGenerator    gen=KeyPairGenerator.getInstance(RSAKeyDecoder.RSA_ALGORITHM);
            gen.initialize(keySize);

            KeyPair kp=gen.generateKeyPair();
            testAppendPEMPrivateKey(RSAKeyDecoder.RSA_ALGORITHM + "-" + keySize, kp.getPrivate());
        }
    }

    @Test
    public void testAppendPEMPrivateKeyDSS() throws Exception {
        for (int keySize : new int[] { 512, 768, 1024 }) {
            KeyPairGenerator    gen=KeyPairGenerator.getInstance(DSSKeyDecoder.DSS_ALGORITHM);
            gen.initialize(keySize);

            KeyPair kp=gen.generateKeyPair();
            testAppendPEMPrivateKey(DSSKeyDecoder.DSS_ALGORITHM + "-" + keySize, kp.getPrivate());
        }
    }

    private PrivateKey testAppendPEMPrivateKey(String msg, PrivateKey expected) throws IOException {
        logger.info("testAppendPEMPrivateKey(" + msg + ")");

        byte[]  expBytes=expected.getEncoded();
        String  keyData=KeyUtils.appendPEMPrivateKey(new StringBuilder(expBytes.length), expected, null).toString();
        String  format=expected.getFormat();  
        Reader  rdr=new StringReader(keyData);
        try {
            PrivateKey  actual=KeyUtils.loadPEMPrivateKey(rdr, true, null);
            byte[]      actBytes=actual.getEncoded();
            assertEquals(msg + "[" + format + "]: mismatched algorithms", expected.getAlgorithm(), actual.getAlgorithm());
            assertArrayEquals(msg + "[" + format + "]: mismatched encodings", expBytes, actBytes);
            return actual;
        } finally {
            rdr.close();
        }
    }
    
}
