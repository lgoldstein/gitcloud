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

package org.apache.commons.io.input;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * Provides a getter for the branch
 * @author Lyor G.
 * @since Sep 25, 2013 8:47:06 AM
 */
public class ExtendedTeeInputStream extends TeeInputStream {
    protected final OutputStream    output;

    public ExtendedTeeInputStream(InputStream input, OutputStream branch) {
        this(input, branch, false);
    }

    public ExtendedTeeInputStream(InputStream input, OutputStream branch, boolean closeBranch) {
        super(Validate.notNull(input, "No input", ArrayUtils.EMPTY_OBJECT_ARRAY),
              Validate.notNull(branch, "No output", ArrayUtils.EMPTY_OBJECT_ARRAY),
              closeBranch);
        output = branch;
    }

    public final OutputStream getBranch() {
        return output;
    }
}
