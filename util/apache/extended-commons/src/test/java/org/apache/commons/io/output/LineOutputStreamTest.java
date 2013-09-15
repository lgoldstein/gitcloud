/* Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.output;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor Goldstein
 * @since Sep 15, 2013 11:25:45 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LineOutputStreamTest extends AbstractTestSupport {
    public LineOutputStreamTest() {
        super();
    }

    @Test
    public void testStreamCorrectness() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Cannot locate test file", file);
        
        List<String>        expected=FileUtils.readLines(file);
        final List<String>  actual=new ArrayList<String>(expected.size());
        OutputStream        output=new LineOutputStream() {
                @Override
                public void writeLineData(CharSequence lineData) throws IOException {
                    actual.add(lineData.toString());
                }
                
                @Override
                public boolean isWriteEnabled() {
                    return true;
                }
            };
        try {
            long    cpySize=FileUtils.copyFile(file, output);
            assertEquals("Mismatched copy size for " + file, file.length(), cpySize);
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
