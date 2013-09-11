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

package org.apache.sshd.common;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @param <T> Type of entity being created
 * @author Lyor G.
 * @since Aug 29, 2013 9:51:16 AM
 */
public abstract class AbstractNamedFactory<T> extends AbstractFactory<T> implements NamedFactory<T> {
    private final String    _name;

    protected AbstractNamedFactory(Class<?> name) {
        this((LogFactory) null, name);
    }

    protected AbstractNamedFactory(LogFactory factory, Class<?> name) {
        this(factory, Validate.notNull(name, "No name class", ArrayUtils.EMPTY_OBJECT_ARRAY).getSimpleName());
    }

    protected AbstractNamedFactory(String name) {
        this((LogFactory) null, name);
    }
    
    protected AbstractNamedFactory(LogFactory factory, String name) {
        super(factory, Validate.notEmpty(name, "No name", ArrayUtils.EMPTY_OBJECT_ARRAY));
        _name = name;
    }

    protected AbstractNamedFactory(Log log, Class<?> name) {
        this(log, Validate.notNull(name, "No name class", ArrayUtils.EMPTY_OBJECT_ARRAY).getSimpleName());
    }

    protected AbstractNamedFactory(Log log, String name) {
        super(log);
        _name = Validate.notEmpty(name, "No name", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public final String getName() {
        return _name;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + "]";
    }
}
