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

package org.apache.commons.net.ssl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.test.AbstractTestSupport;

/**
 * @author Lyor G.
 * @since Sep 1, 2013 8:43:00 AM
 */
public class SSLUtilsDevelopment extends AbstractTestSupport {
    // args[i]: web site to be accessed with "https://"
    protected static void testHTTPSCertificatesTrustship(BufferedReader stdin, PrintStream stdout, String ...args) throws Exception {
        final AtomicReference<PrintStream>  outputHolder=new AtomicReference<PrintStream>(stdout);
        X509TrustManager manager=new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return SSLUtils.EMPTY_CERTIFICATES_ARRAY;
                }
    
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // do nothing
                }
    
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    if (ArrayUtils.isEmpty(chain)) {
                        System.err.append('\t').println("No server chain");
                        return;
                    }

                    PrintStream out=outputHolder.get();
                    for (X509Certificate c : chain) {
                        out.println("===========================================================");
                        out.println(c);
                    }
                }
            };
        SSLContext sslContext=SSLContext.getInstance(SSLUtils.SSL_PROTOCOL);
        // Install the all-trusting trust manager
        sslContext.init(null, new TrustManager[] { manager }, new SecureRandom());
        SSLSocketFactory    factory=sslContext.getSocketFactory();
        File                targetFolder=ensureFolderExists(new File(detectTargetFolder(SSLUtilsDevelopment.class), SSLUtilsDevelopment.class.getSimpleName()));

        for (int argIndex=0; ; argIndex++) {
           String   site=(argIndex < ArrayUtils.getLength(args)) ? args[argIndex] : getval(stdout, stdin, "site (or quit)");
           if (StringUtils.isEmpty(site)) {
               continue;
           }
           
           if (isQuit(site)) {
               break;
           }
           
           try {
               URL  url=new URL("https://" + site);
               HttpsURLConnection   conn=(HttpsURLConnection) url.openConnection();
               try {
                   conn.setHostnameVerifier(SSLUtils.ACCEPT_ALL_HOSTNAME_VERIFIER);
                   conn.setSSLSocketFactory(factory);
                   conn.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5L));
                   conn.setReadTimeout((int) TimeUnit.SECONDS.toMillis(15L));
                   conn.setRequestMethod("GET");

                   File outputFile=new File(targetFolder, url.getHost() + ".txt");
                   outputHolder.set(new PrintStream(outputFile));

                   InputStream  data=conn.getInputStream();
                   try {
                       IOUtils.copy(data, NullOutputStream.NULL_OUTPUT_STREAM);
                   } finally {
                       data.close();
                   }
               } finally {
                   conn.disconnect();
               }
           } catch(Exception e) {
               System.err.append(e.getClass().getSimpleName()).append(": ").println(e.getMessage());
           } finally {
               PrintStream  prev=outputHolder.getAndSet(stdout);
               if (prev != stdout) {
                   prev.close();
               }
           }
       }
    }
    
    //////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
        try {
            testHTTPSCertificatesTrustship(getStdin(), System.out, args);
        } catch(Exception e) {
            System.err.append(e.getClass().getSimpleName()).append(": ").println(e.getMessage());
            e.printStackTrace(System.err);
        }

    }

}
