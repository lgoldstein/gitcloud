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

package org.apache.commons.collections15.set;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Oct 3, 2011 8:24:42 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LRUSetTest extends AbstractTestSupport {
	public LRUSetTest ()
	{
		super();
	}

	@Test
	public void testLRUSet ()
	{
		final int				MAX_SIZE=Short.SIZE;
		final LRUSet<Integer>	testSet=new LRUSet<Integer>(MAX_SIZE, true);
		for (int index=0; index < 2 * MAX_SIZE; index++)
		{
			assertEquals("Mismatched set size", Math.min(index, testSet.maxSize()), testSet.size());
			assertTrue("Failed to add value=" + index, testSet.add(Integer.valueOf(index)));

			final int	windowStart=Math.max(0, 1 + index - testSet.maxSize());
			for (int	loIndex=0; loIndex <= index; loIndex++)
				assertEquals("Containment inconsistency for " + loIndex + " on index=" + index,
							 (loIndex >= windowStart),
							  testSet.contains(Integer.valueOf(loIndex)));
		}
	}
}
