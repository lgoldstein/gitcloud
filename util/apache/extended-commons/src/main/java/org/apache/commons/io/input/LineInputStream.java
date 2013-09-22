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
import java.nio.channels.Channel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.commons.io.output.AsciiLineOutputStream;
import org.apache.commons.io.output.LineLevelAppender;
import org.apache.commons.io.output.LineOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * A {@link FilterInputStream} that reads data until LF is encountered
 * and then outputs this data line-by-line
 * @author Lyor G.
 * @since Sep 15, 2013 12:06:24 PM
 */
public abstract class LineInputStream extends FilterInputStream implements LineLevelAppender, Channel {
    protected final LineOutputStream    _output;

    protected LineInputStream(InputStream input) {
        this(input, Charset.defaultCharset());
    }

    protected LineInputStream(InputStream input, boolean useAscii) {
        super(Validate.notNull(input, "No input", ArrayUtils.EMPTY_OBJECT_ARRAY));

        if (useAscii) {
            _output = new AsciiLineOutputStream() {
                @Override
                public void writeLineData(CharSequence lineData) throws IOException {
                    LineInputStream.this.writeLineData(lineData);
                }
                
                @Override
                public boolean isWriteEnabled() {
                    return LineInputStream.this.isWriteEnabled();
                }
            };
        } else {
            _output = new LineOutputStream() {
                @Override
                public void writeLineData(CharSequence lineData) throws IOException {
                    LineInputStream.this.writeLineData(lineData);
                }
                
                @Override
                public boolean isWriteEnabled() {
                    return LineInputStream.this.isWriteEnabled();
                }
            };
        }
    }

    protected LineInputStream(InputStream input, String charset) {
        this(input, Charset.forName(Validate.notEmpty(charset, "No charset name", ArrayUtils.EMPTY_OBJECT_ARRAY)));
    }

    protected LineInputStream(InputStream input, Charset charset) {
        this(input, Validate.notNull(charset, "No charset", ArrayUtils.EMPTY_OBJECT_ARRAY).newDecoder());
    }

    protected LineInputStream(InputStream input, CharsetDecoder decoder) {
        super(Validate.notNull(input, "No input", ArrayUtils.EMPTY_OBJECT_ARRAY));

        _output = new LineOutputStream(decoder) {
            @Override
            public void writeLineData(CharSequence lineData) throws IOException {
                LineInputStream.this.writeLineData(lineData);
            }
            
            @Override
            public boolean isWriteEnabled() {
                return LineInputStream.this.isWriteEnabled();
            }
        };
    }

    @Override
    public int read() throws IOException {
        int value=super.read();
        if (value == (-1)) {
            return value;
        }
        
        _output.write(value);
        return value;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readLen=super.read(b, off, len);
        if (readLen <= 0) {
            return readLen; // debug breakpoint
        }
        
        _output.write(b, off, readLen);
        return readLen;
    }

    @Override
    public boolean isOpen() {
        return _output.isOpen();
    }

    @Override
    public void close() throws IOException {
        super.close();
        _output.close();
    }
}
