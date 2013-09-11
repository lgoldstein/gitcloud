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

package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

/**
 * A {@link IOFileFilter} that <code>accept</code>s all non-<code>null</code>
 * {@link File}s that are executable
 * @see File#canExecute()
 * @author Lyor G.
 * @since Jun 5, 2013 3:28:15 PM
 */
public class CanExecuteFileFilter extends AbstractFileFilter implements Serializable {
    private static final long serialVersionUID = 771478379808370442L;

    public static final CanExecuteFileFilter    CAN_EXECUTE=new CanExecuteFileFilter();
    public static final IOFileFilter    CANNOT_EXECUTE=new NotFileFilter(CAN_EXECUTE);

    protected CanExecuteFileFilter () {
        super();
    }

    @Override
    public boolean accept(File f) {
        if ((f == null) || (!f.canExecute())) {
            return false;   // debug breakpoint
        } else {
            return true;
        }
    }
}
