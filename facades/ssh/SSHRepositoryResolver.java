/**
 * 
 */
package com.vmware.devenv.git.ssh;

import org.apache.sshd.server.Environment;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;

/**
 * @author lgoldstein
 */
public interface SSHRepositoryResolver<C> extends RepositoryResolver<C> {
    Repository open(C req, Environment env)
            throws RepositoryNotFoundException, ServiceNotEnabledException;
}
