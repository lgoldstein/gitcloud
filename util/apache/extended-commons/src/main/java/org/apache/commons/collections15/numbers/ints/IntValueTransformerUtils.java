/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.collections15.numbers.ints;

import org.apache.commons.lang3.ObjectUtils;

/**
 * @author Lyor G.
 * @since Jun 25, 2013 10:04:51 AM
 */
public class IntValueTransformerUtils {
    /**
     * An {@link ExtendedInt2ValueTransformer} that returns the {@link Integer}
     * wrapper object for the transformed value
     */
    public static final ExtendedInt2ValueTransformer<Integer>   INTEGER_WRAPPER_TRANSFORMER=
            new AbstractExtendedInt2ValueTransformer<Integer>(Integer.class) {
                @Override
                public Integer transform (int value) {
                    return Integer.valueOf(value);
                }
            };
    /**
     * A {@link ExtendedValue2IntTransfomer} that returns the {@link Number#intValue()}
     * of its argument
     */
    public static final ExtendedValue2IntTransfomer<Number> INTEGER_VALUE_EXTRACTOR=
            new AbstractExtendedValue2IntTransfomer<Number>(Number.class) {
                @Override
                public int transform(Number value) {
                    return value.intValue();
                }
            };

    /**
     * An {@link ExtendedValue2IntTransfomer} that returns the hash code
     * of its {@link Object} argument
     * @see ObjectUtils#hashCode(Object)
     */
    public static final ExtendedValue2IntTransfomer<Object> HASH_CODE_EXTRACTOR=
            new AbstractExtendedValue2IntTransfomer<Object>(Object.class) {
                @Override
                public int transform(Object value) {
                    return ObjectUtils.hashCode(value);
                }
            };
}
