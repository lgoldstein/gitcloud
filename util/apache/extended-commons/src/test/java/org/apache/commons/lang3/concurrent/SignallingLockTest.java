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

package org.apache.commons.lang3.concurrent;

import java.util.concurrent.TimeUnit;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SignallingLockTest extends AbstractTestSupport {
    public SignallingLockTest() {
        super();
    }

    @Test
    public void testMultipleOffers () throws InterruptedException {
        SignallingLock  lock=new SignallingLock();
        assertFalse("Mismatched initial state", lock.peek());
        assertTrue("Multiple initial offer", lock.offer());
        assertFalse("Bad initial signal value", lock.offer());

        for (int    index=0; index < Byte.SIZE; index++) {
            assertFalse("Signal reset after " + index + " offers", lock.offer());
            assertTrue("Bad signal value after " + index + " offers", lock.peek());
        }

        assertTrue("No signalling", lock.poll(10L));
        for (int    index=0; index < Byte.SIZE; index++) {
            assertFalse("Unexpected signalling after " + index + " polls", lock.poll(10L));
            assertFalse("Bad signal value after " + index + " polls", lock.peek());
        }
    }

    @Test
    public void testPoll () throws InterruptedException {
        SignallingLock  lock=new SignallingLock();
        final long      WAIT_TIME=125L;
        long            waitStart=System.nanoTime();
        assertFalse("Mismatched initial state", lock.peek());
        assertFalse("Unexpected signalling", lock.poll(WAIT_TIME));

        long    waitEnd=System.nanoTime(), waitDiff=TimeUnit.NANOSECONDS.toMillis(waitEnd - waitStart);
        assertTrue("Wait time too short: " + waitDiff, waitDiff >= WAIT_TIME);
        
        assertTrue("Multiple offers", lock.offer());
        assertTrue("No signalling", lock.poll(WAIT_TIME));
        assertFalse("Bad post-poll signal value", lock.peek());
    }
}
