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

package org.apache.commons.collections15.numbers.longs;

/**
 * @param <V> Transformation result type
 * @author Lyor G.
 * @since Jun 25, 2013 10:01:29 AM
 */
public interface ExtendedLong2ValueTransformer<V> extends Long2ValueTransformer<V> {
    /**
     * @return The transformation result type
     */
    Class<V> getDestinationType ();
}
