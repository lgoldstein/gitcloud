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

package org.apache.commons.io.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * This {@link FilterInputStream} accumulates read data into a byte array.
 * Calling {@link #toByteArray()} at any stage, returns a copy of the data
 * that has been read so far. <B>Note:</B> if data is skipped via the {@link #skip(long)}
 * method then it will <U>not</U> be available in the read data array
 * @author Lyor G.
 * @since Sep 24, 2013 8:10:51 AM
 */
public class ByteArrayAccumulatingInputStream extends FilterInputStream {
    private final ByteArrayOutputStream accumulator;

    public ByteArrayAccumulatingInputStream(InputStream input) {
        this(input, 1024);
    }
    
    public ByteArrayAccumulatingInputStream(InputStream input, int initialSize) {
        super(input);      
        accumulator = new ByteArrayOutputStream(initialSize);
    }

    @Override
    public int read() throws IOException {
        int nRead=super.read();
        if (nRead == (-1)) {
            return (-1);
        }
        
        accumulator.write(nRead);
        return nRead;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readLen=super.read(b, off, len);
        if (readLen <= 0) {
            return readLen;
        }
        
        accumulator.write(b, off, readLen);
        return readLen;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        accumulator.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);  // make sure skip is not implemented as read-and-throw
    }

    public int size() {
        return accumulator.size();
    }
    
    public byte[] toByteArray() {
        return accumulator.toByteArray();
    }
    
    public String toString(String enc) throws UnsupportedEncodingException {
        return accumulator.toString(enc);
    }

    @Override
    public String toString() {
        return accumulator.toString();
    }
}
