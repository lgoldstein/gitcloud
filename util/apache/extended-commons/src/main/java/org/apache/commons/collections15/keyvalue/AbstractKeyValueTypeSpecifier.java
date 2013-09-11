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

import org.apache.commons.lang3.ExtendedValidate;

/**
 * @param <K> Key type 
 * @param <V> Value type
 * @author Lyor G.
 * @since Jun 4, 2013 11:40:46 AM
 */
public abstract class AbstractKeyValueTypeSpecifier<K,V> implements KeyValueTypeSpecifier<K,V> {
	private final Class<K>	keyClass;
	private final Class<V>	valClass;
	
	protected AbstractKeyValueTypeSpecifier(Class<K> keyType, Class<V> valType) {
		keyClass = ExtendedValidate.notNull(keyType, "No key type specified");
		valClass = ExtendedValidate.notNull(valType, "No value type specified");
	}

	@Override
	public final Class<K> getKeyType () {
		return keyClass;
	}

	@Override
	public final Class<V> getValueType () {
		return valClass;
	}

}
