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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.output.LineLevelAppender;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 25, 2013 10:20:08 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HexDumpOutputStreamTest extends AbstractTestSupport {
    public HexDumpOutputStreamTest() {
        super();
    }

    @Test
    public void testStreamCorrectness() throws IOException {
        URL url=ExtendedClassUtils.getClassBytesURL(getClass());
        assertNotNull("Cannot locate test bytes", url);
        
        byte[]              data=IOUtils.toByteArray(url);
        StringBuilderWriter writer=new StringBuilderWriter(data.length * 4 + Long.SIZE);
        try {
            WriterOutputStream  output=new WriterOutputStream(writer);
            try {
                HexDump.dump(data, 0L, output, 0);
            } finally {
                output.close();
            }
        } finally {
            writer.close();
        }
        
        String          strData=writer.toString();
        String[]        lines=StringUtils.splitByWholeSeparator(strData, HexDump.EOL);
        List<String>    expected=new ArrayList<String>(Arrays.asList(lines));
        // compensate for last EOL
        if (StringUtils.isEmpty(StringUtils.trimToEmpty(lines[lines.length - 1]))) {
            expected.remove(lines.length - 1);
        }

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
        OutputStream    output=new HexDumpOutputStream(appender);
        try {
            output.write(data);
        } finally {
            output.close();
        }

        assertEquals("Mismatched number of lines", expected.size(), actual.size());
        for (int    index=0; index < expected.size(); index++) {
            String  expLine=expected.get(index), actLine=actual.get(index);
            assertEquals("Mismatched line data #" + (index + 1), expLine, actLine);
        }
    }
}
