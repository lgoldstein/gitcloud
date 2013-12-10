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

package org.apache.commons.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedFilenameUtilsTest extends AbstractTestSupport {
    public ExtendedFilenameUtilsTest() {
        super();
    }

    @Test
    public void testBuildFileFailureOnEmptyComponents () {
        List<String>    pathComponents=
                new ArrayList<String>(Arrays.asList(getClass().getSimpleName(), null, "testBuildFileFailureOnEmptyComponents", "", String.valueOf(Math.random())));
        for (int    index=0; index < Byte.SIZE; index++) {
            Collections.shuffle(pathComponents);
            try {
                String  path=ExtendedFilenameUtils.buildFilePath(pathComponents);
                fail("Unexpected success for " + pathComponents + ": " + path);
            } catch(IllegalArgumentException e) {
                // expected - ignored
            }
        }
    }

    @Test
    public void testGenerateDefaultTimestampedName() {
        final Calendar  now=Calendar.getInstance();
        final String    tsValue=ExtendedFilenameUtils.FILENAME_TIMESTAMP_FORMAT.format(now.getTime());
        final Calendar  work=Calendar.getInstance();
        for (String prefix : new String[] { null, "", getClass().getSimpleName() }) {
            for (String suffix : new String[] { null, "", getCurrentTestName() }) {
                String  expected=StringUtils.trimToEmpty(prefix) + tsValue + StringUtils.trimToEmpty(suffix);
                String  actual=ExtendedFilenameUtils.generateTimestampedName(prefix, now, suffix);
                assertEquals("Mismatched name result", expected, actual);
                
                Date    recovered=ExtendedFilenameUtils.parseTimestampedName(actual, prefix, suffix);
                assertNotNull("Cannot recover timestamp for " + actual, recovered);
                work.setTime(recovered);

                assertEquals("Mismatched recovered timestamp year", now.get(Calendar.YEAR), work.get(Calendar.YEAR));
                assertEquals("Mismatched recovered timestamp month", now.get(Calendar.MONTH), work.get(Calendar.MONTH));
                assertEquals("Mismatched recovered timestamp day", now.get(Calendar.DAY_OF_MONTH), work.get(Calendar.DAY_OF_MONTH));
                assertEquals("Mismatched recovered timestamp hour", now.get(Calendar.HOUR_OF_DAY), work.get(Calendar.HOUR_OF_DAY));
                assertEquals("Mismatched recovered timestamp minute", now.get(Calendar.MINUTE), work.get(Calendar.MINUTE));
                assertEquals("Mismatched recovered timestamp second", now.get(Calendar.SECOND), work.get(Calendar.SECOND));
            }
        }
    }

    @Test
    public void testParseTimestampedNameOnNonMatchingData() {
        final String    prefix=getClass().getSimpleName(), suffix=getCurrentTestName();
        for (String name : new String[] { null, "", prefix, suffix, prefix + suffix, prefix + "xxx", "xxx" + suffix}) {
            assertNull("Unexpected result for " + name, ExtendedFilenameUtils.parseTimestampedName(name, prefix, suffix));
        }
    }

    @Test
    public void testResolveAbsolutePath () {
        final File  testFile=ExtendedFileUtils.retrieveTempDirectory(), containerFile=ExtendedClassUtils.getClassContainerLocationFile(getClass());
        {
            String   result=ExtendedFilenameUtils.resolveAbsolutePath(containerFile.getAbsolutePath(), testFile.getAbsolutePath());
            assertEquals("Mismatched absolute location", testFile.getAbsolutePath(), result);
        }
        
        {
            File    parentFile=testFile.getParentFile();
            String	result=ExtendedFilenameUtils.resolveAbsolutePath(parentFile.getAbsolutePath(), testFile.getName());
            assertEquals("Mismatched relative location", testFile.getAbsolutePath(), result);
        }
        
        {
            String  name=testFile.getName(), result=ExtendedFilenameUtils.resolveAbsolutePath(containerFile.getAbsolutePath(), name);
            File    expected=new File(containerFile, name);
            assertEquals("Mismatched container resolved location", expected.getAbsolutePath(), result);
        }
    }
    
    @Test
    public void testResolveRelativePath () {
        final File  	testFile=ExtendedClassUtils.getClassContainerLocationFile(getClass());
        final String	testPath=testFile.getAbsolutePath(), testName=testFile.getName();
        {
            File    expected=testFile.getParentFile();
            String	result=ExtendedFilenameUtils.resolveAbsolutePath(testPath, "..");
            assertEquals("Mismatched parent", expected.getAbsolutePath(), result);
        }
        
        for (String relPath : new String[] { ".", ".." + File.separator + testName }) {
            String    result=ExtendedFilenameUtils.resolveAbsolutePath(testPath, relPath);
            assertEquals("Mismatched CWD for relPath=" + relPath, testFile.getAbsolutePath(), result);
        }
    }
}
