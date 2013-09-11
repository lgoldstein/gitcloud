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

package org.springframework.core.convert.support;

import org.apache.commons.lang3.exception.ExtendedExceptionUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * A {@link Converter} that uses the provided {@link ClassLoader} to retrieve
 * the {@link Class} instance specified by its {@link String} argument
 * @author Lyor G.
 */
public class StringToClassConverter implements Converter<String,Class<?>> {
    private final ClassLoader loader;

    public StringToClassConverter(ClassLoader cl) {
        Assert.state(cl != null, "No loader provided");
        loader = cl;
    }

    public final ClassLoader getLoader() {
        return loader;
    }

    @Override
    public Class<?> convert(String className) {
        if (!StringUtils.hasLength(className)) {
            return null;
        }

        try {
            return ClassUtils.forName(className, getLoader());
        } catch(Throwable t) {
            throw ExtendedExceptionUtils.toRuntimeException(t, true);
        }
    }
}
