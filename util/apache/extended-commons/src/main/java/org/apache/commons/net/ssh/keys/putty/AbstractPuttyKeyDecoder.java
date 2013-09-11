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

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.ExtendedIOUtils;
import org.apache.commons.io.input.ExtendedCloseShieldInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.net.ssh.keys.AbstractKeyLoader;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 11:19:42 AM
 */
public abstract class AbstractPuttyKeyDecoder extends AbstractKeyLoader implements PuttyKeyDecoder {
    protected AbstractPuttyKeyDecoder(String keyType, String algName) {
        super(keyType, algName);
    }

    @Override
    public PrivateKey decodePrivateKey(String pubData, String prvData, String prvEncryption, String password) throws IOException {
        byte[]  pubBytes=Base64.decodeBase64(pubData);
        byte[]  prvBytes=Base64.decodeBase64(prvData);
        if (StringUtils.isEmpty(prvEncryption)
         || NO_PRIVATE_KEY_ENCRYPTION_VALUE.equalsIgnoreCase(prvEncryption)
         || StringUtils.isEmpty(password)) {
            return decodePrivateKey(pubBytes, prvBytes);
        }
        
        // format is "<cipher><bits>-<mode>" - e.g., "aes256-cbc"
        int pos=prvEncryption.indexOf('-');
        if (pos <= 0) {
            throw new StreamCorruptedException("Missing private key encryption mode in " + prvEncryption);
        }
        
        String  mode=prvEncryption.substring(pos + 1).toUpperCase(), algName=null;
        int     numBits=0;
        for (int index=0; index < pos; index++) {
            char    ch=prvEncryption.charAt(index);
            if ((ch >= '0') && (ch <= '9')) {
                algName = prvEncryption.substring(0, index).toUpperCase();
                numBits = Integer.parseInt(prvEncryption.substring(index, pos)); 
                break;
            }
        }
        
        if (StringUtils.isEmpty(algName) || (numBits <= 0)) {
            throw new StreamCorruptedException("Missing private key encryption algorithm details in " + prvEncryption);
        }
        
        try {
            prvBytes = decodePrivateKeyBytes(prvBytes, algName, numBits, mode, password);
        } catch(GeneralSecurityException e) {
            throw new StreamCorruptedException("Failed (" + e.getClass().getSimpleName() + ")"
                                             + " to decode encrypted key=" + prvEncryption
                                             + ": " + e.getMessage());
        }

        return decodePrivateKey(pubBytes, prvBytes);
    }

    @Override
    public PrivateKey decodePrivateKey(byte[] pubData, byte[] prvData) throws IOException {
        return decodePrivateKey(new ByteArrayInputStream(pubData), true, new ByteArrayInputStream(prvData), true);
    }

    @Override
    public PrivateKey decodePrivateKey(InputStream pubStream, boolean okToClosePub, InputStream prvStream, boolean okToClosePrv)
            throws IOException {
        PuttyKeyReader  pubReader=null, prvReader=null;
        try {
            pubReader = new PuttyKeyReader(ExtendedCloseShieldInputStream.resolveInputStream(pubStream, okToClosePub));
            prvReader = new PuttyKeyReader(ExtendedCloseShieldInputStream.resolveInputStream(prvStream, okToClosePrv));
            return decodePrivateKey(pubReader, prvReader);
        } finally {
            ExtendedIOUtils.closeAll(pubReader, prvReader);
        }
    }

    protected byte[] decodePrivateKeyBytes(byte[] prvBytes, String algName, int numBits, String algMode, String password)
            throws GeneralSecurityException {
        Validate.notNull(prvBytes, "No encrypted key bytes", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notEmpty(algName, "No encryption algorithm", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.isTrue(numBits > 0, "Invalid encryption key size: %s", numBits);
        Validate.notEmpty(algMode, "No encryption mode", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notEmpty(password, "No encryption password", ArrayUtils.EMPTY_OBJECT_ARRAY);

        if ("AES".equalsIgnoreCase(algName)) {
            return decodePrivateKeyBytes(prvBytes, algName, algMode, numBits, new byte[16], toEncryptionKey(password));
        } else {
            throw new NoSuchAlgorithmException("decodePrivateKeyBytes(" + algName + "-" + numBits + "-" + algMode + ") N/A");
        }
    }

    protected byte[] decodePrivateKeyBytes(byte[] encBytes, String cipherName, String cipherMode, int numBits, byte[] initVector, byte[] keyValue)
            throws GeneralSecurityException {
        String  xform=cipherName + "/" + cipherMode + "/NoPadding";
        int     maxAllowedBits=Cipher.getMaxAllowedKeyLength(xform);
        // see http://www.javamex.com/tutorials/cryptography/unrestricted_policy_files.shtml
        if (numBits > maxAllowedBits) {
            throw new InvalidKeySpecException("decodePrivateKeyBytes(" + xform + ")"
                                            + " required key length (" + numBits + ")"
                                            + " exceeds max. available: " + maxAllowedBits);
        }

        SecretKeySpec   skeySpec=new SecretKeySpec(keyValue, cipherName);
        IvParameterSpec ivspec=new IvParameterSpec(initVector);
        Cipher          cipher=Cipher.getInstance(xform);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
        
        return cipher.doFinal(encBytes);
    }

    /**
     * Converts a pass-phrase into a key, by following the convention that PuTTY uses.
     * Used to decrypt the private key when it's encrypted.
     * @param passphrase the Password to be used as seed for the key - ignored
     * if {@code null}/empty
     * @return The encryption key bytes - {@code null} if no pass-phrase
     */
    public static final byte[] toEncryptionKey(String passphrase) {
        if (StringUtils.isEmpty(passphrase)) {
            return null;
        }

        MessageDigest digest = DigestUtils.getSha1Digest();
        digest.update(new byte[]{ 0, 0, 0, 0});
        digest.update(passphrase.getBytes());
        byte[] key1 = digest.digest();

        digest.update(new byte[]{ 0, 0, 0, 1});
        digest.update(passphrase.getBytes());
        byte[] key2 = digest.digest();

        byte[] r = new byte[32];
        System.arraycopy(key1, 0, r,  0, 20);
        System.arraycopy(key2, 0, r, 20, 12);

        return r;
    }

    protected abstract PrivateKey decodePrivateKey(PuttyKeyReader pubReader, PuttyKeyReader prvReader) throws IOException;
    /**
     * Helper class for {@link PuttyKeyDecoder}s
     */
    protected static class PuttyKeyReader implements Closeable {
        private final DataInputStream di;

        PuttyKeyReader(InputStream s) {
            di = new DataInputStream(s);
        }

        public void skip() throws IOException {
            di.skipBytes(di.readInt());
        }

        private byte[] read() throws IOException {
            int len = di.readInt();
            byte[] r = new byte[len];
            di.readFully(r);
            return r;
        }

        public BigInteger readInt() throws IOException {
            return new BigInteger(read());
        }
        
        @Override
        public void close() throws IOException {
            di.close();
        }
    }
}
