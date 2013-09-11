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

import java.io.IOException;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 1:57:54 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppendableBackedWriterTest extends AbstractTestSupport {
	public AppendableBackedWriterTest () {
		super();
	}

	@Test
	public void testWriter() throws IOException {
		final String	EXPECTED=getClass().getName() + "#testWriter";
		AppendableBackedWriter<StringBuilder>	writer=new AppendableBackedWriter<StringBuilder>(new StringBuilder());
		try {
			writer.write(EXPECTED);
		} finally  {
			writer.close();
		}
		
		StringBuilder	sb=writer.getAppender();
		assertEquals("Mismatched contents", EXPECTED, sb.toString());
	}

	@Test
	public void testWriterCloseBehavior() throws IOException {
		AppendableBackedWriter<StringBuilder>	writer=new AppendableBackedWriter<StringBuilder>(new StringBuilder());
		try {
			assertTrue("Writer not open after construction", writer.isOpen());
			writer.write("testWriterCloseBehavior");
			assertTrue("Writer not open after write", writer.isOpen());
		} finally {
			writer.close();
		}

		assertFalse("Writer still open", writer.isOpen());

		try {
			writer.append("More data");
			fail("Unexpected append success after close");
		} catch(IOException e) {
			// expected - ignored
		}
		
		for (int index=0; index < Byte.SIZE; index++) {
			try {
				writer.close();
				assertFalse("Writer re-opened at index=" + index, writer.isOpen());
			} catch(IOException e) {
				fail("Close not idempotent at index=" + index);
			}
		}
	}
}
