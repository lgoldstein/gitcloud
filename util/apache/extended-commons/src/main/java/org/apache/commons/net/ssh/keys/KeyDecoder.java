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

package org.apache.commons.net.ssh.keys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.commons.collections15.Predicate;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 8:31:13 AM
 */
public interface KeyDecoder extends KeyLoader {
    /**
     * @param s The {@link InputStream} to read from - <B>Note:</B> assumed to
     * be positioned at the start of the actual data for the specific key type
     * - i.e., <U>after</U> any preceding &quot;metadata&quot; that specifies
     * which key decoder to use
     * @return The decoded {@link PublicKey} instance
     * @throws IOException If invalid bytes contents
     */
    PublicKey decodePublicKey(InputStream s) throws IOException;
    
    /**
     * @param privateKey The {@link PrivateKey}
     * @return The matching {@link PublicKey}
     * @throws GeneralSecurityException If failed to recover the public key
     */
    PublicKey recoverPublicKey(PrivateKey privateKey) throws GeneralSecurityException;
    
    Predicate<? super String> getPEMBeginMarker();
    Predicate<? super String> getPEMEndMarker();

    /**
     * @param rdr A {@link BufferedReader} for reading from the PEM file
     * <B>Note:</B> assumes reader positioned AFTER begin marker
     * @param password The password protecting the key data - if {@code null}
     * or empty then data is not password protected
     * @return Decoded {@link PrivateKey}
     * @throws IOException If failed to read or decode the data
     */
    PrivateKey decodePEMPrivateKey(BufferedReader rdr, String password) throws IOException;
    
    /**
     * @param keyData The BASE64 encoded PEM data
     * @param password The password protecting the key data - if {@code null}
     * or empty then data is not password protected
     * @return The decoded {@link PrivateKey}
     * @throws IOException If failed to decode key
     */
    PrivateKey decodePEMPrivateKey(String keyData, String password) throws IOException;

    /**
     * @param keyBytes The raw PEM data bytes
     * @param password The password protecting the key data - if {@code null}
     * or empty then data is not password protected
     * @return The decoded {@link PrivateKey}
     * @throws IOException If failed to decode key
     */
    PrivateKey decodePEMPrivateKey(byte[] keyBytes, String password) throws IOException;

    /**
     * @param keyBytes The raw PEM data bytes
     * @param off The offset of the PEM data bytes in the buffer
     * @param len Number of PEM data bytes (from offset)
     * @param password The password protecting the key data - if {@code null}
     * or empty then data is not password protected
     * @return The decoded {@link PrivateKey}
     * @throws IOException If failed to decode key
     */
    PrivateKey decodePEMPrivateKey(byte[] keyBytes, int off, int len, String password) throws IOException;

    /**
     * @param s The {@link InputStream} containing the raw PEM data bytes.
     * <B>Note:</B> stream is assumed to be positioned at the start of the
     * PEM raw data
     * @param okToClose If <code>true</code> then the method will close the
     * input stream regardless of success or failure
     * @param password The password protecting the key data - if {@code null}
     * or empty then data is not password protected
     * @return The decoded {@link PrivateKey}
     * @throws IOException If failed to decode key
     */
    PrivateKey decodePEMPrivateKey(InputStream s, boolean okToClose, String password) throws IOException;
}
