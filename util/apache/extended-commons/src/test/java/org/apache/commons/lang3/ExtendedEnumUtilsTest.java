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

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.numbers.ints.AbstractExtendedInt2ValueTransformer;
import org.apache.commons.collections15.numbers.ints.ExtendedInt2ValueTransformer;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author lgoldstein
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedEnumUtilsTest extends AbstractTestSupport {
    private static final EnumSet<TimeUnit>   VALUES=ExtendedEnumUtils.fullSet(TimeUnit.class);

	public ExtendedEnumUtilsTest() {
		super();
	}

	@Test
	public void testCaseInsensitiveMapping() {
        SortedMap<String,TimeUnit>    valuesMap=ExtendedEnumUtils.getEnumMap(false, VALUES);
        for (TimeUnit expected : VALUES) {
            for (int index=0; index < Byte.SIZE; index++) {
                String      key=shuffleCase(expected.name());
                TimeUnit    actual=valuesMap.get(key);
                assertSame("Mismatched instance for key=" + key, expected, actual);
            }
        }
	}
	
	@Test
	public void testFromNameTransformerCaseInsensitivity() {
	    Transformer<String,TimeUnit>   xformer=ExtendedEnumUtils.fromNameTransformer(false, VALUES);
        for (TimeUnit expected : VALUES) {
            for (int index=0; index < Byte.SIZE; index++) {
                String      key=shuffleCase(expected.name());
                TimeUnit    actual=xformer.transform(key);
                assertSame("Mismatched instance for key=" + key, expected, actual);
            }
        }
	}
	
	@Test
	public void testFromOrdinalTransformer() {
	    Collection<? extends ExtendedInt2ValueTransformer<? extends Number>> keyGenerators=
	            Arrays.asList(
	                    new AbstractExtendedInt2ValueTransformer<Integer>(Integer.class) {
                                @Override
                                public Integer transform (int value) {
                                    return Integer.valueOf(value);
                                }
                            },
                        new AbstractExtendedInt2ValueTransformer<Long>(Long.class) {
                                @Override
                                public Long transform (int value) {
                                    return Long.valueOf(value);
                                }
                            },
                        new AbstractExtendedInt2ValueTransformer<Float>(Float.class) {
                            @Override
                            public Float transform (int value) {
                                return Float.valueOf(value);
                            }
                        });
        Transformer<Number,TimeUnit>    xformer=ExtendedEnumUtils.fromOrdinalTransformer(VALUES);

        for (TimeUnit expected : VALUES) {
            int ordinal=expected.ordinal();
            for (ExtendedInt2ValueTransformer<? extends Number> generator : keyGenerators) {
                Number      key=generator.transform(ordinal);
                TimeUnit    actual=xformer.transform(key);
                assertSame("Mismatched result for key=" + key + "[" + generator.getDestinationType().getSimpleName() + "]", expected, actual);
            }
        }
	}
}
