/*
 * 
 */
package org.apache.commons.io17.monitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.AbstractLoggingBean;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jun 6, 2013 8:39:33 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileAlterationWatchdogTest extends AbstractTestSupport {
    public FileAlterationWatchdogTest () {
        super();
    }

    @Test
    @Ignore("O/S file system race condition fails this test")
    public void testCheckAndNotifyImmediate() throws Exception {
        testCheckAndNotify(
                "testCheckAndNotifyImmediate",
                new CheckAndNotifyRunner() {
                        @Override
                        public void checkAndNotify(FileAlterationWatchdog  watchdog,
                                                   Pair<File,Kind<Path>>   executedAction,
                                                   FileAlterationAssertions assertions) throws Exception {
                            watchdog.checkAndNotify();
                            assertions.waitForEvents(1);
                        }
                    });
    }

    @Test
    public void testCheckAndNotifyMonitor() throws Exception {
        FileAlterationWatchdog  watchdog=createWatchdog("testCheckAndNotifyMonitor");
        try {
            Runnable    monitor=watchdog.createMonitor();
            Thread      t=new Thread(monitor, "testCheckAndNotifyMonitor");
            t.start();
            
            try {
                FileAlterationAssertions    assertions=testCheckAndNotify(watchdog, null);
                assertions.waitForEvents(3);
                assertFalse("No events collected", ExtendedCollectionUtils.isEmpty(assertions.getEvents()));
            } finally {
                t.interrupt();
                
                t.join(TimeUnit.SECONDS.toMillis(5L));
                assertFalse("Monitor thread not killed", t.isAlive());
            }
        } finally {
            watchdog.destroy();
        }
    }

    private FileAlterationAssertions testCheckAndNotify(String testName, CheckAndNotifyRunner runner) throws Exception  {
        FileAlterationWatchdog  watchdog=createWatchdog(testName);
        try {
            return testCheckAndNotify(watchdog, runner);
        } finally {
            watchdog.destroy();
        }
    }

    private FileAlterationAssertions testCheckAndNotify(FileAlterationWatchdog watchdog, CheckAndNotifyRunner runner) throws Exception {
        FileAlterationAssertions    assertions=new FileAlterationAssertions();
        watchdog.addListener(assertions);

        File    testDir=watchdog.getDirectory(), testFile=new File(testDir, "testFile.txt");
        writeData(StandardWatchEventKinds.ENTRY_CREATE, testFile);
        if (runner != null) {
            runner.checkAndNotify(watchdog, Pair.of(testFile, StandardWatchEventKinds.ENTRY_CREATE), assertions);
        }

        writeData(StandardWatchEventKinds.ENTRY_MODIFY, testFile);
        if (runner != null) {
            runner.checkAndNotify(watchdog, Pair.of(testFile, StandardWatchEventKinds.ENTRY_MODIFY), assertions);
        }
        
        assertTrue("Failed to delete " + ExtendedFileUtils.toString(testFile), testFile.delete());
        assertFalse("File not deleted " + ExtendedFileUtils.toString(testFile), testFile.exists());
        if (runner != null) {
            runner.checkAndNotify(watchdog, Pair.of(testFile, StandardWatchEventKinds.ENTRY_DELETE), assertions);
        }
        
        return assertions;
    }

    private FileAlterationWatchdog createWatchdog(String testName) throws Exception {
        File    testDir=ensureFolderExists(new File(ensureTempFolderExists(), testName));
        FileAlterationWatchdog  watchdog=new FileAlterationWatchdog(testDir);
        try {
            watchdog.initialize();
        } catch(Exception e) {
            watchdog.destroy();
            throw e;
        }
        
        return watchdog;
    }

    private static void writeData(Object data, File targetFile) throws IOException {
        Writer  writer=new FileWriter(targetFile);
        try {
            writer.write(String.valueOf(data));
        } finally {
            writer.close();
        }
    }

    private static interface CheckAndNotifyRunner {
        void checkAndNotify(FileAlterationWatchdog  watchdog,
                            Pair<File,Kind<Path>>   executedAction,
                            FileAlterationAssertions assertions) throws Exception;
    }

    private static class FileAlterationAssertions extends AbstractLoggingBean implements FileAlterationListener {
        private final List<Pair<File,Kind<Path>>>  events=Collections.synchronizedList(new ArrayList<Pair<File,Kind<Path>>>());
        private final Semaphore sigSem=new Semaphore(0);

        public FileAlterationAssertions () {
            this("");
        }

        public FileAlterationAssertions (String index) {
            super(index);
        }

        public List<Pair<File,Kind<Path>>> getEvents() {
            return events;
        }

        public void waitForEvents(int numEvents) throws Exception {
            assertTrue("waitForEvents(" + numEvents + ") failed", sigSem.tryAcquire(numEvents, 5L, TimeUnit.SECONDS));
        }

        @Override
        public void onStart (FileAlterationObserver observer) {
            assertObjectInstanceof("onStart", FileAlterationWatchdog.class, observer);
            logger.info("onStart(" + observer + ")");
        }

        @Override
        public void onDirectoryCreate (File directory) {
            logger.info("onDirectoryCreate(" + ExtendedFileUtils.toString(directory) + ")");
            addEvent(directory, StandardWatchEventKinds.ENTRY_CREATE);
        }

        @Override
        public void onDirectoryChange (File directory) {
            logger.info("onDirectoryChange(" + ExtendedFileUtils.toString(directory) + ")");
            addEvent(directory, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        @Override
        public void onDirectoryDelete (File directory) {
            logger.info("onDirectoryDelete(" + ExtendedFileUtils.toString(directory) + ")");
            addEvent(directory, StandardWatchEventKinds.ENTRY_DELETE);
        }

        @Override
        public void onFileCreate (File file) {
            logger.info("onFileCreate(" + ExtendedFileUtils.toString(file) + ")");
            addEvent(file, StandardWatchEventKinds.ENTRY_CREATE);
        }

        @Override
        public void onFileChange (File file) {
            logger.info("onFileChange(" + ExtendedFileUtils.toString(file) + ")");
            addEvent(file, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        @Override
        public void onFileDelete (File file) {
            logger.info("onFileDelete(" + ExtendedFileUtils.toString(file) + ")");
            addEvent(file, StandardWatchEventKinds.ENTRY_DELETE);
        }

        @Override
        public void onStop (FileAlterationObserver observer) {
            assertObjectInstanceof("onStop", FileAlterationWatchdog.class, observer);
            logger.info("onStop(" + observer + ")");
        }
        
        protected void addEvent(File file, Kind<Path> kind) {
            events.add(Pair.of(file, kind));
            sigSem.release();
        }

        @Override
        public String toString () {
            return String.valueOf(getEvents());
        }
    }
}
