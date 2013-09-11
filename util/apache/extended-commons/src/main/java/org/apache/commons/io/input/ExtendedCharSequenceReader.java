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

package org.apache.commons.io.input;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.Channel;

import org.apache.commons.lang3.exception.ExtendedExceptionUtils;

/**
 * Corrects the behavior of {@link #clone()} so as not to allow any further
 * data to be read after being called
 * @author Lyor G.
 */
public class ExtendedCharSequenceReader extends CharSequenceReader implements Channel {
    private static final long serialVersionUID = -6932733678578404626L;
    private boolean open=true;

    public ExtendedCharSequenceReader(CharSequence charSequence) {
        super(charSequence);
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        if (!isOpen()) {
            ExtendedExceptionUtils.rethrowException(new IOException("read(CharBuffer): stream is closed"));
        }
        
        return super.read(target);
    }

    @Override
    public int read() {
        if (!isOpen()) {
            ExtendedExceptionUtils.rethrowException(new IOException("read(): stream is closed"));
        }

        return super.read();
    }

    @Override
    public int read(char[] array, int offset, int length) {
        if (!isOpen()) {
            ExtendedExceptionUtils.rethrowException(new IOException("read(char[])[" + offset + "/" + length + "]: stream is closed"));
        }
        return super.read(array, offset, length);
    }

    @Override
    public long skip(long n) {
        if (!isOpen()) {
            ExtendedExceptionUtils.rethrowException(new IOException("skip(" + n + "): stream is closed"));
        }
        return super.skip(n);
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        if (isOpen()) {
            open = false;
        }
    }
}
