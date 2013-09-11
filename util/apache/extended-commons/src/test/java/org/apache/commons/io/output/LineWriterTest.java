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
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Aug 13, 2013 8:22:53 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LineWriterTest extends AbstractTestSupport {
    public LineWriterTest() {
        super();
    }

    @Test
    public void testNoWriteIfNotEnabled() throws IOException {
        Writer  writer=new LineWriter() {
                @Override
                public boolean isWriteEnabled() {
                    return false;
                }
    
                @Override
                public void writeLineData(CharSequence lineData) throws IOException {
                    fail("Unexpected call: " + lineData);
                }
            };

        try {
            for (int index=0; index < Byte.MAX_VALUE; index++) {
                writer.append(String.valueOf(index)).append(SystemUtils.LINE_SEPARATOR);
            }
        } finally {
            writer.close();
        }
    }
    
    @Test
    public void testOnlyFullLinesWritten() throws IOException {
        final AtomicInteger lineCount=new AtomicInteger(0);
        Writer  writer=new LineWriter() {
                private final AtomicInteger expectedValue=new AtomicInteger(0);

                @Override
                public boolean isWriteEnabled() {
                    return true;
                }
    
                @Override
                public void writeLineData(CharSequence lineData) throws IOException {
                    lineCount.incrementAndGet();

                    String[]    vals=StringUtils.split(lineData.toString(), ',');
                    for (String v : vals) {
                        assertEquals("Mismatched expected value", expectedValue.incrementAndGet(), Integer.parseInt(v));
                    }
                }
            };

        int expectedLineCount=0;
        try {
            for (int index=1; index <= Byte.MAX_VALUE; index++) {
                writer.append(String.valueOf(index));
                if ((index & 0x01) == 0) {
                    writer.append(SystemUtils.LINE_SEPARATOR);
                    expectedLineCount++;
                } else {
                    writer.append(',');
                }
            }

            String  curData=writer.toString();
            if (!StringUtils.isEmpty(curData)) {
                expectedLineCount++;
            }
        } finally {
            writer.close();
        }
        
        assertEquals("Mismatched line count value", expectedLineCount, lineCount.intValue());
    }
}
