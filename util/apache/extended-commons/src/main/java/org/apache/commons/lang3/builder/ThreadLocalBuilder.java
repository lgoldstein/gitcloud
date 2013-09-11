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

/**
 * A {@link ThreadLocal} implementation where every time {@link #initialValue()}
 * is called, the {@link Builder#build()} method is called.
 * @param <T> Type of built entity
 * @author Lyor G.
 */
public abstract class ThreadLocalBuilder<T> extends ThreadLocal<T> implements Builder<T> {
    protected ThreadLocalBuilder() {
        super();
    }

    @Override
    protected T initialValue() {
        return build();
    }
}