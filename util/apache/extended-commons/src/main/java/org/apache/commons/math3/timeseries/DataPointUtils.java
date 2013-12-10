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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author Lyor G.
 * @since Oct 30, 2013 9:31:43 AM
 */
public class DataPointUtils {
    public DataPointUtils() {
        super();
    }

    /**
     * @param points Original {@link Collection} of {@link DataPoint}s
     * @return A {@link List} of all the points that have a valid timestamp and value
     */
    public static final <D extends DataPoint> List<D> filterEmptyPoints(Collection<? extends D> points) {
        if (ExtendedCollectionUtils.isEmpty(points)) {
            return Collections.emptyList();
        } else {
            return ExtendedCollectionUtils.selectToList(points, DataPoint.VALID_SELECTOR);
        }
    }

    /**
     * @param points Original {@link Collection} of {@link DataPoint}s
     * @return A {@link List} of all the points that have a valid timestamp
     */
    public static final <D extends DataPoint> List<D> filterUnstampedPoints(Collection<? extends D> points) {
        if (ExtendedCollectionUtils.isEmpty(points)) {
            return Collections.emptyList();
        } else {
            return ExtendedCollectionUtils.selectToList(points, DataPoint.TIMESTAMP_SELECTOR);
        }
    }

    /**
     * @param points Original {@link Collection} of {@link DataPoint}s
     * @return A {@link List} of all the points that have a valid value
     */
    public static final <D extends DataPoint> List<D> filterNonValuedPoints(Collection<? extends D> points) {
        if (ExtendedCollectionUtils.isEmpty(points)) {
            return Collections.emptyList();
        } else {
            return ExtendedCollectionUtils.selectToList(points, DataPoint.VALUE_SELECTOR);
        }
    }
    
    public static final double  RESAMPLE_INTERVAL_IMPACT_THRESHOLD=0.5d;
    
    // returned List is sorted by ascending order of timestamp(s)
    public static final List<DataPoint> resampleByBuckets(TimeUnit unit, long count, Collection<? extends DataPoint> points) {
        Validate.notNull(unit, "No time unit specified", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.isTrue(count > 0L, "Non-positive count", count);
        return resampleByBuckets(unit.toMillis(count), points);
    }

    // returned List is sorted by ascending order of timestamp(s)
    public static final List<DataPoint> resampleByBuckets(long msecInterval, Collection<? extends DataPoint> points) {
        Validate.isTrue(msecInterval > 0L, "Non-positive interval", msecInterval);

        if (ExtendedCollectionUtils.isEmpty(points)) {
            return Collections.emptyList();
        }
        
        List<DataPoint>  data=new ArrayList<DataPoint>(points);
        Collections.sort(data, DataPoint.BY_TIMESTAMP_COMPARATOR);

        DataPoint       startPoint=data.get(0), endPoint=data.get(data.size() - 1);
        long            minTimestamp=startPoint.getTimestamp(), maxTimestamp=endPoint.getTimestamp();
        long            dpInterval=1L + (maxTimestamp - minTimestamp);
        int             numIntervals=1 + (int) (dpInterval / msecInterval);
        List<DataPoint> result=new ArrayList<DataPoint>(numIntervals);

        Map<Integer,BucketValue>  bucketsMap=new HashMap<Integer, BucketValue>(numIntervals);
        for (DataPoint p : data) {
            if (!DataPoint.VALID_SELECTOR.evaluate(p)) {
                throw new IllegalArgumentException("Bad data point: " + p);
            }

            long    timestamp=p.getTimestamp(), offset=timestamp - minTimestamp;
            int     intervalIndex=(int) (offset / msecInterval);
            long    intervalStart=minTimestamp + (intervalIndex * msecInterval);
            double  intervalFactor=(double) (timestamp - intervalStart) / msecInterval;

            /*
             * The closer the timestamp is to the middle of the interval the
             * more impact we want it to have
             */
            double  midDistance=(intervalFactor >= RESAMPLE_INTERVAL_IMPACT_THRESHOLD)
                                ? intervalFactor - RESAMPLE_INTERVAL_IMPACT_THRESHOLD
                                : RESAMPLE_INTERVAL_IMPACT_THRESHOLD - intervalFactor
                                ;
            double  impactFactor=1.0d - midDistance, pointValue=p.getValue();

            if (intervalFactor >= 0.5d) {
                // if not last interval, then update the overflow into next one
                if (intervalIndex < (numIntervals - 1)) {
                    BucketValue.addToBucket(bucketsMap, intervalIndex, pointValue * impactFactor);
                    BucketValue.addToBucket(bucketsMap, intervalIndex + 1, pointValue * midDistance);
                } else {
                    BucketValue.addToBucket(bucketsMap, intervalIndex, pointValue);
                }
            } else {
                // if not first interval, then update the overflow into previous one
                if (intervalIndex > 0) {
                    BucketValue.addToBucket(bucketsMap, intervalIndex, pointValue * impactFactor);
                    BucketValue.addToBucket(bucketsMap, intervalIndex - 1, pointValue * midDistance);
                } else {
                    BucketValue.addToBucket(bucketsMap, intervalIndex, pointValue);
                }
            }
        }
        
        for (Map.Entry<? extends Number,BucketValue> e : bucketsMap.entrySet()) {
            int             index=e.getKey().intValue();
            long            timestamp=minTimestamp + index * msecInterval;
            double          value=e.getValue().getAverageValue();
            DataPoint    p=new DataPoint(timestamp, value);
            result.add(p);
        }
        
        Collections.sort(result, DataPoint.BY_TIMESTAMP_COMPARATOR);
        return result;
    }
    
    public static final List<DataPoint> largestTriangleThreeBucketsDownsample(TimeUnit unit, long count, Collection<? extends DataPoint> points) {
        Validate.notNull(unit, "No time unit specified", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.isTrue(count > 0L, "Non-positive count", count);
        return largestTriangleThreeBucketsDownsample(unit.toMillis(count), points);
    }

    // see http://skemman.is/en/item/view/1946/15343, http://skemman.is/stream/get/1946/15343/37285/3/SS_MSthesis.pdf, https://github.com/sveinn-steinarsson/flot-downsample/
    // returned List is sorted by ascending order of timestamp(s)
    public static final List<DataPoint> largestTriangleThreeBucketsDownsample(long msecInterval, Collection<? extends DataPoint> points) {
        Validate.isTrue(msecInterval > 0L, "Non-positive interval", msecInterval);

        if (ExtendedCollectionUtils.isEmpty(points)) {
            return Collections.emptyList();
        }

        List<DataPoint>  data=new ArrayList<DataPoint>(points);
        Collections.sort(data, DataPoint.BY_TIMESTAMP_COMPARATOR);
        if (data.size() <= 2) {
            return data;
        }

        DataPoint   startPoint=data.get(0), endPoint=data.get(data.size() - 1);
        long        minTimestamp=startPoint.getTimestamp(), maxTimestamp=endPoint.getTimestamp();
        long        dpInterval=maxTimestamp - minTimestamp;
        int         numIntervals=(int) (dpInterval / msecInterval);
        if (numIntervals <= 2) {
            return resampleByBuckets(msecInterval, points);
        }

        List<DataPoint>  result=new ArrayList<DataPoint>(Math.max(2, numIntervals));
        result.add(startPoint); // Always add the first point

        // Bucket size. Leave room for start and end data points
        double      every= (double) (data.size() - 2) / (numIntervals - 2);
        int         a=0, next_a=(-1);
        DataPoint   max_area_point=null;

        for (int i = 0; i < numIntervals - 2; i++) {
            // Calculate point average for next bucket (containing c)
            double  avg_x=0.0d, avg_y = 0.0d;
            int     avg_range_start=(int) Math.floor((i + 1 ) * every) + 1;
            int     avg_range_end=(int) Math.floor((i + 2 ) * every) + 1;
            if (avg_range_end > data.size()) {
                avg_range_end = data.size();
            }

            int avg_range_length=avg_range_end - avg_range_start;
            for (int    pos=avg_range_start; pos < avg_range_end; pos++ ) {
                DataPoint    p=data.get(pos);
                if (!DataPoint.VALID_SELECTOR.evaluate(p)) {
                    throw new IllegalArgumentException("Bad data point: " + p);
                }
                avg_x += p.getTimestamp();
                avg_y += p.getValue();
            }
            avg_x /= avg_range_length;
            avg_y /= avg_range_length;

            // Get the range for this bucket
            int         range_offs = (int) Math.floor((i + 0) * every ) + 1, range_to=(int) Math.floor((i + 1) * every) + 1;
            DataPoint   rangePoint=data.get(a);
            // Point a
            double      point_a_x = rangePoint.getTimestamp(), point_a_y=rangePoint.getValue();
            double      max_area=(-1), area=(-1);

            for ( ; range_offs < range_to; range_offs++ ) {
                DataPoint    p=data.get(range_offs);
                if (!DataPoint.VALID_SELECTOR.evaluate(p)) {
                    throw new IllegalArgumentException("Bad data point: " + p);
                }

                // Calculate triangle area over three buckets
                area = Math.abs((point_a_x - avg_x ) * (p.getValue() - point_a_y) -
                                (point_a_x - p.getTimestamp()) * (avg_y - point_a_y)) * 0.5d;
                if ( area > max_area ) {
                    max_area = area;
                    max_area_point = p;
                    next_a = range_offs; // Next a is this b
                }
            }

            Validate.notNull(max_area_point, "No max. aread point", ArrayUtils.EMPTY_OBJECT_ARRAY);
            result.add(max_area_point); // Pick this point from the bucket
            a = next_a; // This a is the next a (chosen b)
        }

        result.add(endPoint);   // Always add the last point
        return result;
    }

}
