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

package org.apache.commons.net.ssh.der;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Lyor G.
 * @since Jul 3, 2013 3:30:59 PM
 */
public enum ASN1Class {
    UNIVERSAL((byte) 0x00),
    APPLICATION((byte) 0x01),
    CONTEXT((byte) 0x02),
    PRIVATE((byte) 0x03);
    
    private final byte  byteValue;
    public final byte getClassValue() {
        return byteValue;
    }

    ASN1Class(byte classValue) {
        byteValue = classValue;
    }

    public static final Set<ASN1Class>  VALUES=
            Collections.unmodifiableSet(EnumSet.allOf(ASN1Class.class));

    /**
     * <P>The first byte in DER encoding is made of following fields</P>
     * <pre>
     *-------------------------------------------------
     *|Bit 8|Bit 7|Bit 6|Bit 5|Bit 4|Bit 3|Bit 2|Bit 1|
     *-------------------------------------------------
     *|  Class    | CF  |        Type                 |
     *-------------------------------------------------
     * </pre>
     * @param value The original DER encoded byte
     * @return The {@link ASN1Class} value - <code>null</code> if no match found
     * @see #fromTypeValue(byte)
     */
    public static final ASN1Class fromDERValue(byte value) {
        return fromTypeValue((byte) ((value >> 6) & 0x03));
    }
    
    /**
     * @param value The &quot;pure&quot; value - unshifted and with no extras
     * @return The {@link ASN1Class} value - <code>null</code> if no match found
     */
    public static final ASN1Class fromTypeValue(byte value) {
        if ((value < 0) || (value > 3)) {   // only 2 bits are used
            return null;
        }
        
        for (ASN1Class c : VALUES) {
            if (c.getClassValue() == value) {
                return c;
            }
        }
        
        return null;
    }
}
