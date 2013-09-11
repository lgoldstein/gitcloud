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

package org.apache.commons.io.nio;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.nio.ByteOrder;

import org.apache.commons.io.ExtendedEndianUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author Lyor G.
 */
public class ByteOrderInputStream extends FilterInputStream implements ExtendedDataInput {
    private final ByteOrder byteOrder;
    private final byte[]    workBuf=new byte[Long.SIZE / Byte.SIZE];

    public ByteOrderInputStream(InputStream input, ByteOrder order) {
        super(input);
        
        if (input == null) {
            throw new IllegalStateException("No input stream provided");
        }

        if ((byteOrder=order) == null) {
            throw new IllegalStateException("No byte order specified");
        }
    }

    public final ByteOrder getByteOrder() {
        return byteOrder;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        IOUtils.readFully(this.in, b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return (int) this.in.skip(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        throw new StreamCorruptedException("readBoolean N/A");
    }

    @Override
    public byte readByte() throws IOException {
        int value=this.in.read();
        if (value == (-1)) {
            throw new EOFException("No more data available");
        }
        
        return (byte) value;
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return readByte() & 0x00FF;
    }

    @Override
    public short readShort() throws IOException {
        return ExtendedEndianUtils.readSignedInt16(this.in, getByteOrder(), workBuf);
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return ExtendedEndianUtils.readUnsignedInt16(this.in, getByteOrder(), workBuf);
    }

    @Override
    public char readChar() throws IOException {
        throw new StreamCorruptedException("readChar N/A");
    }

    @Override
    public int readInt() throws IOException {
        return ExtendedEndianUtils.readSignedInt32(this.in, getByteOrder(), workBuf);
    }

    @Override
    public long readUnsignedInt() throws IOException {
        return ExtendedEndianUtils.readUnsignedInt32(this.in, getByteOrder(), workBuf);
    }

    @Override
    public long readLong() throws IOException {
        return ExtendedEndianUtils.readSignedInt64(this.in, getByteOrder(), workBuf);
    }

    @Override
    public float readFloat() throws IOException {
        int bits=readInt();
        return Float.intBitsToFloat(bits);
    }

    @Override
    public double readDouble() throws IOException {
        long    bits=readLong();
        return Double.longBitsToDouble(bits);
    }

    @Override
    public String readLine() throws IOException {
        throw new StreamCorruptedException("readLine N/A");
    }

    @Override
    public String readUTF() throws IOException {
        throw new StreamCorruptedException("readUTF N/A");
    }
}
