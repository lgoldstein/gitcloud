/* Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.community.chest.gitcloud;

import java.util.Comparator;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.lang3.ExtendedStringUtils;

/**
 * Represents objects that provide a type information system
 * @author Lyor Goldstein
 * @since Sep 11, 2013 1:40:17 PM
 */
public interface TypeInfoCarrier {
    static final String TYPE_PROPERTY="type";
    static final int    MAX_TYPE_NAME_LENGTH=255;
   
    /**
     * @return A type identifier - <B>Note:</B> although the returned value
     * may have a specific case, 2-types are considered equal if they have
     * the same case <U>insensitive</U> value
     * @see #BY_TYPE_COMPARATOR
     */
    @NotNull
    @Size(min=1,max=MAX_TYPE_NAME_LENGTH)
    String getType();

    /**
     * Returns the {@link TypeInfoCarrier#getType()} value
     */
    static final ExtendedTransformer<TypeInfoCarrier,String>    TYPE_EXTRACTOR=
            new AbstractExtendedTransformer<TypeInfoCarrier, String>(TypeInfoCarrier.class,String.class) {
                @Override
                public String transform(TypeInfoCarrier input) {
                    if (input == null) {
                        return null;
                    } else {
                        return input.getType();
                    }
                }
            };
            
    /**
     * Compares 2 {@link TypeInfoCarrier#getType()} values case <U>insensitive</U>
     */
    static final Comparator<TypeInfoCarrier>    BY_TYPE_COMPARATOR=
            new Comparator<TypeInfoCarrier>() {
                @Override
                public int compare(TypeInfoCarrier o1, TypeInfoCarrier o2) {
                    if (o1 == o2) {
                        return 0;
                    }
                    
                    String  t1=TYPE_EXTRACTOR.transform(o1);
                    String  t2=TYPE_EXTRACTOR.transform(o2);
                    return ExtendedStringUtils.safeCompare(t1, t2, false);
                }
        };
}
