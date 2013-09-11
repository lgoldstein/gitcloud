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

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.shell.ShellContext;
import org.apache.commons.cli.shell.ShellIO;
import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 12:39:49 PM
 */
public class ListCommand extends AbstractShellCommandExecutor {
    public static final String LONG_FORMAT = "l", LIST_ALL="a";
    private static final Options options = new Options()
                        .addOption(LONG_FORMAT,false, "Show long details")
                        .addOption(LIST_ALL,false, "Show hidden files as well");
    public static final ListCommand       DEFAULT_INSTANCE=new ListCommand();

    public ListCommand() {
        super(options);
    }

    @Override
    public void execute(ShellContext context, CommandLine cmdLine) throws Throwable {
        execute(context.getIOContext(), cmdLine, resolveMatchingTargets(context, cmdLine));
    }

    public static final void execute(ShellIO ioContext, CommandLine cmdLine, Collection<? extends File> files) {
        execute(ioContext.getStdout(), cmdLine, files);
    }

    public static final void execute(final PrintStream out, CommandLine cmdLine, Collection<? extends File> files) {
        if (ExtendedCollectionUtils.isEmpty(files)) {
            return;
        }

        final Closure<File> formatter;
        if (cmdLine.hasOption(LONG_FORMAT)) {
            formatter = new Closure<File>() {
                @Override
                public void execute(File f) {
                    out.append('\t').append(ExtendedFileUtils.getFileAccess(f))
                       .append('\t').append(new Date(f.lastModified()).toString())
                       .append('\t').append(String.valueOf(f.length()))
                       .append('\t').println(f.getName())
                       ;
                }
            };
        } else {
            formatter = new Closure<File>() {
                @Override
                public void execute(File f) {
                    out.append('\t').append(f.getName()).println(f.isDirectory() ? "/" : "");
                }
            };
        }

        CollectionUtils.forAllDo(files, formatter);
    }
    
    public List<File> resolveMatchingTargets(ShellContext context, CommandLine cmdLine) {
        List<?> args = cmdLine.getArgList();
        int numArgs = ExtendedCollectionUtils.size(args);
        if (numArgs > 1) {
            printHelp(context);
            throw new IllegalArgumentException("Only one target may be specified");
        }

        return resolveMatchingTargets(context.getUserContext(), cmdLine, 0, false, cmdLine.hasOption(LIST_ALL) ? TrueFileFilter.TRUE : ExtendedFileUtils.COMPOUND_NON_HIDDEN_FILE_FILTER);
    }
}
