/**
 * 
 */
package com.vmware.devenv.git.ssh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.sshd.common.Session.AttributeKey;
import org.apache.sshd.server.RunnableAbstractCommand;
import org.apache.sshd.server.session.ServerSession;

/**
 * @author lgoldstein
 */
public abstract class AbstractGitCommand extends RunnableAbstractCommand {
    /**
     * Special session key used to store the actual user executing the command
     */
    public static final String  USERID_KEY="userid";
    public static final AttributeKey<String>    USER_ID_ATTRIBUTE=new AttributeKey<String>() {
            @Override
            public String toString() {
                return USERID_KEY;
            }
        };

    private final String        _command;
    private final List<String>  _args;

    protected AbstractGitCommand(Executor executor, String command) {
        this(executor, command, ArrayUtils.EMPTY_STRING_ARRAY);
    }

    protected AbstractGitCommand(Executor executor, String command, String ... args) {
        this(executor, command, ArrayUtils.isEmpty(args) ? Collections.<String>emptyList() : Arrays.asList(args));
    }

    protected AbstractGitCommand(Executor executor, String command, List<String> args) {
        super(executor, Validate.notEmpty(command, "No command specified", ArrayUtils.EMPTY_OBJECT_ARRAY));
        _command = command;
        _args = ((args == null) || args.isEmpty())
                ? Collections.<String>emptyList()  
                : Collections.unmodifiableList(new ArrayList<String>(args))
                ;
    }

    public final String getCommand() {
        return _command;
    }

    public final List<String> getArguments() {
        return _args;
    }

    public String getSessionUser() {
        ServerSession   session=getSession();
        if (session == null) {
            return null;
        } else {
            return session.getAttribute(AbstractGitCommand.USER_ID_ATTRIBUTE);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getCommand() + "]: " + getArguments();
    }
}
