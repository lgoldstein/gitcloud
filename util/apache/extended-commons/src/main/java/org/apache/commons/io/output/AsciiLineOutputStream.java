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

package org.apache.commons.io.output;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * An optimized {@link LineOutputStream} for US-ASCII characters
 * @author Lyor G.
 * @since Sep 22, 2013 3:13:44 PM
 */
public class AsciiLineOutputStream extends LineOutputStream {
    public static final Charset CHARSET=Charset.forName("US-ASCII");

    public AsciiLineOutputStream(LineLevelAppender appender) {
        super(CHARSET, appender);
    }

    @Override
    protected void writeAccumulatedData(byte[] b, int off, int len) throws IOException {
        LineLevelAppender appender=getLineLevelAppender();
        if (len <= 0) {
            appender.writeLineData("");
            return;
        }
        
        char[]  chars=ensureCharDataCapacity(len);
        for (int index=0, pos=off; index < len; index++, pos++) {
            chars[index] = (char) (b[pos] & 0x00FF);
        }
        
        CharSequence    lineData=(chars[len - 1] == '\r') ? CharBuffer.wrap(chars, 0, len - 1) : CharBuffer.wrap(chars, 0, len);
        appender.writeLineData(lineData);
    }
}
