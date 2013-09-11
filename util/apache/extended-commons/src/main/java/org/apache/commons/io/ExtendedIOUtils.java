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

package org.apache.commons.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triplet;


/**
 * @author lgoldstein
 */
public class ExtendedIOUtils extends IOUtils {
	// these are equivalent PUBLIC values to the ones in IOUtils which are private
	public static final int	EOF_VALUE=(-1);
	public static final int DEFAULT_BUFFER_SIZE_VALUE=4 * 1024;

	public ExtendedIOUtils() {
		super();
	}

    /**
     * @param toClose The {@link Closeable} to close - ignored if {@code null}
     * @throws IOException If {@link IOException} thrown during closing
     */
    public static final void close(Closeable toClose) throws IOException {
        if (toClose != null) {
            toClose.close();
        }
    }

    /**
     * Closes <U>all</U> the non{@code null} resources
     * @param toClose The {@link Closeable}-s to close - ignored if {@code null}/empty
     * @throws IOException If any of the closed resources threw an {@link IOException}
     * while being closed
     */
    public static final void closeAll(Closeable ... toClose) throws IOException {
        closeAll(ExtendedArrayUtils.asList(toClose));
    }

    /**
     * Closes <U>all</U> the non{@code null} resources
     * @param toClose The {@link Closeable}-s to close - ignored if {@code null}/empty
     * @throws IOException If any of the closed resources threw an {@link IOException}
     * while being closed
     */
    public static final void closeAll(Collection<? extends Closeable> toClose) throws IOException {
        if (ExtendedCollectionUtils.isEmpty(toClose)) {
            return;
        }
        
        IOException ioe=null;
        for (Closeable c : toClose) {
            if (c == null) {
                continue;
            }
            
            try {
                c.close();
            } catch(IOException e) {
                // TODO for JDK1.7 can do: if (ioe != null) ioe.addSuppressed(e)
                ioe = e;
            }
        }
        
        if (ioe != null) {
            throw ioe;
        }
    }

    public static final long copyLarge(InputStream input, File output)
            throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE_VALUE]);
    }

    public static final long copyLarge(InputStream input, File output, byte[] buffer)
            throws IOException {
        OutputStream  outStream=new FileOutputStream(output);
        try {
            return copyLarge(input, outStream, buffer);
        } finally {
            outStream.close();
        }
    }

    public static final long copyLarge(InputStream input, File output, long inputOffset, long length)
            throws IOException {
        return copyLarge(input, output, inputOffset, length, new byte[DEFAULT_BUFFER_SIZE_VALUE]);
    }

    public static final long copyLarge(InputStream input, File output, long inputOffset, long length, byte[] buffer)  throws IOException {
        OutputStream  outStream=new FileOutputStream(output);
        try {
            return copyLarge(input, outStream, inputOffset, length, buffer);
        } finally {
            outStream.close();
        }
    }

	public static final int copy (InputStream input, OutputStream output, int copySize) throws IOException {
        final long count=copyLarge(input, output, copySize);
        if (count > Integer.MAX_VALUE)
            return -1;
        else
        	return (int) count;
    }

	public static final long copyLarge (InputStream input, OutputStream output, long copySize) throws IOException {
		if (copySize < 0L)
			return copyLarge(input, output);

		final byte[]	buffer=new byte[DEFAULT_BUFFER_SIZE_VALUE];
        for (long	count=0L, remSize=copySize; count < copySize; )
        {
        	final int	readSize=(remSize > buffer.length) ? buffer.length : (int) remSize;
        	final int	n=input.read(buffer, 0, readSize);
        	if (n == (-1))
        		return count;

        	output.write(buffer, 0, n);
         	count += n;
         	remSize -= n;
        }

        return copySize;
    }

    public static final int append(Reader input, Appendable output) throws IOException {
        long count = appendLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static final int append(Reader input, Appendable output, char[] buffer) throws IOException {
        return append(input, output, buffer, 0, buffer.length);
    }

    public static final int append(Reader input, Appendable output, char[] buffer, int startPos, int length) throws IOException {
        long count = appendLarge(input, output, buffer, startPos, length);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static final long appendLarge(Reader input, Appendable output) throws IOException {
        return appendLarge(input, output, new char[DEFAULT_BUFFER_SIZE_VALUE]);
    }
    
    public static final long appendLarge(Reader input, Appendable output, char[] buffer) throws IOException {
        return appendLarge(input, output, buffer, 0, buffer.length);
    }
    
    public static final long appendLarge(Reader input, Appendable output,
    							   final char[] buffer, final int startPos, int length) throws IOException {
        if (output instanceof Writer) {
            return writeLarge(input, (Writer) output, buffer, startPos, length);
        }

        if (length < Long.SIZE) {
            throw new IOException("Insufficient work buffer size: " + length);
        }

        // wrap the work buffer as a CharSequence so we can append it
        final AtomicInteger readCount=new AtomicInteger(0);
        final CharSequence  readData=new CharSequence() {
                @Override
                public CharSequence subSequence(int start, int end) {
                    return new String(buffer, startPos + start, end - start);
                }
                
                @Override
                public int length() {
                    return readCount.get();
                }
                
                @Override
                public char charAt(int index) {
                    return buffer[startPos + index];
                }
                
                @Override
                public String toString () {
                    return new String(buffer, startPos, readCount.get());
                }
            };

        int n=0;
        long count = 0L;
        while (-1 != (n = input.read(buffer, startPos, length))) {
            readCount.set(n);
            output.append(readData);
            count += n;
        }
        return count;
    }

    public static final long writeLarge(Reader input, Writer output, char[] buffer, int startPos, int length) throws IOException {
        if (length < Long.SIZE) {
            throw new IOException("Insufficient work buffer size: " + length);
        }

        int n=0;
        long count = 0L;
        while (-1 != (n = input.read(buffer, startPos, length))) {
            output.write(buffer, startPos, n);
            count += n;
        }
        return count;
    }
    
    public static final Triplet<Long,Byte,Byte> findDifference (long  readOffset,
                                        byte[] srcBuf, int srcOffset, int srcRead,
                                        byte[] dstBuf, int dstOffset, int dstRead) {
        if (srcRead <= 0) {
            if (dstRead <= 0) {   // both ended at the same time
                return null;
            }

            return new Triplet<Long,Byte,Byte>(Long.valueOf(readOffset), null, Byte.valueOf(dstBuf[0]));
        } else if (dstRead <= 0) {
            return new Triplet<Long,Byte,Byte>(Long.valueOf(readOffset), Byte.valueOf(srcBuf[0]), null);
        }

        final int   cmpLen=Math.min(srcRead, dstRead);
        for (int    cIndex=0, sIndex=srcOffset, dIndex=dstOffset; cIndex < cmpLen; cIndex++, sIndex++, dIndex++) {
            if (srcBuf[sIndex] != dstBuf[dIndex]) {
                return new Triplet<Long,Byte,Byte>(Long.valueOf(readOffset + cIndex), Byte.valueOf(srcBuf[sIndex]), Byte.valueOf(dstBuf[dIndex]));
            }
        }

        if (cmpLen < srcRead) {
            return new Triplet<Long,Byte,Byte>(Long.valueOf(readOffset + cmpLen), null, Byte.valueOf(dstBuf[cmpLen]));
        } else if (cmpLen < dstRead) {
            return new Triplet<Long,Byte,Byte>(Long.valueOf(readOffset + cmpLen), Byte.valueOf(srcBuf[cmpLen]), null);
        } else {
            return null;
        }
    }

    public static final Triplet<Long,Byte,Byte> findDifference (long  readOffset, byte[] srcBuf, int srcRead, byte[] dstBuf, int dstRead) {
        return findDifference(readOffset, srcBuf, 0, srcRead, dstBuf, 0, dstRead);
    }

    /**
     * Compares the contents of the {@link InputStream}-s
     * @param stream1 First stream
     * @param stream2 Second stream
     * @param maxRead Max. number of bytes to compare - if negative then
     * <U>all</U> bytes are compared
     * @param readSize work buffer size to be used to read data from the files
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference(InputStream stream1, InputStream stream2, long maxRead, int readSize)
                throws IOException {
        Validate.notNull(stream1, "No 1st stream", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(stream2, "No 2nd stream", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.isTrue(readSize >= Byte.MAX_VALUE, "Insufficient read size: %s", readSize);

        long    readOffset=0L;
        for (final byte[]   srcBuf=new byte[readSize], dstBuf=new byte[readSize]; ; ) {
            final int   remLen;
            if (maxRead >= 0L) {
                final long  remRead=maxRead - readOffset;
                if (remRead < readSize) {
                    remLen = (int) remRead;
                } else {
                    remLen = readSize;
                }
            } else {
                remLen = readSize;
            }

            if (remLen <= 0)
                break;

            final int                       srcRead=stream1.read(srcBuf, 0, remLen),
                                            dstRead=stream2.read(dstBuf, 0, remLen);
            final Triplet<Long,Byte,Byte>   cmpRes=findDifference(readOffset, srcBuf, srcRead, dstBuf, dstRead);
            if (cmpRes != null) {
                return cmpRes;
            }

            if ((srcRead < 0) || (dstRead < 0)) {
                break;
            }

            readOffset += remLen;
        }

        return null;
    }

    /**
     * Compares the contents of the {@link InputStream}-s
     * @param stream1 First stream
     * @param stream2 Second stream
     * @param maxRead Max. number of bytes to compare - if negative then
     * <U>all</U> bytes are compared
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference(InputStream stream1, InputStream stream2, long maxRead)
            throws IOException {
        return findDifference(stream1, stream2, maxRead, DEFAULT_BUFFER_SIZE_VALUE);
    }

    /**
     * Compares the contents of the {@link InputStream}-s
     * @param stream1 First stream
     * @param stream2 Second stream
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference(InputStream stream1, InputStream stream2) throws IOException {
        return findDifference(stream1, stream2, (-1L));
    }
}
