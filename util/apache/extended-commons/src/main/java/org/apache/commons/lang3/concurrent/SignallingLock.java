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

package org.apache.commons.lang3.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a simple blocking &quot;queue&quot; that provides a signaling
 * method by which one can signal to the wait(er) that something agreed
 * between them has occurred 
 * @author Lyor G.
 */
public class SignallingLock {
    /** Main lock guarding all access */
    private final ReentrantLock lock;
    /** Condition for waiting takes */
    private final Condition notEmpty;
    private final AtomicBoolean signalValue=new AtomicBoolean(false);

    public SignallingLock () {
        lock = new ReentrantLock(true);
        notEmpty = lock.newCondition();
    }

    /**
     * @return The <U>current</U> signal value - it may change on next call
     */
    public boolean peek () {
        return signalValue.get();
    }

    /**
     * Signals that the agreed upon event has occurred
     * @return <code>true</code> if the signal state was <code>false</code>
     * and has become <code>true</code>
     */
    public boolean offer () {
        lock.lock();
        try {
            boolean value=signalValue.getAndSet(true);
            notEmpty.signal();
            return (!value);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Blocks infinitely until someone signals (a.k.a. {@link #offer()}-s)
     * @throws InterruptedException If interrupted while awaiting a signal
     */
    public void take () throws InterruptedException {
        lock.lockInterruptibly();
        try {
            try {
                while (!signalValue.getAndSet(false)) {
                    notEmpty.await();
                }
            } catch (InterruptedException ie) {
                notEmpty.signal(); // propagate to non-interrupted thread
                throw ie;
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Waits the specified amount of time to be signaled
     * @param timeout Timeout (msec.) to wait
     * @return <code>true</code> if signaled before timeout expired
     * @throws InterruptedException If interrupted while awaiting a signal
     * @see #poll(long, TimeUnit)
     */
    public boolean poll (long timeout) throws InterruptedException {
        return poll(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Waits the specified amount of time to be signaled
     * @param timeout Timeout value to wait
     * @param unit The {@link TimeUnit} used for the timeout value
     * @return <code>true</code> if signaled before timeout expired
     * @throws InterruptedException If interrupted while awaiting a signal
     */
    public boolean poll (long timeout, TimeUnit unit) throws InterruptedException {
        long nanos=unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            for ( ; ; ) {
                if (signalValue.getAndSet(false)) {
                    return true;    // signaled
                }

                if (nanos <= 0) {   // time has expired
                    return false;
                }

                try {
                    nanos = notEmpty.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notEmpty.signal(); // propagate to non-interrupted thread
                    throw ie;
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
