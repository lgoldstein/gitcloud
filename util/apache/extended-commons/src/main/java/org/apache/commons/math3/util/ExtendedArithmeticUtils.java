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
 */
public class ExtendedArithmeticUtils {
    /**
     * @param value The current value
     * @param factor The factoring value
     * @return The closest greater or equal to the current value that is a
     * multiple of the given factor
     */
    public static final int lowestMultipleOf(int value, int factor) {
        int remainder=value % factor;
        if (remainder == 0) {
            return value;
        } else {
            return value + (factor - remainder);
        }
    }

    /**
     * @param value The current value
     * @param factor The factoring value
     * @return The closest greater or equal to the current value that is a
     * multiple of the given factor
     */
    public static final long lowestMultipleOf(long value, long factor) {
        long remainder=value % factor;
        if (remainder == 0) {
            return value;
        } else {
            return value + (factor - remainder);
        }
    }

    /**
     * @param value The current value
     * @param factor The factoring value
     * @return The closest less or equal to the current value that is a
     * multiple of the given factor
     */
    public static final int highestMultipleOf(int value, int factor) {
        int remainder=value % factor;
        if (remainder == 0) {
            return value;
        } else {
            return value - remainder;
        }
    }

    /**
     * @param value The current value
     * @param factor The factoring value
     * @return The closest less or equal to the current value that is a
     * multiple of the given factor
     */
    public static final long highestMultipleOf(long value, long factor) {
        long remainder=value % factor;
        if (remainder == 0) {
            return value;
        } else {
            return value - remainder;
        }
    }
}
