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
import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.shell.ShellIO;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Lyor G.
 * @since Jun 9, 2013 1:18:24 PM
 */
public class CopyCommand extends AbstractMoveOrCopyFilesCommandExecutor {
    public static final String PRESERVE="p";
    private static final Options options = new Options()
                        .addOption(RECURSIVE, false, "Copy recursively (applicable only for directories)")
                        .addOption(QUIET, false, "Do not display copied files")
                        .addOption(PRESERVE, false, "Preserve copied last-modified timestamp")
                        .addOption(INTERACTIVE, false, "Ask before copying and/or overwriting a file")
                        ;
    public static final CopyCommand DEFAULT_INSTANCE=new CopyCommand();

    public CopyCommand() {
        super(options);
    }

    @Override
    public void execute (ShellIO ioContext, BufferedReader stdin, CommandLine cmdLine, File src, File dst, Predicate<Pair<File,File>> predicate) throws IOException {
        boolean recursive=cmdLine.hasOption(RECURSIVE), preserveFileDate=cmdLine.hasOption(PRESERVE);
        if (src.isDirectory()) {
            if (!recursive) {
                throw new UnsupportedOperationException("Use the -" + RECURSIVE + " option to copy directories");
            }

            ExtendedFileUtils.copyDirectory(src, dst, predicate, preserveFileDate);
        } else if (dst.exists() && dst.isDirectory()) {
            execute(ioContext, stdin, cmdLine, src, new File(dst, src.getName()), predicate);
        } else {
            if (recursive) {
                throw new UnsupportedOperationException("Use the -" + RECURSIVE + " option to only for directories");
            }

            ExtendedFileUtils.copyFile(src, dst, predicate, preserveFileDate);
        }
    }

}
