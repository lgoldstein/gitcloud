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

package org.springframework.context;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.AbstractLoggingBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.Assert;

/**
 * Provides a &quot;one-shot&quot; call to {@link #onContextInitialized(ApplicationContext)}
 * when Spring context is initialized for the <U>first</U> time
 * @author Lyor G.
 * @since Aug 27, 2013 12:39:47 PM
 */
public abstract class RefreshedContextAttacher extends AbstractLoggingBean implements ApplicationListener<ApplicationEvent> {
    private final transient AtomicReference<ApplicationContext>  contextInstance=new AtomicReference<ApplicationContext>(null);

    protected RefreshedContextAttacher() {
        super();
    }

    protected RefreshedContextAttacher(Log log) {
        super(log);
    }

    protected RefreshedContextAttacher(Class<?> index) {
        super(index);
    }

    protected RefreshedContextAttacher(String index) {
        super(index);
    }

    protected RefreshedContextAttacher(LogFactory factory) {
        super(factory);
    }

    protected RefreshedContextAttacher(LogFactory factory, Class<?> index) {
        super(factory, index);
    }

    protected RefreshedContextAttacher(LogFactory factory, String index) {
        super(factory, index);
    }

    @Override
    public void onApplicationEvent (final ApplicationEvent event) {
        if (!(event instanceof ContextRefreshedEvent)) {
            return;
        }

        ApplicationContext  prevContext, context=((ContextRefreshedEvent) event).getApplicationContext(); 
        synchronized(contextInstance) {
            if ((prevContext=contextInstance.get()) == null) {
                contextInstance.set(context);
            }
        }
        
        if (prevContext == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("onApplicationEvent(" + context.getDisplayName() + ") initializing");
            }
            onContextInitialized(context);
        } else if (logger.isDebugEnabled()) {
            logger.debug("onApplicationEvent(" + event + ") skip - already initialized");
        }
    }

    protected ApplicationContext getApplicationContext () {
        return contextInstance.get();
    }

    protected void onContextInitialized(ApplicationContext context) {
        Assert.state(context != null, "No initialized context provided");
    }
}
