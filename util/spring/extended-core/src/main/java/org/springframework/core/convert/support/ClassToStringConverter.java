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
 * 
 */

package org.springframework.core.convert.support;

import org.springframework.core.convert.converter.Converter;

/**
 * A {@link Converter} that returns the full {@link Class} name
 * @author Lyor G.
 */
public class ClassToStringConverter implements Converter<Class<?>,String> {
    public ClassToStringConverter() {
        super();
    }

    @Override
    public String convert(Class<?> clazz) {
        if (clazz == null) {
            return null;
        } else {
            return clazz.getName();
        }
    }
}
