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

package org.apache.commons.collections15.keyvalue;

/**
 * Represents any accessor that provides write access via a key
 * @param <K> Key type 
 * @param <V> Value type
 * @author Lyor G.
 */
public interface KeyedWriter<K, V> {
    /**
     * @param key The key for mapping a value
     * @param value The value to map
     * @return The previous value - <code>null</code> if none
     */
    V put(K key, V value);
}
