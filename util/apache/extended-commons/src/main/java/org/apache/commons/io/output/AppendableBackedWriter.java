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

import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.channels.Channel;

import org.apache.commons.lang3.ExtendedValidate;

/**
 * Implements the {@link Writer} using a backing {@link Appendable} instance
 * @param <A> Type of {@link Appendable} implementation backing the writer 
 * @author Lyor G.
 * @since Jun 4, 2013 1:49:05 PM
 */
public class AppendableBackedWriter<A extends Appendable> extends Writer implements Channel {
	private final A	appender;
	private boolean	closed;

	public AppendableBackedWriter(A backer) {
		appender = ExtendedValidate.notNull(backer, "No appender backing provided");
	}

	public A getAppender() {
		return appender;
	}

	@Override
	public void write (int c) throws IOException {
		append((char) c);
	}

	@Override
	public void write (char[] cbuf, int off, int len) throws IOException {
        if (!isOpen()) {
            throw new IOException("Instance is closed");
        }

		if (len > 0) {
			append(CharBuffer.wrap(cbuf, off, len));
		}
	}

	@Override
	public void write (String str, int off, int len) throws IOException {
		append(str, off, off + len);
	}

	@Override
	public Writer append (CharSequence csq) throws IOException {
		return append(csq, 0, csq.length());
	}

	@Override
	public Writer append (CharSequence csq, int start, int end) throws IOException {
		if (!isOpen()) {
			throw new IOException("Instance is closed");
		}
		Appendable	a=getAppender();
		a.append(csq, start, end);
		return this;
	}

	@Override
	public Writer append (char c) throws IOException {
		if (!isOpen()) {
			throw new IOException("Instance is closed");
		}
		Appendable	a=getAppender();
		a.append(c);
		return this;
	}

	@Override
	public boolean isOpen () {
		return (!closed);
	}

	@Override
	public void flush () throws IOException {
		if (!isOpen()) {
			throw new IOException("Instance is closed");
		}
	}

	@Override
	public void close () throws IOException {
		if (isOpen()) {
			flush();
			closed = true;
		}
		
	}
}
