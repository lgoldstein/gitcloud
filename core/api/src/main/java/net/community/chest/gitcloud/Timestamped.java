/* Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.community.chest.gitcloud;

import java.util.Comparator;
import java.util.Date;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.lang3.math.ExtendedNumberUtils;

/**
 * Represents a bean that carries a timestamp value
 * @author Lyor Goldstein
 * @since Sep 11, 2013 1:39:16 PM
 */
public interface Timestamped {
    static final String TIMESTAMP_PROPERTY="timestamp";

    /**
     * @return Number of msec. since EPOCH
     */
    long getTimestamp();
    
    /**
     * Compares 2 {@link Timestamped} according to their {@link #getTimestamp()}
     * value(s)
     */
    static final Comparator<Timestamped>    BY_TIMESTAMP_COMPARATOR=
            new Comparator<Timestamped>() {
                @Override
                public int compare(Timestamped o1, Timestamped o2) {
                    if (o1 == o2) {
                        return 0;
                    } else if (o1 == null) {
                        return (+1);
                    } else if (o2 == null) {
                        return (-1);
                    } else {
                        return ExtendedNumberUtils.compare(o1.getTimestamp(), o2.getTimestamp());
                    }
                }
        };
    
    /**
     * Returns the {@link Timestamped#getTimestamp()} value as a {@link Date}
     */
    static final ExtendedTransformer<Timestamped,Date>  TO_DATE_XFORMER=
            new AbstractExtendedTransformer<Timestamped, Date>(Timestamped.class, Date.class) {
                @Override
                public Date transform(Timestamped ts) {
                    if (ts == null) {
                        return null;
                    } else {
                        return new Date(ts.getTimestamp());
                    }
                }
            };
}
