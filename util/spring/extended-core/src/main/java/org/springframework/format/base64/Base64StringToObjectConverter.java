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

package org.springframework.format.base64;

import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ExtendedSerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.AbstractExtendedConverter;

/**
 * @param <T> Type of {@link Serializable} object being serialized
 * @author Lyor G.
 * @since Aug 27, 2013 8:49:12 AM
 */
public class Base64StringToObjectConverter<T extends Serializable> extends AbstractExtendedConverter<String, T> {
    public Base64StringToObjectConverter(Class<T> tgtClass) {
        super(String.class, tgtClass);
    }

    @Override
    public T convert(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }

        byte[]  bytes=Base64.decodeBase64(source);
        T       obj=ExtendedSerializationUtils.deserialize(getTargetType(), bytes);
        return obj;
    }
}
