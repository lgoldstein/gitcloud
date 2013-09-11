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

package org.apache.sshd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.Executor;

import org.apache.commons.io.input.ExtendedCloseShieldInputStream;
import org.apache.commons.io.output.ExtendedCloseShieldOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;
import org.apache.sshd.server.AbstractCommand;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.RunnableAbstractCommand;

/**
 * Implements a {@link AbstractCommand} that echo-es everything until it
 * receives the pre-determined &quot;exit&quot; command
 * @author Lyor G.
 * @since Sep 2, 2013 10:06:50 AM
 */
public class EchoCommand extends RunnableAbstractCommand {
    public static final String  DEFAULT_EXIT_COMMAND="exit";

    private final String    _exitCmd;

    public EchoCommand(Executor executor) {
        this(executor, DEFAULT_EXIT_COMMAND);
    }

    public EchoCommand(Executor executor, String exitCmd) {
        super(executor);
        _exitCmd = Validate.notEmpty(exitCmd, "No exit command", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    public final String getExitCommand() {
        return _exitCmd;
    }

    @Override
    protected Integer executeCommand(Environment env) throws Throwable {
        BufferedReader  rdr=new BufferedReader(new InputStreamReader(new ExtendedCloseShieldInputStream(getInputStream()), "UTF-8"));
        try {
            Writer  w=new OutputStreamWriter(new ExtendedCloseShieldOutputStream(getOutputStream()), "UTF-8");

            try {
                for (String line=rdr.readLine(); line != null; line=rdr.readLine()) {
                    w.append(line).append(SystemUtils.LINE_SEPARATOR).flush();
                    if (getExitCommand().equals(line.trim())) {
                        break;
                    }
                }
                
                return EXIT_SUCCESS;
            } finally {
                w.close();
            }
        } finally {
            rdr.close();
        }
    }
}
