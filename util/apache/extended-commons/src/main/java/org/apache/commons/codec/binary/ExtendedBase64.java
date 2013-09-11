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

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.ExtendedArrayUtils;

/**
 * @author lgoldstein
 */
public class ExtendedBase64 extends Base64 {
	public ExtendedBase64() {
		super();
	}

	public ExtendedBase64(boolean urlSafe) {
		super(urlSafe);
	}

	public ExtendedBase64(int lenOfLine) {
		super(lenOfLine);
	}

	public ExtendedBase64(int lenOfLine, byte ... lineSeparator) {
		super(lenOfLine, lineSeparator);
	}

	public ExtendedBase64(int lenOfLine, byte[] lineSeparator, boolean urlSafe) {
		super(lenOfLine, lineSeparator, urlSafe);
	}

	public static final String decodeBase64ToString(String base64String) throws UnsupportedEncodingException {
		return decodeBase64ToString(base64String, "UTF-8");
	}

	public static final String decodeBase64ToString(String base64String, String charset) throws UnsupportedEncodingException {
		byte[]	data=decodeBase64(base64String);
		if (ExtendedArrayUtils.length(data) <= 0) {
			return "";
		} else {
			return new String(data, charset);
		}
	}

    public static final String decodeBase64ToString(String charset, byte ... bytes) throws UnsupportedEncodingException {
        byte[]  data=decodeBase64(bytes);
        if (ExtendedArrayUtils.length(data) <= 0) {
            return "";
        } else {
            return new String(data, charset);
        }
    }

	public static final String decodeBase64ToString(byte ... bytes) throws UnsupportedEncodingException {
		return decodeBase64ToString("UTF-8", bytes);
	}
}
