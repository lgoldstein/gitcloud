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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Aug 27, 2013 9:43:14 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BlockingReferenceHolderTest extends AbstractTestSupport {
    private static final long TOLERANCE=Short.SIZE;

    public BlockingReferenceHolderTest() {
        super();
    }

    @Test
    public void testWaitForValueWhenNoValueProvided() throws InterruptedException {
        final long  WAIT_TIME=Byte.MAX_VALUE;
        assertNull("Unexpected value", validateWaitTime("testWaitForValueWhenNoValueProvided",
                                                        new BlockingReferenceHolder<String>(),
                                                        WAIT_TIME,
                                                        WAIT_TIME - TOLERANCE,
                                                        WAIT_TIME + TOLERANCE));
    }

    @Test
    public void testWaitForValueWhenValueAlreadyProvided() throws InterruptedException {
        String  expected="testWaitForValueWhenValueAlreadyProvided";
        BlockingReferenceHolder<String> holder=new BlockingReferenceHolder<String>(expected);
        String  actual=validateWaitTime(expected, holder, TimeUnit.SECONDS.toMillis(1L), 0L, TOLERANCE);
        assertSame("Mismatched consumed value", expected, actual);
        assertNull("Unexpected pending value", holder.waitForValue(0L));
    }

    @Test
    public void testAsynchronousWaitForValue() throws InterruptedException {
        final String  expected="testAsynchronousWaitForValue";
        final BlockingReferenceHolder<String>   holder=new BlockingReferenceHolder<String>();
        final long  SLEEP_TIME=2L * Byte.MAX_VALUE, WAIT_TIME=2L * SLEEP_TIME;
        final Runnable  provider=new Runnable() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void run() {
                    try {
                        Thread.sleep(SLEEP_TIME);
                        
                        String  prev=holder.setValue(expected, false);
                        assertNull("Unexpected previous value", prev);
                    } catch(InterruptedException e) {
                        logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
                    }
                }
            };
       Thread   t=new Thread(provider, expected);
       t.start();
       
       String   actual=validateWaitTime(expected, holder, WAIT_TIME, SLEEP_TIME - TOLERANCE, SLEEP_TIME + TOLERANCE);
       assertSame("Mismatched consumed value", expected, actual);
       assertNull("Unexpected pending value", holder.waitForValue(0L));
       
       t.join(TimeUnit.SECONDS.toMillis(5L));
       assertFalse("Provider thread still alive", t.isAlive());
    }

    @Test
    public void testMultithreadedWaitForValue() throws InterruptedException {
        final Set<Integer>                      consumed=Collections.synchronizedSet(new TreeSet<Integer>());
        final BlockingReferenceHolder<Integer>  holder=new BlockingReferenceHolder<Integer>();
        final int                               NUM_THREADS=Byte.SIZE;
        final long                              WAIT_TIME=TimeUnit.SECONDS.toMillis(NUM_THREADS) + TOLERANCE;
        final Runnable                          consumer=new Runnable() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void run() {
                    Thread  thread=Thread.currentThread();
                    String  name=thread.getName();
                    try {
                        Integer value=holder.waitForValue(WAIT_TIME);
                        assertNotNull(name + ": no value", value);
                        assertTrue(name + ": non-unique value: " + value, consumed.add(value));
                        logger.info("Retrieved value: " + value);
                    } catch(InterruptedException e) {
                        logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
                    }
                }
            };
        List<Thread>    threadList=new ArrayList<Thread>(NUM_THREADS);
        for (int index=1; index <= NUM_THREADS; index++) {
            Thread  thread=new Thread(consumer, "testMultithreadedWaitForValue-" + index);
            thread.start();
            threadList.add(thread);
        }
        
        for (int index=0; index < NUM_THREADS; index++) {
            Integer value=Integer.valueOf(index);
            logger.info("Set value: " + value);
            holder.setValue(value, false);
            Thread.sleep(2L * TOLERANCE);
        }
        
        for (Thread thread : threadList) {
            thread.join(WAIT_TIME);
            assertFalse(thread.getName() + ": thread still alive", thread.isAlive());
        }
        
        assertEquals("Mismatched consumed size: " + consumed, NUM_THREADS, consumed.size());
        for (int index=0; index < NUM_THREADS; index++) {
            Integer value=Integer.valueOf(index);
            assertTrue("Missing value: " + value, consumed.remove(value));
        }
        assertTrue("Unexpected consumed values: " + consumed, consumed.isEmpty());
    }

    private <T> T validateWaitTime( // min./max. are inclusive (!)
            String message, BlockingReferenceHolder<T> holder, long waitTime, long minWaitMsec, long maxWaitMsec)
                    throws InterruptedException {
        long    waitStart=System.currentTimeMillis();
        T       value=holder.waitForValue(waitTime);
        long    waitEnd=System.currentTimeMillis(), totalWait=waitEnd - waitStart;
        assertTrue(message + ": Total wait (" + totalWait + ") below min. (" + minWaitMsec + ")", totalWait >= minWaitMsec);
        assertTrue(message + ": Total wait (" + totalWait + ") above max. (" + maxWaitMsec + ")", totalWait <= maxWaitMsec);
        return value;
    }
}
