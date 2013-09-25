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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.commons.io.output.AsciiLineOutputStream;
import org.apache.commons.io.output.LineLevelAppender;
import org.apache.commons.io.output.LineOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * An {@link InputStream} that reads data until LF is encountered
 * and then outputs this data line-by-line using the provided {@link LineLevelAppender}
 * @author Lyor G.
 * @since Sep 15, 2013 12:06:24 PM
 */
public class LineInputStream extends ExtendedTeeInputStream {
    public LineInputStream(InputStream input, LineLevelAppender appender) {
        this(input, Charset.defaultCharset(), appender);
    }

    public LineInputStream(InputStream input, boolean useAscii, LineLevelAppender appender) {
        super(Validate.notNull(input, "No input", ArrayUtils.EMPTY_OBJECT_ARRAY),
              useAscii ? new AsciiLineOutputStream(appender) : new LineOutputStream(appender),
              true);
    }

    public LineInputStream(InputStream input, String charset, LineLevelAppender appender) {
        this(input, Charset.forName(Validate.notEmpty(charset, "No charset name", ArrayUtils.EMPTY_OBJECT_ARRAY)), appender);
    }

    public LineInputStream(InputStream input, Charset charset, LineLevelAppender appender) {
        this(input, Validate.notNull(charset, "No charset", ArrayUtils.EMPTY_OBJECT_ARRAY).newDecoder(), appender);
    }

    public LineInputStream(InputStream input, CharsetDecoder decoder, LineLevelAppender appender) {
        super(Validate.notNull(input, "No input", ArrayUtils.EMPTY_OBJECT_ARRAY), new LineOutputStream(decoder, appender), true);
    }
}
