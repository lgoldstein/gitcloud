/*
 * 
 */
package org.apache.commons.net.ssl;

import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLSession;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

/**
 * @author Lyor G.
 * @since Jun 25, 2013 12:34:11 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SSLUtilsTest extends AbstractTestSupport {
    public SSLUtilsTest () {
        super();
    }

    @Test
    public void testAcceptAllHostnameVerifier() {
        SSLSession  session=Mockito.mock(SSLSession.class);
        assertTrue("Null hostname", SSLUtils.ACCEPT_ALL_HOSTNAME_VERIFIER.verify(null, session));
        assertTrue("Empty hostname", SSLUtils.ACCEPT_ALL_HOSTNAME_VERIFIER.verify("", session));
        assertTrue("Null session", SSLUtils.ACCEPT_ALL_HOSTNAME_VERIFIER.verify("royl", null));
        assertTrue("Both valid", SSLUtils.ACCEPT_ALL_HOSTNAME_VERIFIER.verify("dlog", session));
    }
    
    @Test
    public void testLoadX509Certificate() throws IOException {
        URL url=getClassResource("SSLUtilsTest-X509-cert.pem");
        assertNotNull("Missing test data file", url);
        
        X509Certificate cert=SSLUtils.readX509Certificate(url);
        assertNotNull("No certificate read", cert);
    }
}
