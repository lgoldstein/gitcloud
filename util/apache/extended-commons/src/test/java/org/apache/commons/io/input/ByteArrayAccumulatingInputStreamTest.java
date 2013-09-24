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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 24, 2013 8:18:54 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ByteArrayAccumulatingInputStreamTest extends AbstractTestSupport {
    public ByteArrayAccumulatingInputStreamTest() {
        super();
    }

    @Test
    public void testStreamCorrectness() throws IOException {
        URL url=ExtendedClassUtils.getClassBytesURL(getClass());
        assertNotNull("Cannot locate class bytes", url);
        
        byte[]  expected=IOUtils.toByteArray(url);
        ByteArrayAccumulatingInputStream    input=
                new ByteArrayAccumulatingInputStream(new ByteArrayInputStream(expected), expected.length);
        try {
            byte[]  actual=IOUtils.toByteArray(input);
            assertArrayEquals("Mismatched re-read data", expected, actual);
        } finally {
            input.close();
        }
        
        byte[]  actual=input.toByteArray();
        assertArrayEquals("Mismatched accumulated data", expected, actual);
    }

    @Test
    public void testStreamReset() throws IOException {
        URL url=ExtendedClassUtils.getClassBytesURL(getClass());
        assertNotNull("Cannot locate class bytes", url);
        
        byte[]  expected=IOUtils.toByteArray(url);
        ByteArrayAccumulatingInputStream    input=
                new ByteArrayAccumulatingInputStream(new ByteArrayInputStream(expected), expected.length);
        try {
            byte[]  readBuf=new byte[expected.length / 8];
            for (int index=0; index < Byte.SIZE; index++) {
                input.read(readBuf);
                input.reset();
            }

            byte[]  actual=IOUtils.toByteArray(input);
            assertArrayEquals("Mismatched re-read data", expected, actual);
        } finally {
            input.close();
        }

        byte[]  actual=input.toByteArray();
        assertArrayEquals("Mismatched accumulated data", expected, actual);
    }

    @Test
    public void testStreamSkip() throws IOException {
        URL url=ExtendedClassUtils.getClassBytesURL(getClass());
        assertNotNull("Cannot locate class bytes", url);
        
        byte[]  expected=new byte[2 * Byte.MAX_VALUE];
        byte[]  bytes=IOUtils.toByteArray(url);
        System.arraycopy(bytes, 0, expected, 0, Byte.MAX_VALUE);
        System.arraycopy(bytes, bytes.length - Byte.MAX_VALUE, expected, Byte.MAX_VALUE, Byte.MAX_VALUE);
        ByteArrayAccumulatingInputStream    input=
                new ByteArrayAccumulatingInputStream(new ByteArrayInputStream(bytes), expected.length);
        try {
            byte[]  actual=new byte[expected.length];
            assertEquals("Mismatched start bytes read length", actual.length / 2, input.read(actual, 0, actual.length / 2));
            
            long    toSkip=bytes.length - actual.length, skipped=input.skip(toSkip);
            assertEquals("Mismatched skipped length", toSkip, skipped);
            assertEquals("Mismatched end bytes read length", actual.length / 2, input.read(actual, actual.length / 2, actual.length / 2));
            assertArrayEquals("Mismatched re-read data", expected, actual);
            assertEquals("Unexpected data beyond EOF", (-1), input.read(actual));
        } finally {
            input.close();
        }

        byte[]  actual=input.toByteArray();
        assertArrayEquals("Mismatched accumulated data", expected, actual);
    }
}
