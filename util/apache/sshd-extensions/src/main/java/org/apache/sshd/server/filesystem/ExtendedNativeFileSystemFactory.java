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

package org.apache.sshd.server.filesystem;

import java.io.File;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.Validate;
import org.apache.sshd.common.Session;
import org.apache.sshd.server.FileSystemView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension for {@link NativeFileSystemFactory} that allows selecting
 * the root folder for the session
 * @author Lyor G.
 * @since Sep 10, 2013 11:17:17 AM
 */
public abstract class ExtendedNativeFileSystemFactory extends NativeFileSystemFactory {
    protected final Logger logger;
    
    protected ExtendedNativeFileSystemFactory() {
        logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public FileSystemView createFileSystemView(Session session) {
        String  userName=session.getUsername();
        File    rootDir=Validate.notNull(getSessionRootDir(session), "No root folder resolved", ArrayUtils.EMPTY_OBJECT_ARRAY);
        if (rootDir.exists()) {
            ExtendedValidate.isTrue(rootDir.isDirectory(), "Not a directory: %s", rootDir);
        } else if (isCreateHome()) {
            if (!rootDir.mkdirs()) {
                throw new IllegalStateException("Failed to create root directory: " + rootDir);
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("createFileSystemView(" + userName + ") created " + rootDir);
            }
        } else {
            throw new IllegalStateException("Resolved root directory not found: " + rootDir);
        }
        
        return new ExtendedNativeFileSystemView(userName, rootDir, isCaseInsensitive());
    }

    protected abstract File getSessionRootDir(Session session);
}
