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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import net.community.chest.gitcloud.facade.git.PackFactory;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.SystemPropertyUtils;

/**
 * @author Lyor Goldstein
 * @since Sep 12, 2013 9:19:26 AM
 */
@Component
public class BackendUploadPackFactory<C> extends PackFactory<C> implements UploadPackFactory<C> {
    public static final int DEFAULT_UPLOAD_TIMEOUT_SEC=DEFAULT_TIMEOUT_SEC;
    public static final String  UPLOAD_TIMEOUT_SEC_PROP="gitcloud.backend.upload.pack.timeout.sec";
    private static final String UPLOAD_TIMEOUT_SEC_INJECTION_VALUE=SystemPropertyUtils.PLACEHOLDER_PREFIX
                                              + UPLOAD_TIMEOUT_SEC_PROP
                                              + SystemPropertyUtils.VALUE_SEPARATOR
                                              + DEFAULT_UPLOAD_TIMEOUT_SEC
                                              + SystemPropertyUtils.PLACEHOLDER_SUFFIX
                                              ;
    // we need this subterfuge since the GitBackendServlet has no injection capabilities
    private static final AtomicReference<UploadPackFactory<?>> holder=new AtomicReference<UploadPackFactory<?>>(null);
    @SuppressWarnings("unchecked")
    public static final <T> UploadPackFactory<T> getInstance() {
        return (UploadPackFactory<T>) holder.get();
    }
            
    private final int uploadTimeoutValue;

    @Inject
    public BackendUploadPackFactory(@Value(UPLOAD_TIMEOUT_SEC_INJECTION_VALUE) int timeoutValue) {
        Assert.state(timeoutValue > 0, "Bad timeout value: " + timeoutValue);
        uploadTimeoutValue = timeoutValue;
        
        synchronized(holder) {
            Assert.state(holder.get() == null, "Double registered factory");
            holder.set(this);
        }
    }

    @Override
    public UploadPack create(C req, Repository db)
            throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        final File  dir=db.getDirectory();
        if (logger.isDebugEnabled()) {
            logger.debug("UploadPack(" + dir + ")");
        }

        UploadPack up = new UploadPack(db) {
                @Override
                @SuppressWarnings("synthetic-access")
                public void upload(InputStream input, OutputStream output, OutputStream messages) throws IOException {
                    if (logger.isTraceEnabled()) {
                        
                    } else {
                        super.upload(input, output, messages);
                    }
                }
            };
        up.setTimeout(uploadTimeoutValue);
        return up;
    }
}
