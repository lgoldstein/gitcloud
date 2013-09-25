/* Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * <P>Accumulates all written data into a work buffer and calls the actual
 * writing method only when LF detected. <B>Note:</B> it strips CR if found
 * before the LF</P>
 * @author Lyor Goldstein
 * @since Sep 15, 2013 10:14:24 AM
 */
public class LineOutputStream extends OutputStream implements Channel {
    private boolean _closed;
    protected byte[]  _workBuf;
    protected char[]  _lineBuf;
    protected int _usedLen;
    protected final CharsetDecoder    _decoder;
    protected final byte[]    oneByte=new byte[1];
    protected final LineLevelAppender  _appender;

    public LineOutputStream(LineLevelAppender appender) {
        this(Charset.defaultCharset(), appender);
    }

    public LineOutputStream(String charset, LineLevelAppender appender) {
        this(Charset.forName(Validate.notEmpty(charset, "No charset name", ArrayUtils.EMPTY_OBJECT_ARRAY)), appender);
    }

    public LineOutputStream(Charset charset, LineLevelAppender appender) {
        this(Validate.notNull(charset, "No charset", ArrayUtils.EMPTY_OBJECT_ARRAY).newDecoder(), appender);
    }

    public LineOutputStream(CharsetDecoder decoder, LineLevelAppender appender) {
        _decoder = Validate.notNull(decoder, "No decoder", ArrayUtils.EMPTY_OBJECT_ARRAY);
        _appender = Validate.notNull(appender, "No appender", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    public final LineLevelAppender getLineLevelAppender() {
        return _appender;
    }

    @Override
    public void write(int b) throws IOException {
        oneByte[0] = (byte) b;
        write(oneByte);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (!isOpen()) {
            throw new IOException("Stream is closed");
        }
        
        LineLevelAppender   appender=getLineLevelAppender();
        if (!appender.isWriteEnabled()) {
            if (_usedLen > 0) {
                _usedLen = 0;
            }
            
            return;
        }

        int remLen=len, curOffset=off;
        while (remLen > 0) {
            int lfIndex=ExtendedArrayUtils.indexOf(b, (byte) '\n', curOffset, remLen);
            if (lfIndex < curOffset) {
                break;  // no more lines
            }

            int lineDataLen=lfIndex - curOffset;
            if (_usedLen > 0) {
                accumulateLineData(b, curOffset, lineDataLen);
                try {
                    writeAccumulatedData(_workBuf, 0, _usedLen);
                } finally {
                    _usedLen = 0;
                }
            } else {    // use the user's buffer directly
                writeAccumulatedData(b, curOffset, lineDataLen);
            }
            
            lineDataLen++;  // count the LF
            curOffset += lineDataLen;
            remLen -= lineDataLen;
        }
        
        if (remLen > 0) {
            accumulateLineData(b, curOffset, remLen);
        }
    }

    protected void writeAccumulatedData(byte[] b, int off, int len) throws IOException {
        LineLevelAppender   appender=getLineLevelAppender();
        if (len <= 0) {
            appender.writeLineData("");
            return;
        }
        
        ByteBuffer  bb=(b[off+len-1] == '\r') ? ByteBuffer.wrap(b, off, len - 1) : ByteBuffer.wrap(b, off, len);
        CharBuffer  cc=CharBuffer.wrap(ensureCharDataCapacity(len));
        
        _decoder.reset();
        CoderResult res=_decoder.decode(bb, cc, true);
        if (res.isError() || res.isMalformed() || res.isOverflow() || res.isUnmappable()) {
            throw new StreamCorruptedException("Failed to decode line bytes: " + res);
        }
        
        cc.flip();
        appender.writeLineData(cc);
    }

    protected char[] ensureCharDataCapacity(int numBytes) {
        float   grwFactor=_decoder.maxCharsPerByte();   // worst case
        int     reqChars=(grwFactor > 0.0f) ? (int) (numBytes * grwFactor) : numBytes;
        if ((_lineBuf == null) || (_lineBuf.length < reqChars)) {
            reqChars = Math.max(reqChars, LineLevelAppender.TYPICAL_LINE_LENGTH);
            _lineBuf = new char[reqChars + Byte.SIZE /* a little extra to avoid numerous growths */];
        }
        
        return _lineBuf;
    }

    protected void accumulateLineData(byte[] b, int off, int len) {
        if (len <= 0) {
            return;
        }

        byte[]  accBuf=ensureWorkBufCapacity(len);
        System.arraycopy(b, off, accBuf, _usedLen, len);
        _usedLen += len;
    }

    protected byte[] ensureWorkBufCapacity(int reqLen) {
        if (_workBuf == null) {
            _workBuf = new byte[Math.max(reqLen, Byte.MAX_VALUE)];
        } else if (_workBuf.length < reqLen) {
            byte[]  newBuf=new byte[reqLen + Long.SIZE /* a bit extra to avoid small growths */];
            if (_usedLen > 0) {
                System.arraycopy(_workBuf, 0, newBuf, 0, _usedLen);
            }
            _workBuf = newBuf;
        }
        
        return _workBuf;
    }

    @Override
    public boolean isOpen() {
        return !_closed;
    }

    @Override
    public void close() throws IOException {
        if (isOpen()) {
            LineLevelAppender   appender=getLineLevelAppender();
            try {
                if (appender.isWriteEnabled() && (_usedLen > 0)) {
                    writeAccumulatedData(_workBuf, 0, _usedLen); 
                }
            } finally {
                _usedLen = 0;
                _closed = true;
            }
        }
    }
}
