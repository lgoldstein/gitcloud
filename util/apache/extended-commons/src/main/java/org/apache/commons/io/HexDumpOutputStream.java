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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channel;

import org.apache.commons.codec.binary.ExtendedHex;
import org.apache.commons.io.output.LineLevelAppender;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * An {@link OutputStream} that formats its written bytes in the same way that
 * {@link HexDump#dump(byte[], long, OutputStream, int)} does by default. It
 * can be modified to format data slightly different - e.g., use lowercase
 * instead of uppercase, use {@code long} offset instead of {@code int}
 * @author Lyor G.
 * @since Sep 25, 2013 9:40:27 AM
 */
public class HexDumpOutputStream extends OutputStream implements Channel {
    private final LineLevelAppender   _appender;
    private final byte[]    _workBuf, _oneByte=new byte[1];
    private final boolean   _lowercase, _longOffset;
    private int _filledLen;
    private boolean _closed;
    private long    _displayOffset;
    private final StringBuilder _lineData;

    public static final int DEFAULT_BYTES_PER_LINE=16;
    public static final boolean DEFAULT_HEX_CASE=false, DEFAULT_LONG_OFFSET=false;

    public HexDumpOutputStream(LineLevelAppender appender) {
        this(appender, DEFAULT_BYTES_PER_LINE);
    }

    public HexDumpOutputStream(LineLevelAppender appender, int bytesPerLine) {
        this(appender, bytesPerLine, DEFAULT_HEX_CASE, DEFAULT_LONG_OFFSET);
    }

    public HexDumpOutputStream(LineLevelAppender appender, boolean useLowercase) {
        this(appender, DEFAULT_BYTES_PER_LINE, useLowercase, DEFAULT_LONG_OFFSET);
    }

    public HexDumpOutputStream(LineLevelAppender appender, int bytesPerLine, boolean useLowercase, boolean longOffset) {
        _appender = Validate.notNull(appender, "No appender", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.isTrue(bytesPerLine > 0, "Bad bytes-per-line value: %d", bytesPerLine);
        _workBuf = new byte[bytesPerLine];
        _lineData = new StringBuilder((2 /* HEX */ + 1 /* space */ + 1 /* character */) * bytesPerLine + Long.SIZE /* some extra for the offset */);
        _lowercase = useLowercase;
        _longOffset = longOffset;
    }

    public final LineLevelAppender getLineLevelAppender() {
        return _appender;
    }

    public final boolean isLowercase() {
        return _lowercase;
    }

    public final boolean isLongOffset() {
        return _longOffset;
    }

    @Override
    public boolean isOpen() {
        return (!_closed);
    }

    @Override
    public void write(int b) throws IOException {
        _oneByte[0] = (byte) (b & 0xFF);
        write(_oneByte, 0, 1);

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

        if (len <= 0) {
            return;
        }

        LineLevelAppender   appender=getLineLevelAppender();
        if (!appender.isWriteEnabled()) {
            return;
        }

        int curPos=off, remain=len;
        if (_filledLen < _workBuf.length) {
            int avail=_workBuf.length - _filledLen, cpySize=Math.min(avail, remain);
            System.arraycopy(b, curPos, _workBuf, _filledLen, cpySize);
            if ((_filledLen += cpySize) >= _workBuf.length) {
                try {
                    writeAccumulatedData(appender, _displayOffset, _workBuf, 0, _filledLen, _workBuf.length);
                } finally {
                    _displayOffset += _filledLen;
                    _filledLen = 0;
                }
            }
            curPos += cpySize;
            remain -= cpySize;
        }
        
        while(remain > 0) {
            int avail=Math.min(remain, _workBuf.length);
             // If less than a full line, accumulate for next write iteration
            if (avail < _workBuf.length) {
                System.arraycopy(b, curPos, _workBuf, 0, avail);
                _filledLen = avail;
            } else {
                try {
                    writeAccumulatedData(appender, _displayOffset, b, curPos, avail, _workBuf.length);
                } finally {
                    _displayOffset += avail;
                }
            }
            
            remain -= avail;
            curPos += avail;
        }
    }

    protected void writeAccumulatedData(LineLevelAppender appender, long offset, byte[] data, int off, int len, int bytesPerLine) throws IOException {
        _lineData.setLength(0);
        
        if (isLongOffset()) {
            ExtendedHex.appendHex(_lineData, isLowercase(), offset);
        } else {
            ExtendedHex.appendHex(_lineData, isLowercase(), (int) offset);
        }
        _lineData.append(' ');

        for (int    index=0, pos=off; index < len; index++, pos++) {
            ExtendedHex.appendHex(_lineData, isLowercase(), data[pos]).append(' ');
        }
        
        // if less than bytes per line, then pad with spaces
        for (int    index=len; index < bytesPerLine; index++) {
            _lineData.append("   ");
        }
        
        for (int    index=0, pos=off; index < len; index++, pos++) {
            char    ch=(char) (data[pos] & 0xFF);
            if ((ch < ' ') || (ch > 0x7E)) {
                _lineData.append('.');
            } else {
                _lineData.append(ch);
            }
        }
        
        appender.writeLineData(_lineData);
    }

    @Override
    public void close() throws IOException {
        if (isOpen()) {
            try {
                if (_filledLen > 0) {
                    try {
                        LineLevelAppender   appender=getLineLevelAppender();
                        if (appender.isWriteEnabled()) {
                            writeAccumulatedData(appender, _displayOffset, _workBuf, 0, _filledLen, _workBuf.length);
                        }
                    } finally {
                        _displayOffset += _filledLen;
                        _filledLen = 0;
                    }
                }
            } finally {
                _closed = true;
            }
        }
    }

}
