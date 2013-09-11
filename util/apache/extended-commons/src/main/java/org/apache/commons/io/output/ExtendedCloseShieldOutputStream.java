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

import java.io.OutputStream;
import java.nio.channels.Channel;

/**
 * Avoids the creation of a new {@link ClosedOutputStream} on each {@link #close()} call
 * that exists in {@link CloseShieldOutputStream}
 * @author lgoldstein
 */
public class ExtendedCloseShieldOutputStream extends CloseShieldOutputStream implements Channel {
	public ExtendedCloseShieldOutputStream(OutputStream outStream) {
		super(outStream);
	}

	@Override
	public boolean isOpen() {
		if (this.out != ClosedOutputStream.CLOSED_OUTPUT_STREAM) {
			return true;
		} else {
			return false;	// debug breakpoint
		}
	}

	@Override
	public void close() {
		if (this.out != ClosedOutputStream.CLOSED_OUTPUT_STREAM) {
			this.out = ClosedOutputStream.CLOSED_OUTPUT_STREAM;	// debug breakpoint
		}
	}
	
    /**
     * @param s The original {@link OutputStream}
     * @param okToClose <code>true</code> if the original output stream
     * may be closed
     * @return The original stream if OK to close it, an {@link ExtendedCloseShieldOutputStream}
     * wrapper otherwise
     */
	public static final OutputStream resolveOutputStream(OutputStream s, boolean okToClose) {
	    if (okToClose) {
	        return s;
	    } else {
	        return new ExtendedCloseShieldOutputStream(s);
	    }
	}
}
