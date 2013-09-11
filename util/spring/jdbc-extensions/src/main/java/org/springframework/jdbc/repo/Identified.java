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
package org.springframework.jdbc.repo;

import java.util.Comparator;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.lang3.ExtendedStringUtils;

/**
 * Represents a persisted entity with a unique identifier
 * @author Lyor Goldstein
 * @since Sep 11, 2013 10:26:30 AM
 */
public interface Identified {
    static final String  ID_PROPERTY="id";
    static final int    MAX_ID_VALUE_LENGTH=255;

    /**
     * @return A <U>unique</U> identifier
     */
    @NotNull
    @Size(min=1,max=MAX_ID_VALUE_LENGTH)
    String getId ();

    /**
     * An {@link ExtendedTransformer} that returns the {@link #getId()} value
     */
    static final ExtendedTransformer<Identified,String>    ID_EXTRACTOR=
            new AbstractExtendedTransformer<Identified, String>(Identified.class, String.class) {
                @Override
                public String transform(Identified i) {
                    if (i == null) {
                        return null;
                    } else {
                        return i.getId();
                    }
                }
            };
    
    /**
     * A {@link Comparator} that compares {@link #getId()} values case <U>sensitive</U>
     * @see #ID_EXTRACTOR
     */
    static final Comparator<Identified> BY_ID_COMPARATOR=new Comparator<Identified>() {
            @Override
            public int compare(Identified o1, Identified o2) {
                String  i1=ID_EXTRACTOR.transform(o1);
                String  i2=ID_EXTRACTOR.transform(o2);
                return ExtendedStringUtils.safeCompare(i1, i2);
            }
        };

}
