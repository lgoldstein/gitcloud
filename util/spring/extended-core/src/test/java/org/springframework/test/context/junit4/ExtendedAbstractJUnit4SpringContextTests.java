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

package org.springframework.test.context.junit4;

import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.AbstractSpringTestSupport;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

/**
 * A more convenient {@link AbstractJUnit4SpringContextTests} implementation
 * that automatically adds the jUnit {@code assertXXX} methods as inherited
 * directly
 * @author Lyor G.
 * @since Aug 27, 2013 12:59:08 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
public abstract class ExtendedAbstractJUnit4SpringContextTests
                extends AbstractSpringTestSupport
                implements ApplicationContextAware {

    protected ExtendedAbstractJUnit4SpringContextTests() {
        super();
    }

    /**
     * The {@link ApplicationContext} that was injected into this test instance
     * via {@link #setApplicationContext(ApplicationContext)}.
     */
    protected ApplicationContext applicationContext;


    /**
     * Set the {@link ApplicationContext} to be used by this test instance,
     * provided via {@link ApplicationContextAware} semantics.
     */
    @Override
    public final void setApplicationContext(final ApplicationContext context) {
        applicationContext = context;
    }
}
