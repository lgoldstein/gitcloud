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

package org.apache.sshd.common;

import org.apache.commons.logging.AbstractLoggingBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @param <T> Type of object being created
 * @author Lyor G.
 * @since Aug 29, 2013 9:56:26 AM
 */
public abstract class AbstractFactory<T> extends AbstractLoggingBean implements Factory<T> {
    protected AbstractFactory() {
        super();
    }

    protected AbstractFactory(Log log) {
        super(log);
    }

    protected AbstractFactory(Class<?> index) {
        super(index);
    }

    protected AbstractFactory(String index) {
        super(index);
    }

    protected AbstractFactory(LogFactory factory) {
        super(factory);
    }

    protected AbstractFactory(LogFactory factory, Class<?> index) {
        super(factory, index);
    }

    protected AbstractFactory(LogFactory factory, String index) {
        super(factory, index);
    }
}
