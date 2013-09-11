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

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 3:59:10 PM
 */
public class ExtendedUtil extends Util {
	public static final String	SHORT_OPTION_PREFIX="-", LONG_OPTION_PREFIX="--";

	public static final ExtendedTransformer<String,String> PURE_OPTION_EXTRACTOR=
			new AbstractExtendedTransformer<String,String>(String.class,String.class) {
				@Override
				public String transform (String input) {
					return asPureOption(input);
				}
			};
	public static final String asPureOption(String name) {
		return stripLeadingHyphens(name);
	}

	public static final ExtendedTransformer<String,String> SHORT_OPTION_CREATOR=
			new AbstractExtendedTransformer<String,String>(String.class,String.class) {
				@Override
				public String transform (String input) {
					return asShortOption(input);
				}
			};
	public static final String asShortOption(String name) {
		if (StringUtils.isEmpty(name)) {
			return name;
		} else {
			return SHORT_OPTION_PREFIX + name;
		}
	}
	
	public static final ExtendedTransformer<String,String> LONG_OPTION_CREATOR=
			new AbstractExtendedTransformer<String,String>(String.class,String.class) {
				@Override
				public String transform (String input) {
					return asLongOption(input);
				}
			};
	public static final String asLongOption(String name) {
		return LONG_OPTION_PREFIX + name;
	}

}
