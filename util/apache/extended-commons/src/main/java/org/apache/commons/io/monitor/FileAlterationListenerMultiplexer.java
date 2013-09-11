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

package org.apache.commons.io.monitor;

import java.io.File;

/**
 * @author Lyor G.
 * @since Jun 6, 2013 8:30:00 AM
 */
public class FileAlterationListenerMultiplexer implements FileAlterationListener {
    private final Iterable<? extends FileAlterationListener>  listeners;
    
    public FileAlterationListenerMultiplexer (Iterable<? extends FileAlterationListener> alterationListeners) {
        if ((listeners=alterationListeners) == null) {
            throw new IllegalStateException("No listeners");
        }
    }

    public final Iterable<? extends FileAlterationListener> getListeners() {
        return listeners;
    }

    @Override
    public void onStart (FileAlterationObserver observer) {
        for (FileAlterationListener listener : listeners) {
            listener.onStart(observer);
        }
    }

    @Override
    public void onDirectoryCreate (File directory) {
        for (FileAlterationListener listener : listeners) {
            listener.onDirectoryCreate(directory);
        }
    }

    @Override
    public void onDirectoryChange (File directory) {
        for (FileAlterationListener listener : listeners) {
            listener.onDirectoryChange(directory);
        }
    }

    @Override
    public void onDirectoryDelete (File directory) {
        for (FileAlterationListener listener : listeners) {
            listener.onDirectoryDelete(directory);
        }
    }

    @Override
    public void onFileCreate (File file) {
        for (FileAlterationListener listener : listeners) {
            listener.onFileCreate(file);
        }
    }

    @Override
    public void onFileChange (File file) {
        for (FileAlterationListener listener : listeners) {
            listener.onFileChange(file);
        }
    }

    @Override
    public void onFileDelete (File file) {
        for (FileAlterationListener listener : listeners) {
            listener.onFileDelete(file);
        }
    }

    @Override
    public void onStop (FileAlterationObserver observer) {
        for (FileAlterationListener listener : listeners) {
            listener.onStop(observer);
        }
    }
}
