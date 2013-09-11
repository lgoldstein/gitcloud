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

package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jun 5, 2013 3:36:47 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedFileFilterUtilsTest extends AbstractTestSupport {
    public ExtendedFileFilterUtilsTest () {
        super();
    }

    @Test
    public void testConjunctiveFilter() {
        testConjunctiveFilter(false, 0, TestFileFilter.EMPTY_FILTERS);
        testConjunctiveFilter(false, 2, TestFileFilter.ACCEPTOR(), TestFileFilter.REJECTOR());
        testConjunctiveFilter(false, 1, TestFileFilter.REJECTOR(), TestFileFilter.ACCEPTOR());
        testConjunctiveFilter(false, 1, TestFileFilter.REJECTOR(), TestFileFilter.REJECTOR());
        testConjunctiveFilter(true, 2, TestFileFilter.ACCEPTOR(), TestFileFilter.ACCEPTOR());
    }

    @Test
    public void testDisjunctiveFilter() {
        testDisjunctiveFilter(true, 0, TestFileFilter.EMPTY_FILTERS);
        testDisjunctiveFilter(true, 1, TestFileFilter.ACCEPTOR(), TestFileFilter.REJECTOR());
        testDisjunctiveFilter(true, 2, TestFileFilter.REJECTOR(), TestFileFilter.ACCEPTOR());
        testDisjunctiveFilter(false, 2, TestFileFilter.REJECTOR(), TestFileFilter.REJECTOR());
        testDisjunctiveFilter(true, 1, TestFileFilter.ACCEPTOR(), TestFileFilter.ACCEPTOR());
    }

    private static void testDisjunctiveFilter(boolean expResult, int expCount, TestFileFilter ... filters) {
        testCompoundFilter(false, expResult, expCount, filters);
    }

    private static void testConjunctiveFilter(boolean expResult, int expCount, TestFileFilter ... filters) {
        testCompoundFilter(true, expResult, expCount, filters);
    }

    private static void testCompoundFilter(boolean conjunctive, boolean expResult, int expCount, TestFileFilter ... filters) {
        FileFilter  filter=ExtendedFileFilterUtils.createFileFilter(conjunctive, filters);
        String      testName=(conjunctive ? "CONJ" : "DISJ") + Arrays.toString(filters);
        File        testFile=ExtendedFileUtils.retrieveTempDirectory();
        boolean     actResult=filter.accept(testFile);
        assertEquals("Mismatched result for " + testName, expResult, actResult);
        
        int actCount=0;
        for (TestFileFilter f : filters) {
            actCount += f.getInvocationCount();
        }
        
        assertEquals("Mismatched invocation count for " + testName, expCount, actCount);
    }

    private static class TestFileFilter extends AbstractFileFilter {
        private final boolean   accepting;
        private int invocationCount;

        static final TestFileFilter[]   EMPTY_FILTERS={ };

        static final TestFileFilter ACCEPTOR() {
            return new TestFileFilter(true);
        }

        static final TestFileFilter REJECTOR() {
            return new TestFileFilter(false);
        }

        TestFileFilter(boolean acceptIt) {
            accepting = acceptIt;
        }
        
        boolean isAccepting() {
            return accepting;
        }
        
        int getInvocationCount() {
            return invocationCount;
        }

        @Override
        public boolean accept(File pathname) {
            invocationCount++;
            return isAccepting();
        }

        @Override
        public String toString() {
            return isAccepting() ? "ACC" : "REJ";
        }
    }
}
