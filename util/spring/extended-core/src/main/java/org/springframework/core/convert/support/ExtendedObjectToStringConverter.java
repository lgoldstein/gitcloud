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

import org.apache.commons.lang3.ExtendedStringUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * An equivalent of the original that for some reason is not public...
 * @author Lyor G.
 */
public class ExtendedObjectToStringConverter implements Converter<Object,String> {
    public static final ExtendedObjectToStringConverter INSTANCE=new ExtendedObjectToStringConverter();

    public ExtendedObjectToStringConverter() {
        super();
    }

    @Override
    public String convert(Object source) {
        return ExtendedStringUtils.safeToString(source);
    }

}
