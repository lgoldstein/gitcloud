/**
 * 
 */
package com.vmware.devenv.git.ssh;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.sshd.server.Environment;
import org.eclipse.jgit.lib.Repository;

/**
 * @author lgoldstein
 * see https://www.kernel.org/pub/software/scm/git/docs/v1.7.0.5/technical/pack-protocol.txt
 */
public abstract class AbstractGitPackCommand extends AbstractGitCommand {
    private final SSHRepositoryResolver<? extends AbstractGitCommand>  _repoResolver;

    protected AbstractGitPackCommand(Executor executor, String command, SSHRepositoryResolver<? extends AbstractGitCommand> repoResolver) {
        this(executor, command, repoResolver, ArrayUtils.EMPTY_STRING_ARRAY);
    }

    protected AbstractGitPackCommand(Executor executor, String command, SSHRepositoryResolver<? extends AbstractGitCommand> repoResolver, String... args) {
        this(executor, command, repoResolver, ArrayUtils.isEmpty(args) ? Collections.<String>emptyList() : Arrays.asList(args));
    }

    protected AbstractGitPackCommand(Executor executor, String command, SSHRepositoryResolver<? extends AbstractGitCommand> repoResolver, List<String> args) {
        super(executor, command, args);
        _repoResolver = Validate.notNull(repoResolver, "No repository resolver", repoResolver);
    }

    public final SSHRepositoryResolver<? extends AbstractGitCommand> getRepositoryResolver() {
        return _repoResolver;
    }

    @Override
    protected Integer executeCommand(Environment env) throws Throwable {
        @SuppressWarnings("unchecked")
        SSHRepositoryResolver<AbstractGitCommand>    resolver=
                (SSHRepositoryResolver<AbstractGitCommand>) getRepositoryResolver();
        Repository                                   repo;
        try {
            if ((repo=resolver.open(this, env)) == null) {
                throw new FileNotFoundException("No matching repository");
            }
        } catch(Throwable t) {
            logger.warn("start(" + getCommand() + ")" + getArguments()
                     + " failed (" + t.getClass().getSimpleName() + ")"
                     + " to resolve repository: " + t.getMessage(),
                        t);
            throw t;
        }

        executeCommand(env, repo);
        return EXIT_SUCCESS;
    }

    protected void executeCommand(Environment env, Repository repo) throws Throwable {
        throw new UnsupportedOperationException("Not implemented");
    }
}
