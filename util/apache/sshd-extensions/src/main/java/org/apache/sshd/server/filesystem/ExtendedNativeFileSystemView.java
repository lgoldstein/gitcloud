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
import java.lang.reflect.Field;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ExtendedFieldUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * Allows specifying another root directory other than the user's home (actually
 * overrides via reflection API the original root directory with the provided one)
 * @author Lyor G.
 * @since Sep 10, 2013 11:08:29 AM
 */
public class ExtendedNativeFileSystemView extends NativeFileSystemView {
    // Hack using reflection API since these fields are not exposed
    public static final Field  currDirField=
            Validate.notNull(FieldUtils.getDeclaredField(NativeFileSystemView.class, "currDir", true), "Missing currDir field", ArrayUtils.EMPTY_OBJECT_ARRAY);
    public static final Field  userNameField=
            Validate.notNull(FieldUtils.getDeclaredField(NativeFileSystemView.class, "userName", true), "Missing userName field", ArrayUtils.EMPTY_OBJECT_ARRAY);
    public static final Field  caseInsensitiveField=
            Validate.notNull(FieldUtils.getDeclaredField(NativeFileSystemView.class, "caseInsensitive", true), "Missing caseInsensitive field", ArrayUtils.EMPTY_OBJECT_ARRAY);
    
    public ExtendedNativeFileSystemView(String userName, File rootDir) {
        this(userName, rootDir, false);
    }

    public ExtendedNativeFileSystemView(String userName, String rootDir) {
        this(userName, rootDir, false);
    }

    public ExtendedNativeFileSystemView(String userName, File rootDir, boolean caseInsensitive) {
        this(userName, Validate.notNull(rootDir, "No root dir file specified", ArrayUtils.EMPTY_OBJECT_ARRAY).getAbsolutePath(), caseInsensitive);
    }

    public ExtendedNativeFileSystemView(String userName, String rootDir, boolean caseInsensitive) {
        super(userName, caseInsensitive);

        try {
            FieldUtils.writeField(currDirField, this, Validate.notEmpty(rootDir, "No root dir specified", ArrayUtils.EMPTY_OBJECT_ARRAY));
        } catch(IllegalAccessException e) {
            throw new IllegalStateException("Failed (" + e.getClass().getSimpleName() + ") to override currDir: " + e.getMessage(), e);
        }
    }

    public final String getUsername() {
        try {
            return ExtendedFieldUtils.readTypedField(userNameField, this, String.class);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed (" + e.getClass().getSimpleName() + ") to read userName: " + e.getMessage(), e);
        }
    }

    public final String getCurrDir() {
        try {
            return ExtendedFieldUtils.readTypedField(currDirField, this, String.class);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed (" + e.getClass().getSimpleName() + ") to read currDir: " + e.getMessage(), e);
        }
    }
    
    public final boolean isCaseSensitive() {
        try {
            return ExtendedFieldUtils.readTypedField(caseInsensitiveField, this, Boolean.class).booleanValue();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed (" + e.getClass().getSimpleName() + ") to read currDir: " + e.getMessage(), e);
        }
    }
}
