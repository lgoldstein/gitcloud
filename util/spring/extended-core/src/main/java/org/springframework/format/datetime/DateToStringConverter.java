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

import java.util.Date;

import org.springframework.core.convert.converter.AbstractExtendedConverter;

/**
 * @author Lyor G.
 */
public class DateToStringConverter extends AbstractExtendedConverter<Date,String> {
    public static final DateToStringConverter   INSTANCE=new DateToStringConverter();

    public DateToStringConverter() {
        super(Date.class, String.class);
    }

    @Override
    public String convert(Date source) {
        if (source == null) {
            return null;
        } else {
            return String.valueOf(source.getTime());
        }
    }
}
