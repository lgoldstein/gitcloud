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

package org.apache.commons.io.input.decode;

import java.io.IOException;
import java.io.InputStream;

/**
 * @param <V> Decoded entity type 
 * @author Lyor G.
 */
public interface StreamInstanceDecoder<V> {
    /**
     * @param inputStream The {@link InputStream}
     * @return The decoded instance
     * @throws IOException If unable to decode the instance data
     */
    V read(InputStream inputStream) throws IOException;
}
