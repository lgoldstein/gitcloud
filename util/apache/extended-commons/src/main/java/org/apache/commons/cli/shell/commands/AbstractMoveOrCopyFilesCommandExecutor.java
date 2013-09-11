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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.shell.ShellIO;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.ExtendedIOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Lyor G.
 * @since Jun 9, 2013 4:47:43 PM
 *
 */
public abstract class AbstractMoveOrCopyFilesCommandExecutor extends AbstractFilesCommandExecutor {
    protected AbstractMoveOrCopyFilesCommandExecutor (Options cmdOpts) {
        super(cmdOpts);
    }

    protected AbstractMoveOrCopyFilesCommandExecutor (Options cmdOpts, CommandLineParser cmdParser) {
        super(cmdOpts, cmdParser);
    }

    @Override
    public void execute (final ShellIO ioContext, CommandLine cmdLine, Collection<? extends File> srcFiles, File dst) throws IOException {
        if (ExtendedCollectionUtils.isEmpty(srcFiles)) {
            return;
        }
        
        if (ExtendedCollectionUtils.size(srcFiles) > 0) {
            if (dst.exists()) {
                if (!dst.isDirectory()) {
                    throw new UnsupportedOperationException("Target of multiple source files must be a folder: " + ExtendedFileUtils.toString(dst));
                }
            } else {
                if (!dst.mkdirs()) {
                    throw new IOException("Failed to create target folder: " +  ExtendedFileUtils.toString(dst));
                }
            }
        }

        final boolean interactive=cmdLine.hasOption(INTERACTIVE);
        final boolean quiet=cmdLine.hasOption(QUIET);
        final BufferedReader  stdin=interactive ? getSafeInputReader(ioContext) : null;
        try {
            final MutableBoolean keepCopying=new MutableBoolean(true);
            Predicate<Pair<File,File>>  predicate=
                new Predicate<Pair<File,File>>() {
                    private final PrintStream   stdout=ioContext.getStdout();
                    private final MutableBoolean keepAsking=new MutableBoolean(true);
        
                    @Override
                    public boolean evaluate (Pair<File,File> fp) {
                        File    srcFile=fp.getLeft(), dstFile=fp.getRight();
                        if (!interactive) {
                            if (!quiet) {
                                stdout.append('\t').println(ExtendedFileUtils.toString(dstFile));
                            }
                            return true;
                        }
    
                        if (!keepAsking.booleanValue()) {
                            final boolean   result=keepCopying.booleanValue();
                            if (!result) {
                                if (!quiet) {
                                    stdout.append("Skip ").println(ExtendedFileUtils.toString(srcFile));
                                }
                            }
                            return result;
                        }
        
                        if (dstFile.exists()) {
                            return evaluateInteractive("Overwrite", dstFile);
                        } else {
                            return evaluateInteractive("Copy", srcFile);
                        }
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
                                        keepCopying.setValue(false);
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
                
                for (File src : srcFiles) {
                    if (!src.exists()) {
                        throw new FileNotFoundException("Source not found: " + ExtendedFileUtils.toString(src));
                    }

                    execute(ioContext, stdin, cmdLine, src, dst, predicate);
                    
                    if (!keepCopying.booleanValue()) {
                        break;
                    }
                }
        } finally {
            ExtendedIOUtils.close(stdin);
        }
    }

    public abstract void execute (ShellIO ioContext, BufferedReader stdin, CommandLine cmdLine, File src, File dst, Predicate<Pair<File,File>> predicate) throws IOException;

}
