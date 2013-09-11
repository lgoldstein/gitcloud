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
