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

import java.util.Date;

/**
 * @author Lyor G.
 */
@SuppressWarnings("deprecation")
public class ImmutableDate extends Date {
    private static final long serialVersionUID = 7674820612490029878L;

    public ImmutableDate() {
        super();
    }

    public ImmutableDate(long date) {
        super(date);
    }

    public ImmutableDate(Date date) {
        this(date.getTime());
    }

    @Override
    public final void setYear(int year) {
        throw new UnsupportedOperationException("setYear(" + year + ") N/A");
    }

    @Override
    public final void setMonth(int month) {
        throw new UnsupportedOperationException("setMonth(" + month + ") N/A");
    }

	@Override
    public final void setDate(int date) {
        throw new UnsupportedOperationException("setDate(" + date + ") N/A");
    }

    @Override
    public final void setHours(int hours) {
        throw new UnsupportedOperationException("setHours(" + hours + ") N/A");
    }

    @Override
    public final void setMinutes(int minutes) {
        throw new UnsupportedOperationException("setMinutes(" + minutes + ") N/A");
    }

    @Override
    public final void setSeconds(int seconds) {
        throw new UnsupportedOperationException("setSeconds(" + seconds + ") N/A");
    }

    @Override
    public final void setTime(long time) {
        throw new UnsupportedOperationException("setTime(" + time + ") N/A");
    }

}
