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

package org.apache.commons.io17.monitor;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerMultiplexer;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.io.monitor.FileEntry;

/**
 * An extension to the {@link FileAlterationObserver} that uses JDK 1.7 API(s)
 * to monitor changes to files and/or directories.
 * @author Lyor G.
 * @since Jun 5, 2013 4:00:44 PM
 */
public class FileAlterationWatchdog extends FileAlterationObserver implements Closeable {
    private static final long serialVersionUID = 8256703041177411019L;
    private WatchService watcher;
    private WatchKey    watchKey;
    private final Path    dirPath;

    public FileAlterationWatchdog(String directoryName) {
        this(new File(directoryName));
    }

    public FileAlterationWatchdog(String directoryName, FileFilter fileFilter) {
        this(new File(directoryName), fileFilter);
    }

    public FileAlterationWatchdog(String directoryName, FileFilter fileFilter, IOCase caseSensitivity) {
        this(new File(directoryName), fileFilter, caseSensitivity);
    }

    public FileAlterationWatchdog(File directory) {
        this(directory, (FileFilter)null);
    }

    public FileAlterationWatchdog(File directory, FileFilter fileFilter) {
        this(directory, fileFilter, (IOCase)null);
    }

    public FileAlterationWatchdog(File directory, FileFilter fileFilter, IOCase caseSensitivity) {
        this(new FileEntry(directory), fileFilter, caseSensitivity);
    }

    protected FileAlterationWatchdog (FileEntry rootEntry, FileFilter fileFilter, IOCase caseSensitivity) {
        super(rootEntry, fileFilter, caseSensitivity);
        
        File    dirFile=getDirectory();
        dirPath = dirFile.toPath(); 
    }

    @Override
    public void initialize () throws Exception {
        if (watcher != null) {
            throw new IllegalStateException("Watcher already initialized");
        }

        if (watchKey != null) {
            throw new IllegalStateException("Watch key already initialized");
        }

        FileSystem  fs=FileSystems.getDefault();
        watcher = fs.newWatchService();
        watchKey = dirPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
    }

    @Override
    public void checkAndNotify () {
        checkAndNotify(watchKey);
    }

    public Runnable createMonitor() {
        @SuppressWarnings("resource")
        final FileAlterationWatchdog    watchdog=this;
        return new Runnable() {
            @Override
            public void run () {
                for (Thread t=Thread.currentThread(); ; ) {
                    try {
                        @SuppressWarnings("synthetic-access")
                        WatchKey    key=watcher.take();
                        watchdog.checkAndNotify(key);
                    } catch(InterruptedException e) {
                        t.interrupt();
                        break;
                    }
                }
            }
        };
    }

    public Collection<WatchEvent<?>> checkAndNotify(TimeUnit unit, long count) throws InterruptedException {
        if (count <= 0L) {
            return checkAndNotify(watchKey);
        } else {
            WatchKey    key=watcher.poll(count, unit);
            return checkAndNotify(key);
        }
    }

    protected Collection<WatchEvent<?>> checkAndNotify(WatchKey key) {
        try {
            return checkAndNotify((key == null) ? Collections.<WatchEvent<?>>emptyList() : key.pollEvents());
        } finally {
            /* 
             * Reset the key -- this step is critical if we want to receive
             * further watch events. If the key is no longer valid, the directory
             * is inaccessible
             */
            if ((key != null) && (!key.reset())) {
                throw new IllegalStateException("checkAndNotify(" + ExtendedFileUtils.toString(getDirectory()) + ") folder no longer valid");
            }
        }
    }

    protected Collection<WatchEvent<?>> checkAndNotify(Collection<WatchEvent<?>> events) {
        if (ExtendedCollectionUtils.isEmpty(events)) {
            return events;
        }
        
        final FileAlterationListener  listenersWrapper=new FileAlterationListenerMultiplexer(getListeners());
        listenersWrapper.onStart(this);
        try {
            Collection<WatchEvent<?>>   managedEvents=null;
            for (WatchEvent<?> event : events) {
                Kind<?> kind = event.kind();
                /*
                 * An OVERFLOW event can occur regardless of the actual registered kinds
                 * see http://docs.oracle.com/javase/tutorial/essential/io/notification.html
                 */
                if (StandardWatchEventKinds.OVERFLOW.equals(kind)) {
                    continue;
                }
                
                // The filename is the context of the event.
                @SuppressWarnings("unchecked")
                WatchEvent<Path> pathEvent=(WatchEvent<Path>)event;
                Path filename=pathEvent.context(), child=dirPath.resolve(filename);
                notifiyListeners(child.toFile(), kind, listenersWrapper);
                if (managedEvents == null) {
                    managedEvents = new LinkedList<>();
                }
                
                managedEvents.add(event);
            }
            
            if (managedEvents == null) {
                return Collections.emptyList();
            } else {
                return managedEvents;
            }
        } finally {
            listenersWrapper.onStop(this);
        }
    }

    protected void notifiyListeners(File file, Kind<?> eventKind, FileAlterationListener listenersWrapper) {
        /*
         * NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
         * Due to the asynchronous nature of the watch service, by the
         * time this code is reached the referenced file might not
         * longer be there. Therefore, if we cannot determine whether it is a
         * file or a folder we call BOTH "onXXX" event methofs
         */
        if (StandardWatchEventKinds.ENTRY_CREATE.equals(eventKind)) {
            if (file.isDirectory()) {
                listenersWrapper.onDirectoryCreate(file);
            } else if (file.isFile()) {
                listenersWrapper.onFileCreate(file);
            } else {
                listenersWrapper.onFileCreate(file);
                listenersWrapper.onDirectoryCreate(file);
            }
        } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(eventKind)) {
            if (file.isDirectory()) {
                listenersWrapper.onDirectoryChange(file);
            } else if (file.isFile()) {
                listenersWrapper.onFileChange(file);
            } else {
                listenersWrapper.onFileChange(file);
                listenersWrapper.onDirectoryChange(file);
            }
        } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(eventKind)) {
            /*
             * NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
             * Unfortunately, since WatchService is built on top of the native OS's file event service,
             * it is limited by the information the native service provides. Linux's inotify does
             * indicate what type of filesystem object was deleted, but Microsoft's FileSystemWatcher
             * just gives the name. We therefore call BOTH "onXXXDelete" methods
             */
            listenersWrapper.onFileDelete(file);
            listenersWrapper.onDirectoryDelete(file);
        } else {
            throw new IllegalArgumentException("notifiyListeners(" + ExtendedFileUtils.toString(file) + ")[" + eventKind + "] unknown event kind");
        }
    }

    @Override
    public void destroy () throws Exception {
        close();
    }

    @Override
    public void close () throws IOException {
        if (watchKey != null) {
            watchKey.cancel();
            watchKey = null;
        }

        if (watcher != null) {
            watcher.close();
            watcher = null;
        }
    }
}
