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
import java.nio.channels.Channel;

/**
 * Protects the original {@link Writer} from being closed
 * @author lgoldstein
 */
public class CloseShieldWriter extends ProxyWriter implements Channel {
	public CloseShieldWriter(Writer proxy) {
		super(proxy);
	}

	@Override
	public boolean isOpen() {
		if (this.out != ClosedWriter.CLOSED_WRITER) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void close() throws IOException {
		if (this.out != ClosedWriter.CLOSED_WRITER) {
			this.out = ClosedWriter.CLOSED_WRITER;	// debug breakpoint
		}
	}
	
    /**
     * @param w The original {@link Writer}
     * @param okToClose <code>true</code> if the original writer may be closed
     * @return The original writer if OK to close, a {@link CloseShieldWriter} otherwise
     */
	public static final Writer resolveWriter(Writer w, boolean okToClose) {
	    if (okToClose) {
	        return w;
	    } else {
	        return new CloseShieldWriter(w);
	    }
	}
}
