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

package org.apache.commons.net.tftp;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.tftp.TFTPServer.ServerMode;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 10, 2013 7:25:05 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TFTPServerTest extends AbstractTestSupport {
    private static final int    TEST_PORT=7365;
    private final File  serverDirectory, sourceFile;

    public TFTPServerTest() {
        serverDirectory = ensureFolderExists(new File(detectTargetFolder(), getClass().getSimpleName()));
        sourceFile = getTestJavaSourceFile();
        assertNotNull("Failed to find source file", sourceFile);
        assertTrue("Source file does not exist", sourceFile.exists());
    }

    @Test
    public void testReadOnly() throws IOException {
        final String    TEST_FILE_NAME="testReadOnly";
        File            expectedFile=new File(serverDirectory, TEST_FILE_NAME + "-expected.txt");
        ExtendedFileUtils.quickCopyFile(sourceFile, expectedFile);

        // Start a read-only server
        TFTPServer tftpS = new TFTPServer(serverDirectory, serverDirectory, TEST_PORT, ServerMode.GET_ONLY);
        try {
            // Create our TFTP instance to handle the file transfer.
            ExtendedTFTPClient tftp = new ExtendedTFTPClient();
            try {
                tftp.open();
                tftp.setSoTimeout(2000);
        
                File    actualFile=new File(serverDirectory, TEST_FILE_NAME + "-actual.txt");
                actualFile.delete();
                assertFalse("Reception file not removed: " + actualFile, actualFile.exists());

                tftp.receiveFile(expectedFile.getName(), TFTP.BINARY_MODE, actualFile, "localhost", tftpS.getPort());
        
                assertTrue("Received file not created: " + actualFile, actualFile.exists());
                assertTrue("Mismatched contents: " + actualFile, FileUtils.contentEquals(expectedFile, actualFile));
                assertTrue("Failed to deleted received file: " + actualFile, actualFile.delete());
                assertFalse("Received file not deleted: " + actualFile, actualFile.exists());
        
                try {
                    tftp.sendFile(actualFile.getName(), TFTP.BINARY_MODE, expectedFile, "localhost", tftpS.getPort());
                    fail("Server allowed write: " + actualFile);
                } catch (IOException e) {
                    // expected path
                }
            } finally {
                tftp.close();
            }
        } finally {
            tftpS.shutdown();
        }
    }
    
    @Test
    public void testWriteOnly() throws IOException {
        final String    TEST_FILE_NAME="testWriteOnly";
        File            expectedFile=new File(serverDirectory, TEST_FILE_NAME + "-expected.txt");
        ExtendedFileUtils.quickCopyFile(sourceFile, expectedFile);

        // Start a write-only server
        TFTPServer tftpS = new TFTPServer(serverDirectory, serverDirectory, TEST_PORT, ServerMode.PUT_ONLY);
        try {
            // Create our TFTP instance to handle the file transfer.
            ExtendedTFTPClient tftp = new ExtendedTFTPClient();
            try {
                tftp.open();
                tftp.setSoTimeout(2000);
        
                // make a file to work with.
                File   actualFile=new File(serverDirectory, TEST_FILE_NAME + "-actual.txt");
                actualFile.delete();
                assertFalse("Reception file not removed: " + actualFile, actualFile.exists());
        
                try {
                    tftp.receiveFile(expectedFile.getName(), TFTP.BINARY_MODE, actualFile, "localhost", tftpS.getPort());
                    fail("Server allowed read: " + expectedFile);
                } catch (IOException e) {
                    // expected path
                }

                assertEquals("Reception file created anyway: " + actualFile, 0L, actualFile.length());
                assertTrue("Failed to deleted received file: " + actualFile, actualFile.delete());
                assertFalse("Received file not deleted: " + actualFile, actualFile.exists());

                tftp.sendFile(actualFile.getName(), TFTP.BINARY_MODE, expectedFile, "localhost", TEST_PORT);
                assertTrue("Sent file not created: " + actualFile, actualFile.exists());
                assertTrue("Mismatched contents: " + actualFile, FileUtils.contentEquals(expectedFile, actualFile));
            } finally {
                tftp.close();
            }
        } finally {
            tftpS.shutdown();
        }
    }

    @Test
    public void testWriteOutsideHome() throws IOException {
        final String    TEST_FILE_NAME="testWriteOutsideHome";
        File            expectedFile=new File(serverDirectory, TEST_FILE_NAME + "-expected.txt");
        ExtendedFileUtils.quickCopyFile(sourceFile, expectedFile);

        // Start a server
        TFTPServer tftpS = new TFTPServer(serverDirectory, serverDirectory, TEST_PORT, ServerMode.GET_AND_PUT);
        try {
            // Create our TFTP instance to handle the file transfer.
            ExtendedTFTPClient tftp = new ExtendedTFTPClient();
            try {
                tftp.open();

                File   actualFile=new File(serverDirectory.getParentFile(), TEST_FILE_NAME + "-actual.txt");
                actualFile.delete();
                assertFalse("Reception file not removed: " + actualFile, actualFile.exists());

                try {
                    tftp.sendFile("../" + actualFile.getName(), TFTP.BINARY_MODE, expectedFile, "localhost", tftpS.getPort());
                    fail("Server allowed write: " + expectedFile);
                } catch (IOException e) {
                    // expected path
                }

                assertFalse("file created when it should not have been: " + actualFile, actualFile.exists());
            } finally {
                tftp.close();
            }
        } finally {
            tftpS.shutdown();
        }
    }

}
