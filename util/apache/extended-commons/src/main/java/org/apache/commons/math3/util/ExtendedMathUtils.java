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

package org.apache.commons.math3.util;

/**
 * @author Lyor G.
 * @since Aug 21, 2013 11:09:44 AM
 */
public class ExtendedMathUtils {
    // returns Integer#MAX_VALUE for Integer#MIN_VALUE argument
    public static final int safeAbs(int value) {
        if (value == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE;
        } else if (value >= 0){
            return value;
        } else {
            return 0 - value;
        }
    }

    // returns Long#MAX_VALUE for Long#MIN_VALUE argument
    public static final long safeAbs(long value) {
        if (value == Long.MIN_VALUE) {
            return Long.MAX_VALUE;
        } else if (value >= 0L){
            return value;
        } else {
            return 0L - value;
        }
    }
}
