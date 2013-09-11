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

package org.apache.commons.lang3;

import java.net.URL;
import java.util.Map;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedClassUtilsTest extends AbstractTestSupport {
    public ExtendedClassUtilsTest() {
        super();
    }

    @Test
    public void testGetDefaultClassLoaderForThreadContext () {
        Thread      thread=Thread.currentThread();
        ClassLoader clThread=thread.getContextClassLoader();
        if (clThread != null) {
            ClassLoader cl=ExtendedClassUtils.getDefaultClassLoader(getClass());
            assertSame("Mismatched loaders", clThread, cl);
        }
    }

    @Test
    public void testGetDefaultClassLoaderForCoreClass () {
        Class<?>    TEST_CLASS=String.class;
        ClassLoader clCore=TEST_CLASS.getClassLoader();
        if (clCore == null) {
            clCore = ClassLoader.getSystemClassLoader();
        }

        Thread      thread=Thread.currentThread();
        ClassLoader clThread=thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(null);
            ClassLoader cl=ExtendedClassUtils.getDefaultClassLoader(TEST_CLASS);
            assertSame("Mismatched loaders", clCore, cl);
        } finally {
            thread.setContextClassLoader(clThread);
        }
    }

    @Test
    public void testGetClassContainerLocationURLOnJarClasses () {
        for (Class<?> clazz : new Class[]{ Assert.class, String.class, getClass() }) {
            URL url=ExtendedClassUtils.getClassContainerLocationURL(clazz);
            assertNotNull("No URL detected for " + clazz.getSimpleName(), url);
        }
    }

    @Test
    public void testGetClassBytesUrlOnPackedClasses() {
        for (Class<?> clazz : new Class[]{ Assert.class, Mockito.class }) {
            URL url=ExtendedClassUtils.getClassContainerLocationURL(clazz);
            assertNotNull("No URL detected for " + clazz.getSimpleName(), url);
        }
    }

    @Test
    public void testGetClassBytesUrlOnInternalClasses() {
        for (Class<?> clazz : new Class[]{
                    ObjectUtils.Null.class,
                    Map.Entry.class 
                }) {
            URL url=ExtendedClassUtils.getClassBytesURL(clazz);
            assertNotNull("No URL detected for " + clazz.getSimpleName(), url);
        }
    }
}
