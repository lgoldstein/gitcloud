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
package net.community.chest.gitcloud.facade.backend.git;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;

/**
 * @author Lyor Goldstein
 * @since Sep 12, 2013 9:02:43 AM
 */
public class GitBackendServlet extends GitServlet {
    private static final long serialVersionUID = 1369124756755785887L;
    protected final Log logger;

    public GitBackendServlet() {
        logger = LogFactory.getLog(getClass());
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        RepositoryResolver<HttpServletRequest>  resolver=BackendRepositoryResolver.getInstance();
        if (resolver == null) {
            throw new ServletException("Repository resolver N/A");
        }

        UploadPackFactory<HttpServletRequest>   uploadFactory=BackendUploadPackFactory.getInstance();
        if (uploadFactory == null) {
            throw new ServletException("Backend upload factory N/A");
        }

        ReceivePackFactory<HttpServletRequest>  receiveFactory=BackendReceivePackFactory.getInstance();
        if (receiveFactory == null) {
            throw new ServletException("Backend receive factory N/A");
        }

        setRepositoryResolver(resolver);
        setReceivePackFactory(receiveFactory);
        setUploadPackFactory(uploadFactory);
        super.init(config);
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("service(" + req.getMethod() + ")[" + req.getPathInfo() + "][" + req.getQueryString() + "]");
        }
        
        if (logger.isTraceEnabled()) {
            for (Enumeration<String> hdrs=req.getHeaderNames(); (hdrs != null) && hdrs.hasMoreElements(); ) {
                String  hdrName=hdrs.nextElement(), hdrValue=req.getHeader(hdrName);
                logger.trace("service(" + req.getMethod() + ")[" + req.getPathInfo() + "][" + req.getQueryString() + "]"
                           + " REQ " + hdrName + ": " + hdrValue);
            }
        }

        super.service(req, res);
        
        if (logger.isDebugEnabled()) {
            logger.debug("service(" + req.getMethod() + ")[" + req.getPathInfo() + "][" + req.getQueryString() + "]"
                       + " Content-Type: " + res.getContentType() + ", status=" + res.getStatus());
        }
        
        if (logger.isTraceEnabled()) {
            for (String hdrName : res.getHeaderNames()) {
                String  hdrValue=res.getHeader(hdrName);
                logger.trace("service(" + req.getMethod() + ")[" + req.getPathInfo() + "][" + req.getQueryString() + "]"
                           + " RSP " + hdrName + ": " + hdrValue);
            }
        }
    }
}
