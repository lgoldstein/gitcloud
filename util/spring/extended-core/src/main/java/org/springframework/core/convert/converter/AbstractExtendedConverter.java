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

package org.springframework.core.convert.converter;

import org.springframework.util.Assert;

/**
 * @param <S> Source type 
 * @param <T> Target type
 * @author Lyor G.
 */
public abstract class AbstractExtendedConverter<S, T> implements ExtendedConverter<S, T> {
    private final Class<S> sourceClass;
    private final Class<T> targetClass;
    
    protected AbstractExtendedConverter(Class<S> srcClass, Class<T> tgtClass) {
        Assert.state((sourceClass=srcClass) != null, "No source class");
        Assert.state((targetClass=tgtClass) != null, "No target class");
    }

    @Override
    public final Class<S> getSourceType() {
        return sourceClass;
    }

    @Override
    public final Class<T> getTargetType() {
        return targetClass;
    }
}
