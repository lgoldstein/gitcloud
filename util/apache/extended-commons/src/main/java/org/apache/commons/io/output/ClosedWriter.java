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

package org.apache.commons.io.output;

import java.io.EOFException;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channel;

import org.apache.commons.lang3.ExtendedCharSequenceUtils;

/**
 * A {@link Writer} that throws {@link IOException} on any attempt to write/append to it
 * (but not on {@link #flush()} or {@link #close()})
 * @author lgoldstein
 */
public class ClosedWriter extends Writer implements Channel {
	public static final ClosedWriter	CLOSED_WRITER=new ClosedWriter();

	public ClosedWriter() {
		super();
	}

	@Override
	public final boolean isOpen() {
		return false;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		throw new EOFException("write(CBUF[" + off + "/" + len + "]) writer is closed");
	}

	@Override
	public void write(int c) throws IOException {
		append((char) c);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		append(str, off, off + len);
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		return append(csq, 0, ExtendedCharSequenceUtils.getSafeLength(csq));
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		throw new EOFException("append(CSQ[" + start + "-" + end + "]) writer is closed");
	}

	@Override
	public Writer append(char c) throws IOException {
		throw new EOFException("append(" + String.valueOf(c) + ") writer is closed");
	}

	@Override
	public void flush() throws IOException {
		// ignored
	}

	@Override
	public void close() throws IOException {
		// ignored
	}
}
