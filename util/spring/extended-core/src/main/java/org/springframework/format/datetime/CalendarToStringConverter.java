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

/**
 * @author Lyor G.
 */
public class CalendarToStringConverter extends AbstractExtendedConverter<Calendar,String> {
    public static final CalendarToStringConverter   INSTANCE=new CalendarToStringConverter();

    public CalendarToStringConverter() {
        super(Calendar.class, String.class);
    }

    @Override
    public String convert(Calendar source) {
        if (source == null) {
            return null;
        } else {
            return String.valueOf(source.getTimeInMillis());
        }
    }
}
