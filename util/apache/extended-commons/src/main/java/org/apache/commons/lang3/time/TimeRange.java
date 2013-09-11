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

package org.apache.commons.lang3.time;

import java.io.IOException;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedObjectUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.concurrent.TimeUnitUtils;
import org.apache.commons.lang3.math.ExtendedNumberUtils;

/**
 * Used to represent a time range that has a start/end value and a
 * {@link TimeUnit} - e.g., 3-5 SECONDS, 4-6 HOURS, etc..</BR>
 * <B>Note:</B></BR>
 * <UL>
 *      <LI>
 *      The start/end values are <U>inclusive</U> when used to calculate
 *      whether 2 ranges overlap, contain each other, etc..
 *      </LI>
 *
 *      <LI>
 *      The start/end value can be <U>negative</U> - the semantics of
 *      negativity is up to the user
 *      </LI>
 *
 *      <LI>
 *      The range can be <U>open-ended</U> - i.e., one (or both) of its ends
 *      can be &quot;infinite&quot; - the values {@link Long#MIN_VALUE} and
 *      {@link Long#MAX_VALUE} are used to indicate negative/positive infinity
 *      respectively
 *      </LI>
 *
 *      <LI>
 *      The range interval must ordered and non-zero - i.e. start &lt; end
 *      value(s). <B>Note:</B> this constraint also implies that the start
 *      value cannot be positive infinity and the end value cannot be the
 *      negative one as it would cause either a &quot;reversed&quot; range
 *      (if the start is positive infinity and the end is the negative one)
 *      or a zero size range (if both start and end are either positive or
 *      negative infinity).
 *      </LI>
 * </UL>
 * @author Lyor G.
 * @since Aug 20, 2013 7:45:39 AM
 */
public class TimeRange implements Serializable, Cloneable, TimeUnitCarrier {
    private static final long serialVersionUID = 5286349540302986794L;
    /**
     * Value used to indicate positive infinity
     */
    public static final long    POSITIVE_INFINITY=Long.MAX_VALUE;
    /**
     * Value used to indicate negative infinity
     */
    public static final long    NEGATIVE_INFINITY=Long.MIN_VALUE;

    // characters used to build/parse a range string
    public static final char RANGE_START_DELIM='[', RANGE_END_DELIM=']', RANGE_SEP_DELIM='-';

    /**
     * A cache for full ranges of a given {@link TimeUnit}
     */
    private static final Map<TimeUnit,TimeRange>    fullRangesMap=new EnumMap<TimeUnit,TimeRange>(TimeUnit.class);

    private TimeUnit    unit;
    private long    startValue, endValue;
    private transient volatile Period interval;

    TimeRange() {
        // for de-serializers
    }

    TimeRange(TimeUnit u, long start, long end) {
        unit = u;
        startValue = start;
        endValue = end;
    }

    @Override
    public TimeUnit getUnit() {
        return unit;
    }

    void setUnit(TimeUnit u) {
        unit = u;   // for de-serializers
    }

    public long getStartValue() {
        return startValue;
    }
    
    void setStartValue(long value) {
        startValue = value;   // for de-serializers
    }

    /**
     * @return Number of units in the range - {@link #POSITIVE_INFINITY}
     * if open-ended (at <U>any</U> end)
     */
    public long getNumUnits() {
        long    start=getStartValue(), end=getEndValue();
        if (isOpenEnded(start, end)) {
            return POSITIVE_INFINITY;
        } else {
            return 1L + (end - start);
        }
    }

    /**
     * @return A {@link Period} representing the interval of the range.
     * <B>Note:</B> if the range is open-ended (at either end), then
     * a {@link #POSITIVE_INFINITY} interval is returned 
     */
    public Period getInterval() {
        long    unitCount=getNumUnits();
        synchronized(this) {
            if (interval == null) { 
                if (isInfiniteValue(unitCount)) {
                    interval = Period.valueOf(getUnit(), POSITIVE_INFINITY);
                } else {
                    interval = Period.valueOf(getUnit(), unitCount);
                }
            }
            
            return interval;
        }
    }

    /**
     * @param range Another {@link TimeRange} to test
     * @return {@code true} if the other range is contained (or equal)
     * to this one
     * @see #contains(TimeUnit, long)
     */
    public boolean contains(TimeRange range) {
        Validate.notNull(range, "No range specified", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        if (range == this) {
            return true;
        }

        TimeUnit    u=range.getUnit();
        if (!contains(u, range.getStartValue())) {
            return false;
        }
        
        if (!contains(u, range.getEndValue())) {
            return false;
        }
        
        return true;
    }

    public TimeRange intersection(TimeRange range) {
        Validate.notNull(range, "No range specified", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        if (range == this) {
            return this;
        }
        
        if (!intersects(range)) {
            return null;
        }
        
        TimeUnit    thisUnit=getUnit(), otherUnit=range.getUnit();
        TimeUnit    resultUnit=ExtendedObjectUtils.min(thisUnit, otherUnit, TimeUnitUtils.BY_DURATION_COMPARATOR);
        long        thisStart=getStartValue(), otherStart=range.getStartValue();
        long        thisEnd=getEndValue(), otherEnd=range.getEndValue();

        // convert units to common denominator
        thisStart = isNegativeInfinity(thisStart) ? thisStart : resultUnit.convert(thisStart, thisUnit);
        thisEnd = isPositiveInfinity(thisEnd) ? thisEnd : resultUnit.convert(thisEnd, thisUnit);
        otherStart = isNegativeInfinity(otherStart) ? otherStart : resultUnit.convert(otherStart, otherUnit);
        otherEnd = isPositiveInfinity(otherEnd) ? otherEnd : resultUnit.convert(otherEnd, otherUnit);

        final long    resultStart;
        if (isNegativeInfinity(thisStart)) {
            resultStart = otherStart;
        } else if (isNegativeInfinity(otherStart)) {
            resultStart = thisStart;
        } else {
            resultStart = Math.max(thisStart, otherStart);
        }
        
        final long  resultEnd;
        if (isPositiveInfinity(thisEnd)) {
            resultEnd = otherEnd;
        } else if (isPositiveInfinity(otherEnd)) {
            resultEnd = thisEnd;
        } else {
            resultEnd = Math.min(thisEnd, otherEnd);
        }
        
        if (resultStart >= resultEnd) {
            return null;    // intersection is empty
        }
        
        return valueOf(resultUnit, resultStart, resultEnd);
    }

    /**
     * @param range Another {@link TimeRange} to test
     * @return {@code true} if the other range is intersects with this one
     * (i.e., they have <U>any</U> overlapping values)
     * @see #contains(TimeUnit, long)
     */
    public boolean intersects(TimeRange range) {
        Validate.notNull(range, "No range specified", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        if (range == this) {
            return true;
        }
        
        TimeUnit    thisUnit=getUnit(), otherUnit=range.getUnit();
        long        thisStart=getStartValue(), otherStart=range.getStartValue();
        long        thisEnd=getEndValue(), otherEnd=range.getEndValue();

        // convert units to common denominator
        int nRes=TimeUnitUtils.BY_DURATION_COMPARATOR.compare(thisUnit, otherUnit);
        if (nRes < 0) {
            otherStart = isNegativeInfinity(otherStart) ? otherStart : thisUnit.convert(otherStart, otherUnit);
            otherEnd = isPositiveInfinity(otherEnd) ? otherEnd : thisUnit.convert(otherEnd, otherUnit);
        } else if (nRes > 0) {
            thisStart = isNegativeInfinity(thisStart) ? thisStart : otherUnit.convert(thisStart, thisUnit);
            thisEnd = isPositiveInfinity(thisEnd) ? thisEnd : otherUnit.convert(thisEnd, thisUnit);
        }

        if (otherStart > thisEnd) {
            return false;
        }

        if (otherEnd < thisStart) {
            return false;
        }

        return true;
    }

    /**
     * @param valUnit Value {@link TimeUnit}
     * @param value Number of units
     * @return {@code true} if the specified value is within the range
     * (including if exactly one of the ends...)
     */
    public boolean contains(TimeUnit valUnit, long value) {
        int nRes=compareStartValue(valUnit, value);
        if (nRes == 0) {
            return true;    // exactly the start
        } else if (nRes < 0) {
            return false;   // below start
        }

        if ((nRes=compareEndValue(valUnit, value)) > 0) {
            return false;   // above end
        }
        
        return true;
    }

    /**
     * @param valUnit The compared value {@link TimeUnit}
     * @param value The value
     * @return Negative if the compared value is below the start value,
     * positive if above, zero if exactly at the start value. <B>Note:</B>
     * If the compared value is negative infinity, then the time unit is
     * irrelevant. In this case, all that matters is whether the range's
     * start value is also negative infinity.
     */
    public int compareStartValue(TimeUnit valUnit, long value) {
        Validate.notNull(valUnit, "No time unit specified", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        long    start=getStartValue();
        if (isNegativeInfinity(value)) {
            if (isNegativeInfinity(start)) {
                return 0;
            } else {
                return (-1);
            }
        } else if (isNegativeInfinity(start)) {
            return (+1);
        } else {
            return TimeUnitUtils.compare(valUnit, value, getUnit(), start);
        }
    }

    public long getEndValue() {
        return endValue;
    }
    
    void setEndValue(long value) {
        endValue = value;   // for de-serializers
    }

    /**
     * @param valUnit The compared value {@link TimeUnit}
     * @param value The value
     * @return Negative if the compared value is below the end value,
     * positive if above, zero if exactly at the end value. <B>Note:</B>
     * If the compared value is positive infinity, then the time unit is
     * irrelevant. In this case, all that matters is whether the range's
     * end value is also positive infinity.
     */
    public int compareEndValue(TimeUnit valUnit, long value) {
        Validate.notNull(valUnit, "No time unit specified", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        long    end=getEndValue();
        if (isPositiveInfinity(value)) {
            if (isPositiveInfinity(end)) {
                return 0;
            } else {
                return (+1);
            }
        } else if (isPositiveInfinity(end)) {
            return (-1);
        } else {
            return TimeUnitUtils.compare(valUnit, value, getUnit(), end);
        }
    }

    public <A extends Appendable> A append(A sb) throws IOException {
        return append(sb, this);
    }

    @Override
    public int hashCode() {
        return ExtendedNumberUtils.hashCode(getStartValue())
             + ExtendedNumberUtils.hashCode(getEndValue())
             + ObjectUtils.hashCode(getUnit())
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
        
        TimeRange   other=(TimeRange) obj;
        int         nRes=compareStartValue(other.getUnit(), other.getStartValue());
        if (nRes != 0) {
            return false;   // debug breakpoint
        }
        
        if ((nRes=compareEndValue(other.getUnit(), other.getEndValue())) != 0) {
            return false;   // debug breakpoint
        }

        return true;
    }

    @Override
    public TimeRange clone() {
        try {
            return getClass().cast(super.clone());
        } catch(CloneNotSupportedException e) { // unexpected
            throw new RuntimeException("Failed to clone " + toString() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return toString(getUnit(), getStartValue(), getEndValue());
    }

    public static final String toString(TimeUnit unit, long start, long end) {
        try {
            return append(new StringBuilder(48), unit, start, end).toString();
        } catch(IOException e) {    // unexpected
            throw new RuntimeException(e);
        }
    }

    public static final <A extends Appendable> A append(A sb, TimeRange r) throws IOException {
        Validate.notNull(r, "No time range", ArrayUtils.EMPTY_OBJECT_ARRAY);
        return append(sb, r.getUnit(), r.getStartValue(), r.getEndValue());
    }

    public static final <A extends Appendable> A append(A sb, TimeUnit unit, long start, long end)
            throws IOException {
        Validate.notNull(sb, "No appender", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(unit, "No time unit", ArrayUtils.EMPTY_OBJECT_ARRAY);

        sb.append(RANGE_START_DELIM).append(isNegativeInfinity(start) ? "" : String.valueOf(start))
          .append(RANGE_SEP_DELIM).append(isPositiveInfinity(end) ? "" : String.valueOf(end))
          .append(RANGE_END_DELIM).append(' ').append(unit.name())
          ;
        return sb;
    }

    /**
     * An {@link ExtendedTransformer} that invokes {@link #valueOf(String)}
     */
    public static final ExtendedTransformer<String,TimeRange> FROM_STRING_XFORMER=
            new AbstractExtendedTransformer<String,TimeRange>(String.class, TimeRange.class) {
                @Override
                public TimeRange transform(String input) {
                    return valueOf(input);
                }
            };

    public static final TimeRange valueOf(String str) throws IllegalArgumentException {
        String  s=StringUtils.trim(str);
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        
        if (s.charAt(0) != RANGE_START_DELIM) {
            throw new IllegalArgumentException("Missing range start: " + str);
        }
        
        int pos=s.indexOf(RANGE_END_DELIM);
        if (pos <= 0) {
            throw new IllegalArgumentException("Missing range end: " + str);
        }
        
        if (pos == 1) {
            throw new IllegalArgumentException("Empty range: " + str);
        }
        
        if (pos == (s.length() - 1)) {
            throw new IllegalArgumentException("Missing range unit: " + str);
        }

        String  values=StringUtils.trim(s.substring(1, pos));
        String  units=StringUtils.trim(s.substring(pos + 1));
        if ((pos=values.indexOf(RANGE_SEP_DELIM)) < 0) {
            throw new IllegalArgumentException("Missing range values: " + str);
        }
        
        final long start;
        if (pos == 0) {
            start = NEGATIVE_INFINITY;
        } else {
            String  v=StringUtils.trim(values.substring(0, pos));
            try {
                start = Long.parseLong(v);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("Bad start value (" + v + ") in " + str + ": " + e.getMessage(), e);
            }
        }
        
        final long  end;
        if (pos == (values.length() - 1)) {
            end = POSITIVE_INFINITY;
        } else {
            String  v=StringUtils.trim(values.substring(pos + 1));
            try {
                end = Long.parseLong(v);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("Bad end value (" + v + ") in " + str + ": " + e.getMessage(), e);
            }
        }
        
        TimeUnit    unit=TimeUnitUtils.BY_NAME_MAP.get(units);
        if (unit == null) {
            throw new IllegalArgumentException("Unknown unit (" + units + "): " + str);
        }
        
        if (isFullOpenEnded(start, end)) {
            return fullRangeOf(unit);
        } else {
            return valueOf(unit, start, end);
        }
    }

    public static final TimeRange openStartRange(TimeUnit unit, long end) {
        return valueOf(unit, NEGATIVE_INFINITY, end);
    }

    public static final TimeRange openEndRange(TimeUnit unit, long start) {
        return valueOf(unit, start, POSITIVE_INFINITY);
    }

    public static final TimeRange valueOf(TimeUnit unit, long start, long end) {
        Validate.notNull(unit, "No time unit", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.isTrue(start < end, "Reversed range", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        if (isFullOpenEnded(start, end)) {
            return fullRangeOf(unit);
        } else {
            return new TimeRange(unit, start, end);
        }
    }

    /**
     * @param unit The {@link TimeUnit}
     * @return A (cached) open-ended on both sides {@link TimeRange}
     */
    public static final TimeRange fullRangeOf(TimeUnit unit) {
        Validate.notNull(unit, "No time unit", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        TimeRange   range=null;
        synchronized(fullRangesMap) {
            if ((range=fullRangesMap.get(unit)) != null) {
                return range;
            }
            
            range = new TimeRange(unit, NEGATIVE_INFINITY, POSITIVE_INFINITY);
            fullRangesMap.put(unit, range);
        }
        
        return range;   // debug breakpoint
    }

    public static final boolean isOpenEnded(long start, long end) {
        if (isNegativeInfinity(start) || isPositiveInfinity(end)) {
            return true;
        } else {
            return false;
        }
    }

    public static final boolean isFullOpenEnded(long start, long end) {
        if (isNegativeInfinity(start) && isPositiveInfinity(end)) {
            return true;
        } else {
            return false;
        }
    }

    public static final boolean isInfiniteValue(long value) {
        // debug breakpoints
        if (isPositiveInfinity(value) || isNegativeInfinity(value)) {
            return true;
        } else {
            return false;
        }
    }

    public static final boolean isPositiveInfinity(long value) {
        // debug breakpoints
        if (POSITIVE_INFINITY == value) {
            return true;
        } else {
            return false;
        }
    }

    public static final boolean isNegativeInfinity(long value) {
        // debug breakpoints
        if (NEGATIVE_INFINITY == value) {
            return true;
        } else {
            return false;
        }
    }
}
