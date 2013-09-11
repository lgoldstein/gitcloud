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
 * Marker interface for entities that have a user-friendly label
 * @author Lyor Goldstein
 * @since Sep 11, 2013 1:34:43 PM
 */
public interface Labeled {
    static final int MAX_LABEL_LENGTH = 255;
    static final String LABEL_PROPERTY = "label";

    /**
     * @return user friendly name
     */
    @NotNull
    @Size(min=1, max=MAX_LABEL_LENGTH)
    String getLabel();

    /**
     * An {@link ExtendedTransformer} that returns the {@link #getLabel()} value
     */
    static final ExtendedTransformer<Labeled,String>    LABEL_EXTRACTOR=
            new AbstractExtendedTransformer<Labeled,String>(Labeled.class, String.class) {
                @Override
                public String transform(Labeled input) {
                    if (input == null) {
                        return null;
                    } else {
                        return input.getLabel();
                    }
                }
            };

    /**
     * Compares 2 {@link Labeled} instances according to their {@link #getLabel()} value(s)
     */
    static final Comparator<Labeled> BY_LABEL_COMPARATOR = new Comparator<Labeled>() {
            @Override
            public int compare(Labeled o1, Labeled o2) {
                if (o1 == o2) {
                    return 0;
                }
    
                String n1=LABEL_EXTRACTOR.transform(o1);
                String n2=LABEL_EXTRACTOR.transform(o2);
                return ExtendedStringUtils.safeCompare(n1, n2);
            }
        };
}
