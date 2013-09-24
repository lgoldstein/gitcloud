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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import net.community.chest.gitcloud.facade.git.PackFactory;

import org.apache.commons.io.HexDump;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ByteArrayAccumulatingInputStream;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.AsciiLineOutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.SystemPropertyUtils;

/**
 * @author Lyor Goldstein
 * @since Sep 12, 2013 10:41:52 AM
 */
@Component
public class BackendReceivePackFactory<C> extends PackFactory<C> implements ReceivePackFactory<C> {
    public static final int DEFAULT_RECEIVE_TIMEOUT_SEC=DEFAULT_TIMEOUT_SEC;
    public static final String  RECEIVE_TIMEOUT_SEC_PROP="gitcloud.backend.receive.pack.timeout.sec";
    private static final String RECEIVE_TIMEOUT_SEC_INJECTION_VALUE=SystemPropertyUtils.PLACEHOLDER_PREFIX
                                              + RECEIVE_TIMEOUT_SEC_PROP
                                              + SystemPropertyUtils.VALUE_SEPARATOR
                                              + DEFAULT_RECEIVE_TIMEOUT_SEC
                                              + SystemPropertyUtils.PLACEHOLDER_SUFFIX
                                              ;
    // we need this subterfuge since the GitBackendServlet has no injection capabilities
    private static final AtomicReference<ReceivePackFactory<?>> holder=new AtomicReference<ReceivePackFactory<?>>(null);
    @SuppressWarnings("unchecked")
    public static final <T> ReceivePackFactory<T> getInstance() {
        return (ReceivePackFactory<T>) holder.get();
    }

    private final int receiveTimeoutValue;

    @Inject
    public BackendReceivePackFactory(@Value(RECEIVE_TIMEOUT_SEC_INJECTION_VALUE) int timeoutValue) {
        Assert.state(timeoutValue > 0, "Bad timeout value: " + timeoutValue);
        receiveTimeoutValue = timeoutValue;

        synchronized(holder) {
            Assert.state(holder.get() == null, "Double registered factory");
            holder.set(this);
        }
    }

    @Override
    public ReceivePack create(C request, Repository db)
            throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        final String    logPrefix, encoding;
        if (request instanceof HttpServletRequest) {
            HttpServletRequest  req=(HttpServletRequest) request;
            encoding = req.getHeader("Content-Encoding");
            logPrefix = "create(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]";
        } else {
            encoding = null;
            logPrefix = "create(" + db.getDirectory() + ")";
        }
        if (logger.isDebugEnabled()) {
            logger.debug(logPrefix + ": " +  db.getDirectory());
        }

        ReceivePack receive=new ReceivePack(db) {
            @Override
            @SuppressWarnings("synthetic-access")
            public void receive(InputStream input, OutputStream output, OutputStream messages) throws IOException {
                if (logger.isTraceEnabled()) {
                    ByteArrayAccumulatingInputStream effIn=new ByteArrayAccumulatingInputStream(new CloseShieldInputStream(input));
                    try {
                        OutputStream    effOut=
                                new TeeOutputStream(
                                        new CloseShieldOutputStream(output), new AsciiLineOutputStream() {
                                            @Override
                                            public void writeLineData(CharSequence lineData) throws IOException {
                                                logger.trace(logPrefix + " receive(S): " + lineData);
                                            }
                                            
                                            @Override
                                            public boolean isWriteEnabled() {
                                                return true;
                                            }
                                        });
                        try {
                            super.receive(effIn, effOut, messages);
                        } finally {
                            effOut.close();
                        }
                    } finally {
                        effIn.close();
                    }
                    
                    byte[] gzBytes=effIn.toByteArray();
                    if ("gzip".equalsIgnoreCase(encoding)) {
                        ByteArrayOutputStream   baos=new ByteArrayOutputStream(Math.max(1024, gzBytes.length * 2));
                        try {
                            InputStream gzStream=new GZIPInputStream(new ByteArrayInputStream(gzBytes));
                            try {
                                IOUtils.copyLarge(gzStream, baos);
                            } finally {
                                gzStream.close();
                            }
                        } finally {
                            baos.close();
                        }
                        gzBytes = baos.toByteArray();
                    }
                    
                    AsciiLineOutputStream   logStream=new AsciiLineOutputStream() {
                            @Override
                            public void writeLineData(CharSequence lineData) throws IOException {
                                logger.trace(logPrefix + " receive(C): " + lineData);
                            }
                            
                            @Override
                            public boolean isWriteEnabled() {
                                return true;
                            }
                        };
                    try {
                        HexDump.dump(gzBytes, 0L, logStream, 0);
                    } finally {
                        logStream.close();
                    }
                } else {
                    super.receive(input, output, messages);
                }
            }
        };
        receive.setTimeout(receiveTimeoutValue);
        
        // TODO set pushing user identity for reflog
        // receive.setRefLogIdent(new PersonIdent(user.username, user.username + "@" + origin))
        
        // TODO set advanced options
        // receive.setAllowCreates(user.canCreateRef(repository));
        // receive.setAllowDeletes(user.canDeleteRef(repository));
        // receive.setAllowNonFastForwards(user.canRewindRef(repository));

        // TODO setup the receive hooks
        // receive.setPreReceiveHook(preRcvHook);
        // receive.setPostReceiveHook(postRcvHook);

        return receive;
    }
}
