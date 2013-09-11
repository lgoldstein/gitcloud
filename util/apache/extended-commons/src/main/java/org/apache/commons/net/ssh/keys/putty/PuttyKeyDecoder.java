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

package org.apache.commons.net.ssh.keys.putty;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;

import org.apache.commons.net.ssh.keys.KeyLoader;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 11:17:45 AM
 */
public interface PuttyKeyDecoder extends KeyLoader {
    /**
     * Value (case insensitive) used to denote that private key is not encrypted
     */
    static final String NO_PRIVATE_KEY_ENCRYPTION_VALUE="none";

    /**
     * @param pubData The BASE64 encoded public key data
     * @param prvData The BASE74 encoded private key data
     * @param prvEncryption The type of encryption used on the private key
     * @param password The password used to protect the private key - if
     * {@code null}/empty then no password protection is used
     * @return The decoded {@link PrivateKey}
     * @throws IOException If unable to decode the data
     * @see #decodePrivateKey(byte[], byte[])
     */
    PrivateKey decodePrivateKey(String pubData, String prvData, String prvEncryption, String password) throws IOException;

    /**
     * @param pubData The public key data
     * @param prvData The private key data
     * @return The decoded {@link PrivateKey}
     * @throws IOException If unable to decode the data
     */
    PrivateKey decodePrivateKey(byte[] pubData, byte[] prvData) throws IOException;

    /**
     * @param pubStream The public key byte stream
     * @param okToClosePub <code>true</code> if OK to close the public byte
     * stream (regardless of success or failure)
     * @param prvStream The private key byte stream
     * @param okToClosePrv <code>true</code> if OK to close the private byte
     * stream (regardless of success or failure)
     * @return The decoded {@link PrivateKey}
     * @throws IOException If unable to decode the data
     */
    PrivateKey decodePrivateKey(InputStream pubStream, boolean okToClosePub, InputStream prvStream, boolean okToClosePrv) throws IOException;
}
