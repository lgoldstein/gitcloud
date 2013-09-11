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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.collections15.keyvalue.ExtendedEnumerableKeyedAccessor;
import org.apache.commons.collections15.keyvalue.KeyedAccessUtils;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.test.AbstractSshdTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 2, 2013 2:26:04 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PasswordAuthenticatorUtilsTest extends AbstractSshdTestSupport {
    private static final ServerSession  MOCK_SESSION=Mockito.mock(ServerSession.class);

    public PasswordAuthenticatorUtilsTest () {
        super();
    }

    @Test
    public void testAcceptAllPasswordAuthenticatorOnNullOrEmpty() {
        Collection<String>  values=
                Collections.unmodifiableList(
                        Arrays.asList(null, "", "testAcceptAllPasswordAuthenticatorOnNullOrEmpty"));
        for (String username : values) {
            for (String password : values) {
                assertTrue("user=" + username + "/password=" + password,
                           PasswordAuthenticatorUtils.ACCEPT_ALL_AUTHENTICATOR.authenticate(username, password, MOCK_SESSION));
            }
        }
    }
    
    @Test
    public void testAcceptAllPasswordAuthenticatorOnVariousValues() {
        Collection<String>  values=
                Collections.unmodifiableList(
                        Arrays.asList(getClass().getSimpleName(), "testAcceptAllPasswordAuthenticatorOnVariousValues"));
        for (String username : values) {
            for (String password : values) {
                for (int index=0; index < Byte.SIZE; index++) {
                    String  u=shuffleCase(username), p=shuffleCase(password);
                    assertTrue("user=" + u + "/password=" + p,
                               PasswordAuthenticatorUtils.ACCEPT_ALL_AUTHENTICATOR.authenticate(u, p, MOCK_SESSION));
                }
            }
        }
    }

    @Test
    public void testRejectAllPasswordAuthenticatorOnNullOrEmpty() {
        Collection<String>  values=
                Collections.unmodifiableList(
                        Arrays.asList(null, "", "testRejectAllPasswordAuthenticatorOnNullOrEmpty"));
        for (String username : values) {
            for (String password : values) {
                assertFalse("user=" + username + "/password=" + password,
                            PasswordAuthenticatorUtils.REJECT_ALL_AUTHENTICATOR.authenticate(username, password, MOCK_SESSION));
            }
        }
    }
    
    @Test
    public void testRejectAllPasswordAuthenticatorOnVariousValues() {
        Collection<String>  values=
                Collections.unmodifiableList(
                        Arrays.asList(getClass().getSimpleName(), "testRejectAllPasswordAuthenticatorOnVariousValues"));
        for (String username : values) {
            for (String password : values) {
                for (int index=0; index < Byte.SIZE; index++) {
                    String  u=shuffleCase(username), p=shuffleCase(password);
                    assertFalse("user=" + u + "/password=" + p,
                                PasswordAuthenticatorUtils.REJECT_ALL_AUTHENTICATOR.authenticate(u, p, MOCK_SESSION));
                }
            }
        }
    }

    @Test
    public void testBasicPasswordAuthenticator() {
        final String            USERNAME=getClass().getName(), PASSWORD="Test@testBasicPasswordAuthenticator";
        PasswordAuthenticator   auth=PasswordAuthenticatorUtils.basicPasswordAuthenticator(USERNAME, PASSWORD);
        assertTrue("Configured values not authenticating", auth.authenticate(USERNAME, PASSWORD, MOCK_SESSION));

        Collection<String>  values=
                Collections.unmodifiableList(
                        Arrays.asList(getClass().getSimpleName(), "testBasicPasswordAuthenticator"));
        for (String username : values) {
            assertFalse("Unexpected user mismatch authentication", auth.authenticate(username, PASSWORD, MOCK_SESSION));
            assertFalse("Unexpected password mismatch authentication", auth.authenticate(USERNAME, username, MOCK_SESSION));

            for (String password : values) {
                assertFalse("user=" + username + "/password=" + password,
                            PasswordAuthenticatorUtils.REJECT_ALL_AUTHENTICATOR.authenticate(username, password, MOCK_SESSION));
            }
        }
    }

    @Test
    public void testMappedUsersAuthenticator() throws IOException {
        Properties  props=new Properties();
        InputStream in=getClassResourceAsStream(getClass().getSimpleName() + ".properties");
        try {
            assertNotNull("Test file not found", in);
            props.load(in);
        } finally {
            in.close();
        }
        
        PasswordAuthenticator   auth=PasswordAuthenticatorUtils.mappedUsersAuthenticator(props);
        ExtendedEnumerableKeyedAccessor<String,String>  users=KeyedAccessUtils.keyedAccessor(props);
        for (String username : users.getKeys()) {
            String  password=users.get(username);
            assertTrue("user=" + username + "/password=" + password, auth.authenticate(username, password, MOCK_SESSION));
            assertFalse("(shuffled) user=" + username + "/password=" + password, auth.authenticate(username + username, password, MOCK_SESSION));
            assertFalse("user=" + username + "/(shuffled) password=" + password, auth.authenticate(username, password + password, MOCK_SESSION));
        }
    }
}
