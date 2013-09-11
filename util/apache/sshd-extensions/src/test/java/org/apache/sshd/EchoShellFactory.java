/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sshd;

import java.util.concurrent.Executor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.shell.AbstractShellFactory;

/**
 * Create a &quot;shell&quot; that simply echo-es its input to its output
 * @author Lyor G.
 * @since Sep 2, 2013 10:01:46 AM
 */
public class EchoShellFactory extends AbstractShellFactory {
    public static final String  NAME="echo-shell", DEFAULT_EXIT_COMMAND="exit";
    private final String    _exitCmd;
    private final Executor  _executor;

    public EchoShellFactory(Executor executor) {
        this(executor, DEFAULT_EXIT_COMMAND);
    }

    public EchoShellFactory(Executor executor, String exitCmd) {
        super(NAME);
        _executor = Validate.notNull(executor, "No executor", ArrayUtils.EMPTY_OBJECT_ARRAY);
        _exitCmd = Validate.notEmpty(exitCmd, "No exit command", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    public final String getExitCommand() {
        return _exitCmd;
    }

    public final Executor getExecutor() {
        return _executor;
    }

    @Override
    public Command create() {
        return new EchoCommand(getExecutor(), getExitCommand());
    }
}
