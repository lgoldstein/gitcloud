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

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.ClassToStringConverter;
import org.springframework.core.convert.support.StringToClassConverter;
import org.springframework.format.datetime.ExtendedDateFormatterRegistrar;
import org.springframework.util.ClassUtils;

/**
 * A {@link ConversionServiceFactoryBean} that adds more out-of-the-box converters
 * @author Lyor G.
 * @since Aug 27, 2013 1:11:09 PM
 * @see ExtendedDateFormatterRegistrar#addExtendedDateConverters(ConverterRegistry)
 * @see StringToClassConverter
 * @see ClassToStringConverter
 */
public class ExtendedConversionServiceFactoryBean extends ConversionServiceFactoryBean {
    public ExtendedConversionServiceFactoryBean() {
        super();
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        
        ConversionService   service=getObject();
        if (!(service instanceof ConverterRegistry)) {
            throw new IllegalStateException("Service is not a converter's registry");
        }

        addExtraConverters((ConverterRegistry) service);
    }
    
    public static void addExtraConverters(ConverterRegistry registry) {
        ExtendedDateFormatterRegistrar.addExtendedDateConverters(registry);
        registry.addConverter(new StringToClassConverter(ClassUtils.getDefaultClassLoader()));
        registry.addConverter(Class.class, String.class, new ClassToStringConverter());
    }
}
