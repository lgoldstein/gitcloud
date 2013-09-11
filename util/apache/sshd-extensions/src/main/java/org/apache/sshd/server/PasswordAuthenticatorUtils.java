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

import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections15.keyvalue.KeyedAccessUtils;
import org.apache.commons.collections15.keyvalue.KeyedReader;
import org.apache.sshd.server.session.ServerSession;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 2, 2013 2:18:16 PM
 *
 */
public class PasswordAuthenticatorUtils {
    /**
     * A {@link PasswordAuthenticator} that accepts all authentication requests
     */
    public static final PasswordAuthenticator   ACCEPT_ALL_AUTHENTICATOR=
            new PasswordAuthenticator() {
                @Override
                public boolean authenticate (String username, String password, ServerSession session) {
                    return true;
                }
            };
    /**
     * A {@link PasswordAuthenticator} that rejects all authentication requests
     */
    public static final PasswordAuthenticator   REJECT_ALL_AUTHENTICATOR=
            new PasswordAuthenticator() {
                @Override
                public boolean authenticate (String username, String password, ServerSession session) {
                    return false;
                }
            };
    /**
     * @param username The expected username
     * @param password The expected password
     * @return A {@link PasswordAuthenticator} that accepts only the specified
     * username, password
     */
    public static final PasswordAuthenticator basicPasswordAuthenticator(final String username, final String password) {
        return new PasswordAuthenticator() {
            @Override
            public boolean authenticate (String u, String p, ServerSession session) {
                if (u.equals(username) && p.equals(password)) {
                    return true;
                } else {
                    return false;   // debug breakpoint
                }
            }
        };
    }

    /**
     * @param usersMap The users map as a {@link Properties} object where
     * key=user name, value=expected password
     * @return A {@link PasswordAuthenticator} that authenticates using the
     * provided users map
     */
    public static final PasswordAuthenticator mappedUsersAuthenticator(final Properties usersMap) {
        return mappedUsersAuthenticator(KeyedAccessUtils.keyedAccessor(usersMap));
    }

    /**
     * @param usersMap The users map as a {@link Map} object where
     * key=user name, value=expected password
     * @return A {@link PasswordAuthenticator} that authenticates using the
     * provided users map
     */
    public static final PasswordAuthenticator mappedUsersAuthenticator(final Map<String,String> usersMap) {
        return mappedUsersAuthenticator(KeyedAccessUtils.keyedAccessor(usersMap));
    }

    /**
     * @param usersMap The users map as a {@link KeyedReader} object where
     * key=user name, value=expected password
     * @return A {@link PasswordAuthenticator} that authenticates using the
     * provided users map
     */
    public static final PasswordAuthenticator mappedUsersAuthenticator(final KeyedReader<String,String> usersMap) {
        return new PasswordAuthenticator() {
            @Override
            public boolean authenticate (String u, String p, ServerSession session) {
                String  password=usersMap.get(u);
                if (password == null) {
                    return false;   // no such user
                } else if (p.equals(password)) {
                    return true;
                } else {
                    return false;   // debug breakpoint
                }
            }
        };
    }
}
