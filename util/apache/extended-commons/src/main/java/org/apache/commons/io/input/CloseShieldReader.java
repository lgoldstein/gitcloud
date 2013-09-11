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
import java.io.Reader;
import java.nio.channels.Channel;

/**
 * Protects the proxy {@link Reader} from being closed when {@link #close()}
 * is invoked
 * @author Lyor G.
 * @since Jun 5, 2013 2:51:52 PM
 */
public class CloseShieldReader extends ProxyReader implements Channel {
    public CloseShieldReader (Reader proxy) {
        super(proxy);
    }

    @Override
    public boolean isOpen () {
        if (this.in == ClosedReader.CLOSED_READER) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void close () throws IOException {
        if (this.in != ClosedReader.CLOSED_READER) {
            this.in = ClosedReader.CLOSED_READER;
        }
    }
    
    /**
     * @param rdr The original {@link Reader}
     * @param okToClose <code>true</code> if the original reader may be closed
     * @return The original reader if OK to close, a {@link CloseShieldReader} otherwise
     */
    public static final Reader resolveReader(Reader rdr, boolean okToClose) {
        if (okToClose) {
            return rdr;
        } else {
            return new CloseShieldReader(rdr);
        }
    }
}
