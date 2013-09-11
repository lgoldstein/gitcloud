/*
 * Copyright 2013 Lyor Goldstein
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sshd.server;

import java.io.IOException;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Runs the command on {@link #start(Environment)} call using an {@link Executor}
 * @author Lyor G.
 * @since Sep 3, 2013 8:44:53 AM
 */
public abstract class RunnableAbstractCommand extends AbstractCommand {
    /**
     * Pre-defined success code that can be used by {@link #executeCommand(Environment)}
     */
    public static final Integer EXIT_SUCCESS=Integer.valueOf(0);

    /**
     * Pre-defined failure code that can be used by {@link #executeCommand(Environment)}
     */
    public static final Integer EXIT_FAIL=Integer.valueOf(-1);

    private final Executor  _executor;

    protected RunnableAbstractCommand(Executor executor) {
        this(executor, (Log) null);
    }

    protected RunnableAbstractCommand(Executor executor, Log log) {
        super(log);
        _executor = Validate.notNull(executor, "No executor", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    protected RunnableAbstractCommand(Executor executor, Class<?> index) {
        this(executor, (index == null) ? "" : index.getSimpleName());
    }

    protected RunnableAbstractCommand(Executor executor, String index) {
        this(executor, null, index);
    }

    protected RunnableAbstractCommand(Executor executor, LogFactory factory) {
        this(executor, factory, (String) null);
    }

    protected RunnableAbstractCommand(Executor executor, LogFactory factory, Class<?> index) {
        this(executor, factory, (index == null) ? "" : index.getSimpleName());
    }

    protected RunnableAbstractCommand(Executor executor, LogFactory factory, String index) {
        super(factory, index);
        _executor = Validate.notNull(executor, "No executor", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }
    
    public final Executor getExecutor() {
        return _executor;
    }

    @Override
    public void start(final Environment env) throws IOException {
        final ExitCallback  cbExit=getExitCallback();
        Executor            executor=getExecutor();
        try {
            executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Integer exitCode=executeCommand(env);
                            if (exitCode != null) {
                                cbExit.onExit(exitCode.intValue());
                            }
                        } catch(Throwable t) {
                            getLogger().warn("Failed (" + t.getClass().getSimpleName() + ") to execute: " + t.getMessage(), t);
                            cbExit.onExit(EXIT_FAIL.intValue(), t.getClass().getName());
                        }
                    }
                });
        } catch(Throwable t) {
            logger.warn("Failed (" + t.getClass().getSimpleName() + ") to submit: " + t.getMessage(), t);
            cbExit.onExit(EXIT_FAIL.intValue(), t.getClass().getName());
        }
    }
    
    /**
     * Called <U>asynchronously</U> by the default {@link #start(Environment)}
     * implementation
     * @param env The {@link Environment} instance
     * @return An {@link Integer} representing the exit code to be used upon
     * return from this call to invoke {@link ExitCallback#onExit(int)}. If
     * {@code null} then this means that this method has invoked the {@code onExit}
     * method and there is no need to invoke it
     * @throws Throwable If failed to execute - in which case {@link ExitCallback#onExit(int)}
     * is invoked with the {@link EXIT_FAIL} value and the fully-qualified
     * exception class name as the message
     */
    protected abstract Integer executeCommand(Environment env) throws Throwable;
}
