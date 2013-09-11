/*
 * 
 */
package org.apache.commons.net.ssh.keys;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.commons.codec.signatures.SignatureUtils;
import org.apache.commons.test.AbstractTestSupport;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 11:43:37 AM
 */
public abstract class AbstractSSHKeysTestSupport extends AbstractTestSupport {
    protected AbstractSSHKeysTestSupport() {
        super();
    }

    public static final KeyPair validateKeyPair(KeyPair kp) throws GeneralSecurityException {
        return validateKeyPair(kp, new byte[] { 3, 7, 7, 7, 3, 4, 7});
    }

    public static final KeyPair validateKeyPair(KeyPair kp, byte[] data) throws GeneralSecurityException {
        assertNotNull("No pair", kp);
        validateKeyPair(kp.getPublic(), kp.getPrivate(), data);
        return kp;
    }
    
    public static final void validateKeyPair(PublicKey pubKey, PrivateKey prvKey, byte ... data) throws GeneralSecurityException {
        assertNotNull("No public key", pubKey);
        assertNotNull("No private key", prvKey);
        assertEquals("Mismatched algorithms", pubKey.getAlgorithm(), prvKey.getAlgorithm());
        
        byte[]  signed=SignatureUtils.signData(prvKey, data);
        assertTrue("Failed to verify signature", SignatureUtils.verifySignature(pubKey, data, signed));
    }
}
