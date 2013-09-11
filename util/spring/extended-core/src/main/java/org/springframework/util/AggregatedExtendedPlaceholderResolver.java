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

package org.springframework.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

/**
 * Consults a {@link List} of {@link PlaceholderResolver} in the order in
 * which they have been provided to the aggregator.
 * @author Lyor G.
 */
public class AggregatedExtendedPlaceholderResolver extends AbstractExtendedPlaceholderResolver {
    private final List<PlaceholderResolver>   resolvers;
    public AggregatedExtendedPlaceholderResolver() {
        this(Collections.<PlaceholderResolver>emptyList());
    }

    public AggregatedExtendedPlaceholderResolver(PlaceholderResolver ... resList) {
        this(ExtendedArrayUtils.asList(resList));
    }

    public AggregatedExtendedPlaceholderResolver(Collection<? extends PlaceholderResolver> resList) {
        this.resolvers = ExtendedCollectionUtils.isEmpty(resList)
                            ? new ArrayList<PlaceholderResolver>()
                            : new ArrayList<PlaceholderResolver>(resList)
                            ;
    }
    
    public final List<PlaceholderResolver> getResolvers() {
        return resolvers;
    }

    public void addResolver(PlaceholderResolver r) {
        if (r == null) {
            throw new IllegalArgumentException("No resolver provided");
        } else {
            resolvers.add(r);
        }
    }

    public void addResolvers(PlaceholderResolver ... resList) {
        addResolvers(ExtendedArrayUtils.asList(resList));
    }

    public void addResolvers(Collection<? extends PlaceholderResolver> resList) {
        if (ExtendedCollectionUtils.isEmpty(resList)) {
            return;
        } else {
            resolvers.addAll(resList);
        }
    }

    @Override
    public String resolvePlaceholder(String placeholderName) {
        if (StringUtils.isEmpty(placeholderName)) {
            return null;
        }
        
        Collection<? extends PlaceholderResolver>  resList=getResolvers();
        if (ExtendedCollectionUtils.isEmpty(resList)) {
            return null;
        }
        
        for (PlaceholderResolver r : resList) {
            String  value=r.resolvePlaceholder(placeholderName);
            if (value != null) {
                return value;
            }
        }
        
        return null;
    }
}
