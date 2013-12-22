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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.test.AbstractTestSupport;
import org.apache.commons.test.MicroBenchmark;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Oct 30, 2013 10:07:07 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataPointUtilsTest extends AbstractTestSupport {
    public DataPointUtilsTest() {
        super();
    }

    @Test
    public void testFilterUnstampedPoints() {
        List<DataPoint> expected=new ArrayList<DataPoint>();
        final long      NOW=System.currentTimeMillis();
        for (int index=0; index < Byte.SIZE; index++) {
            synchronized(RANDOMIZER) {
                expected.add(new DataPoint(NOW + index, RANDOMIZER.nextBoolean() ? RANDOMIZER.nextDouble() : Double.NaN));
            }
        }
        
        List<DataPoint> points=new ArrayList<DataPoint>(2 * expected.size());
        points.addAll(expected);
        for (int index=0; index < expected.size(); index++) {
            synchronized(RANDOMIZER) {
                points.add(new DataPoint(DataPoint.NO_TIME_VALUE, RANDOMIZER.nextBoolean() ? RANDOMIZER.nextDouble() : Double.NaN));
            }
        }
        
        for (int index=0; index < points.size(); index++) {
            synchronized(RANDOMIZER) {
                Collections.shuffle(points, RANDOMIZER);
            }
            
            List<DataPoint> actual=DataPointUtils.filterUnstampedPoints(points);
            assertEquals("Mismatched result size for " + points, expected.size(), actual.size());
            
            for (DataPoint   expPoint : expected) {
                int actPos=actual.indexOf(expPoint);
                assertTrue("Missing expected=" + expPoint + " from actual: " + actual, actPos >= 0);
            }
        }
    }

    @Test
    public void testFilterNonValuedPoints() {
        List<DataPoint> expected=new ArrayList<DataPoint>();
        final long      NOW=System.currentTimeMillis();
        for (int index=0; index < Byte.SIZE; index++) {
            synchronized(RANDOMIZER) {
                expected.add(new DataPoint(RANDOMIZER.nextBoolean() ? DataPoint.NO_TIME_VALUE : (NOW + index), RANDOMIZER.nextDouble()));
            }
        }
        
        List<DataPoint> points=new ArrayList<DataPoint>(2 * expected.size());
        points.addAll(expected);
        for (int index=0; index < expected.size(); index++) {
            synchronized(RANDOMIZER) {
                points.add(new DataPoint(RANDOMIZER.nextBoolean() ? DataPoint.NO_TIME_VALUE : (NOW + expected.size() + index), Double.NaN));
            }
        }
        
        for (int index=0; index < points.size(); index++) {
            synchronized(RANDOMIZER) {
                Collections.shuffle(points, RANDOMIZER);
            }
            
            List<DataPoint> actual=DataPointUtils.filterNonValuedPoints(points);
            assertEquals("Mismatched result size for " + points, expected.size(), actual.size());
            
            for (DataPoint   expPoint : expected) {
                int actPos=actual.indexOf(expPoint);
                assertTrue("Missing expected=" + expPoint + " from actual: " + actual, actPos >= 0);
            }
        }
    }

    @Test
    public void testFilterEmptyPoints() {
        List<DataPoint> expected=new ArrayList<DataPoint>();
        final long      NOW=System.currentTimeMillis();
        for (int index=0; index < Byte.SIZE; index++) {
            synchronized(RANDOMIZER) {
                expected.add(new DataPoint(NOW + index, RANDOMIZER.nextDouble()));
            }
        }
        
        List<DataPoint> points=new ArrayList<DataPoint>(2 * expected.size());
        points.addAll(expected);
        for (int index=0; index < expected.size(); index++) {
            synchronized(RANDOMIZER) {
                points.add(new DataPoint(NOW + expected.size() + index, Double.NaN));
                points.add(new DataPoint(DataPoint.NO_TIME_VALUE, RANDOMIZER.nextDouble()));
            }
        }
        
        for (int index=0; index < points.size(); index++) {
            synchronized(RANDOMIZER) {
                Collections.shuffle(points, RANDOMIZER);
            }
            
            List<DataPoint> actual=DataPointUtils.filterEmptyPoints(points);
            assertEquals("Mismatched result size for " + points, expected.size(), actual.size());
            
            for (DataPoint   expPoint : expected) {
                int actPos=actual.indexOf(expPoint);
                assertTrue("Missing expected=" + expPoint + " from actual: " + actual, actPos >= 0);
            }
        }
    }
    
    @Test
    @Category(MicroBenchmark.class)
    public void testResampleByBuckets() {
        testResamplePolicy(new Resampler() {
            @Override
            public List<DataPoint> resample(long resampleInterval, Collection<? extends DataPoint> orgSamples) {
                return DataPointUtils.resampleByBuckets(resampleInterval, orgSamples);
            }
        });
    }

    @Test
    @Category(MicroBenchmark.class)
    public void testLargestTriangleThreeBucketsDownsample() {
        testResamplePolicy(new Resampler() {
            @Override
            public List<DataPoint> resample(long resampleInterval, Collection<? extends DataPoint> orgSamples) {
                return DataPointUtils.largestTriangleThreeBucketsDownsample(resampleInterval, orgSamples);
            }
        });
    }

    private void testResamplePolicy(Resampler executor) {
        final long   NOW=System.currentTimeMillis();
        final long   ORG_INTERVAL=TimeUnit.MINUTES.toMillis(5L);
        final long   RESAMPLE_INTERVAL=TimeUnit.HOURS.toMillis(1L);
        for (long sampleSize : new long[] {
                TimeUnit.HOURS.toMillis(6L),
                TimeUnit.HOURS.toMillis(12L),
                TimeUnit.DAYS.toMillis(1L),
                TimeUnit.DAYS.toMillis(7L),
                TimeUnit.DAYS.toMillis(14L)
                }) {
            System.out.append("======================== ")
                      .append(getCurrentTestName())
                      .append(" resample bucket size=")
                      .append(String.valueOf(sampleSize))
                      .println(" =====================");

            int numSamples=(int) (sampleSize / ORG_INTERVAL);
            List<DataPoint>  orgSamples=new ArrayList<DataPoint>(numSamples);
            for (int index=0; index < numSamples; index++) {
                long    timestamp=NOW - index * ORG_INTERVAL;
                int     testValue;
                synchronized(RANDOMIZER) {
                    if ((testValue=RANDOMIZER.nextInt(Short.MAX_VALUE)) == 0) {
                        testValue = Byte.SIZE;
                    }
                }

                DataPoint    p=new DataPoint(timestamp, (testValue * 100.d) / Short.MAX_VALUE);
                orgSamples.add(p);
            }
            
            validateResampleResult(executor.resample(RESAMPLE_INTERVAL, orgSamples));
        }
    }

    private static interface Resampler {
        List<DataPoint> resample(long resampleInterval, Collection<? extends DataPoint> orgSamples);
    }

    private static List<DataPoint> validateResampleResult(List<DataPoint> newSamples) {
        int                 actualSize=newSamples.size();
        for (int index=0; index < actualSize; index++) {
            DataPoint    pCurrent=newSamples.get(index);
            assertTrue("Unexpected negative value: " + pCurrent, Double.compare(pCurrent.getValue(), 0.0d) >= 0);

            if (index > 0) {
                DataPoint    pPrev=newSamples.get(index - 1);
                assertTrue("Previous " + pPrev + " not before current " + pCurrent, DataPoint.BY_TIMESTAMP_COMPARATOR.compare(pPrev, pCurrent) <= 0);
            }
            
            if (index < (actualSize - 1)) {
                DataPoint    pNext=newSamples.get(index + 1);
                assertTrue("Next " + pNext + " not after current " + pCurrent, DataPoint.BY_TIMESTAMP_COMPARATOR.compare(pCurrent, pNext) <= 0);
            }
        }
        
        return newSamples;
    }
}
