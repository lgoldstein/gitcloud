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

package org.apache.commons.codec.signatures;

import java.io.Closeable;
import java.security.Signature;
import java.security.SignatureException;

/**
 * @author Lyor G.
 * @since Sep 9, 2013 12:50:26 PM
 */
public interface SignerStream extends Closeable {
    /**
     * @return The {@link Signature} instance used to sign the data
     */
    Signature getSigner();
    
    /**
     * @return The signature of the data that passed through the signer so far
     * @throws SignatureException If failed to sign
     */
    byte[] sign() throws SignatureException;
    
    /**
     * @param signature The expected signature bytes
     * @return <code>true</code> if the expected signature matches the
     * signature of the data that passed through the signer so far
     * @throws SignatureException If failed to verify
     */
    boolean verify(byte ... signature) throws SignatureException;
    
    /**
     * @param signature The expected signature bytes
     * @param off The offset of the signature within the signature bytes
     * @param len Number of bytes to be verified
     * @return <code>true</code> if the expected signature matches the
     * signature of the data that passed through the signer so far
     * @throws SignatureException If failed to verify
     */
    boolean verify(byte[] signature, int off, int len) throws SignatureException;
}
