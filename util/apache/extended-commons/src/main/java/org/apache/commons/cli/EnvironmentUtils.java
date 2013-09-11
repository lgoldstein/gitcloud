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

package org.apache.commons.cli;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections15.keyvalue.AbstractExtendedEnumerableKeyedAccessor;
import org.apache.commons.collections15.keyvalue.AbstractExtendedEnumerableKeyedReader;
import org.apache.commons.collections15.keyvalue.ExtendedEnumerableKeyedAccessor;
import org.apache.commons.collections15.keyvalue.ExtendedEnumerableKeyedReader;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 11:26:50 AM
 */
public class EnvironmentUtils {
    /**
     * A {@link ExtendedEnumerableKeyedAccessor} that accesses the system properties
     */
    public static final ExtendedEnumerableKeyedAccessor<String, String> SYSPROPS_READER=
            new AbstractExtendedEnumerableKeyedAccessor<String, String>(String.class, String.class) {
                @Override
                public String get(String key) {
                    if (StringUtils.isEmpty(key)) {
                        return null;
                    } else {
                        return System.getProperty(key);
                    }
                }

				@Override
				public String put (String key, String value) {
					return System.setProperty(key, value);
				}

				@Override
				public Collection<String> getKeys () {
					return Collections.unmodifiableSet(System.getProperties().stringPropertyNames());
				}
            };

    /**
     * A {@link ExtendedEnumerableKeyedAccessor} that accesses the system environment
     */
    public static final ExtendedEnumerableKeyedReader<String, String> SYSENV_READER=
            new AbstractExtendedEnumerableKeyedReader<String, String>(String.class, String.class) {
    			private final Map<String,String>	env=System.getenv();

		        @Override
		        public String get(String key) {
		            if (StringUtils.isEmpty(key)) {
		                return null;
		            } else {
		                return env.get(key);
		            }
		        }
		
				@Override
				public Collection<String> getKeys () {
					return env.keySet();	// NOTE: we rely on the fact that the environment is unmodifiable
				}
		    };
}
