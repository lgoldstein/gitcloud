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

package org.apache.commons.lang3.builder;

import org.apache.commons.lang3.ExtendedValidate;

/**
 * @param <T> Type of built entity
 * @author Lyor G.
 */
public abstract class AbstractExtendedBuilder<T> implements ExtendedBuilder<T> {
    private final Class<T>  eClass;
    
    protected AbstractExtendedBuilder(Class<T> entityClass) {
        eClass = ExtendedValidate.notNull(entityClass, "No entity class specified");
    }

    @Override
    public final Class<T> getEntityClass() {
        return eClass;
    }
}
