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

package org.apache.commons.lang3.tuple;

import org.apache.commons.lang3.ObjectUtils;


/**
 * @param <V1> Type of 1st value
 * @param <V2> Type of 2nd value
 * @param <V3> Type of 3rd value
 * @author Lyor G.
 */
public class Triplet<V1,V2,V3> {
    private V1  v1;
    private V2  v2;
    private V3  v3;

    public Triplet() {
        super();
    }

    public Triplet(V1 value1, V2 value2, V3 value3) {
        this.v1 = value1;
        this.v2 = value2;
        this.v3 = value3;
    }

    public V1 getV1() {
        return v1;
    }

    public void setV1(V1 v) {
        this.v1 = v;
    }

    public V2 getV2() {
        return v2;
    }

    public void setV2(V2 v) {
        this.v2 = v;
    }

    public V3 getV3() {
        return v3;
    }

    public void setV3(V3 v) {
        this.v3 = v;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCodeMulti(getV1(), getV2(), getV3());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        Triplet<?,?,?>  other=(Triplet<?,?,?>) obj;
        if (ObjectUtils.equals(getV1(), other.getV1())
         && ObjectUtils.equals(getV2(), other.getV2())
         && ObjectUtils.equals(getV3(), other.getV3()))
            return true;
        else
            return false;   // debug breakpoint
    }

    @Override
    public String toString() {
        return "v1=" + getV1()
            + ";v2=" + getV2()
            + ";v3=" + getV3()
            ;
    }
}
