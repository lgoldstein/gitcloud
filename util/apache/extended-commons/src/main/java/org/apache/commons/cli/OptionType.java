/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang3.ExtendedCharSequenceUtils;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 4:02:45 PM
 */
public enum OptionType {
	SHORT(ExtendedUtil.SHORT_OPTION_PREFIX),
	LONG(ExtendedUtil.LONG_OPTION_PREFIX);

	private final String	prefix;
	OptionType(String p) {
		prefix = p;
	}

	public final String getPrefix() {
		return prefix;
	}

	public final String createOption(CharSequence name) {
		if (StringUtils.isEmpty(name)) {
			return ExtendedStringUtils.safeToString(name);
		} else {
			return getPrefix() + name;
		}
	}
	
	public static final Set<OptionType>	VALUES=
			Collections.unmodifiableSet(EnumSet.allOf(OptionType.class));
	public static final OptionType classify(CharSequence name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		
		for (OptionType type : VALUES) {
			String	prefix=type.getPrefix();
			if (ExtendedCharSequenceUtils.isProperPrefix(name, prefix, true) && (name.charAt(prefix.length()) != '-')) {
				return type;
			}
		}
		
		return null;
	}
}
