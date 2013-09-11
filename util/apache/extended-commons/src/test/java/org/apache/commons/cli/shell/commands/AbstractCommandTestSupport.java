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

package org.apache.commons.cli.shell.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.cli.shell.ShellCommandExecutor;
import org.apache.commons.cli.shell.ShellContext;
import org.apache.commons.cli.shell.ShellIO;
import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.io.input.ExtendedCloseShieldInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.test.AbstractTestSupport;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 4:12:10 PM
 */
public abstract class AbstractCommandTestSupport extends AbstractTestSupport {
	protected AbstractCommandTestSupport () {
		super();
	}

    public static final void execute(String testName, ShellCommandExecutor command, String ... args) throws Throwable {
    	ShellContext	context=ShellContext.SYSTEM_SHELL;
    	ShellIO			io=context.getIOContext();
    	PrintStream		out=io.getStdout();
    	out.append(testName).println(':');
    	for (int index=0; index < testName.length(); index++) {
    		out.append('=');
    	}
    	out.println();

        command.execute(context, args);
    }

    public static final void doInteractive(String cmdName, ShellCommandExecutor command) {
        doInteractive(ShellContext.SYSTEM_SHELL, cmdName, command);
    }

    public static final void doInteractive(ShellContext context, String cmdName, ShellCommandExecutor command) {
        doInteractive(context, Collections.singletonMap(cmdName, command));
    }
    
    public static final void doInteractive(Map<String,? extends ShellCommandExecutor> commandsMap) {
        doInteractive(ShellContext.SYSTEM_SHELL, commandsMap);
    }

    public static final void doInteractive(ShellContext context, Map<String,? extends ShellCommandExecutor> commandsMap) {
        ShellIO         io=context.getIOContext();
        BufferedReader  in=new BufferedReader(new InputStreamReader(new ExtendedCloseShieldInputStream(io.getStdin())));
        try {
            doInteractive(in, io.getStdout(), io.getStdErr(), context, commandsMap);
        } finally {
            try {
                in.close();
            } catch(IOException e) {
                throw new IllegalStateException("Failed(" + e.getClass().getSimpleName() + ") to close STDIN: " + e.getMessage(), e);
            }
        }
    }

    public static final void doInteractive(
    		BufferedReader in, PrintStream out, PrintStream err, ShellContext context, Map<String,? extends ShellCommandExecutor> commandsMap) {
        for ( ; ; ) {
            String  cmdLine=StringUtils.trimToEmpty(getval(out, in, "::>"));
            if (StringUtils.isEmpty(cmdLine)) {
                cmdLine = "help";
            }

            if (isQuit(cmdLine)) {
                break;
            }
            
            int             pos=cmdLine.indexOf(' ');
            final String    cmdName;
            if (pos < 0) {
                cmdName = cmdLine;
                cmdLine = "";
            } else {
                cmdName = cmdLine.substring(0, pos);
                cmdLine = cmdLine.substring(pos + 1).trim();
            }

            ShellCommandExecutor    command=commandsMap.get(cmdName);
            if (command == null) {
                err.append("\tUnknown command: ").println(cmdName);
                continue;
            }

            try {
                command.execute(context, StringUtils.split(cmdLine, ' '));
            } catch(Throwable t) {
                err.append(t.getClass().getSimpleName()).append(": ").println(t.getMessage());
            }
        }
    }
    
    //////////////////////////////////////////////////////////////////////////
    
    public static void main(String[] args) {
        final Map<String,ShellCommandExecutor>    commandsMap=
                new TreeMap<String,ShellCommandExecutor>(String.CASE_INSENSITIVE_ORDER) {
            private static final long serialVersionUID = 1L;

            {
                put("ls", ListCommand.DEFAULT_INSTANCE);
                put("cp", CopyCommand.DEFAULT_INSTANCE);
                put("mv", MoveCommand.DEFAULT_INSTANCE);
                put("rm", RemoveCommand.DEFAULT_INSTANCE);
            }
        };
        commandsMap.put("help", new ShellCommandExecutor() {
            @Override
            public void execute (ShellContext context, String[] execArgs) throws Throwable {
                ShellIO ioContext=context.getIOContext();
                final PrintStream stdout=ioContext.getStdout();
                ExtendedMapUtils.forAllEntriesDo(commandsMap, new Closure<Map.Entry<String,ShellCommandExecutor>>() {
                    @Override
                    public void execute (Entry<String,ShellCommandExecutor> e) {
                        stdout.append('\t').append(e.getKey())
                              .append('\t').println(e.getValue().getClass().getSimpleName())
                              ;
                    }
                });
            }
            
        });

        doInteractive(commandsMap);
    }
}
