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

package org.springframework.format.datetime;

import java.util.Calendar;

import org.springframework.core.convert.converter.AbstractExtendedConverter;
import org.springframework.util.StringUtils;

/**
 * Converts a {@link String} assumed to contain a <code>long</code> timestamp
 * to a {@link Calendar}
 * @author Lyor G.
 */
public class StringToCalendarConverter extends AbstractExtendedConverter<String,Calendar> {
    public static final StringToCalendarConverter   INSTANCE=new StringToCalendarConverter();

    public StringToCalendarConverter() {
        super(String.class, Calendar.class);
    }

    @Override
    public Calendar convert(String source) {
        if (StringUtils.hasLength(source)) {
            long        timestamp=Long.parseLong(source);
            Calendar    cal=Calendar.getInstance();
            cal.setTimeInMillis(timestamp);
            return cal;
        } else {
            return null;
        }
    }

}
