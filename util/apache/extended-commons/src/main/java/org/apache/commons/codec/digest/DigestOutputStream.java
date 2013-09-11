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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * A {@link FilterOutputStream} that calculates a {@link MessageDigest} while
 * writing the data. Once the stream is {@link #close()}-d the digest is
 * calculated and stored - i.e., repeated calls to {@link #getDigestValue()}
 * will return the <U>same instance</U>
 * @author Lyor G.
 * @since Sep 3, 2013 9:40:31 AM
 */
public class DigestOutputStream extends FilterOutputStream implements DigesterStream {
    private final MessageDigest _digest;
    private byte[]  _digestValue;

    public DigestOutputStream(String algorithm, OutputStream output) {
        this(DigestUtils.getDigest(algorithm), output);
    }
    
    public DigestOutputStream(MessageDigest digest, OutputStream output) {
        super(output);
        _digest = Validate.notNull(digest, "No digester", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public final MessageDigest getDigest() {
        return _digest;
    }

    @Override
    public void write(int b) throws IOException {
        MessageDigest   digest=getDigest();
        out.write(b);
        digest.update((byte) b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        MessageDigest   digest=getDigest();
        out.write(b, off, len);
        digest.update(b, off, len);
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
