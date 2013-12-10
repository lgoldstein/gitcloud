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

package org.apache.commons.math3.timeseries;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.ExtendedNumberUtils;

/**
 * Represents a &quot;bucket&quot; of values containing a count of the values
 * in the bucket and their sum of values
 * @author Lyor G.
 * @since Oct 30, 2013 9:24:35 AM
 */
public class BucketValue implements Cloneable, Serializable  {
    private static final long serialVersionUID = -8999938587994096597L;

    private int numValues;
    private double sumValue;
    
    public BucketValue() {
        super();
    }

    public BucketValue(double value) {
        numValues = 1;
        sumValue = value;
    }
    
    public BucketValue add(double value) {
        sumValue += value;
        numValues++;
        return this;
    }
    
    public int getNumValues() {
        return numValues;
    }
    
    public double getSumValue() {
        return sumValue;
    }
    
    public double getAverageValue() {
        int totalValues=getNumValues();
        if (totalValues <= 1) {
            return getSumValue();
        } else {
            return getSumValue() / totalValues;
        }
    }
    
    public void reset() {
        numValues = 0;
        sumValue = 0.0d;
    }

    @Override
    public int hashCode() {
        return getNumValues() + ExtendedNumberUtils.hashCode(getSumValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        
        BucketValue    other=(BucketValue) obj;
        if ((getNumValues() == other.getNumValues())
         && (ExtendedNumberUtils.compare(getSumValue(), other.getSumValue()) == 0)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public BucketValue clone() {
        try {
            return getClass().cast(super.clone());
        } catch(CloneNotSupportedException e) { // unexpected
            throw new UnsupportedOperationException("Failed to clone " + toString() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(getNumValues()) + "/" + getSumValue() + ": " + getAverageValue();
    }
    
    /**
     * Adds a value to a buckets map at the specified index.
     * @param bucketsMap The buckets {@link Map} - key=bucket index, value=bucket data
     * @param index Bucket index-  if a bucket entry does not exist for the
     * index, then it is created and mapped. Otherwise, the new value is added
     * to the existing entry
     * @param value The value to add
     * @return The mapped/created {@link BucketValue} entry
     * @throws IllegalArgumentException if index is negative or NaN value
     */
    public static final BucketValue addToBucket(Map<Integer,BucketValue>  bucketsMap, int index, double value) {
        Validate.isTrue(index >= 0, "Invalid index value: %d", index);
        Validate.isTrue(!Double.isNaN(value), "NaN value N/A for index=%d", index);

        Integer     intervalIndex=Integer.valueOf(index);    
        BucketValue sumValue=bucketsMap.get(intervalIndex);
        if (sumValue == null) {
            sumValue = new BucketValue(value);
            bucketsMap.put(intervalIndex, sumValue);
        } else {
            sumValue.add(value);
        }
        
        return sumValue;
    }
}
