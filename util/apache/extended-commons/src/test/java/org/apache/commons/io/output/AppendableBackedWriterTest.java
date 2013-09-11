/*
 * 
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
