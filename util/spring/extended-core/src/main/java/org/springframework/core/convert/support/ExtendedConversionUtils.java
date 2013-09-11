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

package org.springframework.core.convert.support;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.collections15.Transformer;
import org.springframework.core.convert.converter.AbstractExtendedConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ExtendedConverter;
import org.springframework.util.Assert;

/**
 * @author Lyor G.
 */
public abstract class ExtendedConversionUtils extends ConversionUtils {

    public static final <S,T> ExtendedConverter<S, T> extend(Class<S> srcType, Class<T> dstType, final Converter<? super S,? extends T> converter) {
        Assert.notNull(converter, "No converter");
        return new AbstractExtendedConverter<S, T>(srcType, dstType) {
            @Override
            public T convert(S source) {
                return converter.convert(source);
            }
        };
    }

    public static final <S,T> Converter<S,T> toConverter(final Transformer<? super S,? extends T> xformer) {
        Assert.notNull(xformer, "No transformer");
        return new Converter<S,T>() {
            @Override
            public T convert(S source) {
                return xformer.transform(source);
            }
        };
    }

    public static final <S,T> ExtendedConverter<S,T> toConverter(final ExtendedTransformer<S,T> xformer) {
        Assert.notNull(xformer, "No transformer");
        return new AbstractExtendedConverter<S,T>(xformer.getSourceType(), xformer.getDestinationType()) {
            @Override
            public T convert(S source) {
                return xformer.transform(source);
            }
        };
    }

    public static final <S,T> Transformer<S,T> toTransformer(final Converter<? super S,? extends T> converter) {
        Assert.notNull(converter, "No converter");
        return new Transformer<S,T>() {
            @Override
            public T transform(S input) {
                return converter.convert(input);
            }
        };
    }

    public static final <S,T> ExtendedTransformer<S,T> toTransformer(final ExtendedConverter<S,T> converter) {
        Assert.notNull(converter, "No converter");
        return new AbstractExtendedTransformer<S,T>(converter.getSourceType(), converter.getTargetType()) {
            @Override
            public T transform(S input) {
                return converter.convert(input);
            }
        };
    }
}
