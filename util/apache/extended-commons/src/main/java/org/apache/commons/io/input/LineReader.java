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

package org.apache.commons.io.input;

import java.io.Reader;

import org.apache.commons.io.output.LineLevelAppender;
import org.apache.commons.io.output.LineWriter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * A {@link Reader} that uses the {@link LineLevelAppender} to accumulate
 * characters until an LF is found
 * @author Lyor G.
 * @since Sep 15, 2013 12:20:36 PM
 */
public class LineReader extends TeeReader {
    public LineReader(Reader rdr, LineLevelAppender appender) {
        super(Validate.notNull(rdr, "No reader", ArrayUtils.EMPTY_OBJECT_ARRAY), new LineWriter(appender), true);
    }
}
