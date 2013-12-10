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

package org.apache.commons.math3.timeseries;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.ExtendedPredicate;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.PredicateUtils;
import org.apache.commons.lang3.math.ExtendedNumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Represents a {@code double} value associated with a {@code long} timestamp (msec.)
 * @author Lyor G.
 * @since Oct 30, 2013 9:20:56 AM
 */
public class DataPoint implements Serializable, Cloneable {
    private static final long serialVersionUID = 3859034877993969891L;

    /**
     * Special value used to indicate that no timestamp has been set
     */
    public static final long    NO_TIME_VALUE=0L;

    /**
     * Special value used to display a {@link #NO_TIME_VALUE} value
     */
    public static final String  NO_TIME_STRING="no-time";

    private long    timestamp;
    private double  value;

    public DataPoint() {
        this(NO_TIME_VALUE, Double.NaN);
    }
    
    public DataPoint(long ts, double v) {
        timestamp = ts;
        value = v;
    }

    /**
     * Compares 2 {@link DataPoint} instances according to their {@link #getTimestamp()} value(s)
     */
    public static final Comparator<DataPoint> BY_TIMESTAMP_COMPARATOR=
            new Comparator<DataPoint>() {
                @Override
                public int compare(DataPoint o1, DataPoint o2) {
                    long    t1=(o1 == null) ? NO_TIME_VALUE : o1.getTimestamp();
                    long    t2=(o2 == null) ? NO_TIME_VALUE : o2.getTimestamp();
                    if (t1 == NO_TIME_VALUE) {
                        if (t2 == NO_TIME_VALUE) {
                            return 0;
                        } else {    // push no timestamps to end
                            return (+1);
                        }
                    } else if (t2 == NO_TIME_VALUE) {
                        return (-1); // push no timestamps to end
                    } else {
                        return ExtendedNumberUtils.compare(t1, t2);
                    }
                }
            };

    /**
     * An {@link ExtendedPredicate} that returns {@code true} if the evaluated
     * {@link DataPoint} instance is not {@code null} and has a valid timestamp
     * @see #NO_TIME_VALUE
     */
    public static final ExtendedPredicate<DataPoint>    TIMESTAMP_SELECTOR=
            new AbstractExtendedPredicate<DataPoint>(DataPoint.class) {
                @Override
                public boolean evaluate(DataPoint p) {
                    if ((p == null) || (NO_TIME_VALUE == p.getTimestamp())) {
                        return false;    // debug breakpoint
                    } else {
                        return true;
                    }
                }
            };

    /**
     * A {@link Predicate} returns {@code true} if the evaluated
     * {@link DataPoint} instance is {@code null} or has no timestamp
     * @see #VALUE_SELECTOR
     */
    public static final Predicate<DataPoint>    NO_TIMESTAMP_SELECTOR=PredicateUtils.notPredicate(TIMESTAMP_SELECTOR);

    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long ts) {
        timestamp = ts;
    }

    /**
     * Compares 2 {@link DataPoint} instances according to their {@link #getValue()} value(s)
     */
    public static final Comparator<DataPoint> BY_VALUE_COMPARATOR=
            new Comparator<DataPoint>() {
                @Override
                public int compare(DataPoint o1, DataPoint o2) {
                    double    v1=(o1 == null) ? Double.NaN : o1.getValue();
                    double    v2=(o2 == null) ? Double.NaN : o2.getValue();
                    return ExtendedNumberUtils.compare(v1, v2);
                }
            };

    /**
     * An {@link ExtendedPredicate} that returns {@code true} if the evaluated
     * {@link DataPoint} instance is not {@code null} and does not have a
     * {@link Double#NaN} value
     */
    public static final ExtendedPredicate<DataPoint>    VALUE_SELECTOR=
            new AbstractExtendedPredicate<DataPoint>(DataPoint.class) {
                @Override
                public boolean evaluate(DataPoint p) {
                    if ((p == null) || Double.isNaN(p.getValue())) {
                        return false;    // debug breakpoint
                    } else {
                        return true;
                    }
                }
            };

    /**
     * A {@link Predicate} returns {@code true} if the evaluated
     * {@link DataPoint} instance is {@code null} or has a
     * {@link Double#NaN} value
     * @see #VALUE_SELECTOR
     */
    public static final Predicate<DataPoint>    NO_VALUE_SELECTOR=PredicateUtils.notPredicate(VALUE_SELECTOR);

    public double getValue() {
        return value;
    }
    
    public void setValue(double v) {
        value = v;
    }

    /**
     * A {@link Predicate} returns {@code true} if the evaluated
     * {@link DataPoint} instance is not {@code null} and has valid timestamp
     * and value
     * @see #TIMESTAMP_SELECTOR
     * @see #VALUE_SELECTOR
     */
    public static final Predicate<DataPoint>    VALID_SELECTOR=PredicateUtils.andPredicate(TIMESTAMP_SELECTOR, VALUE_SELECTOR);

    @Override
    public int hashCode() {
        return ExtendedNumberUtils.hashCode(getTimestamp())
             + ExtendedNumberUtils.hashCode(getValue())
             ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        
        DataPoint    other=(DataPoint) obj;
        if ((getTimestamp() == other.getTimestamp())
         && (ExtendedNumberUtils.compare(getValue(), other.getValue()) == 0)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public DataPoint clone() {
        try {
            return getClass().cast(super.clone());
        } catch(CloneNotSupportedException e) { // unexpected
            throw new UnsupportedOperationException("Failed to clone " + toString() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        long    tsValue=getTimestamp();
        String  tsString=(tsValue == NO_TIME_VALUE) ?  NO_TIME_STRING : DateFormatUtils.ISO_DATETIME_FORMAT.format(tsValue);
        return tsString + ": " + getValue();
    }
}
