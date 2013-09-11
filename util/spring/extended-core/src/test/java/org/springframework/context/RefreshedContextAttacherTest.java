/*
 * Copyright 2002-2012 the original author or authors.
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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.reflect.ExtendedConstructorUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.ExtendedAbstractJUnit4SpringContextTests;

/**
 * @author Lyor G.
 * @since Aug 27, 2013 12:52:01 PM
 */
@ContextConfiguration(locations={ "classpath:org/springframework/context/RefreshedContextAttacherTest-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RefreshedContextAttacherTest extends ExtendedAbstractJUnit4SpringContextTests {
    public RefreshedContextAttacherTest() {
        super();
    }

    @Test
    public void testSingleOnContextInitializedCall() {
        final AtomicInteger         counter=new AtomicInteger(0);
        RefreshedContextAttacher    attacher=new RefreshedContextAttacher() {
                @Override
                protected void onContextInitialized(ApplicationContext context) {
                    super.onContextInitialized(context);
                    logger.info("onContextInitialized(" + context.getDisplayName() + ") call count: " + counter.incrementAndGet());
                }
            };
        ContextRefreshedEvent   event=new ContextRefreshedEvent(applicationContext);
        for (int index=1; index <= Byte.SIZE; index++) {
            attacher.onApplicationEvent(event);
            Assert.assertEquals("Mismatched number of calls at index=" + index, 1, counter.get());
        }
    }
    
    @Test
    public void testOnContextInitializedNotCalledIfNotContextRefreshedEvent() {
        final AtomicInteger         counter=new AtomicInteger(0);
        RefreshedContextAttacher    attacher=new RefreshedContextAttacher() {
                @Override
                protected void onContextInitialized(ApplicationContext context) {
                    super.onContextInitialized(context);
                    logger.info("onContextInitialized(" + context.getDisplayName() + ") call count: " + counter.incrementAndGet());
                }
            };
        Object[]    args={ applicationContext };
        Class<?>[]  parameterTypes={ ApplicationContext.class };    
        for (Class<? extends ApplicationContextEvent> eventClass :
                Arrays.asList(ContextClosedEvent.class, ContextStartedEvent.class, ContextStoppedEvent.class)) {
            ApplicationContextEvent event=ExtendedConstructorUtils.newInstance(eventClass, args, parameterTypes);
            attacher.onApplicationEvent(event);
            Assert.assertEquals("Mismatched number of calls at for " + eventClass.getSimpleName(), 0, counter.get());
        }
    }

}
