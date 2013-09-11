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
