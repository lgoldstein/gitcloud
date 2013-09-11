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

package org.apache.commons.lang3.tuple;

import org.apache.commons.collections15.Transformer;

/**
 * @author Lyor G.
 */
public class TupleUtils {
    public TupleUtils() {
        super();
    }

    private static final Transformer<Pair<?,?>,Object>  leftValueXformer=
            new Transformer<Pair<?,?>,Object>() {
                @Override
                public Object transform(Pair<?, ?> input) {
                    if (input == null) {
                        return null;
                    } else {
                        return input.getLeft();
                    }
                }
        
        };
    /**
     * @return A {@link Transformer} that returns the {@link Pair#getLeft()} value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final <L> Transformer<Pair<L,?>,L> leftValueExtractor() {
        return (Transformer) leftValueXformer;
    }

    private static final Transformer<Pair<?,?>,Object>  rightValueXformer=
            new Transformer<Pair<?,?>,Object>() {
                @Override
                public Object transform(Pair<?, ?> input) {
                    if (input == null) {
                        return null;
                    } else {
                        return input.getRight();
                    }
                }
        
        };
    /**
     * @return A {@link Transformer} that returns the {@link Pair#getRight()} value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final <R> Transformer<Pair<?,R>,R> rightValueExtractor() {
        return (Transformer) rightValueXformer;
    }
    
    private static final Transformer<Triplet<?,?,?>,Object> v1Extractor=
            new Transformer<Triplet<?,?,?>,Object>() {
                @Override
                public Object transform (Triplet<?,?,?> input) {
                    if (input == null) {
                        return null;
                    } else {
                        return input.getV1();
                    }
                }
            };
    /**
     * @return A {@link Transformer} that returns the {@link Triplet#getV1()} value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final <V> Transformer<Triplet<V,?,?>,V> firstValueExtractor() {
        return (Transformer) v1Extractor;
    }
    
    private static final Transformer<Triplet<?,?,?>,Object> v2Extractor=
            new Transformer<Triplet<?,?,?>,Object>() {
                @Override
                public Object transform (Triplet<?,?,?> input) {
                    if (input == null) {
                        return null;
                    } else {
                        return input.getV2();
                    }
                }
            };
    /**
     * @return A {@link Transformer} that returns the {@link Triplet#getV2()} value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final <V> Transformer<Triplet<?,V,?>,V> secondValueExtractor() {
        return (Transformer) v2Extractor;
    }
    
    private static final Transformer<Triplet<?,?,?>,Object> v3Extractor=
            new Transformer<Triplet<?,?,?>,Object>() {
                @Override
                public Object transform (Triplet<?,?,?> input) {
                    if (input == null) {
                        return null;
                    } else {
                        return input.getV3();
                    }
                }
            };
    /**
     * @return A {@link Transformer} that returns the {@link Triplet#getV3()} value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final <V> Transformer<Triplet<?,?,V>,V> thirdValueExtractor() {
        return (Transformer) v3Extractor;
    }
}
