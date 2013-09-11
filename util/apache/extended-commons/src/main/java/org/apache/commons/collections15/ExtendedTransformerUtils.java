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

package org.apache.commons.collections15;

/**
 * @author Lyor G.
 */
public class ExtendedTransformerUtils extends TransformerUtils {
    public ExtendedTransformerUtils() {
        super();
    }
    
    /**
     * Converts a &quot;regular&quot; {@link Transformer} into an extended one
     * @param inType The input type
     * @param outType The output type
     * @param xformer The {@link Transformer} to extend
     * @return An equivalent {@link ExtendedTransformer}
     * @throws IllegalArgumentException if <code>null</code> transformer
     * @throws IllegalStateException if no in/out type(s) specified
     */
    public static final <I,O> ExtendedTransformer<I,O> extend(Class<I> inType, Class<O> outType, final Transformer<? super I,? extends O> xformer) {
        if (xformer == null) {
            throw new IllegalArgumentException("No transformer");
        }
        
        return new AbstractExtendedTransformer<I, O>(inType, outType) {
            @Override
            public O transform(I input) {
                return xformer.transform(input);
            }
        };
    }
}
