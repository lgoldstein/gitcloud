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

import org.apache.commons.cli.ExtendedUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jun 4, 2013 2:52:08 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ListCommandTest extends AbstractCommandTestSupport {
    private static final ListCommand     command=new ListCommand();

    public ListCommandTest() {
        super();
    }

    @Test
    public void testShortFormat() throws Throwable {
    	execute("testShortFormat", ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testLongFormat() throws Throwable {
        execute("testLongFormat", ExtendedUtil.asShortOption(ListCommand.LONG_FORMAT));
    }

    @Test
    public void testLongFormatAll() throws Throwable {
        execute("testLongFormatAll", ExtendedUtil.asShortOption(ListCommand.LONG_FORMAT), ExtendedUtil.asShortOption(ListCommand.LIST_ALL));
    }

    @Test
    public void testHelp() throws Throwable {
    	execute("testHelp", ExtendedUtil.asLongOption(AbstractShellCommandExecutor.HELP_NAME));
    }

    private static void execute(String testName, String ... args) throws Throwable {
    	execute(testName, command, args);
    }
    
    public static final void main(String[] args) {
        doInteractive("ls", command);
    }
}
