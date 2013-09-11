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

package org.apache.commons.codec.signatures;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.nio.CharBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.NullWriter;

/**
 * @author Lyor G.
 * @since Jul 9, 2013 9:06:40 AM
 */
public class SignatureUtils {
    /**
     * @param prvKey The {@link PrivateKey} to be used to sign the data
     * @param data The data to be signed
     * @return The signed data bytes
     * @throws GeneralSecurityException If bad key or failed to sign
     */
    public static final byte[] signData(PrivateKey prvKey, byte ... data) throws GeneralSecurityException {
        Signature signer=getInstance(prvKey);
        signer.initSign(prvKey);
        signer.update(data);
        return signer.sign();
    }

    /**
     * @param prvKey The {@link PrivateKey} to be used to sign the data
     * @param file The {@link File} whose contents are to be signed
     * @return The signed data bytes
     * @throws IOException If failed to read data or bad key or failed to sign
     * @see #signData(PrivateKey, InputStream)
     */
    public static final byte[] signData(PrivateKey prvKey, File file) throws IOException {
        InputStream s=new FileInputStream(file);
        try {
            return signData(prvKey, s);
        } finally {
            s.close();
        }
    }

    /**
     * @param prvKey The {@link PrivateKey} to be used to sign the data
     * @param url The {@link URL} of the data to be signed
     * @return The signed data bytes
     * @throws IOException If failed to read data or bad key or failed to sign
     * @see #signData(PrivateKey, InputStream)
     */
    public static final byte[] signData(PrivateKey prvKey, URL url) throws IOException {
        InputStream s=url.openStream();
        try {
            return signData(prvKey, s);
        } finally {
            s.close();
        }
    }

    /**
     * @param prvKey The {@link PrivateKey} to be used to sign the data
     * @param s The {@link InputStream} data to be signed
     * @return The signed data bytes
     * @throws IOException If failed to read data or bad key or failed to sign
     */
    public static final byte[] signData(PrivateKey prvKey, InputStream s) throws IOException {
        SignerOutputStream  out;
        try {
            out = new SignerOutputStream(getInstance(prvKey), prvKey, NullOutputStream.NULL_OUTPUT_STREAM);
        } catch(GeneralSecurityException e) {
            throw new StreamCorruptedException("Bad (" + e.getClass().getSimpleName() + ") private key: " + e.getMessage());
        }
        
        try {
            long    cpySize=IOUtils.copyLarge(s, out);
            if (cpySize < 0L) {
                throw new StreamCorruptedException("Bad copy size: " + cpySize);
            }
        } finally {
            out.close();
        }
        
        try {
            return out.sign();
        } catch(SignatureException e) {
            throw new StreamCorruptedException("Failed (" + e.getClass().getSimpleName() + ") to sign: " + e.getMessage());
        }
    }

    /**
     * @param prvKey The {@link PrivateKey} to be used to sign the data
     * @param chars The characters to be signed
     * @return The signed data bytes
     * @throws IOException If failed to sign data or bad key or failed to sign
     */
    public static final byte[] signData(PrivateKey prvKey, char ... chars) throws IOException {
        return signData(prvKey, chars, 0, chars.length);
    }
    
    /**
     * @param prvKey The {@link PrivateKey} to be used to sign the data
     * @param chars The characters to be signed
     * @param offset Offset to start signing
     * @param len Number of characters to sign
     * @return The signed data bytes
     * @throws IOException If failed to sign data or bad key or failed to sign
     */
    public static final byte[] signData(PrivateKey prvKey, char[] chars, int offset, int len) throws IOException {
        return signData(prvKey, CharBuffer.wrap(chars, offset, len));
    }

    /**
     * @param prvKey The {@link PrivateKey} to be used to sign the data
     * @param csq The {@link CharSequence} of characters to be signed
     * @return The signed data bytes
     * @throws IOException If failed to sign data or bad key or failed to sign
     */
    public static final byte[] signData(PrivateKey prvKey, CharSequence csq) throws IOException {
        return signData(prvKey, csq, 0, csq.length());
    }

    /**
     * @param prvKey The {@link PrivateKey} to be used to sign the data
     * @param csq The {@link CharSequence} of characters to be signed
     * @param start The start offset to sign (inclusive)
     * @param end The end offset to sign (exclusive)
     * @return The signed data bytes
     * @throws IOException If failed to sign data or bad key or failed to sign
     */
    public static final byte[] signData(PrivateKey prvKey, CharSequence csq, int start, int end) throws IOException {
        CharSequenceReader  rdr=new CharSequenceReader(csq.subSequence(start, end));
        try {
            return signData(prvKey, rdr);
        } finally {
            rdr.close();
        }
    }

    /**
     * @param prvKey The {@link PrivateKey} to be used to sign the data
     * @param r The {@link Reader} data to be signed
     * @return The signed data bytes
     * @throws IOException If failed to read data or bad key or failed to sign
     */
    public static final byte[] signData(PrivateKey prvKey, Reader r) throws IOException {
        SignerWriter    out;
        try {
            out = new SignerWriter(getInstance(prvKey), prvKey, NullWriter.NULL_WRITER);
        } catch(GeneralSecurityException e) {
            throw new StreamCorruptedException("Bad (" + e.getClass().getSimpleName() + ") private key: " + e.getMessage());
        }
        
        try {
            long    cpySize=IOUtils.copyLarge(r, out);
            if (cpySize < 0L) {
                throw new StreamCorruptedException("Bad copy size: " + cpySize);
            }
        } finally {
            out.close();
        }
        
        try {
            return out.sign();
        } catch(SignatureException e) {
            throw new StreamCorruptedException("Failed (" + e.getClass().getSimpleName() + ") to sign: " + e.getMessage());
        }
    }

    /**
     * @param pubKey The {@link PublicKey} to use for verifying the signature
     * @param data The data whose signature is to be verified
     * @param signature The expected signature bytes
     * @return <code>true</code> if signature matches expected
     * @throws GeneralSecurityException If bad key or failed to encode/decode data
     */
    public static final boolean verifySignature(PublicKey pubKey, byte[] data, byte[] signature)
            throws GeneralSecurityException {
        Signature signer=getInstance(pubKey);
        signer.initVerify(pubKey);
        signer.update(data);
        return signer.verify(signature);
    }

    /**
     * @param pubKey The {@link PublicKey} to use for verifying the signature
     * @param file The {@link File} whose contents signature is to be verified
     * @param signature The expected signature bytes
     * @return <code>true</code> if signature matches expected
     * @throws IOException If failed to read data or bad key or failed to encode/decode data
     * @see #verifySignature(PublicKey, InputStream, byte...)
     */
    public static final boolean verifySignature(PublicKey pubKey, File file, byte ... signature) throws IOException {
        InputStream s=new FileInputStream(file);
        try {
            return verifySignature(pubKey, s, signature);
        } finally {
            s.close();
        }
    }

    /**
     * @param pubKey The {@link PublicKey} to use for verifying the signature
     * @param url The {@link URL} of the data contents whose signature is to be verified
     * @param signature The expected signature bytes
     * @return <code>true</code> if signature matches expected
     * @throws IOException If failed to read data or bad key or failed to encode/decode data
     * @see #verifySignature(PublicKey, InputStream, byte...)
     */
    public static final boolean verifySignature(PublicKey pubKey, URL url, byte ... signature) throws IOException {
        InputStream s=url.openStream();
        try {
            return verifySignature(pubKey, s, signature);
        } finally {
            s.close();
        }
    }

    /**
     * @param pubKey The {@link PublicKey} to use for verifying the signature
     * @param s The {@link InputStream} containing the data whose signature is to be verified
     * @param signature The expected signature bytes
     * @return <code>true</code> if signature matches expected
     * @throws IOException If failed to read data or bad key or failed to encode/decode data
     */
    public static final boolean verifySignature(PublicKey pubKey, InputStream s, byte ... signature) throws IOException {
        SignerOutputStream  out;
        try {
            out = new SignerOutputStream(getInstance(pubKey), pubKey, NullOutputStream.NULL_OUTPUT_STREAM);
        } catch(GeneralSecurityException e) {
            throw new StreamCorruptedException("Bad (" + e.getClass().getSimpleName() + ") public key: " + e.getMessage());
        }

        try {
            long    cpySize=IOUtils.copyLarge(s, out);
            if (cpySize < 0L) {
                throw new StreamCorruptedException("Bad copy size: " + cpySize);
            }
        } finally {
            out.close();
        }
        
        try {
            return out.verify(signature);
        } catch(SignatureException e) {
            throw new StreamCorruptedException("Failed (" + e.getClass().getSimpleName() + ") to sign: " + e.getMessage());
        }
    }

    /**
     * @param pubKey The {@link PublicKey} to use for verifying the signature
     * @param chars The characters whose signature is to be verified
     * @param signature The expected signature bytes
     * @return <code>true</code> if signature matches expected
     * @throws IOException If failed to read data or bad key or failed to encode/decode data
     */
    public static final boolean verifySignature(PublicKey pubKey, char[] chars, byte ... signature) throws IOException {
        return verifySignature(pubKey, chars, 0, chars.length, signature);
    }

    /**
     * @param pubKey The {@link PublicKey} to use for verifying the signature
     * @param chars The characters whose signature is to be verified
     * @param offset Offset of characters whose signature is to be verified
     * @param len Number of characters whose signature is to be verified
     * @param signature The expected signature bytes
     * @return <code>true</code> if signature matches expected
     * @throws IOException If failed to read data or bad key or failed to encode/decode data
     */
    public static final boolean verifySignature(PublicKey pubKey, char[] chars, int offset, int len, byte ... signature) throws IOException {
        return verifySignature(pubKey, CharBuffer.wrap(chars, offset, len), signature);
    }

    /**
     * @param pubKey The {@link PublicKey} to use for verifying the signature
     * @param csq The {@link CharSequence} data whose signature is to be verified
     * @param signature The expected signature bytes
     * @return <code>true</code> if signature matches expected
     * @throws IOException If failed to read data or bad key or failed to encode/decode data
     */
    public static final boolean verifySignature(PublicKey pubKey, CharSequence csq, byte ... signature) throws IOException {
        return verifySignature(pubKey, csq, 0, csq.length(), signature);
    }

    /**
     * @param pubKey The {@link PublicKey} to use for verifying the signature
     * @param csq The {@link CharSequence} data whose signature is to be verified
     * @param start The start offset (inclusive) of the data whose signature
     * is to be verified
     * @param end The end offset (exclusive) of the data whose signature
     * is to be verified
     * @param signature The expected signature bytes
     * @return <code>true</code> if signature matches expected
     * @throws IOException If failed to read data or bad key or failed to encode/decode data
     */
    public static final boolean verifySignature(PublicKey pubKey, CharSequence csq, int start, int end, byte ... signature) throws IOException {
        CharSequenceReader  rdr=new CharSequenceReader(csq.subSequence(start, end));
        try {
            return verifySignature(pubKey, rdr, signature);
        } finally {
            rdr.close();
        }
    }

    /**
     * @param pubKey The {@link PublicKey} to use for verifying the signature
     * @param r The {@link Reader} containing the data whose signature is to be verified
     * @param signature The expected signature bytes
     * @return <code>true</code> if signature matches expected
     * @throws IOException If failed to read data or bad key or failed to encode/decode data
     */
    public static final boolean verifySignature(PublicKey pubKey, Reader r, byte ... signature) throws IOException {
        SignerWriter    out;
        try {
            out = new SignerWriter(getInstance(pubKey), pubKey, NullWriter.NULL_WRITER);
        } catch(GeneralSecurityException e) {
            throw new StreamCorruptedException("Bad (" + e.getClass().getSimpleName() + ") public key: " + e.getMessage());
        }

        try {
            long    cpySize=IOUtils.copyLarge(r, out);
            if (cpySize < 0L) {
                throw new StreamCorruptedException("Bad copy size: " + cpySize);
            }
        } finally {
            out.close();
        }
        
        try {
            return out.verify(signature);
        } catch(SignatureException e) {
            throw new StreamCorruptedException("Failed (" + e.getClass().getSimpleName() + ") to sign: " + e.getMessage());
        }
    }

    /**
     * @param key The {@link Key} instance
     * @return The SHA-1 {@link Signature} instance matching the key
     * @throws NoSuchAlgorithmException If unsupported key algorithm specified
     */
    public static final Signature getInstance(Key key) throws NoSuchAlgorithmException {
        String  algorithm=key.getAlgorithm();
        return Signature.getInstance("SHA1with" + algorithm);
    }
}
