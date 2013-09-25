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

package org.apache.commons.net.ssh.keys.rsa;

import java.util.Arrays;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 16, 2013 9:37:29 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RSAUtilsTest extends AbstractTestSupport {
    public RSAUtilsTest() {
        super();
    }
    
    @Test
    public void testPkcsV15PadType2() {
        byte[]      expected=new byte[Long.SIZE], actual=new byte[expected.length];
        final int   PADDED_SIZE=expected.length * Byte.SIZE;
        for (int index=0; index < Byte.SIZE; index++) {
            synchronized(RANDOMIZER) {
                RANDOMIZER.nextBytes(expected);
            }
            
            final byte[]    padded;
            synchronized(RANDOMIZER) {
                padded = RSAUtils.pkcsV15PadType2(RANDOMIZER, PADDED_SIZE, expected);
            }
            
            String  ds=Arrays.toString(expected), ps=Arrays.toString(padded), sv=ds + ": " + ps;
            assertEquals("Mismatched padded size for " + sv, PADDED_SIZE, padded.length);
            assertEquals("Mismatched pad octet #0 for " + sv, 0, padded[0]);
            assertEquals("Mismatched pad octet #1 for " + sv, RSAUtils.PAD_V15_BLOCKTYPE_2, padded[1]);
            
            for (int    pi=2; pi < padded.length - expected.length - 1; pi++) {
                assertNotEquals("Unexpected zero byte at index=" + pi + " of " + sv, 0, padded[pi]);
            }

            assertEquals("Mismatched pre-data pad octet for " + sv, 0, padded[padded.length - expected.length - 1]);

            System.arraycopy(padded, padded.length - expected.length, actual, 0, expected.length);
            assertArrayEquals("Mismatched padded data for " + sv, expected, actual);
        }
    }
}
