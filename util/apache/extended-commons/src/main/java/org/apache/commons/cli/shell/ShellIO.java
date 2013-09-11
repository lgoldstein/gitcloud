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

package org.apache.commons.cli.shell;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 12:49:36 PM
 */
public interface ShellIO {
	InputStream getStdin();
	PrintStream getStdout();
	PrintStream getStdErr();
	
	static final ShellIO SYSTEM_IO=new ShellIO() {
		@Override
		public InputStream getStdin () {
			return System.in;
		}

		@Override
		public PrintStream getStdout () {
			return System.out;
		}

		@Override
		public PrintStream getStdErr () {
			return System.out;
		}
	};
}
