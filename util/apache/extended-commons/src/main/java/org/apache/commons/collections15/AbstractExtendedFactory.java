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

package org.apache.commons.collections15;

import org.apache.commons.lang3.ExtendedValidate;

/**
 * @param <T> Type of created entity
 * @author Lyor G.
 */
public abstract class AbstractExtendedFactory<T> implements ExtendedFactory<T> {
    private final Class<T>  instType;

    protected AbstractExtendedFactory(Class<T> instanceType) {
        instType = ExtendedValidate.notNull(instanceType, "No instance type specified");
    }

    @Override
    public final Class<T> getInstanceType() {
        return instType;
    }
}
