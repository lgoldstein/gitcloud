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

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.math.BigInteger;

import org.apache.commons.lang3.ExtendedArrayUtils;

/**
 * A bare-minimum ASN.1 DER decoder, just having enough functions to 
 * decode PKCS#1 private keys. Especially, it doesn't handle explicitly
 * tagged types with an outer tag.
 * 
 * <p/>This parser can only handle one layer. To parse nested constructs,
 * get a new parser for each layer using <code>Asn1Object.getParser()</code>.
 * 
 * <p/>There are many DER decoders in JRE but using them will tie this
 * program to a specific JCE/JVM.
 * 
 * Based on code from <A HREF="http://oauth.googlecode.com/svn-history/r1178/code/branches/jmeter/jmeter/src/main/java/org/apache/jmeter/protocol/oauth/sampler/PrivateKeyReader.java">here</A>
 * @author Lyor G.
 * @since Jul 3, 2013 3:43:00 PM
 */
public class DERParser extends FilterInputStream {
    public DERParser(byte ... bytes) {
        this(bytes, 0, ExtendedArrayUtils.length(bytes));
    }
    
    public DERParser(byte[] bytes, int offset, int len) {
        this(new ByteArrayInputStream(bytes, offset, len));
    }

    public DERParser(InputStream s) {
        super(s);
    }

    public ASN1Object readObject() throws IOException {
        int tag=read();
        if (tag == -1) {
            return null;
        }

        int     length=readLength();
        byte[]  value=new byte[length];
        int     n=read(value);
        if (n < length) {
            throw new StreamCorruptedException("Invalid DER: stream too short, missing value: read " + n + " out of required " + length);
        }

        return new ASN1Object((byte) tag, length, value);
    }

    /**
     * Decode the length of the field. Can only support length
     * encoding up to 4 octets.
     * 
     * <p/>In BER/DER encoding, length can be encoded in 2 forms,
     * <ul>
     * <li>Short form. One octet. Bit 8 has value "0" and bits 7-1
     * give the length.
     * <li>Long form. Two to 127 octets (only 4 is supported here). 
     * Bit 8 of first octet has value "1" and bits 7-1 give the 
     * number of additional length octets. Second and following 
     * octets give the length, base 256, most significant digit first.
     * </ul>
     * @return The length as integer
     * @throws IOException
     */
    private int readLength() throws IOException {
        int i = read();
        if (i == -1) {
            throw new StreamCorruptedException("Invalid DER: length missing");
        }

        // A single byte short length
        if ((i & ~0x7F) == 0) {
            return i;
        }
        
        int num = i & 0x7F;
        // TODO We can't handle length longer than 4 bytes
        if ( i >= 0xFF || num > 4) { 
            throw new StreamCorruptedException("Invalid DER: length field too big: " + i);
        }
        
        byte[]  bytes=new byte[num];           
        int     n=read(bytes);
        if (n < num) {
            throw new StreamCorruptedException("Invalid DER: length too short: " + n);
        }

        // TODO try to avoid using a BigInteger
        return new BigInteger(1, bytes).intValue();
    }
}
