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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.LineLevelAppender;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 15, 2013 12:15:10 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LineInputStreamTest extends AbstractTestSupport {
    public LineInputStreamTest() {
        super();
    }

    @Test
    public void testLineInputStream() throws IOException {
        testStreamCorrectness(false);
    }

    @Test
    public void testAsciiLineInputStream() throws IOException {
        testStreamCorrectness(true);
    }

    private void testStreamCorrectness(boolean useAscii) throws IOException {
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
        LineInputStream     input=new LineInputStream(new FileInputStream(file), useAscii, appender);
        try {
            long    cpySize=IOUtils.copyLarge(input, NullOutputStream.NULL_OUTPUT_STREAM);
            assertEquals("Mismatched copy size for " + file, file.length(), cpySize);
        } finally {
            input.close();
        }
        
        assertEquals("Mismatched number of lines", expected.size(), actual.size());
        for (int    index=0; index < expected.size(); index++) {
            String  expLine=expected.get(index), actLine=actual.get(index);
            assertEquals("Mismatched line data #" + (index + 1), expLine, actLine);
        }
    }
}
