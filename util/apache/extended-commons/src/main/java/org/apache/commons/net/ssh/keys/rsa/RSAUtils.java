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

package org.apache.commons.net.ssh.keys.rsa;

import java.math.BigInteger;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * Some useful RSA related utilities
 * @author Lyor G.
 * @since Sep 16, 2013 8:22:06 AM
 */
public class RSAUtils {
    /**
     * @param key The {@link RSAKey}
     * @return The number of bytes required to store the modulus of this RSA key
     * @see #getRequiredDataSize(BigInteger)
     */
    public static int getRequiredDataSize(RSAKey key) {
        return getRequiredDataSize(Validate.notNull(key, "No key", ArrayUtils.EMPTY_OBJECT_ARRAY).getModulus());
    }

    /**
     * @param n The RSA key modulus
     * @return The number of bytes required to store the magnitude {@code byte[]}
     * of the modulus value
     */
    public static final int getRequiredDataSize(BigInteger n) {
        int bitSize = Validate.notNull(n, "No modulus", ArrayUtils.EMPTY_OBJECT_ARRAY).bitLength();
        return (bitSize + 7) >> 3;
    }

    public static final BigInteger rawEncrypt(RSAPublicKey key, byte[] plainText, int off, int len) {
        Validate.notNull(key, "No key", ArrayUtils.EMPTY_OBJECT_ARRAY);
        return rawEncrypt(key.getModulus(), key.getPublicExponent(), plainText, off, len);
    }

    public static final BigInteger rawEncrypt(RSAPublicKey key, byte ... plainText) {
        Validate.notNull(key, "No key", ArrayUtils.EMPTY_OBJECT_ARRAY);
        return rawEncrypt(key.getModulus(), key.getPublicExponent(), plainText);
    }

    public static final BigInteger rawEncrypt(RSAPublicKey key, BigInteger msg) {
        Validate.notNull(key, "No key", ArrayUtils.EMPTY_OBJECT_ARRAY);
        return rawEncrypt(key.getModulus(), key.getPublicExponent(), msg);
    }

    public static final BigInteger rawEncrypt(BigInteger n, BigInteger e, byte[] plainText, int off, int len) {
        Validate.notNull(plainText, "No plain text", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.isTrue(len > 0, "Invalid plain-text size: %d", len);
        Validate.isTrue(off >= 0, "Invalid plain-text offset: %d", off);

        if ((off == 0) && (len == plainText.length)) {
            return rawEncrypt(n, e, plainText);
        }
        
        byte[]  bytes=new byte[len];
        System.arraycopy(plainText, off, bytes, 0, len);
        return rawEncrypt(n, e, bytes);
    }

    public static final BigInteger rawEncrypt(BigInteger n, BigInteger e, byte ... plainText) {
        Validate.notNull(plainText, "No plain text", ArrayUtils.EMPTY_OBJECT_ARRAY);
        return rawEncrypt(n, e, new BigInteger(1, plainText));
    }
    
    public static final BigInteger rawEncrypt(BigInteger n, BigInteger e, BigInteger msg) {
        Validate.notNull(n, "No modulus", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(e, "No exponent", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(msg, "No message", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.isTrue(msg.compareTo(n) < 0, "Message is larger than modulus", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        return msg.modPow(e, n);
    }

    public static final BigInteger rawDecrypt(RSAPrivateCrtKey key, byte ... cipherText) {
        Validate.notNull(cipherText, "No cipher text", ArrayUtils.EMPTY_OBJECT_ARRAY);
        return rawDecrypt(key, new BigInteger(1, cipherText));
    }

    public static final BigInteger rawDecrypt(RSAPrivateCrtKey key, BigInteger c) {
        Validate.notNull(key, "No key", ArrayUtils.EMPTY_OBJECT_ARRAY);
        return rawDecrypt(key.getModulus(), key.getPrimeP(), key.getPrimeQ(), key.getPrimeExponentP(), key.getPrimeExponentQ(), key.getCrtCoefficient(), c);
    }

    public static final BigInteger rawDecrypt(RSAPrivateCrtKey key, byte[] cipherText, int off, int len) {
        Validate.notNull(key, "No key", ArrayUtils.EMPTY_OBJECT_ARRAY);
        return rawDecrypt(key.getModulus(), key.getPrimeP(), key.getPrimeQ(), key.getPrimeExponentP(), key.getPrimeExponentQ(), key.getCrtCoefficient(), cipherText, off, len);
    }

    public static final BigInteger rawDecrypt(
            BigInteger n, BigInteger p, BigInteger q, BigInteger dP, BigInteger dQ, BigInteger qInv, byte[] cipherText, int off, int len) {
        Validate.notNull(cipherText, "No cipher text", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.isTrue(len > 0, "Invalid cipher-text size: %d", len);
        Validate.isTrue(off >= 0, "Invalid cipher-text offset: %d", off);
        
        if ((off == 0) && (len == cipherText.length)) {
            return rawDecrypt(n, p, q, dP, dQ, qInv, cipherText);
        }

        byte[]  bytes=new byte[len];
        System.arraycopy(cipherText, off, bytes, 0, len);
        return rawDecrypt(n, p, q, dP, dQ, qInv, bytes);
    }


    public static final BigInteger rawDecrypt(BigInteger n, BigInteger p, BigInteger q, BigInteger dP, BigInteger dQ, BigInteger qInv, byte ... cipherText) {
        Validate.notNull(cipherText, "No cipher text", ArrayUtils.EMPTY_OBJECT_ARRAY);
        return rawDecrypt(n, p, q, dP, dQ, qInv, new BigInteger(1, cipherText));
    }

    public static final BigInteger rawDecrypt(BigInteger n, BigInteger p, BigInteger q, BigInteger dP, BigInteger dQ, BigInteger qInv, BigInteger c) {
        Validate.notNull(n, "No modulus", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(p, "No p-value", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(q, "No q-value", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(dP, "No dP-value", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(dQ, "No dQ-value", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(qInv, "No qInv-value", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(c, "No ciphertext", ArrayUtils.EMPTY_OBJECT_ARRAY);

        // m1 = c ^ dP mod p
        BigInteger m1 = c.modPow(dP, p);
        // m2 = c ^ dQ mod q
        BigInteger m2 = c.modPow(dQ, q);

        // h = (m1 - m2) * qInv mod p
        BigInteger mtmp = m1.subtract(m2);
        if (mtmp.signum() < 0) {
            mtmp = mtmp.add(p);
        }
        BigInteger h = mtmp.multiply(qInv).mod(p);

        // m = m2 + q * h
        return h.multiply(q).add(m2);
    }
    /*
     * format: 00 || BT || PS || 00 || D
     * 
     * where:   BT - block type octet
     *          PS - padding octets
     *          D  - the original data
     * see <A HREF="http://tools.ietf.org/html/rfc2313">PKCS #1: RSA Encryption - Version 1.5</A>
     * NOTE: specifying Random only as a base class - the SecureRandom should be used...
     */
    public static final byte[] pkcsV15PadType2(Random r, int paddedSize, byte ... data) {
        return pkcsV15PadType2(r, paddedSize, data, 0, data.length);
    }

    public static final byte PAD_V15_BLOCKTYPE_2=2;
    public static final byte[] pkcsV15PadType2(Random r, int paddedSize, byte[] data, int off, int len) {
        Validate.notNull(r, "No randomizer", ArrayUtils.EMPTY_OBJECT_ARRAY);
        // ensure at least the zero + block type + padding + zero
        Validate.isTrue(paddedSize > (len + 4), "Invalid padded size: %d", paddedSize);
        
        byte[]  padded=new byte[paddedSize];
        padded[0] = 0;
        padded[1] = PAD_V15_BLOCKTYPE_2;
        
        byte[]  randomBytes=new byte[Byte.MAX_VALUE];
        int     padSize=paddedSize - len - 3, rbIndex=randomBytes.length;
        /*
         *  We use a local "scratch pad" of random bytes to avoid repeated
         *  calls to the randomizer
         */
        r.nextBytes(randomBytes);
        for (int    index=2; padSize > 0; index++, padSize--) {
            for (rbIndex++ ; (rbIndex >= randomBytes.length) || (randomBytes[rbIndex] == 0); rbIndex++) {
                if (rbIndex >= randomBytes.length) {
                    r.nextBytes(randomBytes);
                    rbIndex = 0;
                }
            }
            
            padded[index] = randomBytes[rbIndex];
        }

        padded[paddedSize - len - 1] = 0;
        System.arraycopy(data, off, padded, paddedSize - len, len);
        return padded;
    }
}
