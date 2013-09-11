/*
 * 
 */
package org.apache.commons.net.ssh.keys;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;

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
    public KeyUtilsTest() {
        super();
    }
    
    @BeforeClass
    public static final void listAllProvidersProperties() throws IOException {
        final Class<?>  ANCHOR=KeyUtilsTest.class;
        File    targetFolder=ensureFolderExists(new File(detectTargetFolder(ANCHOR), ANCHOR.getSimpleName()));    
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
        PublicKey       expected=KeyUtils.decodePublicKey(KEY_DATA);
        PublicKey       actual=KeyUtils.decodePublicKey(RSAKeyDecoder.SSH_RSA + " " + KEY_DATA + "  dummy@dummy");
        assertEquals("Mismatched decoded key algorithm", expected.getAlgorithm(), actual.getAlgorithm());
        assertArrayEquals("Mismatched encoded key data", expected.getEncoded(), actual.getEncoded());
        assertEquals("Mismatched key comparator result", 0, KeyUtils.BY_ALGORITHM_ENCODED_FORM_KEY_COMPARATOR.compare(expected, actual));
    }

    private PrivateKey testLoadPEMPrivateKey(String algorithm) throws Exception {
        URL url=getClassResource(getClass().getSimpleName() + "-" + algorithm + KeyUtils.PEM_KEYFILE_EXT);
        assertNotNull("Missing test file", url);
        
        PrivateKey  key=KeyUtils.loadPEMPrivateKey(url, null);
        assertNotNull("No key extracted", key);
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
        
        KeyPair     kp=KeyUtils.loadKeyPair(url, null);
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
        return validateKeyPair(KeyUtils.loadKeyPair(url, null));
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
            File    pubFile=new File(keysFolder, KeyUtils.getStandardPublicKeyFilename(algorithm));
            File    prvFile=new File(keysFolder, KeyUtils.getStandardPrivateKeyFilename(algorithm));
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
  
        return validateKeyPair(KeyUtils.loadKeyPair(url, PASSWORD));
    }
}
