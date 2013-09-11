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

package org.apache.commons.io.output;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author Lyor G.
 */
public class MultiplexedPrintStream extends SkeletonPrintStream {
    private final List<PrintStream>   streams;

    public MultiplexedPrintStream(PrintStream ... s) {
        this(ExtendedArrayUtils.asList(s));
    }
    
    public MultiplexedPrintStream(Collection<PrintStream> s) {
        super(NullOutputStream.NULL_OUTPUT_STREAM);
        Validate.isTrue(!ExtendedCollectionUtils.isEmpty(s), "No streams provided", ArrayUtils.EMPTY_OBJECT_ARRAY);
        streams = new ArrayList<PrintStream>(s);
    }

    @Override
    public void flush() {
        for (PrintStream ps : streams) {
            ps.flush();
        }
    }

    @Override
    public void close() {
        for (PrintStream ps : streams) {
            ps.close();
        }
    }

    @Override
    public boolean checkError() {
        for (PrintStream ps : streams) {
            if (ps.checkError()) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    protected void setError() {
        // TODO find a way to implement this (e.g., via reflection)
    }

    @Override
    protected void clearError() {
        // TODO find a way to implement this (e.g., via reflection)
    }

    @Override
    public void write(int b) {
        for (PrintStream ps : streams) {
            ps.write(b);
        }
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        for (PrintStream ps : streams) {
            ps.write(buf, off, len);
        }
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        for (PrintStream ps : streams) {
            ps.append(csq, start, end);
        }
        
        return this;
    }

    @Override
    public PrintStream append(char c) {
        for (PrintStream ps : streams) {
            ps.append(c);
        }
        
        return this;
    }
}
