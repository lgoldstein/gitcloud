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

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * A <code>SequenceReader</code> represents the logical concatenation of other
 * input streams. It starts out with an ordered collection of input streams
 * and reads from the first one until end of file is reached, whereupon it reads
 * from the second one, and so on, until end of file is reached on the last of
 * the contained input streams.
 * @author Lyor G. - inspired by {@link java.io.SequenceInputStream}
 * @since Sep 24, 2013 11:06:40 AM
 */
public class SequenceReader extends Reader {
    private final Iterator<? extends Reader>  streams;
    private final char[]    oneChar=new char[1];

    private Reader  in;

    public SequenceReader(Reader ... rdrs) throws IOException {
        this(ExtendedArrayUtils.asList(Validate.notEmpty(rdrs, "No readers", ArrayUtils.EMPTY_OBJECT_ARRAY)));
    }

    public SequenceReader(Iterable<? extends Reader> iter) throws IOException {
        this(Validate.notNull(iter, "No readers iterator", ArrayUtils.EMPTY_OBJECT_ARRAY).iterator());
    }

    public SequenceReader(Iterator<? extends Reader> iter) throws IOException {
        streams = Validate.notNull(iter, "No readers iterator", ArrayUtils.EMPTY_OBJECT_ARRAY);
        in = nextStream();
    }

    @Override
    public int read() throws IOException {
        int readLen=read(oneChar);
        if (readLen <= 0) {
            return (-1);
        }
        
        return oneChar[0];
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (in == null) {
            return (-1);
        }
        
        int remaining=len, pos=off;
        while(remaining > 0) {
            int readLen=in.read(cbuf, pos, remaining);
            if (readLen < 0) {
                Reader  nextReader=nextStream();
                if (nextReader == null) {
                    // check if out of options and nothing read
                    if (pos == off) {
                        return (-1);
                    }
                    break;
                }
                
                continue;
            }

            Validate.isTrue(readLen > 0, "No data read but no EOF signalled", ArrayUtils.EMPTY_OBJECT_ARRAY);
            remaining -= readLen;
            pos += readLen;
        }

        return (pos - off);
    }

    @Override
    public void close() throws IOException {
        IOException err=null;
        for ( ; ; ) {
            try {
                Reader  nextReader=nextStream();
                if (nextReader == null) {
                    break;
                }
            } catch(IOException e) {
                err = e;    // delay closure IOException(s) until all remaining readers closed
            }
        }
        
        if (err != null) {
            throw err;
        }
    }

    protected Reader nextStream() throws IOException {
        if (in != null) {
            try {
                in.close();
            } finally {
                in = null;
            }
        }

        if (streams.hasNext()) {
            in = Validate.notNull(streams.next(), "Invalid next iterated reader", ArrayUtils.EMPTY_OBJECT_ARRAY);
        } else {
            in = null;
        }

        return in;
    }

}
