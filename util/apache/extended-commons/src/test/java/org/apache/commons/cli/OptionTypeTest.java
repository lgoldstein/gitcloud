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

package org.apache.commons.cli;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 4:14:05 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OptionTypeTest extends AbstractTestSupport {
	public OptionTypeTest () {
		super();
	}
	
	@Test
	public void testClassifier() {
		final String	BASE_NAME="testClassifier";
		assertNull("Unexpected classification", OptionType.classify(BASE_NAME));

		for (OptionType expected : OptionType.VALUES) {
			String	prefix=expected.getPrefix();
			assertNull("Unexpected classification for prefix=" + prefix, OptionType.classify(prefix));

			String	optValue=expected.createOption(BASE_NAME);
			OptionType	actual=OptionType.classify(optValue);
			assertSame(optValue + ": mismatched result", expected, actual);
		}
	}
}
