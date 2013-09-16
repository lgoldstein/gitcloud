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

package org.apache.commons.net.ssh.keys.rsa;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.ExtendedPredicate;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.io.input.ExtendedCloseShieldInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ssh.der.ASN1Object;
import org.apache.commons.net.ssh.der.ASN1Type;
import org.apache.commons.net.ssh.der.DERParser;
import org.apache.commons.net.ssh.keys.AbstractKeyDecoder;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 8:35:57 AM
 */
public class RSAKeyDecoder extends AbstractKeyDecoder {
    public static final String  SSH_RSA="ssh-rsa", RSA_ALGORITHM="RSA";

    // Note exactly according to standard but good enough
    public static final String PEM_RSA_BEGIN_MARKER="-BEGIN RSA PRIVATE KEY-";
    public static final ExtendedPredicate<String>   BEGIN_MARKER=
            new AbstractExtendedPredicate<String>(String.class) {
                @Override
                public boolean evaluate(String line) {
                    if (StringUtils.isEmpty(line)) {
                        return false;
                    } else if (line.contains(PEM_RSA_BEGIN_MARKER)) {
                        return true;    // debug breakpoint
                    } else {
                        return false;
                    }
                }
            };

    public static final String PEM_RSA_END_MARKER="-END RSA PRIVATE KEY-";
    public static final ExtendedPredicate<String>   END_MARKER=
            new AbstractExtendedPredicate<String>(String.class) {
                @Override
                public boolean evaluate(String line) {
                    if (StringUtils.isEmpty(line)) {
                        return false;
                    } else if (line.contains(PEM_RSA_END_MARKER)) {
                        return true;    // debug breakpoint
                    } else {
                        return false;
                    }
                }
            };

    public static final RSAKeyDecoder   DECODER=new RSAKeyDecoder();

    public RSAKeyDecoder() {
        super(SSH_RSA, RSA_ALGORITHM);
    }

    /**
     * <p>The ASN.1 syntax for the private key as per RFC-3447 section A.1.1:</P>
     * <pre>
     * RSAPublicKey ::= SEQUENCE {
     *      modulus           INTEGER,  -- n
     *      publicExponent    INTEGER   -- e
     * }
     * </pre>
     * @param s The {@link InputStream}
     * @return The decoded {@link PublicKey}
     */
    @Override
    public PublicKey decodePublicKey(InputStream s) throws IOException {
        BigInteger  e=decodeBigInt(s);
        BigInteger  n=decodeBigInt(s);
        try {
            return generatePublicKey(new RSAPublicKeySpec(n, e));
        } catch(GeneralSecurityException t) {
            throw new IOException("Failed (" + t.getClass().getSimpleName() + ") to generate key: " + t.getMessage(), t);
        }
    }

    @Override
    public PublicKey recoverPublicKey(PrivateKey privateKey) throws GeneralSecurityException {
        if (!(privateKey instanceof RSAPrivateCrtKey)) {
            throw new InvalidKeySpecException("Non-" + RSAPrivateCrtKey.class.getSimpleName() + " key: " + ((privateKey == null) ? null : privateKey.getClass().getSimpleName()));
        }

        RSAPrivateCrtKey    rsaKey=(RSAPrivateCrtKey) privateKey;
        BigInteger          p=rsaKey.getPrimeP(), q=rsaKey.getPrimeQ();
        BigInteger          n=p.multiply(q), e=rsaKey.getPublicExponent();
        return generatePublicKey(new RSAPublicKeySpec(n, e));
    }

    @Override
    public PrivateKey decodePEMPrivateKey(InputStream s, boolean okToClose, String password) throws IOException {
        try {
            if (StringUtils.isEmpty(password)) {
                return generatePrivateKey(decodeRSAKeySpec(s, okToClose));
            } else {
                throw new StreamCorruptedException("Decode key with password protection N/A");
            }
        } catch(GeneralSecurityException t) {
            throw new IOException("Failed (" + t.getClass().getSimpleName() + ") to generate key: " + t.getMessage(), t);
        }
    }

    @Override
    public Predicate<? super String> getPEMBeginMarker() {
        return BEGIN_MARKER;
    }

    @Override
    public Predicate<? super String> getPEMEndMarker() {
        return END_MARKER;
    }

    /**
     * <p>The ASN.1 syntax for the private key as per RFC-3447 section A.1.2:</P>
     * <pre>
     * RSAPrivateKey ::= SEQUENCE {
     *   version           Version, 
     *   modulus           INTEGER,  -- n
     *   publicExponent    INTEGER,  -- e
     *   privateExponent   INTEGER,  -- d
     *   prime1            INTEGER,  -- p
     *   prime2            INTEGER,  -- q
     *   exponent1         INTEGER,  -- d mod (p-1)
     *   exponent2         INTEGER,  -- d mod (q-1) 
     *   coefficient       INTEGER,  -- (inverse of q) mod p
     *   otherPrimeInfos   OtherPrimeInfos OPTIONAL 
     * }
     * </pre>
     * @param s The {@link InputStream} containing the encoded bytes
     * @param okToClose <code>true</code> if the method may close the input
     * stream regardless of success or failure
     * @return The recovered {@link RSAPrivateCrtKeySpec}
     * @throws IOException If failed to read or decode the bytes
     */
    public static final RSAPrivateCrtKeySpec decodeRSAKeySpec(InputStream s, boolean okToClose) throws IOException {
        DERParser parser=new DERParser(ExtendedCloseShieldInputStream.resolveInputStream(s, okToClose));
        ASN1Object sequence;
        try {
            sequence = parser.readObject();
        } finally {
            parser.close();
        }

        if (!ASN1Type.SEQUENCE.equals(sequence.getObjType())) {
            throw new IOException("Invalid DER: not a sequence: " + sequence.getObjType());
        }
        
        // Parse inside the sequence
        parser = sequence.createParser();
        try {
            ASN1Object  versionObject=parser.readObject(); // Skip version
            if (versionObject == null) {
                throw new StreamCorruptedException("No version");
            }

            BigInteger  version=versionObject.asInteger();
            if (!BigInteger.ZERO.equals(version)) { // as per RFC-3447 section A.1.2
                throw new StreamCorruptedException("Multi-primes N/A");
            }

            BigInteger modulus=parser.readObject().asInteger();
            BigInteger publicExp=parser.readObject().asInteger();
            BigInteger privateExp=parser.readObject().asInteger();
            BigInteger prime1=parser.readObject().asInteger();
            BigInteger prime2=parser.readObject().asInteger();
            BigInteger exp1=parser.readObject().asInteger();
            BigInteger exp2=parser.readObject().asInteger();
            BigInteger crtCoef=parser.readObject().asInteger();
            
            return new RSAPrivateCrtKeySpec(modulus, publicExp, privateExp, prime1, prime2, exp1, exp2, crtCoef);
        } finally {
            parser.close();
        }
    }
}
