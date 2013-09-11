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

package org.apache.commons.collections15.numbers.longs;

import org.apache.commons.collections15.numbers.ints.ExtendedInt2ValueTransformer;

/**
 * @author Lyor G.
 * @since Jun 25, 2013 1:41:45 PM
 */
public class LongValueTransformerUtils {
    /**
     * An {@link ExtendedInt2ValueTransformer} that returns the {@link Long}
     * wrapper object for the transformed value
     */
    public static final ExtendedLong2ValueTransformer<Long>   LONG_WRAPPER_TRANSFORMER=
            new AbstractExtendedLong2ValueTransformer<Long>(Long.class) {
                @Override
                public Long transform (long value) {
                    return Long.valueOf(value);
                }
            }; 

    /**
     * An {@link ExtendedValue2LongTransformer} that returns the value of
     * the {@link Number#longValue()} argument
     */
    public static final ExtendedValue2LongTransformer<Number>   LONG_VALUE_EXTRACTOR=
            new AbstractExtendedValue2LongTransfomer<Number>(Number.class) {
                @Override
                public long transform(Number value) {
                    return value.longValue();
                }
            };
}
