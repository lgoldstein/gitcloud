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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.channels.Channel;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * This {@link OutputStream} can be used for either signing or verifying a
 * stream of bytes. After writing all the bytes that are to be signed/verified
 * simply call the respective {@link #sign()} or {@link #verify(byte...)} methods
 * (can be called at any time - even after closing)
 * @author Lyor G.
 * @since Jul 9, 2013 9:13:34 AM
 */
public class SignerOutputStream extends FilterOutputStream implements Channel, SignerStream {
    private boolean open=true;
    private final Signature signer;

    /**
     * @param sig A constructed (but not initialized) {@link Signature}
     * @param privateKey The {@link PrivateKey} to be used for signing
     * @param output The real {@link OutputStream} to write to
     * @throws InvalidKeyException If invalid private key
     */
    public SignerOutputStream(Signature sig, PrivateKey privateKey, OutputStream output) throws InvalidKeyException {
        this(sig, output);
        sig.initSign(privateKey);
    }

    /**
     * @param sig A constructed (but not initialized) {@link Signature}
     * @param publicKey The {@link PublicKey} to be used for verification
     * @param output The real {@link OutputStream} to write to
     * @throws InvalidKeyException If invalid private key
     */
    public SignerOutputStream(Signature sig, PublicKey publicKey, OutputStream output) throws InvalidKeyException {
        this(sig, output);
        sig.initVerify(publicKey);
    }

    /**
     * @param sig A fully configured {@link Signature}
     * @param output The real {@link OutputStream} to write to
     */
    public SignerOutputStream(Signature sig, OutputStream output) {
        super(Validate.notNull(output, "No output stream", ArrayUtils.EMPTY_OBJECT_ARRAY));
        signer = Validate.notNull(sig, "No signer provided", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public final Signature getSigner() {
        return signer;
    }

    @Override
    public byte[] sign() throws SignatureException {
        Signature   s=getSigner();
        return s.sign();
    }

    public int sign(byte[] outbuf, int offset, int len) throws SignatureException {
        Signature   s=getSigner();
        return s.sign(outbuf, offset, len);
    }

    @Override
    public boolean verify(byte ... signature) throws SignatureException {
        return verify(signature, 0, signature.length);
    }
    
    @Override
    public boolean verify(byte[] signature, int off, int len) throws SignatureException {
        Signature   s=getSigner();
        return s.verify(signature, off, len);
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (!isOpen()) {
            throw new IOException("Stream is closed");
        }
        
        try {
            signer.update(b, off, len);
        } catch (SignatureException e) {
            throw new StreamCorruptedException("Failed (" + e.getClass().getSimpleName() + ") to update signature: " + e.getMessage());
        }
    }

    @Override
    public void write(int b) throws IOException {
        if (!isOpen()) {
            throw new IOException("Stream is closed");
        }

        try {
            signer.update((byte) b);
        } catch (SignatureException e) {
            throw new StreamCorruptedException("Failed (" + e.getClass().getSimpleName() + ") to update signature: " + e.getMessage());
        } 
    }

    @Override
    public void close() throws IOException {
        if (isOpen()) {
            open = false;
        }
        
        super.close();
    }
}
