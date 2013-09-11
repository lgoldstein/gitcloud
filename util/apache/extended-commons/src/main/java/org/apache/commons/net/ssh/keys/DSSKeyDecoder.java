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

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.ExtendedPredicate;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.io.input.ExtendedCloseShieldInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ssh.der.ASN1Object;
import org.apache.commons.net.ssh.der.ASN1Type;
import org.apache.commons.net.ssh.der.DERParser;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 8:50:24 AM
 */
public class DSSKeyDecoder extends AbstractKeyDecoder {
    public static final String  SSH_DSS="ssh-dss", DSS_ALGORITHM="DSA";
    public static final String PEM_DSS_BEGIN_MARKER="-BEGIN DSA PRIVATE KEY-";
    // Note exactly according to standard but good enough
    public static final ExtendedPredicate<String>   BEGIN_MARKER=
            new AbstractExtendedPredicate<String>(String.class) {
                @Override
                public boolean evaluate(String line) {
                    if (StringUtils.isEmpty(line)) {
                        return false;
                    } else if (line.contains(PEM_DSS_BEGIN_MARKER)) {
                        return true;    // debug breakpoint
                    } else {
                        return false;
                    }
                }
            };

    public static final String PEM_DSS_END_MARKER="--END DSA PRIVATE KEY-";
    public static final ExtendedPredicate<String>   END_MARKER=
            new AbstractExtendedPredicate<String>(String.class) {
                @Override
                public boolean evaluate(String line) {
                    if (StringUtils.isEmpty(line)) {
                        return false;
                    } else if (line.contains(PEM_DSS_END_MARKER)) {
                        return true;    // debug breakpoint
                    } else {
                        return false;
                    }
                }
            };

    public static final DSSKeyDecoder   DECODER=new DSSKeyDecoder();

    public DSSKeyDecoder() {
        super(SSH_DSS, DSS_ALGORITHM);
    }

    @Override
    public PublicKey decodePublicKey(InputStream s) throws IOException {
        BigInteger  p=decodeBigInt(s);
        BigInteger  q=decodeBigInt(s);
        BigInteger  g=decodeBigInt(s);
        BigInteger  y=decodeBigInt(s);

        try {
            return generatePublicKey(new DSAPublicKeySpec(y, p, q, g));
        } catch(GeneralSecurityException t) {
            throw new IOException("Failed (" + t.getClass().getSimpleName() + ") to generate key: " + t.getMessage(), t);
        }
    }

    // based on code from http://www.jarvana.com/jarvana/view/org/opensaml/xmltooling/1.3.1/xmltooling-1.3.1-sources.jar!/org/opensaml/xml/security/SecurityHelper.java?format=ok
    @Override
    public PublicKey recoverPublicKey(PrivateKey privateKey) throws GeneralSecurityException {
        if (!(privateKey instanceof DSAPrivateKey)) {
            throw new InvalidKeySpecException("Non-" + DSAPrivateKey.class.getSimpleName() + " key: " + ((privateKey == null) ? null : privateKey.getClass().getSimpleName()));
        }

        DSAPrivateKey       dsaKey=(DSAPrivateKey) privateKey;
        DSAParams           keyParams=dsaKey.getParams();
        BigInteger          p=keyParams.getP(), x=dsaKey.getX(), q=keyParams.getQ();
        BigInteger          g=keyParams.getG(), y=g.modPow(x, p);
        return generatePublicKey(new DSAPublicKeySpec(y, p, q, g));
    }

    @Override
    public Predicate<? super String> getPEMBeginMarker() {
        return BEGIN_MARKER;
    }

    @Override
    public Predicate<? super String> getPEMEndMarker() {
        return END_MARKER;
    }

    @Override
    public PrivateKey decodePEMPrivateKey(InputStream s, boolean okToClose, String password) throws IOException {
        try {
            if (StringUtils.isEmpty(password)) {
                return generatePrivateKey(decodeDSSKeySpec(s, okToClose));
            } else {
                throw new StreamCorruptedException("Decode key with password protection N/A");
            }
        } catch(GeneralSecurityException t) {
            throw new IOException("Failed (" + t.getClass().getSimpleName() + ") to generate key: " + t.getMessage(), t);
        }
    }

    /**
     * <p>The ASN.1 syntax for the private key:</P>
     * <pre>
     * DSAPrivateKey ::= SEQUENCE {
     *      version Version,
     *      p       INTEGER,
     *      q       INTEGER,
     *      g       INTEGER,
     *      y       INTEGER,
     *      x       INTEGER
     * }
     * </pre>
     * @param s The {@link InputStream} containing the encoded bytes
     * @param okToClose <code>true</code> if the method may close the input
     * stream regardless of success or failure
     * @return The recovered {@link DSAPrivateKeySpec}
     * @throws IOException If failed to read or decode the bytes
     */
    public static final DSAPrivateKeySpec decodeDSSKeySpec(InputStream s, boolean okToClose) throws IOException {
        ASN1Object sequence;
        DERParser parser=new DERParser(ExtendedCloseShieldInputStream.resolveInputStream(s, okToClose));
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
            ASN1Object  version=parser.readObject(); // Skip version
            if (version == null) {
                throw new StreamCorruptedException("No version");
            }
            
            BigInteger p=parser.readObject().asInteger();
            BigInteger q=parser.readObject().asInteger();
            BigInteger g=parser.readObject().asInteger();
            @SuppressWarnings("unused")
            BigInteger y=parser.readObject().asInteger();     // don't need it, but have to read it to get to x
            BigInteger x=parser.readObject().asInteger();
            
            return new DSAPrivateKeySpec(x, p, q, g);
        } finally {
            parser.close();
        }
    }
}
