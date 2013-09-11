/**
 * 
 */
package org.apache.commons.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.output.ExposedBufferOutputStream;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedEndianUtilsTest extends AbstractTestSupport {
    public ExtendedEndianUtilsTest() {
        super();
    }

    @Test
    public void testReadInt32() {
        testReadWriteCycle(new NumberTestStream<Integer>() {
            @Override
            public Class<Integer> getNumberType() {
                return Integer.class;
            }

            @Override
            public Collection<Integer> getSpecialTestValues() {
                return Collections.emptyList();
            }

            @Override
            public Integer generateExpectedValue() {
                return Integer.valueOf(RANDOMIZER.nextInt());
            }

            @Override
            public void write(OutputStream outStream, Integer value, ByteOrder outOrder) throws IOException {
                ExtendedEndianUtils.writeInt32(outStream, outOrder, value.intValue());
            }

            @Override
            public Integer read(InputStream inStream, ByteOrder inOrder) throws IOException {
                return Integer.valueOf(ExtendedEndianUtils.readSignedInt32(inStream, inOrder));
            }
        });
    }

    @Test
    public void testReadInt64() {
        testReadWriteCycle(new NumberTestStream<Long>() {
            @Override
            public Class<Long> getNumberType() {
                return Long.class;
            }

            @Override
            public Collection<Long> getSpecialTestValues() {
                return Collections.emptyList();
            }

            @Override
            public Long generateExpectedValue() {
                return Long.valueOf(RANDOMIZER.nextLong());
            }

            @Override
            public void write(OutputStream outStream, Long value, ByteOrder outOrder) throws IOException {
                ExtendedEndianUtils.writeInt64(outStream, outOrder, value.longValue());
            }

            @Override
            public Long read(InputStream inStream, ByteOrder inOrder) throws IOException {
                return Long.valueOf(ExtendedEndianUtils.readSignedInt64(inStream, inOrder));
            }
        });
    }

    @Test
    public void testReadFloat() {
        testReadWriteCycle(new NumberTestStream<Float>() {
            @Override
            public Class<Float> getNumberType() {
                return Float.class;
            }

            @Override
            public Collection<Float> getSpecialTestValues() {
                return Arrays.asList(Float.valueOf(Float.NaN), Float.valueOf(Float.POSITIVE_INFINITY), Float.valueOf(Float.NEGATIVE_INFINITY));
            }

            @Override
            public Float generateExpectedValue() {
                return Float.valueOf(RANDOMIZER.nextFloat());
            }

            @Override
            public void write(OutputStream outStream, Float value, ByteOrder outOrder) throws IOException {
                ExtendedEndianUtils.writeFloat(outStream, outOrder, value.floatValue());
            }

            @Override
            public Float read(InputStream inStream, ByteOrder inOrder) throws IOException {
                return Float.valueOf(ExtendedEndianUtils.readFloat(inStream, inOrder));
            }
        });
    }
    
    @Test
    public void testReadDouble() {
        testReadWriteCycle(new NumberTestStream<Double>() {
            @Override
            public Class<Double> getNumberType() {
                return Double.class;
            }

            @Override
            public Collection<Double> getSpecialTestValues() {
                return Arrays.asList(Double.valueOf(Double.NaN), Double.valueOf(Double.POSITIVE_INFINITY), Double.valueOf(Double.NEGATIVE_INFINITY));
            }

            @Override
            public Double generateExpectedValue() {
                return Double.valueOf(RANDOMIZER.nextDouble());
            }

            @Override
            public void write(OutputStream outStream, Double value, ByteOrder outOrder) throws IOException {
                ExtendedEndianUtils.writeDouble(outStream, outOrder, value.doubleValue());
            }

            @Override
            public Double read(InputStream inStream, ByteOrder inOrder) throws IOException {
                return Double.valueOf(ExtendedEndianUtils.readDouble(inStream, inOrder));
            }
        });
    }

    private <N extends Number> void testReadWriteCycle(final NumberTestStream<N> numStream) {
        final ExposedBufferOutputStream outStream=new ExposedBufferOutputStream(Short.SIZE);
        final SwappingTestRunner<N>     runner=new SwappingTestRunner<N>() {
                @Override
                public void run(N expected, N outValue, ByteOrder outOrder, ByteOrder inOrder) throws IOException {
                    outStream.reset();
                    try {
                        numStream.write(outStream, outValue, outOrder);
                    } finally {
                        outStream.close();
                    }
                    
                    ByteArrayInputStream    inStream=new ByteArrayInputStream(outStream.getBuffer(), 0, outStream.size());
                    try {
                        Number  actual=numStream.read(inStream, inOrder);
                        assertEquals("Mismatched recovered value for " + outOrder + " => " + inOrder, expected, actual);
                    } finally {
                        inStream.close();
                    }
                }
            };
        ByteOrder       nativeOrder=ByteOrder.nativeOrder();
        ByteOrder       compOrder=ExtendedEndianUtils.complementingOrder(nativeOrder);
        Class<N>        numType=numStream.getNumberType();
        Collection<N>   reservedValues=numStream.getSpecialTestValues();
        for (int index=0; index < Long.SIZE; index++) {
            N   expected=numStream.generateExpectedValue();
            @SuppressWarnings("unchecked")
            N   swapped=(N) ExtendedEndianUtils.swapValueOrder(expected);
            if (reservedValues.contains(expected) || reservedValues.contains(swapped)) {
                logger.info("testReadWriteCycle(" + numType.getSimpleName() + ")[" + nativeOrder + " => " + compOrder + "]"
                          + " skip special value(s): " + expected + " / " + swapped);
                continue; // we have special testing further on for reserved value
            }

            assertEquals("Mismatched comparison result for " + nativeOrder + " => " + compOrder
                       + " compare(" + expected + "," + swapped,
                         0, ExtendedEndianUtils.SWAPPED_ORDER_COMPARATOR.compare(expected, swapped));
            try {
                runner.run(expected, swapped, nativeOrder, compOrder);
            } catch(IOException e) {
                fail(e.getClass().getSimpleName()
                   + " while processing " + numType.getSimpleName() + "[" + expected + "]"
                   + ": " + e.getMessage());
            }
        }
        
        /*
         * NOTE: the reserved values cannot be written in one order
         * and read in another as their bits have special meanings
         */
        ByteOrder[] orders={ nativeOrder, compOrder };
        for (N expected : reservedValues) {
            for (ByteOrder testOrder : orders) {
                logger.info("testReadWriteCycle(" + numType.getSimpleName() + ")[" + testOrder + "]: " + expected);
                try {
                    runner.run(expected, expected, testOrder, testOrder);
                } catch(IOException e) {
                    fail(e.getClass().getSimpleName()
                       + " while processing special value " + numType.getSimpleName() + "[" + expected + "]"
                       + ": " + e.getMessage());
                }
            }
        }
    }

    private static interface SwappingTestRunner<N extends Number> {
        void run(N expected, N outValue, ByteOrder outOrder, ByteOrder inOrder) throws IOException;
    }

    private static interface NumberTestStream<N extends Number> {
        Class<N> getNumberType();
        N generateExpectedValue();
        void write(OutputStream outStream, N value, ByteOrder outOrder) throws IOException;
        N read(InputStream inStream, ByteOrder inOrder) throws IOException;
        Collection<N> getSpecialTestValues();
    }
}
