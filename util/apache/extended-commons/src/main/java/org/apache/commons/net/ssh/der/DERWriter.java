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

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * A bare-minimum ASN.1 DER enccoder, just having enough functions to 
 * encode PKCS#1 private keys
 * @author Lyor G.
 * @since Sep 22, 2013 12:06:38 PM
 */
public class DERWriter extends FilterOutputStream {
    public DERWriter(OutputStream output) {
        super(Validate.notNull(output, "No output stream", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    public void writeBigInteger(BigInteger value) throws IOException {
        byte[]  bytes=Validate.notNull(value, "No value", ArrayUtils.EMPTY_OBJECT_ARRAY).toByteArray();
        // TODO generate the tag directly and don't go through the ASN1Object
        writeObject(new ASN1Object(ASN1Class.UNIVERSAL, ASN1Type.INTEGER, false, bytes.length, bytes));
    }

    public void writeObject(ASN1Object obj) throws IOException {
        Validate.notNull(obj, "No ASN.1 object", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        ASN1Type    type=obj.getObjType();
        byte        typeValue=type.getTypeValue();
        ASN1Class   clazz=obj.getObjClass();
        byte        classValue=clazz.getClassValue();
        byte        tagValue=(byte) (((classValue << 6) & 0xC0) | (typeValue & 0x1F));
        writeObject(tagValue, obj.getLength(), obj.getValue());
    }
    
    public void writeObject(byte tag, int len, byte ... data) throws IOException {
        write(tag & 0xFF);
        writeLength(len);
        write(data, 0, len);
    }
    
    public DERWriter startSequence() {
        final ByteArrayOutputStream baos=new ByteArrayOutputStream();
        final AtomicBoolean dataWritten=new AtomicBoolean(false);
        @SuppressWarnings("resource")
        final DERWriter encloser=this;
        return new DERWriter(baos) {
            @Override
            public void close() throws IOException {
                baos.close();
                
                if (!dataWritten.getAndSet(true)) {
                    encloser.writeObject(new ASN1Object(ASN1Class.UNIVERSAL, ASN1Type.SEQUENCE, false, baos.size(), baos.toByteArray()));
                }
            }
        };
    }

    protected void writeLength(int len) throws IOException {
        Validate.isTrue(len > 0, "Invalid length: %d", len);
        // short form - MSBit is zero 
        if (len <= 127) {
            write(len);
            return;
        }
        
        // TODO see if can use something other than BigInteger 
        BigInteger  v=new BigInteger(String.valueOf(len));
        byte[]      bytes=v.toByteArray();
        int         nonZeroPos=0;
        for ( ; nonZeroPos < bytes.length; nonZeroPos++) {
            if (bytes[nonZeroPos] != 0) {
                break;
            }
        }
        
        if (nonZeroPos >= bytes.length) {
            throw new StreamCorruptedException("All zeroes length representation");
        }
        
        int bytesLen=bytes.length - nonZeroPos;
        // TODO more than 4 length bytes
        if (bytesLen > 4) {
            throw new StreamCorruptedException("Too many length bytes: " + bytesLen);
        }
        
        write(0x80 | bytesLen);
        write(bytes, nonZeroPos, bytesLen);
    }
}
