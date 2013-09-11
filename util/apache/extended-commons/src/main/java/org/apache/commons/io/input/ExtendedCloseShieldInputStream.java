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

import java.io.InputStream;
import java.nio.channels.Channel;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;

/**
 * Avoids the creation of a {@link ClosedInputStream} on each call to
 * {@link CloseShieldInputStream#close()}
 * @author Lyor G.
 * @since Jun 5, 2013 8:51:57 AM
 */
public class ExtendedCloseShieldInputStream extends CloseShieldInputStream implements Channel {
    public static final ExtendedTransformer<InputStream,ExtendedCloseShieldInputStream>  CLOSE_SHIELD_INPUT=
            new AbstractExtendedTransformer<InputStream,ExtendedCloseShieldInputStream>(InputStream.class,ExtendedCloseShieldInputStream.class) {
                @Override
                public ExtendedCloseShieldInputStream transform (InputStream input) {
                    if (input == null) {
                        return null;
                    } else {
                        return new ExtendedCloseShieldInputStream(input);
                    }
                }
            };

	public ExtendedCloseShieldInputStream (InputStream input) {
		super(input);
	}

	@Override
	public boolean isOpen () {
		if (this.in == ClosedInputStream.CLOSED_INPUT_STREAM) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void close () {
		if (this.in != ClosedInputStream.CLOSED_INPUT_STREAM) {
			this.in = ClosedInputStream.CLOSED_INPUT_STREAM;
		}
	}
    
    /**
     * @param s The original {@link InputStream}
     * @param okToClose <code>true</code> if the original input stream
     * may be closed
     * @return The original stream if OK to close it, an {@link ExtendedCloseShieldInputStream}
     * wrapper otherwise
     */
    public static final InputStream resolveInputStream(InputStream s, boolean okToClose) {
        if (okToClose) {
            return s;
        } else {
            return new ExtendedCloseShieldInputStream(s);
        }
    }
}
