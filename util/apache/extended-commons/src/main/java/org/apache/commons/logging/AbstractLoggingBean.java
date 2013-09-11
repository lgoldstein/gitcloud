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

package org.apache.commons.logging;

import org.apache.commons.lang3.StringUtils;

/**
 * Provides a base class for beans (and non-beans) with a {@link Log}-ger instance.
 * It is highly recommended to use this base class wherever possible so we can have
 * as much a centralized control over the logging in our code as possible.
 * @author Lyor G.
 */
public abstract class AbstractLoggingBean {
    protected final transient Log logger;

    protected AbstractLoggingBean() {
        this("");
    }

    protected AbstractLoggingBean(Log log) {
        logger = (log == null) ? LogFactory.getLog(getClass()) : log;
    }

    protected AbstractLoggingBean(Class<?> index) {
        this(null, index);
    }

    protected AbstractLoggingBean(String index) {
        this(null, index);
    }
    
    protected AbstractLoggingBean(LogFactory factory) {
        this(factory, "");
    }

    protected AbstractLoggingBean(LogFactory factory, Class<?> index) {
        this(factory, (index == null) ? "" : index.getSimpleName());
    }

    protected AbstractLoggingBean(LogFactory factory, String index) {
        if (StringUtils.isEmpty(index)) {
            logger = (factory == null)
                   ? LogFactory.getLog(getClass())
                   : factory.getInstance(getClass())
                   ;
        } else {
            logger = (factory == null)
                   ? LogFactory.getLog(getClass().getName() + "[" + index + "]")
                   : factory.getInstance(getClass().getName() + "[" + index + "]")
                   ;
        }
    }
    
    public final Log getLogger() {
        return logger;
    }
}
