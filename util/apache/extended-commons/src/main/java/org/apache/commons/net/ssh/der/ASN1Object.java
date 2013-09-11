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

package org.apache.commons.net.ssh.der;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

/**
 * @author Lyor G.
 * @since Jul 3, 2013 3:46:39 PM
 */
public class ASN1Object implements Serializable, Cloneable {
    private static final long serialVersionUID = 4687581744706127265L;
    private ASN1Class   objClass;
    private ASN1Type    objType;
    private boolean     constructed;
    private int         length;
    private byte[]      value;
    
    // Constructed Flag
    public final static byte CONSTRUCTED = 0x20;

    public ASN1Object() {
        super();
    }

    /*
     * <P>The first byte in DER encoding is made of following fields</P>
     * <pre>
     *-------------------------------------------------
     *|Bit 8|Bit 7|Bit 6|Bit 5|Bit 4|Bit 3|Bit 2|Bit 1|
     *-------------------------------------------------
     *|  Class    | CF  |        Type                 |
     *-------------------------------------------------
     * </pre>
     */
    public ASN1Object(byte tag, int len, byte ... data) {
        this(ASN1Class.fromDERValue(tag), ASN1Type.fromDERValue(tag), (tag & CONSTRUCTED) == CONSTRUCTED, len, data);
    }
    
    public ASN1Object(ASN1Class c, ASN1Type t, boolean ctored, int len, byte ... data) {
        objClass = c;
        objType = t;
        constructed = ctored;
        length = len;
        value = data;
    }

    public ASN1Class getObjClass() {
        return objClass;
    }

    public void setObjClass(ASN1Class c) {
        objClass = c;
    }

    public ASN1Type getObjType() {
        return objType;
    }

    public void setObjType(ASN1Type y) {
        objType = y;
    }

    public boolean isConstructed() {
        return constructed;
    }

    public void setConstructed(boolean c) {
        constructed = c;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int l) {
        length = l;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] v) {
        value = v;
    }

    public DERParser createParser() {
        return new DERParser(getValue(), 0, getLength());
    }

    public Object asObject() throws IOException {
        ASN1Type    type=getObjType();
        if (type == null) {
            throw new IOException("No type set");
        }
        
        switch (type) {
            case INTEGER    :
                return asInteger();

            case NUMERIC_STRING:
            case PRINTABLE_STRING:
            case VIDEOTEX_STRING:
            case IA5_STRING:
            case GRAPHIC_STRING:
            case ISO646_STRING:
            case GENERAL_STRING:
            case BMP_STRING:
            case UTF8_STRING:
                return asString();

            case SEQUENCE   :
                return getValue();

            default:
                throw new IOException("Invalid DER: unsupported type: " + type);
        }
    }
    /**
     * Get the value as {@link BigInteger}
     * @return BigInteger
     * @throws IOException if type not an {@link ASN1Type#INTEGER}
     */
    public BigInteger asInteger() throws IOException {
        if (ASN1Type.INTEGER.equals(getObjType())) {
            return new BigInteger(getValue());
        } else {
            throw new IOException("Invalid DER: object is not integer: " + getObjType());
        }
    }
    
    /**
     * Get value as string. Most strings are treated as Latin-1.
     * @return Java string
     * @throws IOException if
     */
    public String asString() throws IOException {
        ASN1Type    type=getObjType();
        if (type == null) {
            throw new IOException("No type set");
        }

        final String encoding;
        switch (type) {
            // Not all are Latin-1 but it's the closest thing
            case NUMERIC_STRING:
            case PRINTABLE_STRING:
            case VIDEOTEX_STRING:
            case IA5_STRING:
            case GRAPHIC_STRING:
            case ISO646_STRING:
            case GENERAL_STRING:
                encoding = "ISO-8859-1";
                break;

            case BMP_STRING:
                encoding = "UTF-16BE";
                break;

            case UTF8_STRING:
                encoding = "UTF-8";
                break;

            case UNIVERSAL_STRING:
                throw new IOException("Invalid DER: can't handle UCS-4 string");

            default:
                throw new IOException("Invalid DER: object is not a string: " + type);
        }

        return new String(getValue(), 0, getLength(), encoding);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(getObjClass())
             + ObjectUtils.hashCode(getObjType())
             + ObjectUtils.hashCode(Boolean.valueOf(isConstructed()))
             + getLength()
             + ExtendedArrayUtils.hashCode(getValue(), 0, getLength())
             ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        ASN1Object  other=(ASN1Object) obj;
        if (ObjectUtils.equals(this.getObjClass(), other.getObjClass())
         && ObjectUtils.equals(this.getObjType(), other.getObjType())
         && (this.isConstructed() == other.isConstructed())
         && (this.getLength() == other.getLength())
         && (ExtendedArrayUtils.diffOffset(this.getValue(), 0, other.getValue(), 0, this.getLength()) < 0)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ASN1Object clone() {
        try {
            ASN1Object  cpy=getClass().cast(super.clone());
            byte[]      data=cpy.getValue();
            if (data != null) {
                cpy.setValue(data.clone());
            }
            return cpy;
        } catch(CloneNotSupportedException e) {
            throw new IllegalStateException("Unexpected clone failure: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        String      valObject;
        try {
            Object  o=asObject();
            if (o instanceof byte[]) {
                valObject = Arrays.toString((byte[]) o);
            } else {
                valObject = String.valueOf(o);
            }
        } catch(IOException e) {
            valObject = Arrays.toString(getValue());
        }

        return String.valueOf(getObjClass())
             + "/" + String.valueOf(getObjType())
             + "/" + isConstructed()
             + "[" + getLength() + "]"
             + ": " + valObject
             ;
    }
}
