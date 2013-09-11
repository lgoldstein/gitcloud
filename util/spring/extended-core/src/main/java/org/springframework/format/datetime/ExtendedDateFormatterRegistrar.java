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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.time.Period;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.ExtendedConverter;
import org.springframework.core.convert.converter.ExtendedConverterRegistryUtils;
import org.springframework.core.convert.support.ExtendedObjectToStringConverter;
import org.springframework.format.FormatterRegistry;

/**
 * @author Lyor G.
 */
public class ExtendedDateFormatterRegistrar extends DateFormatterRegistrar {
    /**
     * An (un-modifiable) {@link List} of extra {@link ExtendedConverter} added
     * by the {@link #addExtendedDateConverters(ConverterRegistry)} method
     */
    public static final List<? extends ExtendedConverter<?,?>>    EXTRA_DATE_CONVERTERS=
            Collections.unmodifiableList(
                Arrays.asList(
                    StringToDateConverter.INSTANCE,
                    DateToStringConverter.INSTANCE,
                    StringToCalendarConverter.INSTANCE,
                    CalendarToStringConverter.INSTANCE,
                    StringToPeriodConverter.INSTANCE));

    public ExtendedDateFormatterRegistrar() {
        super();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        super.registerFormatters(registry);
        addExtendedDateConverters(registry);
    }

    public static void addExtendedDateConverters(ConverterRegistry registry) {
        ExtendedConverterRegistryUtils.addConverters(registry, EXTRA_DATE_CONVERTERS);
        registry.addConverter(Period.class, String.class, ExtendedObjectToStringConverter.INSTANCE);
    }
}
