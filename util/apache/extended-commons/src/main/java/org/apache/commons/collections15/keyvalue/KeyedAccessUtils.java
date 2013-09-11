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

package org.apache.commons.collections15.keyvalue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.lang3.ExtendedStringUtils;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 11:46:09 AM
 */
public class KeyedAccessUtils {
	@SuppressWarnings("rawtypes")
	private static final EnumerableKeyedReader	emptyReader=new EnumerableKeyedReader() {
			@Override
			public Collection getKeys () {
				return Collections.emptyList();
			}

			@Override
			public Object get (Object key) {
				return null;
			}
		};
	/**
	 * @return An {@link EnumerableKeyedReader} that returns an empty
	 * {@link Collection} of keys and also <code>null</code> on any
	 * invocation of the <code>get</code> method with any key
	 */
	@SuppressWarnings("unchecked")
	public static final <K,V> EnumerableKeyedReader<K,V> emptyReader() {
		return emptyReader;
	}

	public static final <K,V> EnumerableKeyedAccessor<K,V> keyedAccessor(final Map<K,V> map) {
	    return new EnumerableKeyedAccessor<K,V>() {
			@Override
			public Collection<K> getKeys () {
				return Collections.unmodifiableSet(map.keySet());
			}

            @Override
            public V get(K key) {
                if (ExtendedMapUtils.isEmpty(map)) {
                    return null;
                } else {
                    return map.get(key);
                }
            }

            @Override
            public V put(K key, V value) {
                return map.put(key, value);
            }
	    };
	}

	public static final ExtendedEnumerableKeyedAccessor<String,String> keyedAccessor(final Properties props) {
		return new AbstractExtendedEnumerableKeyedAccessor<String,String>(String.class,String.class) {
			@Override
			public Collection<String> getKeys () {
				return Collections.unmodifiableSet(props.stringPropertyNames());
			}

			@Override
			public String get (String key) {
				return props.getProperty(key);
			}

			@Override
			public String put (String key, String value) {
				return ExtendedStringUtils.safeToString(props.setProperty(key, value));
			}
		};
	}
}
