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
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.Period;

/**
 * Provides a blocking way by which 2 (or more threads) can pass a
 * <U>single</U> object reference between them where the &quot;consumer&quot;
 * thread(s) can block indefinitely or with a timeout until the
 * &quot;producer&quot;(s) provide the required value. <B>Note:</B> once the
 * provided value has been consumed, the holder becomes empty and ready for
 * the next value to be passed
 * @param <T> Type of object being passed
 * @author Lyor G.
 * @since Aug 27, 2013 8:59:23 AM
 */
public class BlockingReferenceHolder<T> {
    /**
     * Value that can be used to specify infinite wait
     */
    public static final long    INFINITE_WAIT=(-1L);

    private final AtomicReference<T>    holder=new AtomicReference<T>(null);

    /**
     * Empty holder
     */
    public BlockingReferenceHolder() {
        super();
    }

    /**
     * A pre-initialized holder - which means that any call to
     * {@code waitForValue} method(s) will succeed immediately
     * @param initialValue The initial value - may not be {@code null}
     */
    public BlockingReferenceHolder(T initialValue) {
        Validate.notNull(initialValue, "Initial value may not be null", ArrayUtils.EMPTY_OBJECT_ARRAY);
        holder.set(initialValue);
    }

    /**
     * Wait indefinitely for a value, and when one arrives, return it (and
     * clear the holder). If there's already a value present, there's no need
     * to wait - the existing value is returned.
     * @return The waited-for value
     * @throws InterruptedException if the waiting thread is interrupted
     */
    public synchronized T waitForValue() throws InterruptedException {
        T   value;

        while ((value=holder.getAndSet(null)) == null) {
            wait();
        }

        return value;
    }

    /**
     * @param period The wait {@link Period} - if {@code null} then infinite
     * wait (consider using {@link #waitForValue()})
     * @return The waited value - {@code null} if timeout expired before value
     * has been provided
     * @throws InterruptedException if the waiting thread is interrupted
     */
    public T waitForValue(Period period) throws InterruptedException {
        if (period == null) {
            return waitForValue(INFINITE_WAIT);
        } else {
            return waitForValue(period.getUnit(), period.getCount());
        }
    }

    /**
     * Wait for a value, and when one arrives, return it (and clear the
     * holder). If there's already a value present, there's no need to wait
     * - the existing value is returned. If timeout is reached and value
     * hasn't arrived, then {@code null} is returned.
     * @param unit The wait {@link TimeUnit}
     * @param count The number of units to wait - use {@link #INFINITE_WAIT}
     * to specify infinity (or consider using {@link #waitForValue()})
     * @return The waited value - {@code null} if timeout expired before value
     * has been provided
     * @throws InterruptedException if the waiting thread is interrupted
     * @see #waitForValue(long)
     */
    public T waitForValue(TimeUnit unit, long count) throws InterruptedException {
        Validate.notNull(unit, "No time unit", ArrayUtils.EMPTY_OBJECT_ARRAY);
        if (count == INFINITE_WAIT) {
            return waitForValue(INFINITE_WAIT);
        } else {
            return waitForValue(unit.toMillis(count));
        }
    }

    /**
     * Wait for a value, and when one arrives, return it (and clear the
     * holder). If there's already a value present, there's no need to wait
     * - the existing value is returned. If timeout is reached and value
     * hasn't arrived, then {@code null} is returned.
     * 
     * @param timeout timeout in milliseconds - use {@link #INFINITE_WAIT} to
     * specify infinity (or consider using {@link #waitForValue()})
     * @return The waited value - {@code null} if timeout expired before value
     * has been provided
     * @throws InterruptedException if the waiting thread is interrupted
     * @see #waitForValue()
     */
    public synchronized T waitForValue(long timeout) throws InterruptedException {
        if (INFINITE_WAIT == timeout) {
            return waitForValue();
        }

        Validate.isTrue(timeout >= 0L, "Negative timeout - use " + INFINITE_WAIT + " for infinity: %s", timeout);

        T       value=null;
        long    maxTime=System.currentTimeMillis() + timeout, now;
        while (((value=holder.getAndSet(null)) == null)
            && ((now=System.currentTimeMillis()) < maxTime)) {
            wait(maxTime - now);
        }

        return value;
    }
    
    /**
     * Sets the holder value and signals that it is available to any of the
     * waiting threads
     * @param value The value to be set - cannot be {@code null} 
     * @param overrideExisting If {@code false} then the method will fail with
     * and {@link IllegalStateException} if a value was set but not consumed.
     * Otherwise, it will override whatever waiting value there is
     * @return The previous held value (if not consumed) - {@code null} if no
     * previous pending (un-consumed) value
     * @throws IllegalStateException if not allowed to override an existing
     * (pending) value and one is still pending
     */
    public synchronized T setValue(T value, boolean overrideExisting) throws IllegalStateException {
        Validate.notNull(value, "No value", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        if (!overrideExisting) {
            T   curValue=holder.get();
            if (curValue != null) {
                throw new IllegalStateException("Not allowed to override existing value");
            }
        }
        
        T   prevValue=holder.getAndSet(value);
        notifyAll();
        return prevValue;
    }
}
