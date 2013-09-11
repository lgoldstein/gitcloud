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

import org.apache.commons.lang3.ExtendedValidate;

/**
 * @param <V> Transformation result type
 * @author Lyor G.
 * @since Jun 25, 2013 9:36:52 AM
 */
public abstract class AbstractExtendedInt2ValueTransformer<V> implements ExtendedInt2ValueTransformer<V> {
    private final Class<V>  resultType;
    
    protected AbstractExtendedInt2ValueTransformer(Class<V> resType) {
        resultType = ExtendedValidate.notNull(resType, "No result type specified");
    }

    @Override
    public final Class<V> getDestinationType () {
        return resultType;
    }
}
