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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.common.AbstractNamedFactory;
import org.apache.sshd.server.Command;

/**
 * @author Lyor G.
 * @since Sep 2, 2013 9:59:51 AM
 */
public abstract class AbstractShellFactory extends AbstractNamedFactory<Command> {
    protected AbstractShellFactory(Class<?> name) {
        super(name);
    }

    protected AbstractShellFactory(Log log, Class<?> name) {
        super(log, name);
    }

    protected AbstractShellFactory(Log log, String name) {
        super(log, name);
    }

    protected AbstractShellFactory(LogFactory factory, Class<?> name) {
        super(factory, name);
    }

    protected AbstractShellFactory(LogFactory factory, String name) {
        super(factory, name);
    }

    protected AbstractShellFactory(String name) {
        super(name);
    }

}
