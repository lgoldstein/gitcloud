/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.context.support;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.lang3.concurrent.TimeUnitUtils;
import org.apache.commons.lang3.time.Period;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ExtendedObjectToStringConverter;
import org.springframework.format.datetime.CalendarToStringConverter;
import org.springframework.format.datetime.DateToStringConverter;
import org.springframework.format.datetime.StringToCalendarConverter;
import org.springframework.format.datetime.StringToDateConverter;
import org.springframework.format.datetime.StringToPeriodConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.ExtendedAbstractJUnit4SpringContextTests;

/**
 * @author Lyor G.
 * @since Aug 27, 2013 1:15:58 PM
 */
@ContextConfiguration(locations={ "classpath:org/springframework/context/support/ExtendedConversionServiceFactoryBeanTest-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedConversionServiceFactoryBeanTest extends ExtendedAbstractJUnit4SpringContextTests {
    @Inject private ConversionService   conversionService;

    public ExtendedConversionServiceFactoryBeanTest() {
        super();
    }

    @Test
    public void testString2DateConversion() {
        Date    date=new Date(System.currentTimeMillis());
        testTwoWayConversion(Date.class, date, String.class, DateToStringConverter.INSTANCE);
        testTwoWayConversion(String.class, DateToStringConverter.INSTANCE.convert(date), Date.class, StringToDateConverter.INSTANCE);
    }

    @Test
    public void testString2CaledarConversion() {
        Calendar    cal=Calendar.getInstance();
        testTwoWayConversion(Calendar.class, cal, String.class, CalendarToStringConverter.INSTANCE);
        testTwoWayConversion(String.class, CalendarToStringConverter.INSTANCE.convert(cal), Calendar.class, StringToCalendarConverter.INSTANCE);
    }

    @Test
    public void testString2PeriodConversion() {
        for (TimeUnit unit : TimeUnitUtils.VALUES) {
            Period  p=Period.valueOf(unit, 1L + unit.ordinal());
            String  s=ExtendedObjectToStringConverter.INSTANCE.convert(p);
            testTwoWayConversion(String.class, s, Period.class, StringToPeriodConverter.INSTANCE);
            testTwoWayConversion(Period.class, p, String.class, ExtendedObjectToStringConverter.INSTANCE);
        }
    }

    private <S,T> Pair<T,S> testTwoWayConversion(Class<S> sClass, S sData, Class<T> tClass, Converter<? super S,? extends T> converter) {
        T   converted=testConversion(sClass, sData, tClass, converter.convert(sData));
        S   recovered=testConversion(tClass, converted, sClass, sData);
        return Pair.of(converted, recovered);
    }

    private <S,T> T testConversion(Class<S> sClass, S sData, Class<T> tClass, T expected) {
        assertTrue("Convert " + sClass.getSimpleName() + " -> " + tClass.getSimpleName() + " ?", conversionService.canConvert(sClass, tClass));
        
        T   actual=conversionService.convert(sData, tClass);
        assertEquals("Mismatched " + sClass.getSimpleName() + " -> " + tClass.getSimpleName() + " conversion result", expected, actual);
        return actual;
    }
}
