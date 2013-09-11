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

package org.apache.commons.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.ByteOrder;
import java.util.Comparator;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.math.ExtendedNumberUtils;

/**
 * @author Lyor G.
 */
public class ExtendedEndianUtils extends EndianUtils {
    public ExtendedEndianUtils() {
        super();
    }

    /**
     * @return The complement of the current byte order
     * @see ByteOrder#nativeOrder()
     * @see #complementingOrder(ByteOrder)
     */
    public static final ByteOrder complementingOrder() {
        return complementingOrder(ByteOrder.nativeOrder());
    }

    /**
     * @param order The {@link ByteOrder} to complement - ignored if <code>null</code>
     * @return The complement {@link ByteOrder} (or <code>null</code> if no
     * argument to complement)
     */
    public static final ByteOrder complementingOrder(ByteOrder order) {
        if (order == null) {
            return null;
        } else if (ByteOrder.BIG_ENDIAN.equals(order)) {
            return ByteOrder.LITTLE_ENDIAN;
        } else {
            return ByteOrder.BIG_ENDIAN;
        }
    }

    /**
     * @param value The original {@link Number} - ignored if <code>null</code>
     * @return A {@link Number} of the same type with its byte order swapped
     * @throws UnsupportedOperationException if not one of the primitive wrapper
     */
    public static final Number swapValueOrder(Number value) {
        if (value == null) {
            return null;
        } else if (value instanceof Integer) {
            return Integer.valueOf(swapInteger(value.intValue()));
        } else if (value instanceof Long) {
            return Long.valueOf(swapLong(value.longValue()));
        } else if (value instanceof Short) {
            return Short.valueOf(swapShort(value.shortValue()));
        } else if (value instanceof Float) {
            return Float.valueOf(swapFloat(value.floatValue()));
        } else if (value instanceof Double) {
            return Double.valueOf(swapDouble(value.doubleValue()));
        } else if (value instanceof Byte) {
            return value;
        } else {
            throw new UnsupportedOperationException("swapValueOrder(" + value + ") unknown type: " + value.getClass().getSimpleName());
        }
    }

    /**
     * An {@link ExtendedTransformer} that invokes the {@link #swapValueOrder(Number)} method.
     * @see #swapValueOrder(Number)
     */
    public static final ExtendedTransformer<Number,Number> VALUE_ORDER_SWAPPER=
            new AbstractExtendedTransformer<Number, Number>(Number.class, Number.class) {
                @Override
                public Number transform(Number input) {
                    if (input == null) {
                        return null;
                    } else {
                        return swapValueOrder(input);
                    }
                }
            };

    /**
     * A {@link Comparator} that returns 0 if one number is the swapped
     * value representation of the other - provided they are of same type
     * @see #VALUE_ORDER_SWAPPER
     */
    public static final Comparator<Number> SWAPPED_ORDER_COMPARATOR=
            new Comparator<Number>() {
                /*
                 * @throws IllegalStateException if cannot determine which value comes
                 * 1st on swapped values mismatch
                 * @throws UnsupportedOperationException if not one of the primitive wrapper
                 */
                @Override
                public int compare(Number o1, Number o2) {
                    if (o1 == null) {
                        if (o2 == null) {
                            return 0;
                        } else {
                            return (+1);
                        }
                    } else if (o2 == null) {
                        return (-1);
                    }

                    // if not of same class then cannot compare them
                    int nRes=ExtendedClassUtils.BY_FULL_NAME_COMPARATOR.compare(o1.getClass(), o2.getClass());
                    if (nRes != 0) {
                        return nRes;
                    }
                    
                    Number  swapped=VALUE_ORDER_SWAPPER.transform(o1);
                    if (swapped.equals(o2)) {
                        return 0;
                    }

                    if ((nRes=ExtendedNumberUtils.compareNumbers(o1, swapped)) != 0) {
                        return nRes;
                    }

                    throw new IllegalStateException("compare(" + o1 + "," + o2 + ") exhausted all options");
                }
        };
    
    public static final short readSignedInt16 (final ByteOrder inOrder, final byte[] data, final int off, final int len)
        throws NumberFormatException
    {
        if (null == inOrder)
            throw new NumberFormatException("readSignedInt16() no byte order specified");
        if (len < 2)
            throw new NumberFormatException("readSignedInt16(" + inOrder + ") insufficient data length: " + len);

        if (ByteOrder.BIG_ENDIAN.equals(inOrder))
            return (short) ((((data[off] << 8) & 0x00FF00) | (data[off + 1] & 0x00FF)) & 0x00FFFF);
        else
            return (short) ((((data[off + 1] << 8) & 0x00FF00) | (data[off] & 0x00FF)) & 0x00FFFF);
    }

    public static final short readSignedInt16 (final ByteOrder inOrder, final byte ... data)
        throws NumberFormatException
    {
        return readSignedInt16(inOrder, data, 0, ExtendedArrayUtils.length(data));
    }

    public static final short readSignedInt16 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 2)
            throw new StreamCorruptedException("readSignedInt16(" + inOrder + ") insufficient data buffer size: " + len);

        IOUtils.readFully(inStream, workBuf, off, 2);
        return readSignedInt16(inOrder, workBuf, off, 2);
    }

    public static final short readSignedInt16 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readSignedInt16(inStream, inOrder, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final short readSignedInt16 (
            final InputStream inStream, final ByteOrder inOrder) throws IOException
    {
        final byte[]    data={ 0, 0 };
        return readSignedInt16(inStream, inOrder, data);
    }

    public static final int readUnsignedInt16 (final ByteOrder inOrder, final byte[] data, final int off, final int len)
        throws NumberFormatException
    {
        if (null == inOrder)
            throw new NumberFormatException("readUnsignedInt16() no byte order specified");
        if (len < 2)
            throw new NumberFormatException("readUnsignedInt16(" + inOrder + ") insufficient data length: " + len);

        if (ByteOrder.BIG_ENDIAN.equals(inOrder))
            return ((((data[off] << 8) & 0x00FF00) | (data[off + 1] & 0x00FF)) & 0x00FFFF);
        else
            return ((((data[off + 1] << 8) & 0x00FF00) | (data[off] & 0x00FF)) & 0x00FFFF);
    }

    public static final int readUnsignedInt16 (final ByteOrder inOrder, final byte ... data)
        throws NumberFormatException
    {
        return readUnsignedInt16(inOrder, data, 0, ExtendedArrayUtils.length(data));
    }

    public static final int readUnsignedInt16 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 2)
            throw new StreamCorruptedException("readUnsignedInt16(" + inOrder + ") insufficient data buffer size: " + len);

        IOUtils.readFully(inStream, workBuf, off, 2);
        return readUnsignedInt16(inOrder, workBuf, off, 2);
    }

    public static final int readUnsignedInt16 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readUnsignedInt16(inStream, inOrder, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final int readUnsignedInt16 (
            final InputStream inStream, final ByteOrder inOrder) throws IOException
    {
        final byte[]    data={ 0, 0 };
        return readUnsignedInt16(inStream, inOrder, data);
    }

    public static final int readSignedInt32 (final ByteOrder inOrder, final byte[] data, final int off, final int len)
        throws NumberFormatException
    {
        if (null == inOrder)
            throw new NumberFormatException("readSignedInt32() no byte order specified");
        if (len < 4)
            throw new NumberFormatException("readSignedInt32(" + inOrder + ") insufficient data length: " + len);

        if (ByteOrder.BIG_ENDIAN.equals(inOrder))
            return (((data[off] << 24)   & 0x00FF000000)
                  | ((data[off+1] << 16) & 0x0000FF0000)
                  | ((data[off+2] << 8)  & 0x000000FF00) 
                  | (data[off+3]         & 0x00000000FF))
                  ;
        else
            return (((data[off+3] << 24) & 0x00FF000000)
                  | ((data[off+2] << 16) & 0x0000FF0000)
                  | ((data[off+1] << 8)  & 0x000000FF00) 
                  | (data[off]           & 0x00000000FF))
                  ;
    }

    public static final int readSignedInt32 (final ByteOrder inOrder, final byte ... data)
        throws NumberFormatException
    {
        return readSignedInt32(inOrder, data, 0, ExtendedArrayUtils.length(data));
    }

    public static final int readSignedInt32 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 4)
            throw new StreamCorruptedException("readSignedInt32(" + inOrder + ") insufficient data buffer size: " + len);

        IOUtils.readFully(inStream, workBuf, off, 4);
        return readSignedInt32(inOrder, workBuf, off, 4);
    }

    public static final int readSignedInt32 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readSignedInt32(inStream, inOrder, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final int readSignedInt32 (
            final InputStream inStream, final ByteOrder inOrder) throws IOException
    {
        final byte[]    data={ 0, 0, 0, 0 };
        return readSignedInt32(inStream, inOrder, data);
    }

    public static final float readFloat (ByteOrder inOrder, byte ... data)
        throws NumberFormatException
    {
        return readFloat(inOrder, data, 0, ExtendedArrayUtils.length(data));
    }

    public static final float readFloat (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 4)
            throw new StreamCorruptedException("readSignedInt32(" + inOrder + ") insufficient data buffer size: " + len);

        IOUtils.readFully(inStream, workBuf, off, 4);
        return readFloat(inOrder, workBuf, off, 4);
    }

    public static final float readFloat (final ByteOrder inOrder, final byte[] data, int offset, int len)
            throws NumberFormatException {
        return Float.intBitsToFloat(readSignedInt32(inOrder, data, offset, len));
    }

    public static final float readFloat (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readFloat(inStream, inOrder, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final float readFloat(InputStream inStream, ByteOrder inOrder) throws IOException {
        int floatBits=readSignedInt32(inStream, inOrder);
        return Float.intBitsToFloat(floatBits);
    }

    public static final long readUnsignedInt32 (final ByteOrder inOrder, final byte[] data, final int off, final int len)
        throws NumberFormatException
    {
        if (null == inOrder)
            throw new NumberFormatException("readUnsignedInt32() no byte order specified");
        if (len < 4)
            throw new NumberFormatException("readUnsignedInt32(" + inOrder + ") insufficient data length: " + len);

        if (ByteOrder.BIG_ENDIAN.equals(inOrder))
            return (((data[off] << 24)   & 0x00FF000000L)
                  | ((data[off+1] << 16) & 0x0000FF0000L)
                  | ((data[off+2] << 8)  & 0x000000FF00L) 
                  | (data[off+3]         & 0x00000000FFL))
                  ;
        else
            return (((data[off+3] << 24) & 0x00FF000000L)
                 | ((data[off+2] << 16)  & 0x0000FF0000L)
                 | ((data[off+1] << 8)   & 0x000000FF00L) 
                 | (data[off]            & 0x00000000FFL))
                 ;
    }

    public static final long readUnsignedInt32 (final ByteOrder inOrder, final byte ... data)
        throws NumberFormatException
    {
        return readUnsignedInt32(inOrder, data, 0, ExtendedArrayUtils.length(data));
    }

    public static final long readUnsignedInt32 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 4)
            throw new StreamCorruptedException("readUnsignedInt32(" + inOrder + ") insufficient data buffer size: " + len);

        IOUtils.readFully(inStream, workBuf, off, 4);
        return readUnsignedInt32(inOrder, workBuf, off, 4);
    }

    public static final long readUnsignedInt32 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readUnsignedInt32(inStream, inOrder, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final long readUnsignedInt32 (
            final InputStream inStream, final ByteOrder inOrder) throws IOException
    {
        final byte[]    data={ 0, 0, 0, 0 };
        return readUnsignedInt32(inStream, inOrder, data);
    }

    public static final long readSignedInt64 (final ByteOrder inOrder, final byte[] data, final int off, final int len)
        throws NumberFormatException
    {
        final int   maxIndex=off + len;
        if (null == inOrder)
            throw new NumberFormatException("readSignedInt64() no byte order specified");
        if (len < 8)
            throw new NumberFormatException("readSignedInt64(" + inOrder + ") insufficient data length: " + len);

        long    ret=0L;
        if (ByteOrder.BIG_ENDIAN.equals(inOrder))
        {
            for (int    dIndex=off, shiftSize=Long.SIZE - Byte.SIZE;
                 dIndex < maxIndex;
                 dIndex++, shiftSize -= Byte.SIZE)
            {
                long    val=data[dIndex] & 0x00FFL;
                if (shiftSize > 0)
                    val <<= shiftSize;
                ret |= val;
            }
        }
        else
        {
            for (int    dIndex=maxIndex, shiftSize=Long.SIZE - Byte.SIZE;
                 dIndex > off;
                 dIndex--, shiftSize -= Byte.SIZE)
            {
                long    val=data[dIndex-1] & 0x00FFL;
                if (shiftSize > 0)
                    val <<= shiftSize;
                ret |= val;
            }
        }

        return ret;
    }

    public static final long readSignedInt64 (final ByteOrder inOrder, final byte ... data)
        throws NumberFormatException
    {
        return readSignedInt64(inOrder, data, 0, ExtendedArrayUtils.length(data));
    }

    public static final long readSignedInt64 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 8)
            throw new StreamCorruptedException("readSignedInt64(" + inOrder + ") insufficient data buffer size: " + len);

        IOUtils.readFully(inStream, workBuf, off, 8);
        return readSignedInt64(inOrder, workBuf, off, 8);
    }

    public static final long readSignedInt64 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readSignedInt64(inStream, inOrder, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final long readSignedInt64 (final InputStream inStream, final ByteOrder inOrder) throws IOException
    {
        final byte[]    data={ 0, 0, 0, 0, 0, 0, 0, 0 };
        return readSignedInt64(inStream, inOrder, data);
    }

    public static final double readDouble (ByteOrder inOrder, byte ... data)
            throws NumberFormatException {
        return readDouble(inOrder, data, 0, ExtendedArrayUtils.length(data));
    }

    public static final double readDouble (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 4)
            throw new StreamCorruptedException("readSignedInt32(" + inOrder + ") insufficient data buffer size: " + len);

        IOUtils.readFully(inStream, workBuf, off, 4);
        return readDouble(inOrder, workBuf, off, 4);
    }

    public static final double readDouble (final ByteOrder inOrder, final byte[] data, int offset, int len)
            throws NumberFormatException {
        return Double.longBitsToDouble(readSignedInt64(inOrder, data, offset, len));
    }

    public static final double readDouble (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readDouble(inStream, inOrder, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final double readDouble(InputStream inStream, ByteOrder inOrder) throws IOException {
        return Double.longBitsToDouble(readSignedInt64(inStream, inOrder));
    }

    // returns number of used bytes
    public static final int toInt16ByteArray (
            final int val, final ByteOrder outOrder, final byte[] buf, final int off)
        throws NumberFormatException
    {
        if (null == outOrder)
            throw new NumberFormatException("toInt16ByteArray(" + val + ") no order specified");
        
        final boolean   isBigEndian=ByteOrder.BIG_ENDIAN.equals(outOrder);
        buf[off] = (byte) ((isBigEndian ?  (val >> 8) : val) & 0x00FF); 
        buf[off+1] = (byte) ((isBigEndian ?  val : (val >> 8)) & 0x00FF);
        return 2;
    }

    // returns number of used bytes
    public static final int toInt16ByteArray (
            final int val, final ByteOrder outOrder, final byte[] buf)
        throws NumberFormatException
    {
        return toInt16ByteArray(val, outOrder, buf, 0);
    }

    public static final byte[] toInt16ByteArray (
            final int val, final ByteOrder outOrder)
        throws NumberFormatException
    {
        final byte[]    data=new byte[2];
        final int       eLen=toInt16ByteArray(val, outOrder, data);
        if (eLen != data.length)
            throw new NumberFormatException("toInt16ByteArray(" + val + ")[" + outOrder + "] unexpected used length: expected=" + data.length + "/got=" + eLen);
        return data;
    }

    public static final byte[] toNativeInt16ByteArray (final int val)
    {
        return toInt16ByteArray(val, ByteOrder.nativeOrder());
    }

    // returns number of used/written bytes
    public static final int writeInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val,
            final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 2)
            throw new StreamCorruptedException("writeInt16(" + val + ")[" + outOrder + "] insufficient work buffer length: " + len);

        toInt16ByteArray(val, outOrder, workBuf, off);
        outStream.write(workBuf, off, 2);
        return 2;
    }

    // returns number of used/written bytes
    public static final int writeInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val, final byte[] workBuf)
        throws IOException
    {
        return writeInt16(outStream, outOrder, val, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final byte[] writeInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val) throws IOException
    {
        if ((null == outStream) || (null == outOrder))
            throw new IOException("writeInt16(" + outOrder + ")[" + val + "] incomplete arguments");

        final byte[]    data=toInt16ByteArray(val, outOrder);
        outStream.write(data);
        return data;
    }

    // returns number of used/written bytes
    public static final int writeUnsignedInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val,
            final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if ((val > 0x00FFFF) || (val < 0))
            throw new StreamCorruptedException("writeUnsignedInt16(" + val + ")[" + outOrder + "] value exceeds max. unsigned int16 value");
        return writeInt16(outStream, outOrder, val, workBuf, off, len);
    }

    // returns number of used/written bytes
    public static final int writeUnsignedInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val, final byte[] workBuf)
        throws IOException
    {
        return writeUnsignedInt16(outStream, outOrder, val, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final byte[] writeUnsignedInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val) throws IOException
    {
        if ((val > 0x00FFFF) || (val < 0))
            throw new StreamCorruptedException("writeUnsignedInt16(" + val + ")[" + outOrder + "] value exceeds max. unsigned int16 value");

        return writeInt16(outStream, outOrder, val & 0x00FFFF);
    }

    // returns number of used bytes
    public static final int toInt32ByteArray (
            final int val, final ByteOrder outOrder, final byte[] buf, final int off)
        throws NumberFormatException
    {
        if (null == outOrder)
            throw new NumberFormatException("toInt32ByteArray(" + val + ") no order specified");

        final boolean   isBigEndian=ByteOrder.BIG_ENDIAN.equals(outOrder);
        buf[off]     = (byte) ((isBigEndian ?  (val >> 24) : val) & 0x00FF); 
        buf[off + 1] = (byte) ((isBigEndian ?  (val >> 16) : (val >> 8)) & 0x00FF); 
        buf[off + 2] = (byte) ((isBigEndian ?  (val >> 8) : (val >> 16)) & 0x00FF); 
        buf[off + 3] = (byte) ((isBigEndian ?  val : (val >> 24)) & 0x00FF);

        return 4;
    }

    public static final int toInt32ByteArray (
            final int val, final ByteOrder outOrder, final byte[] buf)
        throws NumberFormatException
    {
        return toInt32ByteArray(val, outOrder, buf, 0);
    }

    public static final byte[] toInt32ByteArray (
            final int val, final ByteOrder outOrder)
        throws NumberFormatException
    {
        final byte[]    data=new byte[4];
        final int       eLen=toInt32ByteArray(val, outOrder, data);
        if (eLen != data.length)
            throw new NumberFormatException("toInt32ByteArray(" + val + ")[" + outOrder + "] unexpected used length: expected=" + data.length + "/got=" + eLen);
        return data;
    }

    public static final byte[] toNativeInt32ByteArray (final int val)
    {
        return toInt32ByteArray(val, ByteOrder.nativeOrder());
    }

    // returns number of used/written bytes
    public static final int writeInt32 (
            final OutputStream outStream, final ByteOrder outOrder, final int val,
            final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 4)
            throw new StreamCorruptedException("writeInt32(" + val + ")[" + outOrder + "] insufficient work buffer length: " + len);

        toInt32ByteArray(val, outOrder, workBuf, off);
        outStream.write(workBuf, off, 4);
        return 4;
    }

    // returns number of used/written bytes
    public static final int writeInt32 (
            final OutputStream outStream, final ByteOrder outOrder, final int val, final byte[] workBuf)
        throws IOException
    {
        return writeInt32(outStream, outOrder, val, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final byte[] writeInt32 (
            final OutputStream outStream, final ByteOrder outOrder, final int val) throws IOException
    {
        if ((null == outStream) || (null == outOrder))
            throw new IOException("writeInt32(" + outOrder + ")[" + val + "] incomplete arguments");

        final byte[]    data=toInt32ByteArray(val, outOrder);
        outStream.write(data);
        return data;
    }

    // returns number of used bytes
    public static final int toFloatByteArray (float val, ByteOrder outOrder, byte[] buf, int off)
        throws NumberFormatException {
        return toInt32ByteArray(Float.floatToIntBits(val), outOrder, buf, off);
    }

    public static final int toFloatByteArray (float val, ByteOrder outOrder, byte[] buf)
            throws NumberFormatException {
        return toFloatByteArray(val, outOrder, buf, 0);
    }

    public static final byte[] toFloatByteArray (float val, ByteOrder outOrder)
            throws NumberFormatException {
        return toInt32ByteArray(Float.floatToIntBits(val), outOrder);
    }

    public static final byte[] toNativeFloatByteArray (float val)
    {
        return toFloatByteArray(val, ByteOrder.nativeOrder());
    }

    // returns number of used/written bytes
    public static final int writeFloat (OutputStream outStream, ByteOrder outOrder, float val,
                                        byte[] workBuf, int off, int len)
        throws IOException
    {
        return writeInt32(outStream, outOrder, Float.floatToIntBits(val), workBuf, off, len);
    }

    // returns number of used/written bytes
    public static final int writeFloat (OutputStream outStream, ByteOrder outOrder, float val, byte[] workBuf)
            throws IOException {
        return writeFloat(outStream, outOrder, val, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final byte[] writeFloat (OutputStream outStream, ByteOrder outOrder, float val)
            throws IOException {
        int floatBits=Float.floatToIntBits(val);
        return writeInt32(outStream, outOrder, floatBits);
    }

    public static final byte[] writeUnsignedInt32 (final OutputStream out, final ByteOrder outOrder, final long val) throws IOException
    {
        if ((val > 0x00FFFFFFFFL) || (val < 0L))
            throw new StreamCorruptedException("writeUnsignedInt32(" + val + ")[" + outOrder + "] value exceeds max. unsigned int32 value");

        return writeInt32(out, outOrder, (int) (val & 0x00FFFFFFFFL));
    }

    // returns number of used/written bytes
    public static final int writeUnsignedInt32 (
            final OutputStream outStream, final ByteOrder outOrder, final long val,
            final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if ((val > 0x00FFFFFFFFL) || (val < 0L))
            throw new StreamCorruptedException("writeUnsignedInt32(" + val + ")[" + outOrder + "] value exceeds max. unsigned int32 value");
        return writeInt32(outStream, outOrder, (int) (val & 0x00FFFFFFFFL), workBuf, off, len);
    }
    // returns number of used/written bytes
    public static final int writeUnsignedInt32 (
            final OutputStream outStream, final ByteOrder outOrder, final long val, final byte[] workBuf)
        throws IOException
    {
        return writeUnsignedInt32(outStream, outOrder, val, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    // returns number of used bytes
    public static final int toInt64ByteArray (
            final long val, final ByteOrder outOrder, final byte[] buf, final int off)
        throws NumberFormatException
    {
        if (null == outOrder)
            throw new NumberFormatException("toInt64ByteArray(" + val + ") no order specified");

        if (ByteOrder.BIG_ENDIAN.equals(outOrder))
        {
            for (int    dIndex=0, bIndex=off, shiftSize=Long.SIZE - Byte.SIZE;
                 dIndex < 8;
                 dIndex++, shiftSize -= Byte.SIZE, bIndex++)
            {
                final long  dv=(shiftSize > 0) ? (val >> shiftSize) : val;
                buf[bIndex] = (byte) (dv & 0x00FFL);
            }
        }
        else
        {
            for (int    dIndex=off + 8, shiftSize=Long.SIZE - Byte.SIZE;
                 dIndex > off;
                 dIndex--, shiftSize -= Byte.SIZE)
            {
                final long  dv=(shiftSize > 0) ? (val >> shiftSize) : val;
                buf[dIndex - 1] = (byte) (dv & 0x00FFL);
            }
        }

        return 8;
    }

    public static final int toInt64ByteArray (
            final long val, final ByteOrder outOrder, final byte[] buf)
        throws NumberFormatException
    {
        return toInt64ByteArray(val, outOrder, buf, 0);
    }

    public static final byte[] toInt64ByteArray (long val, ByteOrder outOrder) throws NumberFormatException {
        final byte[]    data=new byte[Long.SIZE / Byte.SIZE];
        final int       eLen=toInt64ByteArray(val, outOrder, data);
        if (eLen != data.length)
            throw new NumberFormatException("toInt64ByteArray(" + val + ")[" + outOrder + "] unexpected used length: expected=" + data.length + "/got=" + eLen);
        return data;
    }

    public static final byte[] toNativeInt64ByteArray (final int val) {
        return toInt64ByteArray(val, ByteOrder.nativeOrder());
    }

    public static final byte[] writeInt64 (
            final OutputStream outStream, final ByteOrder outOrder, final long val) throws IOException
    {
        if ((null == outStream) || (null == outOrder))
            throw new IOException("writeInt64(" + outOrder + ")[" + val + "] incomplete arguments");

        final byte[]    data=toInt64ByteArray(val, outOrder);
        outStream.write(data);
        return data;
    }

    // returns number of used/written bytes
    public static final int writeInt64 (
            final OutputStream outStream, final ByteOrder outOrder, final long val,
            final byte[] workBuf, final int off, final int len)
                    throws IOException {
        if (len < 8)
            throw new StreamCorruptedException("writeInt64(" + val + ")[" + outOrder + "] insufficient work buffer length: " + len);

        toInt64ByteArray(val, outOrder, workBuf, off);
        outStream.write(workBuf, off, 8);
        return 8;
    }

    // returns number of used/written bytes
    public static final int writeInt64 (
            final OutputStream outStream, final ByteOrder outOrder, final long val, final byte[] workBuf)
        throws IOException
    {
        return writeInt64(outStream, outOrder, val, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    // returns number of used bytes
    public static final int toDoubleByteArray (double val, ByteOrder outOrder, byte[] buf, int off)
            throws NumberFormatException {
        return toInt64ByteArray(Double.doubleToLongBits(val), outOrder, buf, off);
    }

    public static final int toDoubleByteArray (double val, ByteOrder outOrder, byte[] buf)
            throws NumberFormatException {
        return toDoubleByteArray(val, outOrder, buf, 0);
    }

    public static final byte[] toDoubleByteArray (double val, ByteOrder outOrder)
            throws NumberFormatException {
        return toInt64ByteArray(Double.doubleToLongBits(val), outOrder);
    }

    public static final byte[] toNativeDoubleByteArray (double val) {
        return toDoubleByteArray(val, ByteOrder.nativeOrder());
    }

    // returns number of used/written bytes
    public static final int writeDouble (OutputStream outStream, ByteOrder outOrder, double val,
                                         byte[] workBuf, int off, int len)
            throws IOException {
        return writeInt64(outStream, outOrder, Double.doubleToLongBits(val), workBuf, off, len);
    }

    // returns number of used/written bytes
    public static final int writeDouble (OutputStream outStream, ByteOrder outOrder, double val, byte[] workBuf)
            throws IOException {
        return writeDouble(outStream, outOrder, val, workBuf, 0, ExtendedArrayUtils.length(workBuf));
    }

    public static final byte[] writeDouble (OutputStream outStream, ByteOrder outOrder, double val)
            throws IOException {
        return writeInt64(outStream, outOrder, Double.doubleToLongBits(val));
    }
}
