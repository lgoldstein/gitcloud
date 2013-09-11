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

/**
 * A {@link ThreadLocalFactory} implementation that invokes a delegate
 * {@link Factory} whenever its {@link #create()} method is invoked
 * @param <T> Type of created entity
 * @author Lyor G.
 */
public class DelegateThreadLocalFactory<T> extends ThreadLocalFactory<T> {
    private final Factory<T>    delegate;

    public DelegateThreadLocalFactory(Factory<T> factory) {
        if ((delegate=factory) == null) {
            throw new IllegalStateException("No delegate instance");
        }
    }

    public final Factory<T>  getDelegate() {
        return delegate;
    }

    @Override
    public T create() {
        return delegate.create();
    }

}
