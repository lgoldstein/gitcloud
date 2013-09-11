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
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.shell.ShellContext;
import org.apache.commons.cli.shell.ShellIO;
import org.apache.commons.cli.shell.ShellUser;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Lyor G.
 * @since Jun 9, 2013 1:09:59 PM
 */
public abstract class AbstractFilesCommandExecutor extends AbstractShellCommandExecutor {

    protected AbstractFilesCommandExecutor (Options cmdOpts) {
        super(cmdOpts);
    }

    protected AbstractFilesCommandExecutor (Options cmdOpts, CommandLineParser cmdParser) {
        super(cmdOpts, cmdParser);
    }

    @Override
    public void execute(ShellContext context, CommandLine cmdLine) throws Throwable {
        Pair<List<File>,File> files=resolveFileTargets(context, cmdLine);
        execute(context, cmdLine, files.getLeft(), files.getRight());
    }

    public void execute(ShellContext context, CommandLine cmdLine, Collection<? extends File> src, File dst) throws IOException {
        execute(context.getIOContext(), cmdLine, src, dst);
    }

    public abstract void execute(ShellIO ioContext, CommandLine cmdLine, Collection<? extends File> src, File dst) throws IOException;

    public Pair<List<File>,File> resolveFileTargets(ShellContext context, CommandLine cmdLine) {
        List<?> args = cmdLine.getArgList();
        int numArgs = ExtendedCollectionUtils.size(args);
        if (numArgs != 2) {
            printHelp(context);
            throw new IllegalArgumentException("Missing or extra source/destination file(s)");
        }
        
        ShellUser   userContext=context.getUserContext();
        List<File>  src=resolveMatchingTargets(userContext, cmdLine, 0, false, TrueFileFilter.TRUE);
        File        dst=resolveTargetFile(userContext, ExtendedStringUtils.safeToString(args.get(1)));
        return Pair.of(src, dst);
    }
}
