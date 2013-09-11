/*
 * 
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
