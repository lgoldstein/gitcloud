/* Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.jdbc.repo.impl;

import java.util.UUID;

import org.springframework.jdbc.repo.Identified;
import org.springframework.jdbc.repo.IdentifiedEntityIdGenerator;
import org.springframework.util.Assert;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 10:45:57 AM
 */
public class IdentifiedEntityIdGeneratorImpl<E extends Identified> implements IdentifiedEntityIdGenerator<E> {
    private final Class<E> entityClass;
    private final String    strValue;

    public IdentifiedEntityIdGeneratorImpl(Class<E> eClass) {
        Assert.state(eClass != null, "No entity class specified");
        entityClass = eClass;
        strValue = getClass().getSimpleName() + "[" + getEntityClass().getSimpleName() + "]";
    }

    @Override
    public final Class<E> getEntityClass() {
        return entityClass;
    }

    @Override
    public String build() {
        String  prefix=entityClass.getSimpleName();
        String  suffix=UUID.randomUUID().toString();
        return new StringBuilder(prefix.length() + suffix.length() + 1)
                        .append(prefix)
                        .append('-')
                        .append(suffix)
                        .toString();
    }

    @Override
    public String toString() {
        return strValue;
    }
}
