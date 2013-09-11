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

package org.apache.commons.collections15;

import org.apache.commons.lang3.ExtendedValidate;

/**
 * @param <T> Type of predicate argument
 * @author Lyor G.
 */
public abstract class AbstractExtendedPredicate<T> implements ExtendedPredicate<T> {
    private final Class<T>  argType;

    protected AbstractExtendedPredicate(Class<T> argClass) {
        argType = ExtendedValidate.notNull(argClass, "No argument type provided");
    }

    @Override
    public final Class<T> getArgumentType() {
        return argType;
    }
}
