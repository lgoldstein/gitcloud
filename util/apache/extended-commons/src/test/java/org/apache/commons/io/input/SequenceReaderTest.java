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
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 24, 2013 11:24:26 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SequenceReaderTest extends AbstractTestSupport {
    public SequenceReaderTest() {
        super();
    }

    @Test
    public void testReaderCorrectness() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Cannot locate test file", file);
        
        char[]          expected=ExtendedFileUtils.readFileToCharArray(file);
        List<Reader>    rdrs=new ArrayList<Reader>(expected.length / Byte.MAX_VALUE);
        for (int index=0; index < expected.length; index += Byte.MAX_VALUE) {
            int     availableLen=Math.min(Byte.MAX_VALUE, expected.length - index);
            rdrs.add(new CharArrayReader(expected, index, availableLen));
        }
        
        Reader  r=new SequenceReader(rdrs);
        try {
            CharArrayWriter w=new CharArrayWriter(expected.length);
            try {
                assertEquals("Mismatched copy size", expected.length, IOUtils.copyLarge(r, w));
            } finally {
                w.close();
            }
            
            char[]  actual=w.toCharArray();
            assertArrayEquals("Mismatched recovered data", expected, actual);
        } finally {
            r.close();
        }
    }

    @Test
    public void testPrematureClose() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Cannot locate test file", file);
        
        char[]          chars=ExtendedFileUtils.readFileToCharArray(file);
        List<Reader>    rdrs=new ArrayList<Reader>(chars.length / (2 * Byte.MAX_VALUE));
        for (int index=0; index < chars.length; index += Byte.MAX_VALUE) {
            int     availableLen=Math.min(Byte.MAX_VALUE, chars.length - index);
            rdrs.add(new CharArrayReader(chars, index, availableLen));
        }
        
        char[] expected=new char[Byte.MAX_VALUE], actual=new char[expected.length];
        System.arraycopy(chars, 0, expected, 0, expected.length);
        
        Reader  r=new SequenceReader(rdrs);
        try {
            IOUtils.readFully(r, actual);
            assertArrayEquals("Mismatched recovered data", expected, actual);
        } finally {
            r.close();
        }
        
        assertEquals("Unexpected post-close read", (-1), r.read(actual));
    }
    
    @Test
    public void testSkip() throws IOException {
        File    file=getTestJavaSourceFile();
        assertNotNull("Cannot locate test file", file);

        char[]          chars=ExtendedFileUtils.readFileToCharArray(file);
        List<Reader>    rdrs=new ArrayList<Reader>(chars.length / (2 * Byte.MAX_VALUE));
        for (int index=0; index < chars.length; index += Byte.MAX_VALUE) {
            int     availableLen=Math.min(Byte.MAX_VALUE, chars.length - index);
            rdrs.add(new CharArrayReader(chars, index, availableLen));
        }
        
        char[]  expected=new char[2 * Byte.MAX_VALUE];
        System.arraycopy(chars, 0, expected, 0, Byte.MAX_VALUE);
        System.arraycopy(chars, chars.length - Byte.MAX_VALUE, expected, Byte.MAX_VALUE, Byte.MAX_VALUE);

        Reader  rdr=new SequenceReader(rdrs);
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
    }
}
