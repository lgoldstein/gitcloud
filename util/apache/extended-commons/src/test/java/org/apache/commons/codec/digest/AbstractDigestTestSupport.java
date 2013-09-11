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

package org.apache.commons.codec.digest;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.SortedSet;

import org.apache.commons.collections15.set.UnmodifiableSortedSet;
import org.apache.commons.test.AbstractTestSupport;

/**
 * @author Lyor G.
 * @since Sep 3, 2013 12:07:46 PM
 */
public abstract class AbstractDigestTestSupport extends AbstractTestSupport {
    protected static final SortedSet<String> ALGORITHMS=UnmodifiableSortedSet.decorate(ExtendedDigestUtils.getDigestAlgorithms());

    protected AbstractDigestTestSupport() {
        super();
    }
    
    protected <S extends DigesterStream> S testSameDigestInstanceAfterClose(S stream) throws IOException {
        MessageDigest   digest=stream.getDigest();
        String          algorithm=digest.getAlgorithm();

        stream.close();
        byte[]  expected=stream.getDigestValue();
        for (int index=0; index < Short.SIZE; index++) {
            byte[]  actual=stream.getDigestValue();
            assertSame(algorithm + ": mismatched digest at index=" + index, expected, actual);
        }

        return stream;
    }
}
