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
import org.eclipse.jgit.transport.UploadPack;

/**
 * @author lgoldstein
 */
public class GitUploadPackCommand extends AbstractGitPackCommand {
    public GitUploadPackCommand(Executor executor, SSHRepositoryResolver<? extends AbstractGitCommand> repoResolver) {
        this(executor, repoResolver, ArrayUtils.EMPTY_STRING_ARRAY);
    }
    
    public GitUploadPackCommand(Executor executor, SSHRepositoryResolver<? extends AbstractGitCommand> repoResolver, String ...args) {
        this(executor, repoResolver, ArrayUtils.isEmpty(args) ? Collections.<String>emptyList() : Arrays.asList(args));
    }
    
    public GitUploadPackCommand(Executor executor, SSHRepositoryResolver<? extends AbstractGitCommand> repoResolver, List<String> args) {
        super(executor, GitSmartHttpTools.UPLOAD_PACK, repoResolver, args);
    }

    @Override
    protected void executeCommand(Environment env, Repository repo) throws Exception {
        // TODO create a singleton read from configuration or per-user
//        PackConfig  config=new PackConfig();
//        config.setDeltaCompress(false);
//        config.setThreads(1);

        UploadPack  up=new UploadPack(repo);
        // TODO check what if using the default (i.e., not set)
//        up.setPackConfig(config);
        up.setTimeout(10);  // TODO read from configuration
        up.setBiDirectionalPipe(true); // just making sure even though default
        up.upload(getInputStream(), getOutputStream(), getErrorStream());
    }
}
