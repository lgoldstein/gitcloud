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
 * Represents an object that has a namespace property.
 * @author Lyor Goldstein
 * @since Sep 11, 2013 1:38:12 PM
 */
public interface NamespaceSeparated {
    static final String NAMESPACE_PROPERTY = "namespace";
    static final int    MAX_NAMESPACE_LENGTH = 72;

    /**
     * @return A namespace identifier used to distinguish between similar
     * objects that originate from distinct sources
     */
    @NotNull
    @Size(min=1,max=MAX_NAMESPACE_LENGTH)
    String getNamespace();
    
    /**
     * An {@link ExtendedTransformer} that returns the {@link #getNamespace()} value
     */
    static final ExtendedTransformer<NamespaceSeparated, String>   NAMESPACE_EXTRACTOR=
            new AbstractExtendedTransformer<NamespaceSeparated, String>(NamespaceSeparated.class, String.class) {
                @Override
                public String transform(NamespaceSeparated n) {
                    if (n == null) {
                        return null;
                    } else {
                        return n.getNamespace();
                    }
                }
            };

    /**
     * A {@link Comparator} that compares {@link #getNamespace()} values case <U>sensitive</U>
     * @see #NAMESPACE_EXTRACTOR
     */
    static final Comparator<NamespaceSeparated> BY_NAMESPACE_COMPARATOR=new Comparator<NamespaceSeparated>() {
            @Override
            public int compare(NamespaceSeparated o1, NamespaceSeparated o2) {
                if (o1 == o2) {
                    return 0;
                }
                String  n1=NAMESPACE_EXTRACTOR.transform(o1);
                String  n2=NAMESPACE_EXTRACTOR.transform(o2);
                return ExtendedStringUtils.safeCompare(n1, n2);
            }
        };
}
