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
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 24, 2013 10:39:25 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CharArrayAccumulatingReaderTest extends AbstractTestSupport {
    public CharArrayAccumulatingReaderTest() {
        super();
    }

    @Test
    public void testReaderCorrectness() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Test file not found", file);
        
        char[]                      expected=ExtendedFileUtils.readFileToCharArray(file);
        CharArrayAccumulatingReader rdr=new CharArrayAccumulatingReader(new CharArrayReader(expected));
        try {
            CharArrayWriter writer=new CharArrayWriter(Byte.SIZE);
            try {
                assertEquals("Mismatched copy size", file.length(), IOUtils.copyLarge(rdr, writer, new char[2 * Byte.MAX_VALUE]));
            } finally {
                writer.close();
            }
            
            char[]  actual=writer.toCharArray();
            assertArrayEquals("Mismatched read data", expected, actual);
        } finally {
            rdr.close();
        }

        char[]  actual=rdr.toCharArray();
        assertArrayEquals("Mismatched accumulated data", expected, actual);
    }

    @Test
    public void testReaderReset() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Test file not found", file);
        
        char[]                      expected=ExtendedFileUtils.readFileToCharArray(file);
        CharArrayAccumulatingReader rdr=new CharArrayAccumulatingReader(new CharArrayReader(expected));
        try {
            char[]  actual=new char[expected.length];
            for (int    index=0; index < Byte.SIZE; index++) {
                IOUtils.readFully(rdr, actual, 0, Byte.MAX_VALUE);
                rdr.reset();
                assertArrayEquals("Reset trial #" + index, expected, actual, 0, Byte.MAX_VALUE);
            }
            
            IOUtils.readFully(rdr, actual);
            assertArrayEquals("Mismatched read data", expected, actual);
        } finally {
            rdr.close();
        }

        char[]  actual=rdr.toCharArray();
        assertArrayEquals("Mismatched accumulated data", expected, actual);
    }

    @Test
    public void testReaderSkip() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Test file not found", file);
        
        char[] chars=ExtendedFileUtils.readFileToCharArray(file), expected=new char[2 * Byte.MAX_VALUE];
        System.arraycopy(chars, 0, expected, 0, Byte.MAX_VALUE);
        System.arraycopy(chars, chars.length - Byte.MAX_VALUE, expected, Byte.MAX_VALUE, Byte.MAX_VALUE);
        CharArrayAccumulatingReader rdr=new CharArrayAccumulatingReader(new CharArrayReader(chars));
        try {
            char[]  actual=new char[expected.length];
            assertEquals("Mismatched start bytes read length", actual.length / 2, rdr.read(actual, 0, actual.length / 2));
            
            long    toSkip=chars.length - actual.length, skipped=rdr.skip(toSkip);
            assertEquals("Mismatched skipped length", toSkip, skipped);
            assertEquals("Mismatched end bytes read length", actual.length / 2, rdr.read(actual, actual.length / 2, actual.length / 2));
            assertArrayEquals("Mismatched re-read data", expected, actual);
            assertEquals("Unexpected data beyond EOF", (-1), rdr.read(actual));
        } finally {
            rdr.close();
        }

        char[]  actual=rdr.toCharArray();
        assertArrayEquals("Mismatched accumulated data", expected, actual);
    }
}
