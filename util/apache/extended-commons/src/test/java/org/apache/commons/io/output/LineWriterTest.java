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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
    public void testWriterCorrectness() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Cannot locate test file", file);
        
        List<String>        expected=FileUtils.readLines(file);
        final List<String>  actual=new ArrayList<String>(expected.size());
        LineLevelAppender   appender=new LineLevelAppender() {
                @Override
                public void writeLineData(CharSequence lineData) throws IOException {
                    actual.add(lineData.toString());
                }
                
                @Override
                public boolean isWriteEnabled() {
                    return true;
                }
            };
        LineWriter          output=new LineWriter(appender);
        try {
            Reader  input=new FileReader(file);
            try {
                long    cpySize=IOUtils.copyLarge(input, output);
                assertEquals("Mismatched copy size for " + file, file.length(), cpySize);
            } finally {
                input.close();
            }
        } finally {
            output.close();
        }
        
        assertEquals("Mismatched number of lines", expected.size(), actual.size());
        for (int    index=0; index < expected.size(); index++) {
            String  expLine=expected.get(index), actLine=actual.get(index);
            assertEquals("Mismatched line data #" + (index + 1), expLine, actLine);
        }
    }

    @Test
    public void testNoWriteIfNotEnabled() throws IOException {
        LineLevelAppender   appender=new LineLevelAppender() {
                @Override
                public boolean isWriteEnabled() {
                    return false;
                }
    
                @Override
                public void writeLineData(CharSequence lineData) throws IOException {
                    fail("Unexpected call: " + lineData);
                }
            };
        Writer  writer=new LineWriter(appender);
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
        LineLevelAppender   appender=new LineLevelAppender() {
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
        int     expectedLineCount=0;
        Writer  writer=new LineWriter(appender);
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
