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

import java.io.ByteArrayOutputStream;

/**
 * Exposes the internal buffer of the {@link ByteArrayOutputStream} 
 * so that one can use it instead of a new buffer created by {@link ByteArrayOutputStream#toByteArray()}.
 * The amount of valid data in the buffer can be queried via {@link ByteArrayOutputStream#size()}
 * @author Lyor G.
 */
public class ExposedBufferOutputStream extends ByteArrayOutputStream {
    public ExposedBufferOutputStream() {
        super();
    }

    public ExposedBufferOutputStream(int size) {
        super(size);
    }

    public synchronized byte[] getBuffer() {
        return this.buf;
    }
}
