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

/**
 * Allows comparison of 2 &quot;incompatible&quot; {@link Object}-s - e.g., based
 * on some comparable attribute of each operand
 * @param <V1> Type of 1st compared operand
 * @param <V2> Type of 2nd compared operand
 * @author Lyor G.
 * @since Oct 11, 2011 8:47:28 AM
 */
public interface AsymmetricComparator<V1,V2> {
	/**
	 * @param v1 1st compared operand
	 * @param v2 2nd compared operand
	 * @return Comparison result - negative if 1st operand is
	 * &quot;smaller&quot;, positive is &quot;greater&quot;, zero if
	 * &quot;equal&quot;
	 */
	int compare (V1 v1, V2 v2);
}
