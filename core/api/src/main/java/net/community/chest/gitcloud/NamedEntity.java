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
 * Represents entities that have a name
 * @author Lyor Goldstein
 * @since Sep 11, 2013 1:36:04 PM
 */
public interface NamedEntity {
    public static final String  NAME_PROPERTY="name";
    public static final int MAX_NAME_LENGTH=80;

    @NotNull
    @Size(min=1,max=MAX_NAME_LENGTH)
    String getName();
    
    static final ExtendedTransformer<NamedEntity,String> NAME_EXTRACTOR=
            new AbstractExtendedTransformer<NamedEntity, String>(NamedEntity.class, String.class) {
                @Override
                public String transform(NamedEntity input) {
                    if (input == null) {
                        return null;
                    } else {
                        return input.getName();
                    }
                }
            };

    /**
     * Compares 2 {@link NamedEntity} instances according to their {@link #getName()} value(s)
     */
    static final Comparator<NamedEntity> BY_NAME_COMPARATOR = new Comparator<NamedEntity>() {
            @Override
            public int compare(NamedEntity o1, NamedEntity o2) {
                if (o1 == o2) {
                    return 0;
                }
    
                String n1=NAME_EXTRACTOR.transform(o1);
                String n2=NAME_EXTRACTOR.transform(o2);
                return ExtendedStringUtils.safeCompare(n1, n2);
            }
        };
}
