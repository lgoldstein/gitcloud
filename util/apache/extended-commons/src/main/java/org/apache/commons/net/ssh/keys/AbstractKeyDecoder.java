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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 8:34:19 AM
 */
public abstract class AbstractKeyDecoder extends AbstractKeyLoader implements KeyDecoder {
    protected AbstractKeyDecoder(String keyType, String algName) {
        super(keyType, algName);
    }

    @Override
    public PrivateKey decodePEMPrivateKey(BufferedReader rdr, String password) throws IOException {
        byte[]  keyBytes=readTillPEMEndMarker(rdr, getPEMEndMarker(), password);
        return decodePEMPrivateKey(keyBytes, null /* readTillPEMEndMarker takes care of decoding the bytes */);
    }

    @Override
    public PrivateKey decodePEMPrivateKey(String keyData, String password) throws IOException {
        return decodePEMPrivateKey(Base64.decodeBase64(keyData), password);
    }

    @Override
    public PrivateKey decodePEMPrivateKey(byte[] keyBytes, String password) throws IOException {
        return decodePEMPrivateKey(keyBytes, 0, keyBytes.length, password);
    }

    @Override
    public PrivateKey decodePEMPrivateKey(byte[] keyBytes, int off, int len, String password) throws IOException {
        return decodePEMPrivateKey(new ByteArrayInputStream(keyBytes, off, len), true, password);
    }

    protected byte[] readTillPEMEndMarker(BufferedReader rdr, Predicate<? super String> marker, String password)
            throws IOException {
        Boolean         encrypted=null;
        byte[]          initVector=null;
        String          algInfo=null;
        StringBuilder   writer=new StringBuilder(2048);
        for (String    line=rdr.readLine(); line != null; line=rdr.readLine()) {
            line = line.trim();
            
            int headerPos=line.indexOf(':');
            if (headerPos > 0) {
                if (StringUtils.isEmpty(password)) {
                    continue;
                } else if (line.startsWith("Proc-Type: 4,ENCRYPTED")) {
                    if (encrypted != null) {
                        throw new StreamCorruptedException("Multiple encryption indicators");
                    }
                    
                    encrypted = Boolean.TRUE;
                } else if (line.startsWith("DEK-Info:")) {
                    if ((initVector != null) || (algInfo != null)) {
                        throw new StreamCorruptedException("Multiple encryption settings");
                    }

                    line = line.substring(headerPos + 1).trim();
                    
                    if ((headerPos=line.indexOf(',')) < 0) {
                        throw new StreamCorruptedException("Missing encryption data values separator: " + line);
                    }
                    
                    algInfo = line.substring(0, headerPos).trim();

                    try {
                        String  algInitVector=line.substring(headerPos + 1).trim();
                        initVector = Hex.decodeHex(algInitVector.toCharArray());
                    } catch(DecoderException e) {
                        throw new StreamCorruptedException("Failed to decode encryption data vector (" + line + "): " + e.getMessage());
                    }
                }

                continue;   // skip headers
            }

            if (marker.evaluate(line)) {
                byte[]  keyBytes=Base64.decodeBase64(writer.toString());

                if ((encrypted != null) || (algInfo != null) || (initVector != null)) {
                    try {
                        keyBytes = decryptPrivateKeyData(keyBytes, algInfo, initVector, password);
                    } catch(GeneralSecurityException e) {
                        throw new StreamCorruptedException("Failed (" + e.getClass().getSimpleName() + ")"
                                                         + " to decrypt private key data: " + e.getMessage());
                    }
                }
                
                return keyBytes;
            }
            
            writer.append(line);
        }

        throw new StreamCorruptedException("Data exhausted while looking for end marker");
    }

    protected byte[] decryptPrivateKeyData(byte[] encBytes, String algInfo, byte[] initVector, String password)
            throws GeneralSecurityException {
        Validate.notNull(encBytes, "No encrypted data", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notEmpty(algInfo, "No encryption algorithm data", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(initVector, "No encryption init vector", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notEmpty(password, "No encryption password", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        String[]    cipherData=StringUtils.split(algInfo, '-');
        if (ArrayUtils.getLength(cipherData) != 3) {
            throw new IllegalArgumentException("Bad encryption alogrithm data: " + algInfo);
        }
        
        // TODO decrypt the private key data
        String  cipherName=cipherData[0], cipherType=cipherData[1], cipherMode=cipherData[2];
        if ("AES".equalsIgnoreCase(cipherName)) {
            return decryptAESPrivateKey(encBytes, cipherName, cipherType, cipherMode, initVector, password);
        } else if ("DES".equalsIgnoreCase(cipherName)) {
            return decryptDESPrivateKey(encBytes, cipherName, cipherType, cipherMode, initVector, password);
        } else {
            throw new NoSuchAlgorithmException("decryptPrivateKeyData(" + algInfo + ") unknown cipher: " + cipherName);
        }
    }

    // see http://openssl.6102.n7.nabble.com/DES-EDE3-CBC-technical-details-td24883.html
    protected byte[] decryptDESPrivateKey(byte[] encBytes, String cipherName, String cipherType, String cipherMode, byte[] initVector, String password)
            throws GeneralSecurityException {
        MessageDigest   md5=DigestUtils.getMd5Digest();
        Validate.notNull(encBytes, "No encrypted data", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notEmpty(cipherType, "No cipher type", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(initVector, "No encryption init vector", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notEmpty(password, "No encryption password", ArrayUtils.EMPTY_OBJECT_ARRAY);

        byte[]   passBytes=password.getBytes();
        byte[]  d1Input=new byte[passBytes.length + initVector.length];
        System.arraycopy(passBytes, 0, d1Input, 0, passBytes.length);
        System.arraycopy(initVector, 0, d1Input, passBytes.length, initVector.length);
        md5.reset();

        byte[]  d1Value=md5.digest(d1Input);
        byte[]  d2Input=new byte[d1Value.length + d1Input.length];
        System.arraycopy(d1Value, 0, d2Input, 0, d1Value.length);
        System.arraycopy(d1Input, 0, d2Input, d1Value.length, d1Input.length);
        md5.reset();

        byte[]  d2Value=md5.digest(d2Input);
        byte[]  keyValue=new byte[24 /* hardwired size for 3DES */];
        System.arraycopy(d1Value, 0, keyValue, 0, d1Value.length);
        System.arraycopy(d2Value, 0, keyValue, d1Value.length, keyValue.length - d1Value.length);

        String  effName="EDE3".equalsIgnoreCase(cipherType) ? (cipherName + "ede") : cipherName;
        return decryptPrivateKey(encBytes, effName, cipherMode, keyValue.length * Byte.SIZE, keyValue, initVector);
    }

    // see http://martin.kleppmann.com/2013/05/24/improving-security-of-ssh-private-keys.html
    protected byte[] decryptAESPrivateKey(byte[] encBytes, String cipherName, String cipherType, String cipherMode, byte[] initVector, String password)
            throws GeneralSecurityException {
        Validate.notNull(encBytes, "No encrypted data", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notEmpty(cipherType, "No cipher type", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(initVector, "No encryption init vector", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.isTrue(initVector.length >= 8, "Bad AES init vector length: %s", initVector.length);
        Validate.notEmpty(password, "No encryption password", ArrayUtils.EMPTY_OBJECT_ARRAY);

        byte[]  passBytes=password.getBytes();
        byte[]  digestInput=new byte[passBytes.length + 8];
        System.arraycopy(passBytes, 0, digestInput, 0, passBytes.length);
        System.arraycopy(initVector, 0, digestInput, passBytes.length, 8);

        MessageDigest   md5=DigestUtils.getMd5Digest();
        byte[]          keyValue=md5.digest(digestInput);
        return decryptPrivateKey(encBytes, cipherName, cipherMode, Integer.parseInt(cipherType), keyValue, initVector);
    }

    protected byte[] decryptPrivateKey(byte[] encBytes, String cipherName, String cipherMode, int numBits, byte[] keyValue, byte[] initVector)
            throws GeneralSecurityException {
        String  xform=cipherName + "/" + cipherMode + "/NoPadding";
        int     maxAllowedBits=Cipher.getMaxAllowedKeyLength(xform);
        // see http://www.javamex.com/tutorials/cryptography/unrestricted_policy_files.shtml
        if (numBits > maxAllowedBits) {
            throw new InvalidKeySpecException("decryptPrivateKey(" + xform + ")"
                                            + " required key length (" + numBits + ")"
                                            + " exceeds max. available: " + maxAllowedBits);
        }

        SecretKeySpec   skeySpec=new SecretKeySpec(keyValue, cipherName);
        IvParameterSpec ivspec=new IvParameterSpec(initVector);
        Cipher          cipher=Cipher.getInstance(xform);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
        
        return cipher.doFinal(encBytes);
    }

    public static final String decodeString(InputStream s) throws IOException {
        return new String(readRLEBytes(s));
    }

    public static final BigInteger decodeBigInt(InputStream s) throws IOException {
        return new BigInteger(readRLEBytes(s));
    }

    public static final byte[] readRLEBytes(InputStream s) throws IOException {
        int     len=decodeInt(s);
        byte[]  bytes=new byte[len];
        IOUtils.readFully(s, bytes);
        return bytes;
    }

    public static final int decodeInt(InputStream s) throws IOException {
        byte[]  bytes={ 0, 0, 0, 0 };
        IOUtils.readFully(s, bytes);
        return ((bytes[0] & 0xFF) << 24)
             | ((bytes[1] & 0xFF) << 16)
             | ((bytes[2] & 0xFF) << 8)
             | (bytes[3] & 0xFF);
    }
}
