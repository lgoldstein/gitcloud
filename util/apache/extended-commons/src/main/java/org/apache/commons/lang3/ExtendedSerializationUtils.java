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

package org.apache.commons.lang3;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Lyor G.
 * @since Jul 30, 2013 3:19:47 PM
 */
public class ExtendedSerializationUtils extends SerializationUtils {
    public ExtendedSerializationUtils() {
        super();
    }

    public static final Object deserialize(byte[] objectData, int offset, int len) {
        Validate.notNull(objectData, "No data bytes", ArrayUtils.EMPTY_OBJECT_ARRAY);
        ByteArrayInputStream bais = new ByteArrayInputStream(objectData, offset, len);
        try {
            return deserialize(bais);
        } finally {
            try {
                bais.close();
            } catch(IOException e) {
                // ignored
            }
        }
    }

    public static final <T> T deserialize(Class<T> objType, byte[] objectData) {
        return deserialize(objType, objectData, 0, ArrayUtils.getLength(objectData));
    }

    public static final <T> T deserialize(Class<T> objType, byte[] objectData, int offset, int len) {
        Object  obj=deserialize(objectData, offset, len);
        try {
            return objType.cast(obj);
        } catch(ClassCastException e) {
            throw new SerializationException(e);
        }
    }
}
