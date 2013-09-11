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

package org.apache.commons.codec.digest;

import java.io.EOFException;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.security.MessageDigest;

import org.apache.commons.io.output.NullWriter;

/**
 * A {@link FilterReader} that calculates a {@link MessageDigest} while
 * reading he data. Once the stream is {@link #close()}-d the digest is
 * calculated and stored - i.e., repeated calls to {@link #getDigestValue()}
 * will return the <U>same instance</U>
 * @author Lyor G.
 * @since Sep 3, 2013 12:35:39 PM
 */
public class DigestReader extends FilterReader implements DigesterStream {
    private DigestWriter  _output;

    public DigestReader(String algorithm, Reader rdr) {
        this(DigestUtils.getDigest(algorithm), rdr);
    }

    public DigestReader(MessageDigest digest, Reader rdr) {
        super(rdr);
        _output = new DigestWriter(digest, NullWriter.NULL_WRITER);
    }

    @Override
    public final MessageDigest getDigest() {
        return _output.getDigest();
    }

    @Override
    public int read() throws IOException {
        int value=in.read();
        if (value == (-1)) {
            return value;   // ignore EOF
        }
        
        _output.append((char) value);
        return value;
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int readLen=in.read(cbuf, off, len);
        if (readLen <= 0) { // ignore EOF/empty
            return readLen;
        }
        
        _output.write(cbuf, off, readLen);
        return readLen;
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        int readLen=in.read(target);
        if (readLen <= 0) { // ignore EOF/empty
            return readLen;
        }
        
        _output.append(target, 0, readLen);
        return readLen;
    }

    @Override
    public void reset() throws IOException {
        super.reset();

        if (_output.getDigestValue() != null) {
            throw new EOFException("Digester already closed");
        }

        MessageDigest   digest=_output.getDigest();
        digest.reset();
        _output = new DigestWriter(digest, NullWriter.NULL_WRITER);
    }

    @Override
    public byte[] getDigestValue() {
        return _output.getDigestValue();
    }

    @Override
    public void close() throws IOException {
        super.close();
        _output.close();
    }
}
