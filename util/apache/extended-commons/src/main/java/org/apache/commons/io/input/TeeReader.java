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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * <P>
 * {@link Reader} proxy that transparently writes a copy of all characters read
 * from the proxied reader to a given {@link Writer}. Using {@link #skip(long)}
 * or {@link #mark(int)}/{@link #reset()} on the reader will result on some
 * bytes from the input reader being skipped or duplicated in the output
 * writer.
 * </P>
 * <P>
 * The proxied input reader is closed when the {@link #close()} method is
 * called on this proxy. It is configurable whether the associated output
 * writer will also closed.
 * </P>
 * @author Lyor G.  - inspired by {@link TeeInputStream}
 * @since Sep 25, 2013 8:55:08 AM
 */
public class TeeReader extends FilterReader {
    protected final Writer  branch;
    protected final boolean closeOutput;

    public TeeReader(Reader r, Writer w) {
        this(r, w, false);
    }
    
    public TeeReader(Reader r, Writer w, boolean closeWriter) {
        super(Validate.notNull(r, "No reader", ArrayUtils.EMPTY_OBJECT_ARRAY));
        branch = Validate.notNull(w, "No writer", ArrayUtils.EMPTY_OBJECT_ARRAY);
        closeOutput = closeWriter;
    }
    
    public final Writer getBranch() {
        return branch;
    }

    @Override
    public int read() throws IOException {
        int ch = super.read();
        if (ch != -1) {
            branch.write(ch);
        }
        return ch;
    }

    @Override
    public int read(char[] crs) throws IOException {
        return read(crs, 0, crs.length);
    }

    @Override
    public int read(char[] crs, int off, int len) throws IOException {
        int n = super.read(crs, off, len);
        if (n > 0) {
            branch.write(crs, off, n);
        }

        return n;
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);  // ensure skip not implemented as read-and-throw
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        int n=in.read(target);  // default allocates a char[] and calls read(char[]);
        if (n > 0) {
            branch.append(target);
        }

        return n;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (closeOutput) {
                branch.close();
            }
        }
    }
}
