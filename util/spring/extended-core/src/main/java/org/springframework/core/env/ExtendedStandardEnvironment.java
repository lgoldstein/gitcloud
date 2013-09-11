/*
 * Copyright 2002-2012 the original author or authors.
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
 * 
 */

package org.springframework.core.env;

import org.springframework.util.ExtendedPlaceholderResolver;
import org.springframework.util.ExtendedPlaceholderResolverUtils;
import org.springframework.util.NamedExtendedPlaceholderResolver;

/**
 * @author lgoldstein
 */
public class ExtendedStandardEnvironment extends StandardEnvironment {
	private static final class LazyEnvironmentHolder {
		private static final ExtendedStandardEnvironment	INSTANCE=new ExtendedStandardEnvironment();
		private static final ExtendedPlaceholderResolver	RESOLVER=
				ExtendedPlaceholderResolverUtils.toPlaceholderResolver(INSTANCE);
	}

	private static final class LazySyspropsHolder {
		private static final MapPropertySource	INSTANCE=
				new MapPropertySource(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, ExtendedStandardEnvironment.getStandardEnvironment().getSystemProperties());
		private static final NamedExtendedPlaceholderResolver	RESOLVER=
				ExtendedPlaceholderResolverUtils.toPlaceholderResolver(INSTANCE);
	}

	private static final class LazySysenvPropsHolder {
		private static final SystemEnvironmentPropertySource	INSTANCE=
				new SystemEnvironmentPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,  ExtendedStandardEnvironment.getStandardEnvironment().getSystemEnvironment());
		private static final NamedExtendedPlaceholderResolver	RESOLVER=
				ExtendedPlaceholderResolverUtils.toPlaceholderResolver(INSTANCE);
	}

	public ExtendedStandardEnvironment() {
		super();
	}

	@SuppressWarnings("synthetic-access")
    public static final ExtendedStandardEnvironment getStandardEnvironment() {
		return LazyEnvironmentHolder.INSTANCE;
	}
	
    @SuppressWarnings("synthetic-access")
	public static final ExtendedPlaceholderResolver getStandardResolver() {
		return LazyEnvironmentHolder.RESOLVER;
	}

    @SuppressWarnings("synthetic-access")
	public static final MapPropertySource getSystemPropertiesPropertySource() {
		return LazySyspropsHolder.INSTANCE;
	}

    @SuppressWarnings("synthetic-access")
	public static final NamedExtendedPlaceholderResolver getSystemPropertiesResolver() {
		return LazySyspropsHolder.RESOLVER;
	}

    @SuppressWarnings("synthetic-access")
	public static final SystemEnvironmentPropertySource getSystemEnvironmentPropertySource() {
		return LazySysenvPropsHolder.INSTANCE;
	}

    @SuppressWarnings("synthetic-access")
	public static final NamedExtendedPlaceholderResolver getSystemEnvironmentResolver() {
		return LazySysenvPropsHolder.RESOLVER;
	}
}
