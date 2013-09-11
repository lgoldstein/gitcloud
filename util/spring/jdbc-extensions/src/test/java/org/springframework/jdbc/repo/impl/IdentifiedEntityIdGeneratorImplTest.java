/* Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.jdbc.repo.impl;

import java.util.HashSet;
import java.util.Set;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.jdbc.repo.Identified;
import org.springframework.jdbc.repo.IdentifiedEntityIdGenerator;
import org.springframework.test.AbstractSpringTestSupport;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 10:48:33 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IdentifiedEntityIdGeneratorImplTest extends AbstractSpringTestSupport {
    public IdentifiedEntityIdGeneratorImplTest() {
        super();
    }

    @Test
    public void testUniqueIdentifiers() {
        IdentifiedEntityIdGenerator<Identified> generator=new IdentifiedEntityIdGeneratorImpl<>(Identified.class);
        Set<String> idsSet=new HashSet<>(Byte.MAX_VALUE);
        for (int index=0; index < Byte.MAX_VALUE; index++) {
            String  id=generator.build();
            assertTrue("Non unique identifier: " + id, idsSet.add(id));
        }
    }
}
