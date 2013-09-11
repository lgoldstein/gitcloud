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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.shell.ShellCommandExecutor;
import org.apache.commons.cli.shell.ShellContext;
import org.apache.commons.cli.shell.ShellIO;
import org.apache.commons.cli.shell.ShellUser;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.ExtendedFilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.input.ExtendedCloseShieldInputStream;
import org.apache.commons.io.output.ExtendedCloseShieldOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 12:40:20 PM
 */
public abstract class AbstractShellCommandExecutor implements ShellCommandExecutor {
	public static final String	HELP_NAME="help";
	public static final Option	HELP_OPTION=new Option(null, HELP_NAME, false, "Shows this help message");
    public static final String RECURSIVE="r", INTERACTIVE="i", QUIET="q", VERBOSE="v";

	private final Options	options;
	private final CommandLineParser	parser;

	protected AbstractShellCommandExecutor(Options cmdOpts) {
		this(cmdOpts, new PosixParser());
	}

	protected AbstractShellCommandExecutor(Options cmdOpts, CommandLineParser cmdParser) {
		if (cmdOpts == null) {
			throw new IllegalStateException("No options specified");
		}
		options = cmdOpts.addOption(HELP_OPTION);
		if ((parser=cmdParser) == null) {
			throw new IllegalStateException("No parser specified");
		}
	}
	
	public final Options getOptions() {
		return options;
	}

	public final CommandLineParser getCommandLineParser() {
		return parser;
	}

    @Override
    public void execute(ShellContext context, String[] args) throws Throwable {
        execute(context, true, args);
    }

	public void execute (ShellContext context, boolean stopAtNonOption, String ... args) throws Throwable {
		CommandLineParser	cmdParser=getCommandLineParser();
		CommandLine			cmdLine=cmdParser.parse(getOptions(), args, stopAtNonOption);
		if (cmdLine.hasOption(HELP_NAME)) {
		    printHelp(context);
		}
		
	    execute(context, cmdLine);
	}

	public void printHelp(ShellContext context) {
	    printHelp(context.getIOContext());
	}

    public void printHelp(ShellIO ioContext) {
        HelpFormatter   formatter=new HelpFormatter();
        PrintWriter     pw=new PrintWriter(new ExtendedCloseShieldOutputStream(ioContext.getStdout()), true);
        try {
            formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH,
                                getClass().getSimpleName(), "", getOptions(),
                                HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD,
                                "", true);
        } finally {
            pw.close();
        }
    }

	public abstract void execute(ShellContext context, CommandLine cmdLine) throws Throwable;
	
	public static final List<File> resolveMatchingTargets(
	        ShellUser userContext, CommandLine cmdLine, int argIndex, boolean includeParentFolder, IOFileFilter regularFilter) {
        List<?> args = cmdLine.getArgList();
        int numArgs = ExtendedCollectionUtils.size(args);
        File    targetFile = null;
        String  pathArg = null;
        IOFileFilter    wildcardFilter = null;
        if (numArgs > argIndex) {
            pathArg = ExtendedStringUtils.safeToString(args.get(argIndex));

            int wildcardPos=StringUtils.indexOfAny(pathArg, "*?");
            if (wildcardPos >= 0) {
                int sepPos=StringUtils.lastIndexOfAny(pathArg, "/", "\\");
                if (sepPos >= wildcardPos) {
                    throw new IllegalArgumentException("resolveMatchingTargets(" + pathArg + ") wildcards can only be used as the last component");
                }
                
                wildcardFilter = new WildcardFileFilter((sepPos >= 0) ? pathArg.substring(sepPos + 1) : pathArg);
                pathArg = (sepPos > 0) ? pathArg.substring(0, sepPos) : null;
            }
        }

        targetFile = resolveTargetFile(userContext, pathArg);
        if (targetFile.isFile()) {
            if (wildcardFilter != null) {
                throw new IllegalArgumentException("resolveMatchingTargets(" + ExtendedFileUtils.toString(targetFile) + ") wildcards not allowed");
            }
            
            return Collections.singletonList(targetFile);
        } else if (targetFile.isDirectory()) {
            File[]  matches=targetFile.listFiles((FileFilter) ((wildcardFilter == null) ? regularFilter : new AndFileFilter(wildcardFilter, regularFilter)));
            if (includeParentFolder && (wildcardFilter == null)) {
                if (ArrayUtils.isEmpty(matches)) {
                    return Collections.singletonList(targetFile);
                } else {
                    List<File>  files=new ArrayList<File>(matches.length);
                    files.addAll(Arrays.asList(matches));
                    files.add(targetFile);
                    return files;
                }
            } else {
                return ExtendedArrayUtils.asList(matches);
            }
        } else {
            throw new IllegalArgumentException("resolveMatchingTargets(" + ExtendedFileUtils.toString(targetFile) + ") unknown target");
        }
	}

	public static final File resolveTargetFile(ShellUser userContext, CommandLine cmdLine, int argIndex) {
        List<?> args = cmdLine.getArgList();
        int numArgs = ExtendedCollectionUtils.size(args);
        if (numArgs <= argIndex) {
            return resolveTargetFile(userContext, null);
        } else {
        	return resolveTargetFile(userContext, ExtendedStringUtils.safeToString(args.get(argIndex)));
        }
	}

	public static final File resolveTargetFile(ShellUser userContext, String pathArg) {
		return resolveTargetFile(userContext.getCurrentWorkingDirectory(), pathArg);
	}

	public static final File resolveTargetFile(String cwd, String pathArg) {
		if (StringUtils.isEmpty(pathArg)) {
			return new File(cwd);
		} else {
			return new File(ExtendedFilenameUtils.resolveAbsolutePath(cwd, pathArg.replace('/', File.separatorChar)));
		}
	}

	public static final BufferedReader getSafeInputReader(ShellIO ioContext) {
	    return new BufferedReader(new InputStreamReader(new ExtendedCloseShieldInputStream(ioContext.getStdin())));
	}
	
   public static final String getval (final PrintStream out, final BufferedReader in, final String prompt) throws IOException {
        out.append(prompt).print(": ");
        return in.readLine();
    }
}
