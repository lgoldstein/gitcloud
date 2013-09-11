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

package org.apache.commons.codec.binary;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.nio.charset.Charset;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedCharSequenceUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lyor G.
 */
public class ExtendedHex extends Hex {
    /**
     * Used to build output as lowercase
     */
    public static final String DIGITS_LOWER="0123456789abcdef";

    /**
     * Used to build output as as uppercase
     */
    public static final String DIGITS_UPPER="0123456789ABCDEF";

    public ExtendedHex() {
        this(DEFAULT_CHARSET);
    }

    public ExtendedHex(String charsetName) {
        this(Charset.forName(charsetName));
    }

    public ExtendedHex(Charset charset) {
        super(charset);
    }

    public static final String encodeHexString(boolean toLowerCase, CharSequence separator, byte ... data) {
        return encodeHexString(toLowerCase, separator, data, 0, ExtendedArrayUtils.length(data));
    }

    public static final String encodeHexString(boolean toLowerCase, CharSequence separator, byte[] data, int offset, int len) {
        if (len <= 0) {
            return "";
        }
        
        try {
            return appendHexData(new StringBuilder(len * (2 + ExtendedCharSequenceUtils.getSafeLength(separator))), toLowerCase, separator, data, offset, len).toString();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final <A extends Appendable> A appendHexData(A sb, boolean toLowerCase, byte ... data) throws IOException {
        return appendHexData(sb, toLowerCase, "", data);
    }

    public static final <A extends Appendable> A appendHexData(A sb, boolean toLowerCase, CharSequence separator, byte ... data) throws IOException {
        return appendHexData(sb, toLowerCase, separator, data, 0, ExtendedArrayUtils.length(data));
    }

    public static final <A extends Appendable> A appendHexData(A sb, boolean toLowerCase, CharSequence separator, byte[] data, int offset, int len) throws IOException {
        boolean haveSeparator=!StringUtils.isEmpty(separator);
        for (int index=0, pos=offset; index < len; index++, pos++) {
            if (haveSeparator && (index > 0)) {
                sb.append(separator);
            }
            appendHex(sb, toLowerCase, data[pos]);
        }

        return sb;
    }

    public static final <A extends Appendable> A appendHex(A sb, boolean toLowerCase, byte value) throws IOException {
        appendHexChar(sb, toLowerCase, (value >> 4) & 0x0F);
        appendHexChar(sb, toLowerCase, value & 0x0F);
        return sb;
    }

    public static final <A extends Appendable> A appendHexChar (A sb, boolean toLowerCase, int value) throws IOException {
        String  charset=toLowerCase ? DIGITS_LOWER : DIGITS_UPPER;
        if ((value < 0) || (value >= charset.length())) {
            throw new StreamCorruptedException("appendHexChar - bad nibble: " + value);
        }

        sb.append(charset.charAt(value));
        return sb;
    }

    public static final String encodeHexString(boolean toLowerCase, byte ... data) {
        if (ArrayUtils.isEmpty(data)) {
            return "";
        } else {
            return new String(encodeHex(data, toLowerCase));
        }
    }

}
