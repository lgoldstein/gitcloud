/**
 * 
 */
package com.vmware.devenv.git.ssh;

import java.util.concurrent.Executor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.AbstractLoggingBean;
import org.apache.sshd.server.CommandFactory;
import org.eclipse.jgit.http.server.GitSmartHttpTools;

/**
 * @author lgoldstein
 */
public class GitCommandFactory extends AbstractLoggingBean implements CommandFactory {
    private final SSHRepositoryResolver<? extends AbstractGitCommand>  _repoResolver;
    private final Executor  _executor;

    public GitCommandFactory(Executor executor, SSHRepositoryResolver<? extends AbstractGitCommand> repoResolver) {
        _repoResolver = Validate.notNull(repoResolver, "No repository resolver", repoResolver);
        _executor = Validate.notNull(executor, "No executor", repoResolver);
    }

    public final Executor getExecutor() {
        return _executor;
    }

    public final SSHRepositoryResolver<? extends AbstractGitCommand> getRepositoryResolver() {
        return _repoResolver;
    }

    @Override
    public AbstractGitCommand createCommand(String cmd) {
        String  command=StringUtils.trim(cmd);
        Validate.notEmpty(command, "No command", ArrayUtils.EMPTY_OBJECT_ARRAY);

        int         cmdPos=command.indexOf(' ');
        String[]    args=ArrayUtils.EMPTY_STRING_ARRAY;
        if (cmdPos > 0) {
            args = StringUtils.split(StringUtils.trim(command.substring(cmdPos + 1)), ' ');
            command = command.substring(0, cmdPos);
        }

        if (GitSmartHttpTools.UPLOAD_PACK.equalsIgnoreCase(command)) {
            return new GitUploadPackCommand(getExecutor(), getRepositoryResolver(), args);
        } else if (GitSmartHttpTools.RECEIVE_PACK.equalsIgnoreCase(command)) {
            return new GitReceivePackCommand(getExecutor(), getRepositoryResolver(), args);
        } else {
            RuntimeException    e=new UnsupportedOperationException("createCommand(" + command + ") N/A");
            logger.error(e.getMessage());
            throw e;
        }
    }
}
