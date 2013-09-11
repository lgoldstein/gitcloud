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

package org.springframework.util;

import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

/**
 * @author Lyor G.
 */
public interface ExtendedPlaceholderResolver extends PlaceholderResolver {
	/**
	 * Resolves the supplied placeholder name into the replacement value.
	 * @param placeholderName the name of the placeholder to resolve.
	 * @param defaultValue the default value to return if no replacement available
	 * @return the replacement value or default value if no replacement is to be made.
	 */
	String resolvePlaceholder(String placeholderName, String defaultValue);
}
