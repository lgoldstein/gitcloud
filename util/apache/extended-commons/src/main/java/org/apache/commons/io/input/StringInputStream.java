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

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * A simple {@link ByteArrayInputStream} that uses the bytes of an input {@link String}.
 * <B>Note:</B> this is a simpler version of the {@link CharSequenceInputStream}
 * @author Lyor G.
 */
public class StringInputStream extends ByteArrayInputStream {
    public StringInputStream(String data) {
        this(data, Charset.defaultCharset());
    }

    public StringInputStream(String data, String charsetName) {
        this(data, Charset.forName(charsetName));
    }

    public StringInputStream(String data, Charset charset) {
        super(StringUtils.isEmpty(data) ? ArrayUtils.EMPTY_BYTE_ARRAY : data.getBytes(Charsets.toCharset(charset)));
    }
}
