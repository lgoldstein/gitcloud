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

import java.util.Comparator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.collections15.map.UnmodifiableSortedMap;
import org.apache.commons.collections15.set.UnmodifiableSortedSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedEnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.ExtendedNumberUtils;

/**
 * @author Lyor G.
 * @since Jul 17, 2013 3:01:30 PM
 */
public class TimeUnitUtils {
    public TimeUnitUtils() {
        super();
    }

    /**
     * An un-modifiable {@link SortedMap} whose key is the name of the
     * {@link TimeUnit} (case <U>insensitive</U>) and the value the associated
     * unit 
     */
    public static final SortedMap<String,TimeUnit>  BY_NAME_MAP=
            UnmodifiableSortedMap.decorate(ExtendedEnumUtils.getEnumMap(TimeUnit.class, false));

    /**
     * An {@link ExtendedTransformer} that returns the {@link TimeUnit}
     * associated with a name (case <U>insensitive</U>)
     * @see #BY_NAME_MAP
     */
    public static final ExtendedTransformer<String,TimeUnit>    FROM_NAME_XFORM=
            new AbstractExtendedTransformer<String,TimeUnit>(String.class, TimeUnit.class) {
                @Override
                public TimeUnit transform(String name) {
                    if (StringUtils.isEmpty(name)) {
                        return null;
                    } else {
                        return BY_NAME_MAP.get(name);
                    }
                }
            };

    /**
     * A {@link Comparator} that compares 2 {@link TimeUnit}s according to
     * their duration (e.g., TimeUnit#NANOSECONDS < TimeUnit#MILLISECONDS).
     */
    public static final Comparator<TimeUnit> BY_DURATION_COMPARATOR=new Comparator<TimeUnit>() {
            @Override
            public int compare(TimeUnit o1, TimeUnit o2) {
                if (ObjectUtils.equals(o1, o2)) {
                    return 0;
                } else if (o1 == null) {
                    return (+1);
                } else if (o2 == null) {
                    return (-1);
                }
                
                /*
                 * NOTE: currently, the ordinal order is same as duration, but
                 * we do not rely on that
                 */
                long    u1=o1.toNanos(1L), u2=o2.toNanos(1L);
                return ExtendedNumberUtils.compare(u1, u2);
            }
        };

    /**
     * An un-modifiable {@link SortedSet} of all the {@link TimeUnit} values,
     * organized according to the {@link #BY_DURATION_COMPARATOR} 
     */
    public static final SortedSet<TimeUnit>   VALUES=
            UnmodifiableSortedSet.decorate(ExtendedEnumUtils.fullSortedSet(TimeUnit.class, BY_DURATION_COMPARATOR));

    /**
     * Compares 2 time values that may potentially have different {@link TimeUnit}s.
     * This is done by converting the &quot;longer&quot; unit into the
     * &quot;shorter&quot; one (e.g., seconds -&lt; to milliseconds) before
     * comparing them. This minimizes precision issues - we cannot eliminate
     * them entirely since the conversion to the &quot;shorter&quot; unit may
     * yield an overflow
     * @param u1 The 1st value {@link TimeUnit}
     * @param v1 The 1st value
     * @param u2 The 2nd value {@link TimeUnit}
     * @param v2 The 2nd value
     * @return Negative if 1st value is less than 2nd one, positive if the other
     * way around and zero if equal
     */
    public static final int compare(TimeUnit u1, long v1, TimeUnit u2, long v2) {
        Validate.notNull(u1, "No 1st value unit", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notNull(u2, "No 2nd value unit", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        int nRes=BY_DURATION_COMPARATOR.compare(u1, u2);
        if (nRes == 0) {    // same time unit
            return ExtendedNumberUtils.compare(v1, v2);
        }
        
        long    ev1=v1, ev2=v2;
        if (nRes < 0) {
            ev2 = u1.convert(v2, u2);
        } else {
            ev1 = u2.convert(v1, u1);
        }
        
        return ExtendedNumberUtils.compare(ev1, ev2);
    }
}
