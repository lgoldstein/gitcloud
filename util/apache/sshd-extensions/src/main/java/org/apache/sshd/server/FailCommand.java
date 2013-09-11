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

package org.apache.sshd.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.output.CloseShieldWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 * A {@link Command} that always fails with a give status code and message
 * @author Lyor G.
 * @since Aug 29, 2013 10:03:04 AM
 */
public class FailCommand extends AbstractCommand {
    private final int       errorCode;
    private final String    errorMsg;

    public FailCommand(int code) {
        this(code, null);
    }

    public FailCommand(int code, String message) {
        errorCode = code;
        errorMsg = message;
    }

    public final int getErrorCode() {
        return errorCode;
    }

    public final String getErrorMessage() {
        return errorMsg;
    }

    @Override
    public void start(Environment env) throws IOException {
        logger.warn("start(" + errorCode + "): " + errorMsg);

        try {
            Writer  err=new CloseShieldWriter(new OutputStreamWriter(getErrorStream()));
            try {
                if (StringUtils.isEmpty(errorMsg)) {
                    err.append("Status code: ").append(String.valueOf(getErrorCode()));
                } else {
                    err.append(errorMsg);
                }
                err.append(SystemUtils.LINE_SEPARATOR);
            } finally {
                err.close();
            }
        } finally {
            ExitCallback    cbExit=getExitCallback();
            if (StringUtils.isEmpty(errorMsg)) {
                cbExit.onExit(errorCode);
            } else {
                cbExit.onExit(errorCode, errorMsg);
            }
        }
    }
}
