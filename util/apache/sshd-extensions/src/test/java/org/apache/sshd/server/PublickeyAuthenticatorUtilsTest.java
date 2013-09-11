/*
 * 
 */
package org.apache.sshd.server;

import java.security.PublicKey;
import java.util.Arrays;

import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.test.AbstractSshdTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 2, 2013 3:55:50 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PublickeyAuthenticatorUtilsTest extends AbstractSshdTestSupport {
    private static final ServerSession  MOCK_SESSION=Mockito.mock(ServerSession.class);
    private static final PublicKey  MOCK_KEY=Mockito.mock(PublicKey.class);

    public PublickeyAuthenticatorUtilsTest() {
       super();
    }

    @Test
    public void testAcceptAllAuthenticator() {
        for (String username : Arrays.asList(null, "", "testAcceptAllAuthenticator")) {
            for (PublicKey key : Arrays.asList(null, MOCK_KEY)) {
                assertTrue("user=" + username + "/key=" + key,
                        PublickeyAuthenticatorUtils.ACCEPT_ALL_AUTHENTICATOR.authenticate(username, key, MOCK_SESSION));
            }
        }
    }

    @Test
    public void testRejectAllAuthenticator() {
        for (String username : Arrays.asList(null, "", "testRejectAllAuthenticator")) {
            for (PublicKey key : Arrays.asList(null, MOCK_KEY)) {
                assertFalse("user=" + username + "/key=" + key,
                        PublickeyAuthenticatorUtils.REJECT_ALL_AUTHENTICATOR.authenticate(username, key, MOCK_SESSION));
            }
        }
    }
}
