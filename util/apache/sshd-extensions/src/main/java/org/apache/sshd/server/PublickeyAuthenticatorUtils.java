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

package org.apache.sshd.server;

import java.io.File;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.net.ssh.keys.CryptoKeyEntry;
import org.apache.sshd.server.session.ServerSession;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 2, 2013 3:20:47 PM
 */
public class PublickeyAuthenticatorUtils {
    /**
     * A {@link PublickeyAuthenticator} that accepts all authentication attempts
     */
    public static final PublickeyAuthenticator  ACCEPT_ALL_AUTHENTICATOR=
            new PublickeyAuthenticator() {
                @Override
                public boolean authenticate(String username, PublicKey key, ServerSession session) {
                    return true;
                }
            };

    /**
     * A {@link PublickeyAuthenticator} that rejects all authentication attempts
     */
    public static final PublickeyAuthenticator  REJECT_ALL_AUTHENTICATOR=
            new PublickeyAuthenticator() {
                @Override
                public boolean authenticate(String username, PublicKey key, ServerSession session) {
                    return false;
                }
            };

    /**
     * @param entries A {@link Collection} of {@link CryptoKeyEntry}-ies
     * @return A {@link PublickeyAuthenticator} that matches the received encoded
     * public key bytes to one of the authorized keys published by the user
     * @see CryptoKeyEntry#readAuthorizedKeys(File)
     */
    public static final PublickeyAuthenticator authorizedKeysAuthenticator(Collection<? extends CryptoKeyEntry> entries) {
        final Map<String,? extends Collection<CryptoKeyEntry>>  keysMap=
                ExtendedMapUtils.mapCollectionMultiValues(
                        CryptoKeyEntry.USERNAME_EXTRACTOR, ExtendedCollectionUtils.<CryptoKeyEntry>linkedListFactory(), entries);
        return new PublickeyAuthenticator() {
            @Override
            public boolean authenticate(String username, PublicKey key, ServerSession session) {
                Collection<CryptoKeyEntry>  keySet=keysMap.get(username);
                if (ExtendedCollectionUtils.isEmpty(keySet)) {
                    return false;
                }
                
                final byte[]  keyBytes=key.getEncoded();
                if (ArrayUtils.isEmpty(keyBytes)) {
                    return false;   // TODO consider throwing an exception
                }
                
                CryptoKeyEntry  entry=CollectionUtils.find(keySet, new AbstractExtendedPredicate<CryptoKeyEntry>(CryptoKeyEntry.class) {
                        @Override
                        public boolean evaluate(CryptoKeyEntry e) {
                            byte[]  entryBytes=e.getKeyData();
                            if (Arrays.equals(keyBytes, entryBytes)) {
                                return true;
                            } else {
                                return false;   // debug breakpoint;
                            }
                        }
                    });
                if (entry == null) {
                    return false;
                } else {
                    return true;
                }
            }
        };
    }
}
