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
import org.springframework.util.StringUtils;

/**
 * Converts a {@link String} assumed to contained a <code>long</code>
 * timestamp value to a {@link Date}
 * @author Lyor G.
 */
public class StringToDateConverter extends AbstractExtendedConverter<String,Date> {
    public static final StringToDateConverter   INSTANCE=new StringToDateConverter();

    public StringToDateConverter() {
        super(String.class, Date.class);
    }

    @Override
    public Date convert(String source) {
        if (StringUtils.hasLength(source)) {
            long    timestamp=Long.parseLong(source);
            return new Date(timestamp);
        } else {
            return null;
        }
    }
}