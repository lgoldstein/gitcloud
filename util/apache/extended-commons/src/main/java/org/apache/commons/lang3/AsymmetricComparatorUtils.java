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

package org.apache.commons.lang3;

import java.util.Comparator;

import org.apache.commons.collections15.ExtendedComparatorUtils;
import org.apache.commons.collections15.Transformer;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 3:44:49 PM
 *
 */
public class AsymmetricComparatorUtils {
	/**
	 * @param v1Xformer The {@link Transformer} to use in order to extract
	 * the value for comparison of objects of type <code>V1</code>
	 * @param v2Xformer The {@link Transformer} to use in order to extract
	 * the value for comparison of objects of type <code>V2</code>
	 * @return The matching {@link AsymmetricComparator}
	 * @throws IllegalArgumentException if missing a transformer
	 */
	public static final <V1,V2,V extends Comparable<V>> AsymmetricComparator<V1,V2> comparator(
			final Transformer<? super V1,? extends V> v1Xformer,
			final Transformer<? super V2,? extends V> v2Xformer) {
		return comparator(v1Xformer, v2Xformer, ExtendedComparatorUtils.<V>comparableComparator());
	}

	/**
	 * @param v1Xformer The {@link Transformer} to use in order to extract
	 * the value for comparison of objects of type <code>V1</code>
	 * @param v2Xformer The {@link Transformer} to use in order to extract
	 * the value for comparison of objects of type <code>V2</code>
	 * @param comp The {@link Comparator} instance to use to compare the
	 * extracted values from the objects
	 * @return The matching {@link AsymmetricComparator}
	 * @throws IllegalArgumentException if missing a transformer or the
	 * comparator
	 */
	public static final <V1,V2,V> AsymmetricComparator<V1,V2> comparator(
			final Transformer<? super V1,? extends V> v1Xformer,
			final Transformer<? super V2,? extends V> v2Xformer,
			final Comparator<? super V> comp) {
		if ((v1Xformer == null) || (v2Xformer == null) || (comp == null)) {
			throw new IllegalArgumentException("Incomplete specification");
		}
		
		return new AsymmetricComparator<V1,V2>() {
			@Override
			public int compare (V1 v1, V2 v2) {
				V	a1=v1Xformer.transform(v1), a2=v2Xformer.transform(v2);
				return comp.compare(a1, a2);
			}
			
		};
	}
}
