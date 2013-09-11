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

package org.apache.sshd.server.shell;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.FailCommand;

/**
 * A {@link NamedFactory} that can be used as a &quot;null&quot; shell - i.e.,
 * it fails any attempt to run a command on it
 * @author Lyor G.
 * @since Aug 29, 2013 9:50:09 AM
 */
public class NullShellFactory extends AbstractShellFactory {
    public static final String  NAME="null-shell";
    public static final NullShellFactory    INSTANCE=new NullShellFactory();

    public NullShellFactory() {
        super(NAME);
    }

    @Override
    public Command create() {
        return new FailCommand(-1, NAME);
    }
}
