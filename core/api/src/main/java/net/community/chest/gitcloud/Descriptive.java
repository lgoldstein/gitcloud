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
 * Marker interface for entities that have a description
 * @author Lyor Goldstein
 * @since Sep 11, 2013 1:33:32 PM
 */
public interface Descriptive {
    static final int    MAX_DESCRIPTION_LENGTH=255;
    static final String    DESCRIPTION_PROPERTY="description";

    @NotNull
    @Size(min=1,max=MAX_DESCRIPTION_LENGTH)
    String getDescription ();
    
    /**
     * An {@link ExtendedTransformer} that returns the {@link #getDescription()} value
     */
    static final ExtendedTransformer<Descriptive,String>    DESCRIPTION_EXTRACTOR=
            new AbstractExtendedTransformer<Descriptive, String>(Descriptive.class, String.class) {
                @Override
                public String transform(Descriptive input) {
                    if (input == null) {
                        return null;
                    } else {
                        return input.getDescription();
                    }
                }
            };
    /**
     * Compares 2 {@link Descriptive} instances according to their {@link #getDescription()} value(s)
     */
    static final Comparator<Descriptive> BY_DESCRIPTION_COMPARATOR=new Comparator<Descriptive>() {
            @Override
            public int compare(Descriptive o1, Descriptive o2) {
                if (o1 == o2) {
                    return 0;
                }
    
                String n1=DESCRIPTION_EXTRACTOR.transform(o1);
                String n2=DESCRIPTION_EXTRACTOR.transform(o2);
                return ExtendedStringUtils.safeCompare(n1, n2);
            }
        };
}
