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

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.channels.Channel;

/**
 * A {@link Reader} that returns (-1) for any <code>read</code> calls
 * @author Lyor G.
 * @since Jun 5, 2013 2:52:50 PM
 */
public class ClosedReader extends Reader implements Channel {
    public static final ClosedReader    CLOSED_READER=new ClosedReader();

    public ClosedReader () {
        super();
    }

    @Override
    public boolean isOpen () {
        return false;
    }

    @Override
    public int read (CharBuffer target) throws IOException {
        return (-1);
    }

    @Override
    public int read () throws IOException {
        return (-1);
    }

    @Override
    public int read (char[] cbuf, int off, int len) throws IOException {
        return (-1);
    }

    @Override
    public void close () throws IOException {
        // ignored
    }
}
