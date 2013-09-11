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

package org.apache.commons.codec.digest;

import java.io.Closeable;
import java.security.MessageDigest;

/**
 * @author Lyor G.
 * @since Sep 4, 2013 8:23:40 AM
 */
public interface DigesterStream extends Closeable {
    /**
     * @return The {@link MessageDigest} being used
     */
    MessageDigest getDigest();
    
    /**
     * @return The calculated digest value - {@code null} if called <U>before</U>
     * {@link #close()} 
     */
    byte[] getDigestValue();
}
