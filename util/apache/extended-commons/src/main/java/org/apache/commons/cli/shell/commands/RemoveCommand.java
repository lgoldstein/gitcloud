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

package org.apache.commons.cli.shell.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.shell.ShellContext;
import org.apache.commons.cli.shell.ShellIO;
import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedPredicate;
import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.ExtendedIOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

/**
 * @author Lyor G.
 * @since Jun 10, 2013 7:53:30 AM
 */
public class RemoveCommand extends AbstractShellCommandExecutor {
    private static final Options options = new Options()
                                    .addOption(RECURSIVE, false, "Delete folder(s) recursively")
                                    .addOption(INTERACTIVE, false, "Prompt before removing")
                                    .addOption(QUIET, false, "Do not display removed files")
                                    ;
    public static final RemoveCommand       DEFAULT_INSTANCE=new RemoveCommand();

    public RemoveCommand() {
        super(options);
    }
    
    @Override
    public void execute (ShellContext context, CommandLine cmdLine) throws Throwable {
        execute(context.getIOContext(), cmdLine, resolveMatchingTargets(context, cmdLine));
    }

    public void execute(final ShellIO ioContext, CommandLine cmdLine, Collection<? extends File> files) throws Throwable {
        if (ExtendedCollectionUtils.isEmpty(files)) {
            return;
        }

        final boolean interactive=cmdLine.hasOption(INTERACTIVE);
        final boolean quiet=cmdLine.hasOption(QUIET);
        final BufferedReader  stdin=interactive ? getSafeInputReader(ioContext) : null;
        try {
            final MutableBoolean    keepRemoving=new MutableBoolean(true);
            ExtendedPredicate<File> predicate=new AbstractExtendedPredicate<File>(File.class) {
                private final PrintStream   stdout=ioContext.getStdout();
                private final MutableBoolean keepAsking=new MutableBoolean(true);

                @Override
                public boolean evaluate(File file) {
                    if (!interactive) {
                        if (!quiet) {
                            stdout.append('\t').println(ExtendedFileUtils.toString(file));
                        }
                        return true;
                    }
                    if (!keepAsking.booleanValue()) {
                        final boolean   result=keepRemoving.booleanValue();
                        if (!result) {
                            if (!quiet) {
                                stdout.append("Skip ").println(ExtendedFileUtils.toString(file));
                            }
                        }
                        return result;
                    }

                    return evaluateInteractive("Remove", file);
                }
                
                private boolean evaluateInteractive(String action, File fileArg) {
                    for (final String prompt=action + " [y]/n/q/a: " + ExtendedFileUtils.toString(fileArg) ; ; ) {
                        try {
                            String  ans=getval(stdout, stdin, prompt);
                            if (StringUtils.isEmpty(ans)) {
                                ans = "y";
                            }
    
                            char    op=Character.toLowerCase(ans.charAt(0));
                            switch(op) {
                                case 'y'    :
                                    return true;

                                case 'n'    :
                                    return false;
    
                                case 'q'    :
                                    keepAsking.setValue(false);
                                    keepRemoving.setValue(false);
                                    return false;

                                case 'a'    :
                                    keepAsking.setValue(false);
                                    return true;

                                default     : // do nothing - ask again
                            }
                        } catch(IOException e) {
                            throw new RuntimeException(e.getClass().getSimpleName() + " while prompt " + ExtendedFileUtils.toString(fileArg) + ": " + e.getMessage());
                        }
                    }
                }
            };
            
            boolean recursive=cmdLine.hasOption(RECURSIVE);
            for (File file : files) {
                if (file.isDirectory() && (!recursive)) {
                    printHelp(ioContext);
                    throw new IllegalArgumentException("Use -" + RECURSIVE + " option for folder: " + ExtendedFileUtils.toString(file));
                }
                ExtendedFileUtils.deleteFile(file, predicate);
                
                if (!keepRemoving.booleanValue()) {
                    break;
                }
            }
        } finally {   
            ExtendedIOUtils.close(stdin);
        }
    }

    public List<File> resolveMatchingTargets(ShellContext context, CommandLine cmdLine) {
        List<?> args = cmdLine.getArgList();
        int numArgs = ExtendedCollectionUtils.size(args);
        if (numArgs > 1) {
            printHelp(context);
            throw new IllegalArgumentException("Only one target may be specified");
        }

        return resolveMatchingTargets(context.getUserContext(), cmdLine, 0, true, TrueFileFilter.TRUE);
    }
}
