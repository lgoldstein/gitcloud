/* Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.community.chest.gitcloud.facade.frontend.git;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.ExtendedLogUtils;
import org.apache.commons.net.ssl.SSLUtils;
import org.eclipse.jgit.http.server.GitSmartHttpTools;
import org.eclipse.jgit.lib.Constants;
import org.springframework.context.RefreshedContextAttacher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Lyor Goldstein
 * @since Sep 12, 2013 1:17:34 PM
 */
@Controller
public class GitController extends RefreshedContextAttacher {
    public static final Set<String> ALLOWED_SERVICES=
            Collections.unmodifiableSet(
                    new TreeSet<String>(
                            Arrays.asList(GitSmartHttpTools.UPLOAD_PACK, GitSmartHttpTools.RECEIVE_PACK)));

    public GitController() {
        super();
    }
    
    @RequestMapping(method=RequestMethod.GET)
    public void serveGetRequests(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        serveRequest(RequestMethod.GET, req, rsp);
    }
    
    @RequestMapping(method=RequestMethod.POST)
    public void servePostRequests(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        serveRequest(RequestMethod.POST, req, rsp);
    }

    private void serveRequest(RequestMethod method, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]");
        }

        URL url=resolveTargetRepository(req);
        if (logger.isDebugEnabled()) {
            logger.debug("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                       + " redirected to " + url.toExternalForm());
        }

        HttpURLConnection   conn=openTargetConnection(method, url, req);
        try {
            if (RequestMethod.POST.equals(method)) {
                InputStream postData=req.getInputStream();
                try {
                    OutputStream    postTarget=conn.getOutputStream();
                    try {
                        long    cpyLen=IOUtils.copyLarge(postData, postTarget);
                        if (logger.isTraceEnabled()) {
                            logger.trace("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                       + " copied " + cpyLen + " bytes to " + url.toExternalForm());
                        }
                    } finally {
                        postTarget.close();
                    }
                } finally {
                    postData.close();
                }
            }
            
            int statusCode=conn.getResponseCode();
            if ((statusCode < HttpServletResponse.SC_OK) || (statusCode >= HttpServletResponse.SC_MULTIPLE_CHOICES)) {
                String    rspMsg=conn.getResponseMessage();
                logger.warn("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                          + " bad status code (" + statusCode + ")"
                          + " on redirection to " + url.toExternalForm() + ": " + rspMsg);
                rsp.sendError(statusCode, rspMsg);
            } else {
                rsp.sendError(statusCode);
                
                copyResponseHeadersValues(conn, rsp, "Content-type");

                if (RequestMethod.GET.equals(method)) {
                    InputStream rspData=conn.getInputStream();
                    try {
                        OutputStream    rspTarget=rsp.getOutputStream();
                        try {
                            long    cpyLen=IOUtils.copyLarge(rspData, rspTarget);
                            if (logger.isTraceEnabled()) {
                                logger.trace("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                           + " copied " + cpyLen + " bytes from " + url.toExternalForm());
                            }
                        } finally {
                            rspTarget.close();
                        }
                    } finally {
                        rspData.close();
                    }
                }
            }
        } finally {
            conn.disconnect();
        }
    }
    
    private URL resolveTargetRepository(HttpServletRequest req) throws IOException {
        String  op=req.getParameter("service");
        if (!StringUtils.isEmpty(op)) {
            ExtendedValidate.isTrue(ALLOWED_SERVICES.contains(op), "Unsupported service: %s", op);
        }
        
        String  uriPath=req.getPathInfo(), repoName=extractRepositoryName(uriPath);
        if (StringUtils.isEmpty(repoName)) {
            throw ExtendedLogUtils.thrownLogging(logger, Level.WARNING,
                                                 "resolveTargetRepository(" + uriPath + ")",
                                                 new IllegalArgumentException("Failed to extract repo name from " + uriPath));
        }

        String  query=req.getQueryString();
        if (StringUtils.isEmpty(query)) {
            return new URL("http://localhost:8080/git-backend/git" + uriPath);
        } else {
            return new URL("http://localhost:8080/git-backend/git" + uriPath + "?" + query);
        }
    }
    
    private HttpURLConnection openTargetConnection(RequestMethod method, URL url, HttpServletRequest req) throws IOException {
        HttpURLConnection   conn=(HttpURLConnection) url.openConnection();
        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection    https=(HttpsURLConnection) conn;
            https.setHostnameVerifier(SSLUtils.ACCEPT_ALL_HOSTNAME_VERIFIER);
            https.setSSLSocketFactory(SSLUtils.ACCEPT_ALL_FACTORY.create());
        }

        conn.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5L));    // TODO inject from configuration
        conn.setReadTimeout((int) TimeUnit.SECONDS.toMillis(30L));    // TODO inject from configuration
        conn.setRequestMethod(method.name());
        
        if (RequestMethod.POST.equals(method)) {
            conn.setDoOutput(true);
            copyRequestHeadersValues(req, conn, "Content-Type");
        }

        // TODO copy the "Accept" and "Accept-Encoding" header values from the original request
        copyRequestHeadersValues(req, conn, "Accept", "Accept-Encoding");
        return conn;
    }

    private Map<String,String> copyRequestHeadersValues(HttpServletRequest req, HttpURLConnection conn, String ... headers) {
        return copyRequestHeadersValues(req, conn, ExtendedArrayUtils.asList(headers));
    }
    
    private Map<String,String> copyRequestHeadersValues(HttpServletRequest req, HttpURLConnection conn, Collection<String> headers) {
        if (ExtendedCollectionUtils.isEmpty(headers)) {
            return Collections.emptyMap();
        }
        
        Map<String,String>  hdrsValues=null;
        for (String hdrName : headers) {
            String  hdrValue=req.getHeader(hdrName);
            if (StringUtils.isEmpty(hdrValue)) {
                continue;
            }
            
            conn.setRequestProperty(hdrName, hdrValue);
            
            if (hdrsValues == null) {
                hdrsValues = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
            }
            
            hdrsValues.put(hdrName, hdrValue);
        }
        
        if (hdrsValues == null) {
            return Collections.emptyMap();
        } else {
            return hdrsValues;
        }
    }

    private Map<String,String> copyResponseHeadersValues(HttpURLConnection conn, HttpServletResponse rsp, String ... headers) {
        return copyResponseHeadersValues(conn, rsp, ExtendedArrayUtils.asList(headers));
    }
    
    private Map<String,String> copyResponseHeadersValues(HttpURLConnection conn, HttpServletResponse rsp, Collection<String> headers) {
        if (ExtendedCollectionUtils.isEmpty(headers)) {
            return Collections.emptyMap();
        }
        
        Map<String,String>  hdrsValues=null;
        for (String hdrName : headers) {
            String  hdrValue=conn.getHeaderField(hdrName);
            if (StringUtils.isEmpty(hdrValue)) {
                continue;
            }
            
            rsp.setHeader(hdrName, hdrValue);
            
            if (hdrsValues == null) {
                hdrsValues = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
            }
            
            hdrsValues.put(hdrName, hdrValue);
        }
        
        if (hdrsValues == null) {
            return Collections.emptyMap();
        } else {
            return hdrsValues;
        }
    }

    // TODO move this to some generic util location
    public static final String extractRepositoryName(String uriPath) {
        if (StringUtils.isEmpty(uriPath)) {
            return null;
        }
        
        int gitPos=uriPath.indexOf(Constants.DOT_GIT_EXT);
        if (gitPos <= 0) {
            return null;
        }
        
        int startPos=gitPos;
        for ( ; startPos >= 0; startPos--) {
            if (uriPath.charAt(startPos) == '/') {
                startPos++;
                break;
            }
        }
        
        if (startPos < 0) {
            startPos = 0;   // in case did not start with '/'
        }

        int endPos=gitPos;
        for ( ; endPos < uriPath.length(); endPos++) {
            if (uriPath.charAt(endPos) == '/') {
                break;
            }
        }
        
        String  pureName=uriPath.substring(startPos, endPos);
        if (Constants.DOT_GIT_EXT.equals(pureName)) {
            return null;
        } else {
            return pureName;
        }
    }
}
