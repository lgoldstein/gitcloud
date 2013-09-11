/*
 * 
 */
package org.apache.commons.lang3.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 2:16:07 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedDateFormatUtilsTest extends AbstractTestSupport {
	public ExtendedDateFormatUtilsTest ()
	{
		super();
	}

	@Test
	public void testNullDateValue() {
		DateFormat	fmt=new SimpleDateFormat("yyyy-mm-dd HH:MM:SS");
		assertNull("Unexpected null-Date result", ExtendedDateFormatUtils.format((Date) null, fmt));
		assertNull("Unexpected null-Calendar result", ExtendedDateFormatUtils.format((Calendar) null, fmt));
	}
	
	@Test
	public void testNullFormatter() {
		final Date		date=new Date(System.currentTimeMillis());
		final String	expected=date.toString();
		assertEquals("Mismatched Date value result", expected, ExtendedDateFormatUtils.format(date, (DateFormat) null));
		
		Calendar	cal=Calendar.getInstance();
		cal.setTime(date);
		assertEquals("Mismatched Calendar value result", expected, ExtendedDateFormatUtils.format(cal, (DateFormat) null));
	}
}
