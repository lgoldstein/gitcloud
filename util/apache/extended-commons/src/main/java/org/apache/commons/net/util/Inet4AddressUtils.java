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

package org.apache.commons.net.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.Inet4Address;
import java.net.InetAddress;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedCharSequenceUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.ExtendedNumberUtils;

public class Inet4AddressUtils {
    /**
     * String used to refer to the local host &quot;name&quot;
     */
    public static final String  LOCALHOST_NAME="localhost";

    /**
     * The most widely used loopback address (although <U>any</U> <code>127.x.x.x</code>
     * is a loopback)
     */
    public static final String  LOOPBACK_ADDRESS="127.0.0.1";

    /**
     * The default loopback IP address as a {@code long} value
     */
    public static final Long    LOOPBACK_IP=Long.valueOf(toLong(LOOPBACK_ADDRESS));

    /**
     * Number of components in a standard IPv4 address string and/or byte array
     */
    public static final int ADDRESS_LENGTH=4;

    /**
     * Number of bits for representing an IPv4 address
     */
    public static final int ADDRESS_NUM_BITS=ADDRESS_LENGTH * Byte.SIZE;

    /**
     * Automatic mask applied to all <code>long</long> values
     * to ensure only low 32 bits are used 
     */
    public static final long    IPv4ADRESS_VALUE_MASK=0x00FFFFFFFFL;

    /**
     * Default separator used between address components
     */
    public static final char ADDRESS_SEP_CHAR='.';

    /**
     * A {@link Predicate} that returns {@code true} if its evaluated
     * argument is an {@link Inet4Address}.
     */
    public static final Predicate<Object>  INET4_ADDRESS_CLASSIFIER=new Predicate<Object>() {
            @Override
            public boolean evaluate(Object object) {
                return Inet4Address.class.isInstance(object);
            }
        };

    /**
     * @param address The (IPv4) {@link InetAddress} to convert to {@code long} value
     * @return The 32-bits of the IPv4 address in the lower half of the
     * {@code long} value
     * @throws NullPointerException if no address instance
     * @throws IllegalArgumentException if the address is not an {@link Inet4Address}
     * @see #validateInet4Address(Object)
     */
    public static final long toLong(InetAddress address) {
        return toLong(validateInet4Address(address).getAddress());
    }

    public static final long toLong (byte ... av) {
        return toLong(av, 0, ArrayUtils.getLength(av));
    }

    public static final long toLong(byte[] av, int off, int len) {
        Validate.isTrue(len >= ADDRESS_LENGTH, "Incomplete address bytes: %s", len);

        long    longVal=0L;
        for (int    aIndex=0, curOffset=off; aIndex < ADDRESS_LENGTH; aIndex++, curOffset++) {
            int   cVal=av[curOffset] & 0x00FF;
            longVal <<= Byte.SIZE;
            longVal |= cVal;
        }

        return longVal & IPv4ADRESS_VALUE_MASK;
    }

    /**
     * @param sv string in "a.b.c.d" notation
     * @return 32-bit integer value represented by the notation 
     * @throws NumberFormatException bad/illegal format in string
     */
    public static final long toLong (String sv) throws NumberFormatException {
        ExtendedValidate.notEmpty(sv, "No input string");

        int         numElements=0;
        long        longVal=0L;
        for (int curPos=0, svLen=sv.length(); curPos < svLen; ) {
            int       nextPos=sv.indexOf(ADDRESS_SEP_CHAR, curPos);
            boolean   done=((nextPos <= curPos) || (nextPos >= svLen));
            String    dotStr=done ? sv.substring(curPos) : sv.substring(curPos, nextPos);
            int       dotVal=Integer.parseInt(dotStr);
            if ((dotVal < 0) || (dotVal > 255)) {
                throw new NumberFormatException("Address component (" + dotStr + ") not in range for address=" + sv);
            }

            longVal <<= Byte.SIZE;
            longVal |= dotVal & 0x00FF;
            numElements++;

            if (done) {
                break;
            }

            if ((curPos=nextPos + 1) >= svLen) {
                throw new NumberFormatException("Bad/Illegal dot position after component=" + dotStr + " in address=" + sv);
            }
        }

        if (numElements != ADDRESS_LENGTH) {
            throw new NumberFormatException("Bad/Illegal IPv4 string format: " + sv);
        }

        return longVal & IPv4ADRESS_VALUE_MASK;
    }

    // NOTE: must have at least IPv4_ADDRESS_LENGTH available length
    public static final byte[] fromLongValue (long aVal) {
        return fromLongValue(aVal, new byte[ADDRESS_LENGTH]);
    }

    // NOTE: must have at least ADDRESS_LENGTH available length  
    public static final byte[] fromLongValue (long aVal, byte ... av) {
        return fromLongValue(aVal, av, 0, ArrayUtils.getLength(av));
    }

    // NOTE: must have at least ADDRESS_LENGTH available length  
    public static final byte[] fromLongValue (long aVal, byte[] av, int off, int len) {
        Validate.isTrue(len >= ADDRESS_LENGTH, "Incomplete address bytes: %s", len);

        long    curVal=aVal;
        for (int  aIndex=off + ADDRESS_LENGTH; aIndex > off; aIndex--, curVal >>= Byte.SIZE) {
            av[aIndex - 1] = (byte) (curVal & 0x00FF);
        }

        return av;
    }

    /**
     * @param a array to be converted to dot notation. <B>Note:</B> if length
     * greater than {@link #ADDRESS_LENGTH} then only the first bytes
     * are converted
     * @return string representation
     */
    public static final String toString (byte ... a) {
        return toString(a, 0, ArrayUtils.getLength(a));
    }

    /**
     * @param a array to be converted to dot notation.
     * @param offset offset in array to start conversion
     * @param len number of bytes available for conversion. <B>Note:</B> if
     * greater than {@link #ADDRESS_LENGTH}, then only the first bytes
     * are converted
     * @return string representation
     */
    public static final String toString (byte[] a, int offset, int len) {
        try {
            return appendAddress(new StringBuilder(ADDRESS_LENGTH * 4 + 2), a, offset, len).toString();
        } catch(IOException e)  {      // can happen if bad arguments
            throw new RuntimeException(e);
        }
    }

    public static final <A extends Appendable> A appendAddress (A sb, long a) throws IOException {
        if (sb == null) {
            throw new EOFException("No " + Appendable.class.getSimpleName() + " instance provided");
        }

        for (int    aIndex=0, sSize=24; aIndex < ADDRESS_LENGTH; aIndex++, sSize -= Byte.SIZE) {
            long  v=(a >> sSize) & 0x00FF;
            if (aIndex > 0) {
                sb.append(ADDRESS_SEP_CHAR);
            }
            sb.append(String.valueOf(v));
        }

        return sb;
    }

    public static final <A extends Appendable> A appendAddress (A sb, byte ... a) throws IOException {
        return appendAddress(sb, a, 0, ArrayUtils.getLength(a));
    }

    public static final <A extends Appendable> A appendAddress (A sb, byte[] a, int offset, int len) throws IOException {
        if (sb == null) {
            throw new EOFException("No " + Appendable.class.getSimpleName() + " instance provided");
        }

        int   aLen=ArrayUtils.getLength(a), maxOffset=offset + ADDRESS_LENGTH;
        if ((aLen < ADDRESS_LENGTH)
         || (offset < 0)
         || (len < ADDRESS_LENGTH)
         || (maxOffset > aLen)
         || ((offset + len) > aLen)) {
            throw new StreamCorruptedException("appendAddress() bad/illegal data");
        }

        for (int    curOffset=offset; curOffset < maxOffset; curOffset++) {
            sb.append(String.valueOf(a[curOffset] & 0x00FF));
            if (curOffset < (maxOffset - 1)) {
                sb.append(ADDRESS_SEP_CHAR);
            }
        }

        return sb;
    }

    public static final byte[] fromString (String sv) throws NumberFormatException {
        byte[]  addr=new byte[ADDRESS_LENGTH];
        fromString(sv, addr);
        return addr;
    }

    /**
     * Initializes contents of address bytes array with the values from the
     * parsed string.
     * @param sv string to be converted - may NOT be null/empty, and MUST be a
     * valid dot notation of an IP address
     * @param av bytes array into which to place the result - may NOT be null,
     * and if more than {@link #ADDRESS_LENGTH} bytes available, then
     * only the first ones are used
     * @return number of bytes used (should be <U>exactly</U> IPv4_ADDRESS_LENGTH)
     * @throws NumberFormatException bad/illegal format in string
     */
    public static final int fromString (String sv, byte[] av) throws NumberFormatException {
        return fromString(sv, av, 0, ArrayUtils.getLength(av));
    }

    /**
     * Initializes contents of address bytes array with the values from the
     * parsed string.
     * @param sv string to be converted - may NOT be null/empty, and MUST be a
     * valid dot notation of an IP address
     * @param av bytes array into which to place the result - may NOT be null
     * @param offset offset in bytes array to place the result
     * @param len number of available bytes - if more than {@link #ADDRESS_LENGTH}
     * bytes available, then only the first ones are used
     * @return number of bytes used (should be <U>exactly</U> IPv4_ADDRESS_LENGTH)
     * @throws NumberFormatException bad/illegal format in string
     */
    public static final int fromString (String sv, byte[] av, int offset, int len) throws NumberFormatException
    {
        int   svLen=ExtendedCharSequenceUtils.getSafeLength(sv), avLen=ArrayUtils.getLength(av);
        if ((svLen <= 0) || (len < ADDRESS_LENGTH) || (offset < 0) || ((offset + len) > avLen)) {
            throw new NumberFormatException("Bad/Illegal string/address bytes array specification");
        }

        int curLen=0, curPos=0;
        for (int    curOffset=offset; curLen < len; curOffset++) {
            int       nextPos=sv.indexOf(ADDRESS_SEP_CHAR, curPos);
            boolean   done=((nextPos <= curPos) || (nextPos >= svLen));
            String    dotStr=done ? sv.substring(curPos) : sv.substring(curPos, nextPos);
            char      ch0=(dotStr.length() <= 0) ? '?' : dotStr.charAt(0);
            if ((ch0 < '0') || (ch0 > '9')) {
                throw new NumberFormatException("Address component (" + dotStr + ") does not start with digit in address=" + sv);
            }
                
            short     dotVal=Short.parseShort(dotStr);
            if ((dotVal < 0) || (dotVal > 255)) {
                throw new NumberFormatException("Address component (" + dotStr + ") not in range for address=" + sv);
            }

            av[curOffset] = (byte) dotVal;
            curLen++;

            if (done) {
                curPos = svLen;
                break;
            }

            if ((curPos=nextPos + 1) >= svLen) {
                throw new NumberFormatException("Bad/Illegal dot position after component=" + dotStr + " in address=" + sv);
            }
        }

        // make sure exhausted ALL dotted values
        if (curPos < svLen) {
            throw new NumberFormatException("Not all input string exhausted for addr=" + sv);
        }

        if (curLen != ADDRESS_LENGTH) {
            throw new NumberFormatException("Bad/incomplete address (len=" + curLen + "): " + sv);
        }

        return curLen;
    }

    /**
     * @param addr The {@link InetAddress} to be verified
     * @return <P><code>true</code> if the address is:</P></BR>
     * <UL>
     *      <LI>Not <code>null</code></LI>
     *      <LI>An {@link Inet4Address}</LI> 
     *      <LI>Not link local</LI>
     *      <LI>Not a multicast</LI>
     *      <LI>Not a loopback</LI>
     * </UL>
     * @see InetAddress#isLinkLocalAddress()
     * @see InetAddress#isMulticastAddress()
     * @see #isLoopbackAddress(InetAddress)
     */
    public static boolean isValidHostAddress (InetAddress addr) {
        if (addr == null) {
            return false;
        }
        
        if (!Inet4Address.class.isInstance(addr)) {
            return false;
        }

        if (isLoopbackAddress(addr)) {
            return false;
        }

        if (addr.isLinkLocalAddress()) {
            return false;
        }

        if (addr.isMulticastAddress()) {
            return false;
        }

        return true;
    }

    /**
     * @param addr The {@link InetAddress} to be considered
     * @return <code>true</code> if the address is an IPv4 loopback one.
     * <B>Note:</B> if {@link InetAddress#isLoopbackAddress()}
     * returns <code>false</code> the address <U>string</U> is checked
     * @see InetAddress#isLoopbackAddress()
     * @see #toAddressString(InetAddress)
     * @see #isLoopbackAddress(CharSequence)
     */
    public static boolean isLoopbackAddress (InetAddress addr) {
        if (addr == null) {
            return false;
        }

        if (!Inet4Address.class.isInstance(addr)) {
            return false;
        }

        if (addr.isLoopbackAddress()) {
            return true;
        }

        String  ip=toAddressString(addr);
        return isLoopbackAddress(ip);
    }

    /**
     * @param addr IP value to be tested
     * @return <code>true</code> if the IP is &quot;localhost&quot; or
     * &quot;127.x.x.x&quot; (see <A HREF="http://en.wikipedia.org/wiki/Loopback">WikiPedia</A>).
     */
    public static final boolean isLoopbackAddress (CharSequence addr) {
        if (StringUtils.isEmpty(addr)) {
            return false;
        }

        if (StringUtils.equals(LOCALHOST_NAME, addr)) {
            return true;
        }

        return isLoopbackAddress(addr, 0, ExtendedCharSequenceUtils.getSafeLength(addr));
    }

    public static final boolean isLoopbackAddress (CharSequence addr, int offset, int len) {
        if (!isIPv4Address(addr, offset, len)) {
            return false;   // debug breakpoint
        }

        for (int    cIndex=offset + 1; cIndex < len; cIndex++) {
            // find 1st component
            if (addr.charAt(cIndex) != ADDRESS_SEP_CHAR)
                continue;

            CharSequence  vSeq=addr.subSequence(offset, cIndex);
            int           vVal=Integer.parseInt(vSeq.toString());
            if (vVal == 127) {
                return true;    // debug breakpoint
            }

            break;
        }

        return false;   // debug breakpoint
    }
    // see http://en.wikipedia.org/wiki/Loopback - any 127.x.x.x is a loopback
    public static final boolean isLoopbackAddress (long addr) {
        if (((addr >> 24) & 0x00FF) != 127) {
            return false;   // debug breakpoint
        } else {
            return true;
        }
    }

    // see http://en.wikipedia.org/wiki/Loopback - any 127.x.x.x is a loopback
    public static final boolean isLoopbackAddress (byte[] addr, int offset, int len) {
        if ((null == addr) || (len != ADDRESS_LENGTH) || (offset < 0) || ((offset+len) > ArrayUtils.getLength(addr))) {
            return false;   // debug breakpoint
        }

        if (addr[offset] != 127) {
            return false;   // debug breakpoint
        }

        return true;
    }

    public static final boolean isLoopbackAddress (byte... addr) {
        return isLoopbackAddress(addr, 0, ArrayUtils.getLength(addr));
    }
    
    /**
     * @param cs {@link CharSequence} to be checked if IPv4 address
     * @return TRUE if (entire) {@link CharSequence} represents an IPv4 address
     * @see #isIPv4Address(CharSequence, int, int)
     */
    public static final boolean isIPv4Address (CharSequence cs) {
        return StringUtils.isEmpty(cs) ? false : isIPv4Address(cs, 0, cs.length());
    }

    /**
     * @param cs {@link CharSequence} to be checked if IPv4 address
     * @param startPos start position (inclusive) to check
     * @param len number of characters to check
     * @return TRUE if {@link CharSequence} represents an IPv4 address (within
     * the specified range)
     */
    public static final boolean isIPv4Address (CharSequence cs, int startPos, int len) {
        int   maxPos=startPos + len;
        if ((null == cs) || (startPos < 0)
         || (len < 7) /* min. address is "0.0.0.0" */ || (maxPos > cs.length())) {
            return false;
        }

        int dotCount=0, lastDotPos=(-1);
        for (int    curPos=startPos; curPos < maxPos; curPos++) {
            char  c=cs.charAt(curPos);
            if (ADDRESS_SEP_CHAR == c) {
                dotCount++;
                // cannot have more than 3 dots
                if (dotCount > 3) {
                    return false;
                }

                final int   cLen;
                if (lastDotPos >= startPos) {
                    cLen = curPos - lastDotPos;
                } else {    // first time we encounter dot
                    cLen = 1 + (curPos - startPos);
                }

                // at least one digit and no more than 3
                if ((cLen <= 1) || (cLen > 4)) {
                    return false;
                }

                // make sure the value between 2 successive dots is a valid IP component
                CharSequence  ns=cs.subSequence((curPos - cLen) + 1, curPos);
                if (!ExtendedNumberUtils.isIntegerNumber(ns)) {
                    return false;
                }

                int  nVal=Integer.parseInt(ns.toString());
                if ((nVal < 0) || (nVal > 255)) {
                    return false;
                }

                lastDotPos = curPos;
            } else if ((c < '0') || (c > '9')) {
                return false;
            }
        }

        return true;
    }

    public static String toAddressString (InetAddress addr) {
        String ip=(addr == null) ? null : addr.toString();
        if (StringUtils.isEmpty(ip)) {
            return null;
        }
        
        if (Inet4Address.class.isInstance(addr)) {
            return ip.replaceAll(".*/", "");
        } else {
            return null;
        }
    }
    
    /**
     * @param address The {@link Object} to be validated
     * @return The object cast as an {@link Inet4Address} if validation successful
     * @throws NullPointerException if no address instance
     * @throws IllegalArgumentException if the address is not an {@link Inet4Address} 
     */
    public static final Inet4Address validateInet4Address(Object address) {
        return validateInet4Address(address, "isInet4Address");
    }

    /**
     * @param address The {@link Object} to be validated
     * @param message The message to be issued if a validation fails
     * @return The object cast as an {@link Inet4Address} if validation successful
     * @throws NullPointerException if no address instance
     * @throws IllegalArgumentException if the address is not an {@link Inet4Address} 
     */
    public static final Inet4Address validateInet4Address(Object address, String message) {
        ExtendedValidate.notNull(address, message);
        ExtendedValidate.isInstanceOf(Inet4Address.class, address, message);
        return Inet4Address.class.cast(address);
    }

    /**
     * @param address The {@link Object} to be validated
     * @param message The message format to be issued if a validation fails
     * @param args The formatting arguments of the exception message(s)
     * @return The object cast as an {@link Inet4Address} if validation successful
     * @throws NullPointerException if no address instance
     * @throws IllegalArgumentException if the address is not an {@link Inet4Address} 
     */
    public static final Inet4Address validateInet4Address(Object address, String message, Object ... args) {
        Validate.notNull(address, message, args);
        Validate.isInstanceOf(Inet4Address.class, address, message, args);
        return Inet4Address.class.cast(address);
    }
}
