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

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 24, 2013 10:22:32 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CharArrayWriterTest extends AbstractTestSupport {
    public CharArrayWriterTest() {
        super();
    }

    @Test
    public void testWriterCorrectness() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Cannot locate test file", file);
        
        char[]          expected=ExtendedFileUtils.readFileToCharArray(file);
        CharArrayWriter writer=new CharArrayWriter(expected.length / 8 /* just so we use a few buffers */);
        try {
            for (int    pos=0; pos < expected.length; pos += Byte.MAX_VALUE) {
                int writeLen=Math.min(Byte.MAX_VALUE, expected.length - pos);
                writer.write(expected, pos, writeLen);
            }
        } finally {
            writer.close();
        }
        
        char[]  actual=writer.toCharArray();
        assertArrayEquals("Mismatched recovered data", expected, actual);
    }

    @Test
    public void testWriteStrings() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Cannot locate test file", file);

        String          expected=FileUtils.readFileToString(file);
        CharArrayWriter writer=new CharArrayWriter(expected.length() / 8 /* just so we use a few buffers */);
        try {
            for (int    pos=0; pos < expected.length(); pos += Byte.MAX_VALUE) {
                int writeLen=Math.min(Byte.MAX_VALUE, expected.length() - pos);
                writer.write(expected, pos, writeLen);
            }
        } finally {
            writer.close();
        }
        
        String  actual=writer.toString();
        assertEquals("Mismatched recovered data", expected, actual);
    }

    @Test
    public void testWriterReset() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Cannot locate test file", file);
        
        char[]          expected=ExtendedFileUtils.readFileToCharArray(file);
        CharArrayWriter writer=new CharArrayWriter(expected.length / 8 /* just so we use a few buffers */);
        try {
            for (int    index=0; index < Byte.SIZE; index++) {
                writer.reset();

                for (int    pos=0; pos < expected.length; pos += Byte.MAX_VALUE) {
                    int writeLen=Math.min(Byte.MAX_VALUE, expected.length - pos);
                    writer.write(expected, pos, writeLen);
                }
            }
        } finally {
            writer.close();
        }
        
        char[]  actual=writer.toCharArray();
        assertArrayEquals("Mismatched recovered data", expected, actual);
    }
    
    @Test
    public void testToReader() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Cannot locate test file", file);
        
        char[]          expected=ExtendedFileUtils.readFileToCharArray(file);
        CharArrayWriter writer=new CharArrayWriter(expected.length / 8 /* just so we use a few buffers */);
        try {
            writer.write(expected);
        } finally {
            writer.close();
        }
        
        Reader  rdr=writer.toReader();
        try {
            char[]  actual=IOUtils.toCharArray(rdr);
            assertArrayEquals("Mismatched recovered data", expected, actual);
        } finally {
            rdr.close();
        }
    }
}
