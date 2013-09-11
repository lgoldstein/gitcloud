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

package org.apache.commons.codec.digest;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.Validate;

/**
 * A {@link FilterWriter} that calculates a {@link MessageDigest} while
 * writing the data. Once the stream is {@link #close()}-d the digest is
 * calculated and stored - i.e., repeated calls to {@link #getDigestValue()}
 * will return the <U>same instance</U>
 * @author Lyor G.
 * @since Sep 3, 2013 11:54:36 AM
 */
public class DigestWriter extends FilterWriter implements DigesterStream {
    private final DigestOutputStream    _digest;
    private final OutputStreamWriter    _output;

    public DigestWriter(String algorithm, Writer w) {
        this(algorithm, w, Charset.defaultCharset());
    }

    public DigestWriter(String algorithm, Writer w, String charset) {
        this(algorithm, w, Charset.forName(charset));
    }

    public DigestWriter(String algorithm, Writer w, Charset charset) {
        this(DigestUtils.getDigest(algorithm), w, charset);
    }

    public DigestWriter(MessageDigest digest, Writer w) {
        this(digest, w, Charset.defaultCharset());
    }

    public DigestWriter(MessageDigest digest, Writer w, String charset) {
        this(digest, w, Charset.forName(charset));
    }

    public DigestWriter(MessageDigest digest, Writer w, Charset charset) {
        super(w);
        
        _digest = new DigestOutputStream(digest, NullOutputStream.NULL_OUTPUT_STREAM);
        _output = new OutputStreamWriter(_digest, charset);
    }

    @Override
    public final MessageDigest getDigest() {
        return _digest.getDigest();
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
    public byte[] getDigestValue() {
        return _digest.getDigestValue();
    }

    @Override
    public void close() throws IOException {
        super.close();
        _output.close();
    }
}
