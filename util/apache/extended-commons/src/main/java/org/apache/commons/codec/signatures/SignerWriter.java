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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.Channel;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * A {@link Writer} that calculates a signature for written data as it is
 * simply call the respective {@link #sign()} or {@link #verify(byte...)} methods
 * (can be called at any time - even after closing)
 * @author Lyor G.
 * @since Sep 9, 2013 12:43:46 PM
 */
public class SignerWriter extends FilterWriter implements Channel, SignerStream {
    private boolean open=true;
    private final Signature signer;
    private final Writer    _output;

    /**
     * @param sig A constructed (but not initialized) {@link Signature}
     * @param privateKey The {@link PrivateKey} to be used for signing
     * @param output The real {@link Writer} to write to
     * @throws InvalidKeyException If invalid private key
     */
    public SignerWriter(Signature sig, PrivateKey privateKey, Writer output) throws InvalidKeyException {
        this(sig, output);
        sig.initSign(privateKey);
    }

    /**
     * @param sig A constructed (but not initialized) {@link Signature}
     * @param publicKey The {@link PublicKey} to be used for verification
     * @param output The real {@link Writer} to write to
     * @throws InvalidKeyException If invalid private key
     */
    public SignerWriter(Signature sig, PublicKey publicKey, Writer output) throws InvalidKeyException {
        this(sig, output);
        sig.initVerify(publicKey);
    }

    /**
     * @param sig A fully configured {@link Signature}
     * @param output The real {@link OutputStream} to write to
     */
    public SignerWriter(Signature sig, Writer output) {
        super(Validate.notNull(output, "No output writer", ArrayUtils.EMPTY_OBJECT_ARRAY));
        signer = Validate.notNull(sig, "No signer provided", ArrayUtils.EMPTY_OBJECT_ARRAY);
        _output = new OutputStreamWriter(new SignerOutputStream(sig, NullOutputStream.NULL_OUTPUT_STREAM));
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
    public void write(int c) throws IOException {
        append((char) c);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        super.write(cbuf, off, len);
        _output.write(cbuf, off, len);
    }

    @Override
    public void write(String str) throws IOException {
        append(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        Validate.isTrue(len >= 0, "Bad length: %s", len);
        append(str, off, off+len);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        if (csq == null) {
            return append("null");
        } else {
            return append(csq, 0, csq.length());
        }
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        out.append(csq, start, end);
        _output.append(csq, start, end);
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        out.append(c);
        _output.append(c);
        return this;
    }

    @Override
    public void close() throws IOException {
        if (isOpen()) {
            open = false;
        }
        
        try {
            _output.close();
        } finally {
            super.close();
        }
    }

}
