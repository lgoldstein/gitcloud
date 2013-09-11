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

package org.apache.commons.io.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author lgoldstein
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CloseShieldWriterTest extends AbstractTestSupport {
	public CloseShieldWriterTest() {
		super();
	}

	@Test
	public void testShielding() throws IOException {
		File	file=createTempFile("testShielding", ".txt");
		Writer	proxy=new FileWriter(file);
		try {
			testShieldedWriting(proxy, "Pure created proxy", false);
			
			Writer	shield=new CloseShieldWriter(proxy);
			try {
				testShieldedWriting(shield, "Shielded proxy", false);
			} finally {
				shield.close();
			}

			testShieldedWriting(shield, "Closed shield", true);
			testShieldedWriting(proxy, "Non-closed proxy", false);
		} finally {
			proxy.close();
		}

		testShieldedWriting(proxy, "Pure closed proxy", true);
	}
	
	private static void testShieldedWriting(Writer w, CharSequence data, boolean throwsException) throws IOException {
		try {
			w.append(data);
			if (throwsException) {
				fail(data + ": Unexpected success when writing");
			}
		} catch(IOException e) {
			if (throwsException) {
				return;	// expected
			} else {
				throw e;
			}
		}
	}
}
