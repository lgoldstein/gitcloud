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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedCharSequenceUtils;

/**
 * Delegates all possible methods to either {@code append} or {@code write}
 * @author Lyor G.
 */
public class SkeletonPrintStream extends PrintStream {
    public static final String EOL=System.getProperty("line.separator");

    public SkeletonPrintStream(OutputStream s) {
        super(s);
    }

    public SkeletonPrintStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public SkeletonPrintStream(File file) throws FileNotFoundException {
        super(file);
    }

    public SkeletonPrintStream(OutputStream s, boolean autoFlush) {
        super(s, autoFlush);
    }

    public SkeletonPrintStream(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public SkeletonPrintStream(File file, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    public SkeletonPrintStream(OutputStream s, boolean autoFlush,
            String encoding) throws UnsupportedEncodingException {
        super(s, autoFlush, encoding);
    }

    @Override
    public void print(char[] s) {
        if (ExtendedArrayUtils.length(s) <= 0) {
            append((s == null) ? "null" : "");
        } else {
            append(new String(s));
        }
    }

    @Override
    public void print(boolean b) {
        append(String.valueOf(b));
    }

    @Override
    public void print(char c) {
        append(c);
    }

    @Override
    public void print(int i) {
        append(String.valueOf(i));
    }

    @Override
    public void print(long l) {
        append(String.valueOf(l));
    }

    @Override
    public void print(float f) {
        append(String.valueOf(f));
    }

    @Override
    public void print(double d) {
        append(String.valueOf(d));
    }

    @Override
    public void print(String s) {
        append(s);
    }

    @Override
    public void print(Object obj) {
        append(String.valueOf(obj));
    }

    @Override
    public void println() {
        println("");
    }

    @Override
    public void println(boolean x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(char x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(int x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(long x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(float x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(double x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(char[] x) {
        if (ExtendedArrayUtils.length(x) <= 0) {
            println((x == null) ? "null" : "");
        } else {
            println(new String(x));
        }
    }

    @Override
    public void println(String x) {
        append(x);
        append(EOL);
    }

    @Override
    public void println(Object x) {
        println(String.valueOf(x));
    }

    @Override
    public PrintStream append(CharSequence csq) {
        return append(csq, 0, ExtendedCharSequenceUtils.getSafeLength(csq));
    }
}
