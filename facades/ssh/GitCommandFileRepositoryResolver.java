/**
 * 
 */
package com.vmware.devenv.git.ssh;

import java.io.File;
import java.util.List;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedCharSequenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.server.Environment;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.resolver.FileResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;

/**
 * @author lgoldstein
 */
public class GitCommandFileRepositoryResolver<C extends AbstractGitCommand>
        extends FileResolver<C>
        implements SSHRepositoryResolver<C> {
    protected final Log logger;

    public GitCommandFileRepositoryResolver() {
        logger = LogFactory.getLog(getClass());
    }

    public GitCommandFileRepositoryResolver(File basePath, boolean exportAll) {
        super(Validate.notNull(basePath, "No base path", ArrayUtils.EMPTY_OBJECT_ARRAY), exportAll);
        logger = LogFactory.getLog(getClass().getName() + "[" + basePath.getAbsolutePath() + "]");
    }

    public Repository open(C req, Environment env)
            throws RepositoryNotFoundException, ServiceNotEnabledException {
        Validate.notNull(req, "No request", ArrayUtils.EMPTY_OBJECT_ARRAY);

        String  command=req.getCommand(), user=req.getSessionUser();
        if (StringUtils.isEmpty(user)) {
            throw new IllegalArgumentException("open(" + command + ") no user");
        }

        List<String>    args=req.getArguments();
        if (ExtendedCollectionUtils.isEmpty(args)) {
            throw new IllegalArgumentException("open(" + command + ")[" + user + "] no name argummet");
        }
        
        String  name=StringUtils.trim(ExtendedCharSequenceUtils.stripQuotes(args.get(0)).toString());
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("open(" + command + ")[" + user + "] empty name argument");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("open(" + command + ")[" + user + "]: " + name);
        }

        return open(req, name);
    }
}
