/**
 * 
 */
package com.vmware.devenv.git.ssh;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sshd.server.Environment;
import org.eclipse.jgit.http.server.GitSmartHttpTools;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;

/**
 * @author lgoldstein
 */
public class GitReceivePackCommand extends AbstractGitPackCommand {
    public GitReceivePackCommand(Executor executor, SSHRepositoryResolver<? extends AbstractGitCommand> repoResolver) {
        this(executor, repoResolver, ArrayUtils.EMPTY_STRING_ARRAY);
    }
    
    public GitReceivePackCommand(Executor executor, SSHRepositoryResolver<? extends AbstractGitCommand> repoResolver, String ...args) {
        this(executor, repoResolver, ArrayUtils.isEmpty(args) ? Collections.<String>emptyList() : Arrays.asList(args));
    }
    
    public GitReceivePackCommand(Executor executor, SSHRepositoryResolver<? extends AbstractGitCommand> repoResolver, List<String> args) {
        super(executor, GitSmartHttpTools.RECEIVE_PACK, repoResolver, args);
    }
    
    @Override
    protected void executeCommand(Environment env, Repository repo) throws Exception {
        ReceivePack receive=new ReceivePack(repo);
        receive.setBiDirectionalPipe(true); // just making sure even though default
        receive.setTimeout(10); // TODO read from configuration
        receive.receive(getInputStream(), getOutputStream(), getErrorStream());
    }
}
