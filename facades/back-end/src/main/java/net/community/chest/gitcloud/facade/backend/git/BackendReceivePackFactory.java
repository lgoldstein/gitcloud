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

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import net.community.chest.gitcloud.facade.git.PackFactory;

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
    public ReceivePack create(C req, Repository db)
            throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        if (logger.isDebugEnabled()) {
            logger.debug("ReceivePack(" + db.getDirectory() + ")");
        }

        ReceivePack receive=new ReceivePack(db);
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
