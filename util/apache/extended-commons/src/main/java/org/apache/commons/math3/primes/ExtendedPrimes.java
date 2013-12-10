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

package org.apache.commons.math3.primes;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * @author Lyor G.
 * @since Nov 11, 2013 11:34:35 AM
 */
public class ExtendedPrimes {
    public ExtendedPrimes() {
        super();
    }

    /**
     * @param n The number to be factored
     * @return A {@link SortedMap} whose key is the {@link Integer} (prime)
     * factor and value is the power of the factor
     * @see Primes#primeFactors(int)
     * @see #factorsMap(Collection)
     */
    public static final SortedMap<Integer,? extends Number> factorsMap(int n) {
        return factorsMap(Primes.primeFactors(n));
    }

    /**
     * @param factors A {@link Collection} of {@link Number}s representing
     * a number's factorization
     * @return A {@link SortedMap} whose key is the {@link Integer} (prime)
     * factor and value is the power of the factor
     */
    public static final SortedMap<Integer,? extends Number> factorsMap(Collection<? extends Number> factors) {
        if (ExtendedCollectionUtils.isEmpty(factors)) {
            return ExtendedMapUtils.emptySortedMap();
        }
       
        SortedMap<Integer,MutableInt> powersMap=new TreeMap<Integer,MutableInt>();
        for (Number f : factors) {
            Integer fac=(f instanceof Integer) ? (Integer) f : Integer.valueOf(f.intValue());
            MutableInt  count=powersMap.get(fac);
            if (count == null) {
                count = new MutableInt(1);
                powersMap.put(fac,  count);
            } else {
                count.increment();
            }
        }
        
        return powersMap;
    }
}
