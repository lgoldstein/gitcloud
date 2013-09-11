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

package org.apache.commons.cli.shell;

import org.apache.commons.cli.EnvironmentUtils;
import org.apache.commons.collections15.keyvalue.EnumerableKeyedAccessor;
import org.apache.commons.collections15.keyvalue.EnumerableKeyedReader;
import org.apache.commons.collections15.keyvalue.KeyedReader;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 12:51:16 PM
 *
 */
public interface ShellEnvironment {
	KeyedReader<String,String>	getSysprops();
	KeyedReader<String,String>	getEnvironment();
	
	static final ShellEnvironment SYSTEM_ENV=new ShellEnvironment() {
		@Override
		public EnumerableKeyedAccessor<String,String> getSysprops () {
			return EnvironmentUtils.SYSPROPS_READER;
		}
		
		@Override
		public EnumerableKeyedReader<String,String> getEnvironment () {
			return EnvironmentUtils.SYSENV_READER;
		}
	};
}
