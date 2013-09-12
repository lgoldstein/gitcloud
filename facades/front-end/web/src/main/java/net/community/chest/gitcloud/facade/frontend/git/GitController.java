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
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.ExtendedLogUtils;
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

        // TODO copy the "Accept" and "Accept-Encoding" header values from the original request
        throw new UnsupportedOperationException(method + " " + req.getRequestURI() + "?" + req.getQueryString());
    }
    
    private URL resolveTargetRepository(HttpServletRequest req) throws IOException {
        String  op=Validate.notEmpty(req.getParameter("service"), "Missing service specification", ArrayUtils.EMPTY_OBJECT_ARRAY);
        ExtendedValidate.isTrue(ALLOWED_SERVICES.contains(op), "Unsupported service: %s", op);
        
        String  uriPath=req.getPathInfo(), repoName=extractRepositoryName(uriPath);
        if (StringUtils.isEmpty(repoName)) {
            throw ExtendedLogUtils.thrownLogging(logger, Level.WARNING,
                                                 "resolveTargetRepository(" + uriPath + ")",
                                                 new IllegalArgumentException("Failed to extract repo name from " + uriPath));
        }

        String  query=Validate.notEmpty(req.getQueryString(), "No query value", ArrayUtils.EMPTY_OBJECT_ARRAY);
        return new URL("http://localhost:8080/git-backend/git" + uriPath + "?" + query);
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
