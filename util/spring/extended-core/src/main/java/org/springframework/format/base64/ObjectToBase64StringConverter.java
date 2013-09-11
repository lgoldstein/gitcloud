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
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.core.convert.converter.AbstractExtendedConverter;

/**
 * @param <T> Type of {@link Serializable} object being serialized
 * @author Lyor G.
 * @since Aug 27, 2013 8:49:50 AM
 */
public class ObjectToBase64StringConverter<T extends Serializable> extends AbstractExtendedConverter<T, String> {
    public ObjectToBase64StringConverter(Class<T> srcClass) {
        super(srcClass, String.class);
    }

    @Override
    public String convert(T source) {
        if (source == null) {
            return "";
        }

        byte[] bytes=SerializationUtils.serialize(source);
        String str=Base64.encodeBase64String(bytes);
        return str;
    }
}
