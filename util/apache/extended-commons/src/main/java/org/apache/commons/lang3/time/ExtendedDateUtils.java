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

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.collections15.AbstractExtendedFactory;
import org.apache.commons.collections15.ExtendedFactory;
import org.apache.commons.collections15.numbers.longs.AbstractExtendedLong2ValueTransformer;
import org.apache.commons.collections15.numbers.longs.ExtendedLong2ValueTransformer;

/**
 * @author Lyor G.
 * @since Jun 25, 2013 10:11:08 AM
 */
public class ExtendedDateUtils extends DateUtils {
    public ExtendedDateUtils() {
        super();    // debug breakpoint
    }

    /**
     * An {@link ExtendedLong2ValueTransformer} that converts timestamp(s)
     * to {@link Date} values
     */
    public static final ExtendedLong2ValueTransformer<Date> LONG2DATE_TRANSFORMER=
            new AbstractExtendedLong2ValueTransformer<Date>(Date.class) {
                @Override
                public Date transform (long value) {
                    return new Date(value);
                }
            };

    /**
     * An {@link ExtendedLong2ValueTransformer} that converts timestamp(s)
     * to {@link Calendar} values
     */
    public static final ExtendedLong2ValueTransformer<Calendar> LONG2CALENDAR_TRANSFORMER=
            new AbstractExtendedLong2ValueTransformer<Calendar>(Calendar.class) {
                @Override
                public Calendar transform (long value) {
                    Calendar    cal=Calendar.getInstance();
                    cal.setTimeInMillis(value);
                    return cal;
                }
            };
    
    /**
     * An {@link ExtendedFactory} that returns the current time as a {@link Date}
     * every time its {@code create} method is called
     */
    public static final ExtendedFactory<Date>  NOW_DATE_FACTORY=
            new AbstractExtendedFactory<Date>(Date.class) {
                @Override
                public Date create () {
                    return LONG2DATE_TRANSFORMER.transform(System.currentTimeMillis());
                }
            };
    /**
     * @param d The original {@link Date} - ignored if {@code null}
     * @return A <U>new</U> {@link Date} that contains only the number of
     * <U>seconds</U> since epoch - useful for converting C/C++ (and other)
     * sources that use seconds granularity
     * @see #setEpochSeconds(Date)
     */
    public static final Date asEpochSeconds(Date d) {
        if (d == null) {
            return null;
        }
         
        Date    e=new Date(d.getTime());
        setEpochSeconds(e);
        return e;
    }
    
    /**
     * @param d The {@link Date} to be updated - ignored if {@code null}
     * @return The number of msec. that had to be &quot;shaved off&quot;
     * in order to set the value to contain only the number of <U>seconds</U>
     * since epoch - useful for converting C/C++ (and other) sources that use
     * seconds granularity (<B>Note:</B> returns negative value for {@code null})
     */
    public static final long setEpochSeconds(Date d) {
        if (d == null) {
            return -1L;
        }
        
        long    time=d.getTime(), msec=time % MILLIS_PER_SECOND;
        if (msec > 0L) {
            d.setTime(time - msec);
        }
        
        return msec;
    }
}
