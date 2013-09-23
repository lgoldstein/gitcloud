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
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import net.community.chest.gitcloud.facade.git.AbstractRepositoryResolver;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.eclipse.jgit.transport.resolver.FileResolver;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.SystemPropertyUtils;

/**
 * @author Lyor Goldstein
 * @since Sep 12, 2013 10:52:46 AM
 */
@Component
@ManagedResource(objectName="net.community.chest.gitcloud.facade.backend.git:name=BackendRepositoryResolver")
public class BackendRepositoryResolver<C> extends AbstractRepositoryResolver<C> {
    public static final String  REPOS_BASE_PROP="gitcloud.backend.repos.dir";
    private static final String REPOS_BASE_INJECTION_VALUE=SystemPropertyUtils.PLACEHOLDER_PREFIX
                                        + REPOS_BASE_PROP
                                        + SystemPropertyUtils.VALUE_SEPARATOR
                                        + ""    // just to make a point...
                                        + SystemPropertyUtils.PLACEHOLDER_SUFFIX;

    // we need this subterfuge since the GitBackendServlet has no injection capabilities
    private static final AtomicReference<RepositoryResolver<?>> holder=new AtomicReference<RepositoryResolver<?>>(null);
    @SuppressWarnings("unchecked")
    public static final <T> RepositoryResolver<T> getInstance() {
        return (RepositoryResolver<T>) holder.get();
    }

    // for unit tests
    @SuppressWarnings("unchecked")
    static final <T> RepositoryResolver<T> clearInstance() {
        return (RepositoryResolver<T>) holder.getAndSet(null);
    }

    private final FileResolver<C>  resolver;
    private final File  reposRoot;

    @Inject
    public BackendRepositoryResolver(@Value(REPOS_BASE_INJECTION_VALUE) String baseDir) {
        this(new File(Validate.notEmpty(baseDir, "No base folder", ArrayUtils.EMPTY_OBJECT_ARRAY)));
    }

    public BackendRepositoryResolver(File baseDir) {
        reposRoot = Validate.notNull(baseDir, "No base folder", ArrayUtils.EMPTY_OBJECT_ARRAY);
        if (reposRoot.exists()) {
            Assert.state(reposRoot.isDirectory(), "Non-folder root: " + reposRoot);
        } else {
            Assert.state(reposRoot.mkdirs(), "Cannot create root folder: " + reposRoot);
        }
        
        resolver = new FileResolver<C>(reposRoot, true);
        logger.info("Base dir: " + ExtendedFileUtils.toString(reposRoot));

        synchronized(holder) {
            Assert.state(holder.get() == null, "Double registered resolver");
            holder.set(this);
        }
    }

    @ManagedAttribute(description="Base repositories folder")
    public String getRepositoriesRootFolder() {
        return reposRoot.getAbsolutePath();
    }

    @Override
    public Repository open(C req, String name)
            throws RepositoryNotFoundException, ServiceNotAuthorizedException,
                   ServiceNotEnabledException, ServiceMayNotContinueException {
        return resolver.open(req, name);
    }
}
