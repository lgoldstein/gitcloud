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

import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.concurrent.TimeUnitUtils;
import org.apache.commons.lang3.math.ExtendedNumberUtils;

/**
 * Represents an <U>immutable <B>non-negative</B></U> time period specified as
 * {@link TimeUnit}s and count of the units. <B>Note:</B> 2 {@link Period}s are
 * considered equal if they have the same {@link #getCanonicalValue()} value
 * (which currently is in nanoseconds)
 * @author Lyor G.
 */
public final class Period implements Serializable, Comparable<Period>, Cloneable, TimeUnitCarrier {
    private static final long serialVersionUID = 4694889912377461831L;

    private TimeUnit    unit;
    private long        count;

    Period() {
        // for de-serializers
    }

    Period(TimeUnit u, long c) {
        unit = u;
        count = c;
    }

    /**
     * @param delta The delta to add/subtract from the current count
     * @return A {@link Period} having the same units but an updated relative count
     */
    public Period addCount(long delta) {
    	if (delta == 0L) {
    		return this;
    	} else {
    		return valueOf(getUnit(), getCount() + delta);
    	}
    }

    public Period mulCount(long factor) {
    	if (factor == 1L) {
    		return this;
    	} else {
    		return valueOf(getUnit(), getCount() * factor);
    	}
    }

    public Period divCount(long factor) {
    	if (factor == 1L) {
    		return this;
    	} else {
    		return valueOf(getUnit(), getCount() / factor);
    	}
    }
    
    /**
     * @param u The target {@link TimeUnit}
     * @return An equivalent {@link Period} whose count has been updated
     * to represent the same value in the target unit. <B>Note:</B> arithmetic
     * rounding errors may occur when converting from lower units to higher
     * ones
     */
    public Period convertUnit(TimeUnit u) {
    	if (ObjectUtils.equals(getUnit(), u)) {
    		return this;
    	} else {
    		return valueOf(u, convert(u));
    	}
    }
    /**
     * Can &quot;reverse&quot; the {@link #toString()} back into a {@link Period}
     * @param s The {@link CharSequence} to be parsed - <B>Note:</B> the used
     * {@link TimeUnit} value may be specified case <U>insensitive</U>
     * @return The matching {@link Period} value - <code>null</code> if
     * <code>null</code>/empty input sequence
     * @throws NumberFormatException if malformed number
     * @throws IllegalArgumentException if unknown unit
     * @see #valueOf(TimeUnit, long)
     */
    public static Period valueOf(CharSequence s) {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        
        int pos=StringUtils.indexOf(s, ' ');
        if ((pos <= 0) || (pos >= (s.length() - 1))) {
            throw new NumberFormatException("Missing/misplaced units separator: " + s);
        }
        
        CharSequence    count=s.subSequence(0, pos), unit=s.subSequence(pos + 1, s.length());
        return valueOf(TimeUnitUtils.FROM_NAME_XFORM.transform(unit.toString()), Long.parseLong(count.toString()));
    }

    /**
     * @param u The {@link TimeUnit}
     * @param c The unit count
     * @return The {@link Period}
     * @throws IllegalArgumentException if no time unit or negative count
     */
    public static Period valueOf(TimeUnit u, long c) {
        Validate.notNull(u, "No time unit", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.isTrue(c >= 0L, "Illegal count value: %s", c);
        
        return new Period(u, c);
    }

    /**
     * @param u The target unit
     * @return Result of converting the period's unit and count to the target unit
     */
    public long convert(TimeUnit u) {
        return u.convert(getCount(), getUnit());
    }

    @Override
    public TimeUnit getUnit() {
        return unit;
    }

    void setUnit(TimeUnit u) {
        unit = u;   // for de-serializers
    }

    public long getCount() {
        return count;
    }

    /**
     * @return A &quot;canonical&quot; value that can be used to compare
     * 2 {@link Period}s that might have different {@link TimeUnit}s
     */
    public long getCanonicalValue() {
        TimeUnit    u=getUnit();
        if (u == null) {
            return (-1L);
        } else {
            return u.toNanos(getCount());
        }
    }

    // for de-serializers
    void setCount(long c) {
        count = c;
    }

    @Override
    public Period clone() {
        try {
            return getClass().cast(super.clone());
        } catch(CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone: " + e.getMessage(), e);
        }
    }

    @Override
    public int hashCode() {
        return ExtendedNumberUtils.hashCode(getCanonicalValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        
        if (compareTo((Period) obj) != 0)
            return false;   // debug breakpoint
        else
            return true;
    }

    /**
     * Compares 2 {@link Period}-s according to their canonical value
     */
    public static final Comparator<Period>  BY_CANONICAL_VALUE_COMPARATOR=new Comparator<Period>() {
            @Override
            public int compare(Period p1, Period p2) {
                if (p1 == p2) {
                    return 0;
                } else if (p1 == null) {
                    return (+1);    // push null(s) to end
                } else if (p2 == null) {
                    return (-1);    // push null(s) to end
                }
                
                long    v1=p1.getCanonicalValue(), v2=p2.getCanonicalValue();
                return ExtendedNumberUtils.signOf(v1 - v2);
            }
        };

    @Override
    public int compareTo(Period other) {
        return BY_CANONICAL_VALUE_COMPARATOR.compare(this, other);
    }

    @Override
    public String toString() {
        return String.valueOf(getCount()) + " " + String.valueOf(getUnit());
    }
}
