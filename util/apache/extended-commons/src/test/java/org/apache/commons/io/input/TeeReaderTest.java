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

import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CharArrayWriter;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 25, 2013 9:04:02 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TeeReaderTest extends AbstractTestSupport {
    public TeeReaderTest() {
        super();
    }

    @Test
    public void testReaderCorrectness() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Test file not found", file);
        
        char[]  expected=ExtendedFileUtils.readFileToCharArray(file);
        CharArrayWriter writer=new CharArrayWriter(expected.length);
        try {
            Reader  r=new TeeReader(new CharArrayReader(expected), writer);
            try {
                assertEquals("Mismatched copy length", expected.length, IOUtils.copyLarge(r, NullWriter.NULL_WRITER));
            } finally {
                r.close();
            }
        } finally {
            writer.close();
        }
        
        char[]  actual=writer.toCharArray();
        assertArrayEquals("Mismatched re-constructed data", expected, actual);
    }
}
