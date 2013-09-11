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

package org.apache.commons.collections15.numbers.longs;

import org.apache.commons.lang3.ExtendedValidate;

/**
 * @param <V> Transformation input type
 * @author Lyor G.
 * @since Jun 25, 2013 10:09:55 AM
 */
public abstract class AbstractExtendedValue2LongTransfomer<V> implements ExtendedValue2LongTransformer<V> {
    private final Class<V>  valueType;
    
    protected AbstractExtendedValue2LongTransfomer(Class<V> argType) {
        valueType = ExtendedValidate.notNull(argType, "No value type specified");
    }

    @Override
    public final Class<V> getSourceType () {
        return valueType;
    }
}
