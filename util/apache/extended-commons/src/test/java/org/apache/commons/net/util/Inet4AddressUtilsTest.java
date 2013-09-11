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

package org.apache.commons.net.util;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jul 31, 2013 9:13:09 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Inet4AddressUtilsTest extends AbstractTestSupport {
    public Inet4AddressUtilsTest() {
        super();
    }

    @Test
    public void testIsLoopback() {
        assertTrue(Inet4AddressUtils.LOCALHOST_NAME + " ?", Inet4AddressUtils.isLoopbackAddress(Inet4AddressUtils.LOCALHOST_NAME));
        assertTrue(Inet4AddressUtils.LOOPBACK_ADDRESS + " ?", Inet4AddressUtils.isLoopbackAddress(Inet4AddressUtils.LOOPBACK_ADDRESS));
        assertTrue(Inet4AddressUtils.LOOPBACK_IP + " ?", Inet4AddressUtils.isLoopbackAddress(Inet4AddressUtils.LOOPBACK_IP.longValue()));
        assertTrue(Inet4AddressUtils.LOOPBACK_ADDRESS + "(bytes) ?", Inet4AddressUtils.isLoopbackAddress(Inet4AddressUtils.fromString(Inet4AddressUtils.LOOPBACK_ADDRESS)));

        StringBuilder   sb=new StringBuilder(16);
        for (int    index=0; index < Long.SIZE; index++) {
            sb.setLength(0);
            sb.append("127");
            for (int    j=0; j < 3; j++) {
                sb.append('.').append(RANDOMIZER.nextInt(256));
            }

            String  ip=sb.toString();
            assertTrue(ip + "? ", Inet4AddressUtils.isLoopbackAddress(ip));
        }

        for (String ip : new String[] { null, "", "foo", "1.2.3.4.5", "1..2.3.4", "1.a.2.3", "1.2.3.4/5", "7.3.6.5" }) {
            assertFalse(ip + "? ", Inet4AddressUtils.isLoopbackAddress(ip));
        }
    }
}
