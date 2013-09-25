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

package org.apache.commons.io.output;

import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channel;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * <P>Accumulates all written data into a work buffer and calls the actual
 * writing method only when LF detected</P>
 * @author Lyor G.
 * @since Aug 13, 2013 7:37:51 AM
 */
public class LineWriter extends Writer implements Channel  {
    private StringBuilder   _workBuf;
    private boolean _closed /* =false */;
    protected final LineLevelAppender   _appender;

    public LineWriter (LineLevelAppender appender) {
        _appender = Validate.notNull(appender, "No appender", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    public final LineLevelAppender getLineLevelAppender() {
        return _appender;
    }

    @Override
    public boolean isOpen () {
        return !_closed;
    }

    @Override
    public Writer append (CharSequence csq) throws IOException {
        if (csq == null) {
            return append("null");
        } else {
            return append(csq, 0, csq.length());
        }
    }

    @Override
    public Writer append (CharSequence csq, int start, int end) throws IOException {
        if (!isOpen()) {
            throw new IOException("append(" + CharSequence.class.getSimpleName() + ")[" + start + " - " + end + "] not open");
        }

        LineLevelAppender appender=getLineLevelAppender();
        if (!appender.isWriteEnabled()) {
            clearWorkBuffer();
            return this;
        }

        int   wLen=end - start;
        if (wLen <= 0) {
            return this;
        }

        StringBuilder sb=getWorkBuffer(wLen);
        int           lOffset=start;
        for (int    cOffset=lOffset; cOffset < end; cOffset++) {
            char  c=csq.charAt(cOffset);
            // if not part of the line separator then skip it
            if (c != '\n') {
                continue;
            }

            if (lOffset < cOffset) {
                sb.append(csq, lOffset, cOffset + 1 /* including this character */);
            } else {
                sb.append(c);
            }

            processAccumulatedMessage(sb);
            lOffset = cOffset + 1;  // skip current character
        }

        // check if have any leftovers
        if (lOffset < end) { // the leftover(s) have no line separator characters for sure
            sb.append(csq, lOffset, end);
        }

        return this;
    }

    @Override
    public Writer append (char c) throws IOException {
        if (!isOpen()) {
            throw new IOException("append(char=" + String.valueOf(c) + ") not open");
        }

        LineLevelAppender appender=getLineLevelAppender();
        if (!appender.isWriteEnabled()) {
            clearWorkBuffer();
            return this;
        }

        StringBuilder sb=getWorkBuffer(1);
        sb.append(c);

        if (c == '\n') {
            processAccumulatedMessage(sb);
        }

        return this;
    }
    @Override
    public void write (char[] cbuf, int off, int len) throws IOException {
        if (!isOpen()) {
            throw new IOException("write(char[])[" + off + "," + len + "] not open");
        }

        if (len <= 0) {
            return;
        }

        LineLevelAppender appender=getLineLevelAppender();
        if (!appender.isWriteEnabled()) {
            clearWorkBuffer();
            return;
        }

        StringBuilder sb=getWorkBuffer(len);
        int           lOffset=off, maxOffset=off + len;
        for (int    cOffset=lOffset; cOffset < maxOffset; cOffset++)
        {
           char  c=cbuf[cOffset];
            // if not part of the line separator then skip it
            if (c != '\n') {
                continue;
            }

            int   cLen=cOffset - lOffset;
            if (cLen > 0) {
                sb.append(cbuf, lOffset, cLen + 1 /* including this character */);
            } else {
                sb.append(c);
            }

            processAccumulatedMessage(sb);
            lOffset = cOffset + 1;  // skip current character
        }

        // check if have any leftovers
        int   remLen=maxOffset - lOffset;
        if (remLen > 0) { // the leftover(s) have no line separator characters for sure
            sb.append(cbuf, lOffset, remLen);
        }
    }

    @Override
    public void write (char[] cbuf) throws IOException {
        write(cbuf, 0, ExtendedArrayUtils.length(cbuf));
    }

    @Override
    public void write (int c) throws IOException {
        append((char) c);
    }

    @Override
    public void write (String str, int off, int len) throws IOException {
        append(str, off, len);
    }

    @Override
    public void write (String str) throws IOException {
        if (str == null) {
            append("null");
        } else {
            append(str, 0, str.length());
        }
    }

    @Override
    public void close () throws IOException {
        if (isOpen()) {
            try {   // check if any leftovers
                StringBuilder   buf=getWorkBuffer(0);
                int             dLen=buf.length();
                if (dLen > 0) {
                    if (buf.charAt(dLen - 1) == '\r') {
                        buf.setLength(dLen - 1);
                    }
                    
                    try {
                        LineLevelAppender appender=getLineLevelAppender();
                        appender.writeLineData(buf);
                    } finally {
                        buf.setLength(0);
                    }
                }
            } finally {
                _closed = true;
            }
        }
    }

    @Override
    public void flush () throws IOException {
        if (!isOpen()) {
            throw new IOException("flush() - not open");
        }
    }

    @Override
    public String toString () {
        CharSequence  o=getWorkBuffer(0);
        if (StringUtils.isEmpty(o)) {
            return "";
        } else {
            return o.toString();
        }
    }

    /**
     * Called in order to retrieve a work buffer. <B>Note:</B> the call occurs
     * every time data is to be appended. It is up to the implementor to "reset"
     * the work buffer instance after actual write takes place.
     * @param reqSize Minimum size of requested buffer size - should be used
     * in order to make a smart allocation
     * @return The {@link StringBuilder} instance to be used as the work buffer.
     * The accumulated line data is appended to it - except for the CR/LF. Once
     * end of line is detected this instance is passed to actual write method.
     * If <code>null</code> then same as write disabled.
     */
    protected StringBuilder getWorkBuffer (int reqSize) {
        int   effSize=Math.max(reqSize, Byte.MAX_VALUE);
        if (_workBuf == null) {
            _workBuf = new StringBuilder(effSize);
        } else {
            _workBuf.ensureCapacity(effSize);
        }

        return _workBuf;
    }

    protected StringBuilder clearWorkBuffer () {
        if ((_workBuf != null) && (_workBuf.length() > 0)) {
            _workBuf.setLength(0);
        }

        return _workBuf;
    }

    protected void processAccumulatedMessage (StringBuilder sb) throws IOException {
        int dLen=(sb == null) ? 0 : sb.length();
        if (dLen <= 0) {
            return;
        }

        // check if data buffer ends in line separator pattern
        if (sb.charAt(dLen - 1) != '\n') {
            return;
        }

        if ((dLen > 1) && (sb.charAt(dLen - 2) == '\r')) {
            dLen -= 2;
        } else {
            dLen--;
        }

        sb.setLength(dLen);
        try {
            LineLevelAppender appender=getLineLevelAppender();
            appender.writeLineData(sb);
        } finally {
            sb.setLength(0);
        }
    }
}
