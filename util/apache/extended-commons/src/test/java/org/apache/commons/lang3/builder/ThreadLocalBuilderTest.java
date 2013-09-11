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

package org.apache.commons.lang3.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ThreadLocalBuilderTest extends AbstractTestSupport {
    public ThreadLocalBuilderTest() {
        super();
    }

    /**
     * Make sure that every thread that asks for a new instance receives
     * indeed a new one
     */
    @Test
    public void testSeparateInstances() {
        final AtomicInteger instanceCount=new AtomicInteger(0);
        final ThreadLocalBuilder<Object>   builder=new ThreadLocalBuilder<Object>() {
                @Override
                public Object build() {
                    instanceCount.incrementAndGet();
                    return new Object();
                }
            };
       final Map<String, Object>    instanceMap=Collections.synchronizedMap(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
       final Semaphore  sigSem=new Semaphore(0);
       final Runnable  runner=new Runnable() {
                @SuppressWarnings("synthetic-access")
				@Override
                public void run() {
                    Thread  thread=Thread.currentThread();
                    String  threadName=thread.getName();

                    try {
                        sigSem.acquire();
                    } catch(InterruptedException e) {
                        logger.error(threadName + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
                        return;
                    }

                    Object  instance=builder.get();
                    Object  prev=instanceMap.put(threadName, instance);
                    assertNull(threadName + ": unexpected previous instance: " + prev, prev);
                    logger.info(threadName + " - created instance=" + instance);
                }
            };

       Collection<Thread>   threads=new ArrayList<>();
       for (int index=0; index < Byte.SIZE; index++) {
           Thread   t=new Thread(runner, "tBuilder-" + index);
           threads.add(t);
           t.start();
       }
       
       sigSem.release(threads.size());
       
       for (Thread t : threads) {
           String  threadName=t.getName();
           try {
               t.join(TimeUnit.SECONDS.toMillis(15L));
           } catch(InterruptedException e) {
               fail(threadName + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
           }
           
           assertFalse(threadName + " still alive", t.isAlive());
       }
       
       assertEquals("Mismatched instance count", threads.size(), instanceCount.get());
       for (Map.Entry<String,?> e1 : instanceMap.entrySet()) {
           String   n1=e1.getKey();
           Object   v1=e1.getValue();

           for (Map.Entry<String,?> e2 : instanceMap.entrySet()) {
               String   n2=e2.getKey();
               if (n1.equals(n2)) {
                   continue;
               }
               
               assertNotSame(n2 + " has same instance as " + n1, v1, e2.getValue());
           }
       }
    }
}
