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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * A {@link FilterInputStream} that calculates a {@link MessageDigest} while
 * reading he data. Once the stream is {@link #close()}-d the digest is
 * calculated and stored - i.e., repeated calls to {@link #getDigestValue()}
 * will return the <U>same instance</U>
 * @author Lyor G.
 * @since Sep 3, 2013 10:24:28 AM
 */
public class DigestInputStream extends FilterInputStream implements DigesterStream {
    private final MessageDigest _digest;
    private byte[]  _digestValue;
    
    public DigestInputStream(String algorithm, InputStream input) {
        this(DigestUtils.getDigest(algorithm), input);
    }
    
    public DigestInputStream(MessageDigest digest, InputStream input) {
        super(input);
        _digest = Validate.notNull(digest, "No digester", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public final MessageDigest getDigest() {
        return _digest;
    }

    @Override
    public int read() throws IOException {
        int data=in.read();
        if (data == (-1)) { // ignore EOF
            return data;
        }
        
        MessageDigest   digest=getDigest();
        digest.update((byte) data);
        return data;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readLen=in.read(b, off, len);
        if (readLen <= 0) { // ignore EOF/empty
            return readLen;
        }

        MessageDigest   digest=getDigest();
        digest.update(b, off, readLen);
        return readLen;
    }

    // NOTE: resets the digest as well
    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        
        MessageDigest   digest=getDigest();
        digest.reset();
    }

    @Override
    public byte[] getDigestValue() {
        return _digestValue;
    }

    @Override
    public void close() throws IOException {
        super.close();
        
        if (_digestValue == null) {
            MessageDigest   digest=getDigest();
            _digestValue = digest.digest();
        }
    }
}
