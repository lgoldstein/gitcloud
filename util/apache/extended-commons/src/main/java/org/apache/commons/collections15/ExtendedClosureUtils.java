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

package org.apache.commons.collections15;

/**
 * @author Lyor G.
 */
public class ExtendedClosureUtils extends ClosureUtils {
    public ExtendedClosureUtils() {
        super();
    }

    public static final <T> ExtendedClosure<T> extend(Class<T> argType, final Closure<? super T> closure) {
        if (closure == null) {
            throw new IllegalArgumentException("No closure to extend");
        }
        
        return new AbstractExtendedClosure<T>(argType) {
            @Override
            public void execute(T input) {
                closure.execute(input);
            }
        };
    }
}
