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

package org.apache.commons.lang3.time;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.ExtendedStringUtils;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 2:09:21 PM
 *
 */
public class ExtendedDateFormatUtils extends DateFormatUtils {
	/**
	 * @param cal The {@link Calendar} value - ignored if <code>null</code>
	 * @param fmt The {@link DateFormat} to use - if <code>null</code> then
	 * the {@link Date#toString()} value is used
	 * @return The result of <code>synchronized</code> invoking
	 * {@link #format(Date, DateFormat)} on the {@link Calendar#getTime()}
	 * value
	 * @see #format(Date, DateFormat)
	 */
	public static final String format(Calendar cal, DateFormat fmt) {
		if (cal == null) {
			return null;
		} else {
			return format(cal.getTime(), fmt);
		}
	}

	/**
	 * @param timestamp The UTC milliseconds offset from EPOCH
	 * @param fmt The {@link DateFormat} to use - if <code>null</code> then
	 * the {@link Date#toString()} value is used
	 * @return The result of invoking {@link DateFormat#format(Date)} using
	 * <code>synchronized</code> on the formatter instance
	 */
	public static final String format(long timestamp, DateFormat fmt) {
		return format(new Date(timestamp), fmt);
	}

	/**
	 * @param date The {@link Date} value - ignored if <code>null</code>
	 * @param fmt The {@link DateFormat} to use - if <code>null</code> then
	 * the {@link Date#toString()} value is used
	 * @return The result of invoking {@link DateFormat#format(Date)} using
	 * <code>synchronized</code> on the formatter instance
	 */
	public static final String format(Date date, DateFormat fmt) {
		if (date == null) {
			return null;
		}

		if (fmt == null) {
			return ExtendedStringUtils.safeToString(date);
		}
		
		synchronized(fmt) {
			return fmt.format(date);
		}
	}
}
