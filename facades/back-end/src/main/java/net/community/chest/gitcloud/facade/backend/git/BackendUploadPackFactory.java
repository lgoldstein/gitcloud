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
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import net.community.chest.gitcloud.facade.git.PackFactory;

import org.apache.commons.io.HexDumpOutputStream;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.AsciiLineOutputStream;
import org.apache.commons.io.output.LineLevelAppender;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
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
    // TODO Add a unit test to ensure field exists in any future version of jgit
    private static final  Field   pckOutField=
            Validate.notNull(FieldUtils.getDeclaredField(PacketLineOutRefAdvertiser.class, "pckOut", true), "Missing pckOut file", ArrayUtils.EMPTY_OBJECT_ARRAY);

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
    public UploadPack create(final C request, Repository db)
            throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        final File      dir=db.getDirectory();
        final String    logPrefix;
        if (request instanceof HttpServletRequest) {
            HttpServletRequest  req=(HttpServletRequest) request;
            logPrefix = "create(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]";
        } else {
            logPrefix = "create(" + dir.getAbsolutePath() + ")";
        }
        if (logger.isDebugEnabled()) {
            logger.debug(logPrefix + ": " + dir.getAbsolutePath());
        }

        UploadPack up = new UploadPack(db) {
                @Override
                @SuppressWarnings("synthetic-access")
                public void upload(InputStream input, OutputStream output, OutputStream messages) throws IOException {
                    InputStream     effIn=input;
                    OutputStream    effOut=output, effMessages=messages;
                    if (logger.isTraceEnabled()) {
                        LineLevelAppender   inputAppender=new LineLevelAppender() {
                                @Override
                                public void writeLineData(CharSequence lineData) throws IOException {
                                    logger.trace(logPrefix + " upload(C): " + lineData);
                                }
                                
                                @Override
                                public boolean isWriteEnabled() {
                                    return true;
                                }
                            };
                       effIn = new TeeInputStream(effIn, new HexDumpOutputStream(inputAppender), true);

                       LineLevelAppender   outputAppender=new LineLevelAppender() {
                               @Override
                               public void writeLineData(CharSequence lineData) throws IOException {
                                   logger.trace(logPrefix + " upload(S): " + lineData);
                               }
                               
                               @Override
                               public boolean isWriteEnabled() {
                                   return true;
                               }
                           };
                       effOut = new TeeOutputStream(effOut, new HexDumpOutputStream(outputAppender));
                       
                       if (effMessages != null) {
                           LineLevelAppender   messagesAppender=new LineLevelAppender() {
                                   @Override
                                   public void writeLineData(CharSequence lineData) throws IOException {
                                       logger.trace(logPrefix + " upload(M): " + lineData);
                                   }
                                   
                                   @Override
                                   public boolean isWriteEnabled() {
                                       return true;
                                   }
                               };
                           // TODO review the decision to use an AsciiLineOutputStream here
                           effMessages = new TeeOutputStream(effMessages, new AsciiLineOutputStream(messagesAppender));
                       }
                    }

                    super.upload(effIn, effOut, effMessages);
                }

                @Override
                @SuppressWarnings("synthetic-access")
                public void sendAdvertisedRefs(RefAdvertiser adv) throws IOException, ServiceMayNotContinueException {
                    RefAdvertiser   effAdv=adv;
                    if (logger.isTraceEnabled() && (adv instanceof PacketLineOutRefAdvertiser)) {
                        PacketLineOut       pckOut=(PacketLineOut) ReflectionUtils.getField(pckOutField, adv);
                        effAdv = new PacketLineOutRefAdvertiser(pckOut) {
                                private final PacketLineOut pckLog=
                                        new PacketLineOut(  // TODO review the decision to use an AsciiLineOutputStream here
                                                new AsciiLineOutputStream(new LineLevelAppender() {
                                                            @Override
                                                            public void writeLineData(CharSequence lineData) throws IOException {
                                                                logger.trace(logPrefix + " S: " + lineData);
                                                            }
                                                            
                                                            @Override
                                                            public boolean isWriteEnabled() {
                                                                return true;
                                                            }
                                                        }));

                                @Override
                                protected void writeOne(CharSequence line) throws IOException {
                                    String  s=line.toString();
                                    super.writeOne(s);
                                    pckLog.writeString(s);
                                }
    
                                @Override
                                protected void end() throws IOException {
                                    super.end();
                                    pckLog.end();
                                }
                            };
                    }

                    super.sendAdvertisedRefs(effAdv);
                }
            };
        up.setTimeout(uploadTimeoutValue);
        return up;
    }
}
