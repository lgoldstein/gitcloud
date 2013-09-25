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

package org.apache.commons.io.output;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.ClosedReader;
import org.apache.commons.io.input.SequenceReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * This class implements a {@link Writer} in which the data is 
 * written into a char array. The buffer automatically grows as data 
 * is written to it.
 * <p> 
 * The data can be retrieved using <code>toCharArray()</code> and
 * <code>toString()</code>.
 * <p>
 * Closing a <tt>CharArrayWriter</tt> has no effect. The methods in
 * this class can be called after the stream has been closed without
 * generating an <tt>IOException</tt>.
 * <p>
 * This is an alternative implementation of the {@link java.io.CharArrayWriter}
 * class. The original implementation only allocates 32 bytes at the beginning.
 * As this class is designed for heavy duty it starts at 1024 bytes. In contrast
 * to the original it doesn't reallocate the whole memory block but allocates
 * additional buffers. This way no buffers need to be garbage collected and
 * the contents don't have to be copied to the new buffer. This class is
 * designed to behave exactly like the original.
 * @author Lyor G. (inspired by the {@link org.apache.commons.io.output.ByteArrayOutputStream}
 * @since Sep 24, 2013 9:53:13 AM
 */
public class CharArrayWriter extends Writer {
    /** The list of buffers, which grows and never reduces. */
    private final List<char[]> buffers = new ArrayList<char[]>();
    /** The index of the current buffer. */
    private int currentBufferIndex;
    /** The total count of bytes in all the filled buffers. */
    private int filledBufferSum;
    /** The current buffer. */
    private char[] currentBuffer;
    /** The total count of bytes written. */
    private int count;
    private final char[]    oneChar=new char[1];
    public CharArrayWriter() {
        this(1024);
    }
    
    public CharArrayWriter(int initialSize) {
        Validate.isTrue(initialSize > 0, "Bad initial size: %d", initialSize);

        synchronized (this) {
            needNewBuffer(initialSize);
        }
    }

    @Override
    public void write(int c) throws IOException {
        synchronized(oneChar) {
            oneChar[0] = (char) c;
            write(oneChar);
        }
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    @Override
    public void write(char[] b, int off, int len) throws IOException {
        if ((off < 0) 
         || (off > b.length) 
         || (len < 0) 
         || ((off + len) > b.length) 
         || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        synchronized (this) {
            int newcount = count + len;
            int remaining = len;
            int inBufferPos = count - filledBufferSum;
            while (remaining > 0) {
                int part = Math.min(remaining, currentBuffer.length - inBufferPos);
                System.arraycopy(b, off + len - remaining, currentBuffer, inBufferPos, part);
                remaining -= part;
                if (remaining > 0) {
                    needNewBuffer(newcount);
                    inBufferPos = 0;
                }
            }
            count = newcount;
        }
    }

    @Override
    public void write(String str) throws IOException {
        write(str, 0, str.length());
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        if ((off < 0) 
         || (off > str.length()) 
         || (len < 0) 
         || ((off + len) > str.length()) 
         || ((off + len) < 0)) {
           throw new IndexOutOfBoundsException();
        } else if (len == 0) {
           return;
        }
        
        synchronized (this) {
            int newcount = count + len;
            int remaining = len;
            int inBufferPos = count - filledBufferSum;
            while (remaining > 0) {
                int part=Math.min(remaining, currentBuffer.length - inBufferPos);
                int srcStart=off + len - remaining, srcEnd=srcStart + part;
                str.getChars(srcStart, srcEnd, currentBuffer, inBufferPos);
                remaining -= part;
                if (remaining > 0) {
                    needNewBuffer(newcount);
                    inBufferPos = 0;
                }
            }
            count = newcount;
        }
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        String s=((csq == null) ? "null" : csq.toString());
        write(s, 0, s.length());
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        String s=((csq == null) ? "null" : csq).subSequence(start, end).toString();
        write(s, 0, s.length());
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        write(c);
        return this;
    }

    @Override
    public void flush() throws IOException {
        // nop
    }

    /**
     * Return the current size of the char array.
     * @return the current size of the char array
     */
    public synchronized int size() {
        return count;
    }

    /**
     * Gets the current contents of this characters as an array.
     * The result is independent of this stream.
     *
     * @return the current contents of this output stream, as a char array
     * @see java.io.CharArrayWriter#toCharArray()
     */
    public synchronized char[] toCharArray() {
        int remaining=size();
        if (remaining <= 0) {
            return ArrayUtils.EMPTY_CHAR_ARRAY; 
        }

        char[]  newbuf=new char[remaining];
        int     pos=0;
        for (char[] buf : buffers) {
            int c=Math.min(buf.length, remaining);
            System.arraycopy(buf, 0, newbuf, pos, c);
            pos += c;
            remaining -= c;

            if (remaining <= 0) {
                break;
            }
        }
        return newbuf;
    }

    /**
     * @return A {@link Reader} backed by a <U>copy</U> of the current characters
     * @throws IOException If failed to generate the reader
     */
    public synchronized Reader toReader() throws IOException {
        int remaining=size();
        if (remaining <= 0) {
            return ClosedReader.CLOSED_READER;
        }

        CharArrayWriter out=new CharArrayWriter(remaining);
        try {
            writeTo(out);
        } finally {
            out.close();
        }
        
        return out.toBufferedReader();
    }

    /**
     * @return A {@link Reader} backed by the current characters.
     * <B><U>Caveat emptor:</B></U> no changes are allowed to the current
     * characters while the reader is in use
     * @throws IOException If failed to generate the reader
     */
    protected synchronized Reader toBufferedReader() throws IOException {
        int remaining=size();
        if (remaining <= 0) {
            return ClosedReader.CLOSED_READER;
        }

        List<Reader>    list=new ArrayList<Reader>(buffers.size());
        for (char[] buf : buffers) {
            int c=Math.min(buf.length, remaining);
            list.add(new CharArrayReader(buf, 0, c));
            remaining -= c;
            if (remaining <= 0) {
                break;
            }
        }
        
        return new SequenceReader(list);
    }
    /**
     * Writes the entire contents of this byte stream to the
     * specified output stream.
     *
     * @param out  the output stream to write to
     * @throws IOException if an I/O error occurs, such as if the stream is closed
     * @see java.io.ByteArrayOutputStream#writeTo(OutputStream)
     */
    public synchronized void writeTo(Writer out) throws IOException {
        int remaining = size();
        for (char[] buf : buffers) {
            int c = Math.min(buf.length, remaining);
            out.write(buf, 0, c);
            remaining -= c;
            if (remaining <= 0) {
                break;
            }
        }
    }

    @Override
    public void close() throws IOException {
        //nop
    }

    @Override
    public String toString() {
        return new String(toCharArray());
    }

    /**
     * @see java.io.CharArrayWriter#reset()
     */
    public synchronized void reset() {
        count = 0;
        filledBufferSum = 0;
        currentBufferIndex = 0;
        currentBuffer = buffers.get(currentBufferIndex);
    }

    /**
     * Makes a new buffer available either by allocating
     * a new one or re-cycling an existing one.
     *
     * @param newcount  the size of the buffer if one is created
     * @return the available buffer
     */
    private char[] needNewBuffer(int newcount) {
        if (currentBufferIndex < (buffers.size() - 1)) {
            //Recycling old buffer
            filledBufferSum += currentBuffer.length;
            
            currentBufferIndex++;
            currentBuffer = buffers.get(currentBufferIndex);
        } else {
            //Creating new buffer
            int newBufferSize;
            if (currentBuffer == null) {
                newBufferSize = newcount;
                filledBufferSum = 0;
            } else {
                newBufferSize = Math.max(
                    currentBuffer.length << 1, 
                    newcount - filledBufferSum);
                filledBufferSum += currentBuffer.length;
            }
            
            currentBufferIndex++;
            currentBuffer = new char[newBufferSize];
            buffers.add(currentBuffer);
        }
        
        return currentBuffer;
    }
}
