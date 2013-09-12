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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public GitController() {
        super();
    }
    
    @RequestMapping(method=RequestMethod.GET)
    public void serveGetRequests(HttpServletRequest req, HttpServletResponse rsp) {
        serveRequest(RequestMethod.GET, req, rsp);
    }
    
    @RequestMapping(method=RequestMethod.POST)
    public void servePostRequests(HttpServletRequest req, HttpServletResponse rsp) {
        serveRequest(RequestMethod.POST, req, rsp);
    }

    private void serveRequest(RequestMethod method, HttpServletRequest req, HttpServletResponse rsp) {
        if (logger.isDebugEnabled()) {
            logger.debug("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]");
        }

        throw new UnsupportedOperationException(method + " " + req.getRequestURI() + "?" + req.getQueryString());
    }
}
