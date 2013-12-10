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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.collections15.AbstractExtendedFactory;
import org.apache.commons.collections15.ExtendedFactory;
import org.apache.commons.io.ExtendedIOUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldReader;
import org.apache.commons.io.input.ExtendedCloseShieldInputStream;
import org.apache.commons.io.output.ExposedBufferOutputStream;

/**
 * @author Lyor G.
 */
public class SSLUtils {
    /**
     * Argument for {@link SSLContext#getInstance(String)}
     */
    public static final String SSL_PROTOCOL="SSL";
    
    /**
     * An empty array of {@link X509Certificate}-s
     */
    public static final X509Certificate[]   EMPTY_CERTIFICATES_ARRAY={ };

    /**
     * A {@link HostnameVerifier} that accepts all incoming hosts
     */
    public static final HostnameVerifier    ACCEPT_ALL_HOSTNAME_VERIFIER=
            new HostnameVerifier()  {        
                @Override
                public boolean verify(String hostname, SSLSession session)   {
                    return true;  
                }
            };
    
    /**
     * A {@link X509TrustManager} that does not validate certificate chains
     */
    public static final X509TrustManager    TRUST_ALL_CERTS_MANAGER=
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(final X509Certificate[] chain, final String authType ) {
                    // do nothing
                }
    
                @Override
                public void checkServerTrusted(final X509Certificate[] chain, final String authType ) {
                    // do nothing
                }
    
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return EMPTY_CERTIFICATES_ARRAY;
                }
            };

    /**
     * Creates a (singleton) {@link SSLSocketFactory} that trusts all certificates
     * @see #TRUST_ALL_CERTS_MANAGER
     */
    public static final ExtendedFactory<SSLSocketFactory>   ACCEPT_ALL_FACTORY=
            new AbstractExtendedFactory<SSLSocketFactory>(SSLSocketFactory.class) {
                @SuppressWarnings("synthetic-access")
				@Override
                public SSLSocketFactory create() {
                    return LazySSLFactoryHolder.ACCEPT_ALL_FACTORY_INSTANCE;
                }
            };

    /**
     * Sets up a {@link HttpsURLConnection} to accept all hosts and certificates
     * @param conn The connection - ignored if <code>null</code>
     * @return Same as input connection
     * @see #ACCEPT_ALL_HOSTNAME_VERIFIER
     * @see #ACCEPT_ALL_HOSTNAME_VERIFIER
     */
    public static final <C extends HttpsURLConnection> C trustAll(C conn) {
        if (conn == null) {
            return null;    // debug breakpoint
        }

        conn.setHostnameVerifier(ACCEPT_ALL_HOSTNAME_VERIFIER);
        conn.setSSLSocketFactory(ACCEPT_ALL_FACTORY.create());
        return conn;
    }

    // Note exactly according to standard but good enough
    public static final String  PEM_CERTIFCATE_START="-BEGIN CERTIFICATE-";
    public static final String  PEM_CERTIFCATE_END="-END CERTIFICATE-";
    public static final String  X509_CERTIFICATE_TYPE="X.509";

    public static final X509Certificate readX509Certificate(URL url) throws IOException {
        return readX509Certificate(url.openStream(), true);
    }

    public static final X509Certificate readX509Certificate(File file) throws IOException {
        Reader  rdr=new FileReader(file);
        try {
            return readX509Certificate(rdr, true);
        } finally {
            rdr.close();
        }
    }

    public static final X509Certificate readX509Certificate(InputStream inStream, boolean okToClose) throws IOException {
        Reader  rdr=new InputStreamReader(ExtendedCloseShieldInputStream.resolveInputStream(inStream, okToClose));
        try {
            return readX509Certificate(rdr, true);
        } finally {
            rdr.close();
        }
    }

    public static final X509Certificate readX509Certificate(Reader r, boolean okToClose) throws IOException {
        BufferedReader  rdr=
                new BufferedReader(CloseShieldReader.resolveReader(r, okToClose));
        try {
            return readX509Certificate(rdr);
        } finally {
            rdr.close();
        }
    }

    public static final X509Certificate readX509Certificate(BufferedReader rdr) throws IOException {
        ExposedBufferOutputStream   baos=null;
        Writer  w=null;
        try {
            for (String line=rdr.readLine(); line != null; line=rdr.readLine()) {
                line = line.trim();
                
                if (w == null) { // have we started accumulating the data ?
                    if (line.contains(PEM_CERTIFCATE_START)) {
                        baos = new ExposedBufferOutputStream(ExtendedIOUtils.DEFAULT_BUFFER_SIZE_VALUE);
                        w = new OutputStreamWriter(baos);
                    }
                }

                if (w != null) {
                    w.append(line).append(IOUtils.LINE_SEPARATOR);
                }

                if (line.contains(PEM_CERTIFCATE_END)) {
                    break;
                }
            }
        } finally {
            ExtendedIOUtils.closeAll(w, baos);
        }
        
        if (baos == null) {
            throw new StreamCorruptedException("Malformed certificate contents");
        }
        
        try {
            CertificateFactory      factory=CertificateFactory.getInstance(X509_CERTIFICATE_TYPE);
            ByteArrayInputStream    inStream=new ByteArrayInputStream(baos.getBuffer(), 0, baos.size());
            try {
                Certificate cert=factory.generateCertificate(inStream);
                if (!(cert instanceof X509Certificate)) {
                    throw new StreamCorruptedException("Non-X509 certificate: " + cert); 
                }
                
                return (X509Certificate) cert;
            } finally {
                inStream.close();
            }
        } catch(CertificateException e) {
            throw new IOException(e);
        }
    }

    private static class LazySSLFactoryHolder {
        private static final SSLSocketFactory   ACCEPT_ALL_FACTORY_INSTANCE=createAcceptAllSSLFactory();
        private static final SSLSocketFactory createAcceptAllSSLFactory() {
            try {
                SSLContext sslContext=SSLContext.getInstance(SSL_PROTOCOL);
                // Install the all-trusting trust manager
                sslContext.init(null, new TrustManager[] { TRUST_ALL_CERTS_MANAGER }, new SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                return sslContext.getSocketFactory();
            } catch(GeneralSecurityException e) {
                throw new IllegalStateException("Failed (" + e.getClass().getSimpleName() + ") to create SSL socket factory: " + e.getMessage(), e);
            }
        }
    }
}
