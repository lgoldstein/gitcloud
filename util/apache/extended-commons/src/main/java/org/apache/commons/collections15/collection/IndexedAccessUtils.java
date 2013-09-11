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

package org.apache.commons.collections15.collection;

import java.util.List;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.keyvalue.KeyedReader;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 11:56:22 AM
 */
public class IndexedAccessUtils {
    /**
     * @param list The backing {@link List}
     * @return A {@link KeyedReader} that returns <code>null</code> if the
     * accessed index is invalid - e.g., negative or beyond the currently
     * available
     * @see #safeIndexedReader(List)
     * @see #keyedReader(IndexedReader)
     */
    public static final <V> KeyedReader<Number,V> safeKeyedReader(List<? extends V> list) {
    	return keyedReader(safeIndexedReader(list));
    }

    /**
     * @param list The backing {@link List}
     * @return A {@link KeyedReader} whose key is a {@link Number} representing
     * the index and value is the list's value at that index. <B>Note:</B>
     * the {@link Number#intValue()} is used as the index regardless of the
     * number's actual type (long, short, float, double)
     * @see #indexedReader(List)
     * @see #keyedReader(IndexedReader)
     */
    public static final <V> KeyedReader<Number,V> keyedReader(List<? extends V> list) {
    	return keyedReader(indexedReader(list));
    }

    /**
     * @param reader The {@link IndexedReader}
     * @return A {@link KeyedReader} whose key is a {@link Number} representing
     * the index and value is the {@link IndexedReader}'s value at that index.
     * <B>Note:</B> the {@link Number#intValue()} is used as the index regardless
     * of the number's actual type (long, short, float, double)
     */
    public static final <V> KeyedReader<Number,V> keyedReader(final IndexedReader<? extends V> reader) {
    	return new KeyedReader<Number,V>() {
			@Override
			public V get (Number key) {
				if (key == null) {
					return null;
				} else {
					return reader.get(key.intValue());
				}
			}
    	};
    }

    /**
     * @param list The backing {@link List}
     * @return An {@link IndexedReader} that returns <code>null</code>
     * if the accessed index is invalid - e.g., negative or beyond the
     * currently available
     */
    public static final <T> IndexedReader<T> safeIndexedReader(final List<? extends T> list) {
        return new IndexedReader<T>() {
            @Override
            public T get(int index) {
            	if ((index < 0) || (index >= ExtendedCollectionUtils.size(list))) {
            		return null;
            	} else {
            		return list.get(index);
            	}
            }
        };
    }

    public static final <T> IndexedReader<T> indexedReader(final List<? extends T> list) {
        return new IndexedReader<T>() {
            @Override
            public T get(int index) {
                return list.get(index);
            }
        };
    }

    public static <T> IndexedWriter<T> indexedWriter(final List<? super T> list) {
        return new IndexedWriter<T>() {
            @Override
            public void set(int index, T value) {
                list.set(index, value);
            }
        };
    }

    public static final <T> IndexedAccessor<T> indexedAccessor(final List<T> list) {
        return new IndexedAccessor<T>() {
            @Override
            public T get(int index) {
                return list.get(index);
            }

            @Override
            public void set(int index, T value) {
                list.set(index, value);
            }
        };
    }
}
