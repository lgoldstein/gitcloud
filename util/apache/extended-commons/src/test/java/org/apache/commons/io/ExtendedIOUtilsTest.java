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

package org.apache.commons.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triplet;
import org.apache.commons.test.AbstractTestSupport;
import org.apache.commons.test.MicroBenchmark;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedIOUtilsTest extends AbstractTestSupport {
	private final byte[]	TEST_DATA=new byte[ExtendedIOUtils.DEFAULT_BUFFER_SIZE_VALUE];

	public ExtendedIOUtilsTest() {
		super();
	}

	@Before
	public void setUp() {
	    synchronized(RANDOMIZER) {
	        RANDOMIZER.nextBytes(TEST_DATA);
	    }
	}

    @Test
    public void testCopyToAppendable() throws IOException {
        String  expected=getClass().getCanonicalName() + "#testCopyToAppendable"
                    + "@" + new Date(System.currentTimeMillis())
                    + "-" + System.nanoTime()
                    + ": " + Math.random()
                    ;
        Appendable  out=new StringBuilder(expected.length());
        Reader      rdr=new StringReader(expected);
        try {
            // use a small buffer on purpose to force multiple read/write cycles
            // use a "partial" buffer to test fully the code
            int         count=ExtendedIOUtils.append(rdr, out, new char[2 * Long.SIZE], 4, Long.SIZE);
            assertEquals("Mismatch copy count", expected.length(), count);
        } finally {
            rdr.close();
        }

        String  actual=out.toString();
        assertEquals("Mismatched read data", expected, actual);
    }

    @Test
    @Category(MicroBenchmark.class)
    public void testCopyCharsPerformance() throws IOException {
        String  data=getClass().getCanonicalName() + "#testCopyCharsPerformance"
                + "@" + new Date(System.currentTimeMillis())
                + "-" + System.nanoTime()
                + ": " + Math.random()
                ;
        for (boolean useWriter : new boolean[] { true, false }) {
            testCopyCharsPerformance(useWriter, data);
        }
    }

	@Test
	public void testExactCopySize() throws IOException {
		final int					size=Byte.SIZE + RANDOMIZER.nextInt(TEST_DATA.length - Long.SIZE);
		final InputStream			in=new ByteArrayInputStream(TEST_DATA);
		final ByteArrayOutputStream	out=new ByteArrayOutputStream(size);
		final int					cpySize=ExtendedIOUtils.copy(in, out, size);
		assertEquals("Mismatched copy size", size, cpySize);

		final byte[]	subArray=ArrayUtils.subarray(TEST_DATA, 0, size),
						outArray=out.toByteArray();
		assertArrayEquals("Mismatched data", subArray, outArray);
	}

	@Test
	public void testCopyOverSize() throws IOException {
		final InputStream			in=new ByteArrayInputStream(TEST_DATA);
		final ByteArrayOutputStream	out=new ByteArrayOutputStream(TEST_DATA.length);
		final int					cpySize=ExtendedIOUtils.copy(in, out, TEST_DATA.length + Long.SIZE);
		assertEquals("Mismatched copy size", TEST_DATA.length, cpySize);

		final byte[]	outArray=out.toByteArray();
		assertArrayEquals("Mismatched data", TEST_DATA, outArray);
	}

	@Test
	public void testCopyUnknownSize() throws IOException {
		final InputStream			in=new ByteArrayInputStream(TEST_DATA);
		final ByteArrayOutputStream	out=new ByteArrayOutputStream(TEST_DATA.length);
		final int					cpySize=ExtendedIOUtils.copy(in, out, (-1));
		assertEquals("Mismatched copy size", TEST_DATA.length, cpySize);

		final byte[]	outArray=out.toByteArray();
		assertArrayEquals("Mismatched data", TEST_DATA, outArray);
	}

	@Test
	public void testFindDifference() throws IOException {
	    byte[] data1={ 3, 7, 7, 7, 3, 4, 7, 1, 0, 2, 8, 1, 7, 1, 3, 7, 3, 6, 5 }, data2=data1.clone();
	    for (int   offset=-1; offset <= data1.length; offset++) {
	        byte   modValue=0;
            if ((offset >= 0) && (offset < data1.length)) {
                modValue = data1[offset];
                data1[offset] = (byte) (data2[offset] + 1);
                assertNotEquals("Cannot modify data at offset=" + offset, data1[offset], data2[offset]);
            }

            String a1=Arrays.toString(data1), a2=Arrays.toString(data2);
	        try {
    	        InputStream    s1=null, s2=null;
    	        try {
    	            s1 = new ByteArrayInputStream(data1);
    	            s2 = new ByteArrayInputStream(data2);
    	            
    	            Triplet<Long,Byte,Byte>    cmpRes=ExtendedIOUtils.findDifference(s1, s2);
    	            if ((offset >= 0) && (offset < data1.length)) {
    	                assertNotNull("Unexpected equality for offset=" + offset + " on " + a1 + " vs. " + a2, cmpRes);
    	                assertEquals("Mismatched offset on "+ a1 + " vs. " + a2, offset, cmpRes.getV1().longValue());
    	                
    	                Byte   v1=cmpRes.getV2(), v2=cmpRes.getV3();
    	                assertEquals("Mismatched diff #1 values at offset=" + offset + " for " + a1 + " vs. " + a2, data1[offset], v1.byteValue());
                        assertEquals("Mismatched diff #2 values at offset=" + offset + " for " + a1 + " vs. " + a2, data2[offset], v2.byteValue());
                        assertNotEquals("Unexpected equal values at offset=" + offset + " for " + a1 + " vs. " + a2, v1.byteValue(), v2.byteValue());
    	            } else {
    	                assertNull("Unexpected difference for " + a1 + " vs. " + a2, cmpRes);
    	            }
    	        } finally {
    	            ExtendedIOUtils.closeAll(s1, s2);
    	        }
	        } finally {    // restore data arrays equality
                if ((offset >= 0) && (offset < data1.length)) {
                    data1[offset] = modValue;
                    data2[offset] = modValue;
                }
	        }
	    }
	}

    private void testCopyCharsPerformance (boolean useWriter, String data) throws IOException {
        Reader          rdr=new StringReader(data);
        StringWriter    wrt=new StringWriter(data.length());
        StringBuffer    buf=wrt.getBuffer();
        Appendable      out=useWriter ? wrt : buf;
        
        System.out.println(" ================ use writer=" + useWriter + " ================");
        System.out.printf("%10s %20s %20s", "Num. calls", "Duration (nano)", "Used memory (B)");
        System.out.println();

        final Runtime   RUNTIME=Runtime.getRuntime();
        // use a small buffer on purpose to force multiple read/write cycles
        final char[]    buffer=new char[Long.SIZE];
        for (final int  NUM_CALLS : new int[] { 1, 2, 5 }) {
            long    totalTime=0L, totalUsed=0L;
            for (int    cIndex=0; cIndex < NUM_CALLS; cIndex++) {
                // prepare for next iteration
                rdr.reset();
                buf.setLength(0);
                encourageGC();
                
                long    startFree=RUNTIME.freeMemory(), startTime=System.nanoTime();
                int count=ExtendedIOUtils.append(rdr, out, buffer);
                long    endFree=RUNTIME.freeMemory(), endTime=System.nanoTime();

                totalUsed += (endFree - startFree);
                totalTime += (endTime - startTime);
                assertEquals("Mismatched copy count", count, data.length());
            }

            System.out.printf("%10d %20d %20d", Integer.valueOf(NUM_CALLS), Long.valueOf(totalTime), Long.valueOf(totalUsed));
            System.out.println();
        }
    }
}
