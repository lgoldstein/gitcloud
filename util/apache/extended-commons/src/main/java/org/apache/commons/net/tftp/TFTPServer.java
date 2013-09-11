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

package org.apache.commons.net.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.AbstractLoggingBean;
import org.apache.commons.net.io.FromNetASCIIOutputStream;
import org.apache.commons.net.io.ToNetASCIIInputStream;

/**
 * A fully multi-threaded tftp server. Can handle multiple clients at the same
 * time. Implements RFC 1350 and wrapping block numbers for large file support.
 * To launch, just create an instance of the class. An IOException will be
 * thrown if the server fails to start for reasons such as port in use, port
 * denied, etc. To stop, use the shutdown method. To check to see if the server
 * is still running (or if it stopped because of an error), call the isRunning()
 * method. By default, events are not logged to stdout/stderr. This can be
 * changed with the setLog and setLogError methods.
 * <p>
 * Example usage is below: <code>
 * public static void main(String[] args) throws Exception
 *  {
 *      if (args.length != 1)
 *      {
 *          System.out
 *                  .println("You must provide 1 argument - the base path for the server to serve from.");
 *          System.exit(1);
 *      }
 * 
 *      TFTPServer ts = new TFTPServer(new File(args[0]), new File(args[0]), GET_AND_PUT);
 *      ts.setSocketTimeout(2000);
 * 
 *      System.out.println("TFTP Server running.  Press enter to stop.");
 *      new InputStreamReader(System.in).read();
 * 
 *      ts.shutdown();
 *      System.out.println("Server shut down.");
 *      System.exit(0);
 *  }
 * 
 * </code>
 * 
 * @author <A HREF="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</A>
 * @since 2.0
 */
// based on http://svn.apache.org/repos/asf/commons/proper/net/trunk/src/test/java/org/apache/commons/net/tftp/TFTPServer.java
public class TFTPServer extends AbstractLoggingBean implements Runnable {
    public static final int DEFAULT_RETRIES_COUNT = 3;

    public static enum ServerMode {
        GET_ONLY, PUT_ONLY, GET_AND_PUT;
    }

    private final HashSet<TFTPTransfer> transfers_ = new HashSet<TFTPTransfer>();
    private volatile boolean shutdownServer = false;
    private TFTP serverTftp_;
    private File serverReadDirectory_;
    private File serverWriteDirectory_;
    private final int port_;
    private Exception serverException = null;
    private final ServerMode mode_;

    private int maxTimeoutRetries_ = DEFAULT_RETRIES_COUNT;
    private int socketTimeout_ = TFTP.DEFAULT_TIMEOUT;
    private Thread serverThread;

    /**
     * Start a TFTP Server on the default port (69). Gets and Puts occur in the
     * specified directories. The server will start in another thread, allowing
     * this constructor to return immediately. If a get or a put comes in with a
     * relative path that tries to get outside of the serverDirectory, then the
     * get or put will be denied. GET_ONLY mode only allows gets, PUT_ONLY mode
     * only allows puts, and GET_AND_PUT allows both. Modes are defined as int
     * constants in this class.
     * 
     * @param serverReadDirectory
     *            directory for GET requests
     * @param serverWriteDirectory
     *            directory for PUT requests
     * @param mode
     *            A value as specified above.
     * @throws IOException
     *             if the server directory is invalid or does not exist.
     */
    public TFTPServer(File serverReadDirectory, File serverWriteDirectory, ServerMode mode) throws IOException {
        this(serverReadDirectory, serverWriteDirectory, TFTP.DEFAULT_PORT, mode);
    }

    /**
     * Start a TFTP Server on the specified port. Gets and Puts occur in the
     * specified directory. The server will start in another thread, allowing
     * this constructor to return immediately. If a get or a put comes in with a
     * relative path that tries to get outside of the serverDirectory, then the
     * get or put will be denied. GET_ONLY mode only allows gets, PUT_ONLY mode
     * only allows puts, and GET_AND_PUT allows both. Modes are defined as int
     * constants in this class.
     * 
     * @param serverReadDirectory
     *            directory for GET requests
     * @param serverWriteDirectory
     *            directory for PUT requests
     * @param port
     *            Service port
     * @param mode
     *            A value as specified above.
     * @throws IOException
     *             if the server directory is invalid or does not exist.
     */
    public TFTPServer(File serverReadDirectory, File serverWriteDirectory, int port, ServerMode mode) throws IOException {
        port_ = port;
        mode_ = mode;
        launch(serverReadDirectory, serverWriteDirectory);
    }

    /**
     * @return The configured server port
     */
    public final int getPort() {
        return port_;
    }

    /**
     * @return The operating {@link ServerMode}
     */
    public final ServerMode getServerMode() {
        return mode_;
    }

    /**
     * @param retries the max number of retries in response to a timeout. Default 3. Min 0.
     */
    public void setMaxTimeoutRetries(int retries) {
        Validate.isTrue(retries > 0, "Invalid retries value: %s", retries);
        maxTimeoutRetries_ = retries;
    }

    /**
     * @return The current value for maxTimeoutRetries
     */
    public int getMaxTimeoutRetries() {
        return maxTimeoutRetries_;
    }

    /**
     * @param timeout
     *            the socket timeout in milliseconds used in transfers. Defaults
     *            to the value here: {@link TFTP#DEFAULT_TIMEOUT} - Min value of
     *            10.
     */
    public void setSocketTimeout(int timeout) {
        Validate.isTrue(timeout >= 10, "Invalid socket timeout value: %s", timeout);
        socketTimeout_ = timeout;
    }

    /**
     * @return The current socket timeout used during transfers in milliseconds.
     */
    public int getSocketTimeout() {
        return socketTimeout_;
    }

    /*
     * start the server, throw an error if it can't start.
     */
    protected void launch(File serverReadDirectory, File serverWriteDirectory) throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("launch(" + getServerMode() + "@" + getPort() + ") read=" + serverReadDirectory + ", write=" + serverWriteDirectory);
        }

        serverReadDirectory_ = serverReadDirectory.getCanonicalFile();
        if (!serverReadDirectory_.exists() || !serverReadDirectory.isDirectory()) {
            throw new IOException("The server read directory " + serverReadDirectory_ + " does not exist");
        }

        serverWriteDirectory_ = serverWriteDirectory.getCanonicalFile();
        if (!serverWriteDirectory_.exists() || !serverWriteDirectory.isDirectory()) {
            throw new IOException("The server write directory " + serverWriteDirectory_ + " does not exist");
        }

        serverTftp_ = new TFTP();

        // This is the value used in response to each client.
        socketTimeout_ = serverTftp_.getDefaultTimeout();

        // we want the server thread to listen forever.
        serverTftp_.setDefaultTimeout(0);

        serverTftp_.open(port_);

        serverThread = new Thread(this);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @Override
    protected void finalize() throws Throwable {
        shutdown();
    }

    /**
     * check if the server thread is still running.
     * 
     * @return true if running, false if stopped.
     * @throws Exception
     *             throws the exception that stopped the server if the server is
     *             stopped from an exception.
     */
    public boolean isRunning() throws Exception {
        if (shutdownServer && serverException != null) {
            throw serverException;
        }
        return !shutdownServer;
    }

    @Override
    public void run() {
        try {
            while (!shutdownServer) {
                TFTPPacket tftpPacket;

                tftpPacket = serverTftp_.receive();

                TFTPTransfer tt = new TFTPTransfer(tftpPacket);
                synchronized(transfers_) {
                    transfers_.add(tt);
                }

                Thread thread = new Thread(tt);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            if (!shutdownServer) {
                serverException = e;
                logger.error("Unexpected Error (" + e.getClass().getSimpleName() + ") shutting down: " + e.getMessage(), e);
            }
        } finally {
            shutdownServer = true; // set this to true, so the launching thread
                                   // can check to see if it started.
            if (serverTftp_ != null && serverTftp_.isOpen()) {
                serverTftp_.close();
            }
        }
    }

    /**
     * Stop the tftp server (and any currently running transfers) and release
     * all opened network resources.
     */
    public void shutdown() {
        shutdownServer = true;

        synchronized(transfers_) {
            for (TFTPTransfer xfer : transfers_) {
                xfer.shutdown();
            }
        }

        try {
            serverTftp_.close();
        } catch (RuntimeException e) {
            logger.warn(e.getClass().getSimpleName() + " during server Tftp close: " + e.getMessage(), e);
        }

        try {
            serverThread.join(TimeUnit.SECONDS.toMillis(15L));
        } catch (InterruptedException e) {
            // we've done the best we could, return
        }
        
        Validate.isTrue(!serverThread.isAlive(), "Server thread still alive", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /*
     * An instance of an ongoing transfer.
     */
    private class TFTPTransfer implements Runnable {
        private final TFTPPacket tftpPacket_;

        private boolean shutdownTransfer = false;

        TFTP transferTftp_ = null;

        public TFTPTransfer(TFTPPacket tftpPacket) {
            tftpPacket_ = tftpPacket;
        }

        @SuppressWarnings("synthetic-access")
        public void shutdown() {
            shutdownTransfer = true;
            try {
                transferTftp_.close();
            } catch (RuntimeException e) {
                logger.warn(e.getClass().getSimpleName() + " during server transferTftp_ close: " + e.getMessage(), e);
            }
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void run() {
            try {
                transferTftp_ = new TFTP();

                transferTftp_.beginBufferedOps();
                transferTftp_.setDefaultTimeout(socketTimeout_);

                transferTftp_.open();

                if (tftpPacket_ instanceof TFTPReadRequestPacket) {
                    handleRead(((TFTPReadRequestPacket) tftpPacket_));
                } else if (tftpPacket_ instanceof TFTPWriteRequestPacket) {
                    handleWrite((TFTPWriteRequestPacket) tftpPacket_);
                } else {
                    logger.warn("Unsupported TFTP request (" + tftpPacket_ + ") - ignored.");
                }
            } catch (Exception e) {
                if (!shutdownTransfer) {
                    logger.error("Unexpected Error (" + e.getClass().getSimpleName() + ")"
                               + " during TFTP file transfer - aborted: " + e.getMessage(),
                                 e);
                }
            } finally {
                try {
                    if (transferTftp_ != null && transferTftp_.isOpen()) {
                        transferTftp_.endBufferedOps();
                        transferTftp_.close();
                    }
                } catch (Exception e) {
                    logger.warn(e.getClass().getSimpleName() + " during server transferTftp_ close: " + e.getMessage(), e);
                }
                synchronized(transfers_) {
                    transfers_.remove(this);
                }
            }
        }

        /*
         * Handle a tftp read request.
         */
        @SuppressWarnings("synthetic-access")
        private void handleRead(TFTPReadRequestPacket trrp) throws IOException, TFTPPacketException {
            InputStream is = null;
            try {
                if (mode_ == ServerMode.PUT_ONLY) {
                    transferTftp_.bufferedSend(new TFTPErrorPacket(trrp.getAddress(), trrp.getPort(),
                            TFTPErrorPacket.ILLEGAL_OPERATION, "Read not allowed by server."));
                    return;
                }

                try {
                    is = new BufferedInputStream(
                            new FileInputStream(buildSafeFile(serverReadDirectory_, trrp.getFilename(), false)));
                } catch (FileNotFoundException e) {
                    transferTftp_.bufferedSend(new TFTPErrorPacket(trrp.getAddress(), trrp.getPort(),
                            TFTPErrorPacket.FILE_NOT_FOUND, e.getMessage()));
                    return;
                } catch (Exception e) {
                    transferTftp_.bufferedSend(new TFTPErrorPacket(trrp.getAddress(), trrp.getPort(), TFTPErrorPacket.UNDEFINED, e.getMessage()));
                    return;
                }

                if (trrp.getMode() == TFTP.NETASCII_MODE) {
                    is = new ToNetASCIIInputStream(is);
                }

                byte[] temp = new byte[TFTPDataPacket.MAX_DATA_LENGTH];

                TFTPPacket answer;

                int block = 1;
                boolean sendNext = true;

                int readLength = TFTPDataPacket.MAX_DATA_LENGTH;

                TFTPDataPacket lastSentData = null;

                // We are reading a file, so when we read less than the
                // requested bytes, we know that we are at the end of the file.
                while (readLength == TFTPDataPacket.MAX_DATA_LENGTH && !shutdownTransfer) {
                    if (sendNext) {
                        readLength = is.read(temp);
                        if (readLength == -1) {
                            readLength = 0;
                        }

                        lastSentData = new TFTPDataPacket(trrp.getAddress(), trrp.getPort(), block, temp, 0, readLength);
                        transferTftp_.bufferedSend(lastSentData);
                    }

                    answer = null;

                    int timeoutCount = 0;

                    while (!shutdownTransfer
                            && (answer == null || !answer.getAddress().equals(trrp.getAddress()) || answer.getPort() != trrp.getPort())) {
                        // listen for an answer.
                        if (answer != null) {
                            // The answer that we got didn't come from the
                            // expected source, fire back an error, and continue
                            // listening.
                            logger.warn("TFTP Server ignoring message from unexpected source.");
                            transferTftp_.bufferedSend(new TFTPErrorPacket(answer.getAddress(), answer.getPort(),
                                    TFTPErrorPacket.UNKNOWN_TID, "Unexpected Host or Port"));
                        }
                        try {
                            answer = transferTftp_.bufferedReceive();
                        } catch (SocketTimeoutException e) {
                            if (timeoutCount >= maxTimeoutRetries_) {
                                throw e;
                            }
                            // didn't get an ack for this data. need to resend
                            // it.
                            timeoutCount++;
                            transferTftp_.bufferedSend(lastSentData);
                            continue;
                        }
                    }

                    if (answer == null || !(answer instanceof TFTPAckPacket)) {
                        if (!shutdownTransfer) {
                            logger.warn("Unexpected response from tftp client during transfer (" + answer + ") - aborted.");
                        }
                        break;
                    } else {
                        // once we get here, we know we have an answer packet
                        // from the correct host.
                        TFTPAckPacket ack = (TFTPAckPacket) answer;
                        if (ack.getBlockNumber() != block) {
                            /*
                             * The origional tftp spec would have called on us
                             * to resend the previous data here, however, that
                             * causes the SAS Syndrome.
                             * http://www.faqs.org/rfcs/rfc1123.html section
                             * 4.2.3.1 The modified spec says that we ignore a
                             * duplicate ack. If the packet was really lost, we
                             * will time out on receive, and resend the previous
                             * data at that point.
                             */
                            sendNext = false;
                        } else {
                            // send the next block
                            block++;
                            if (block > 65535) {
                                // wrap the block number
                                block = 0;
                            }
                            sendNext = true;
                        }
                    }
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    // noop
                }
            }
        }

        /*
         * handle a tftp write request.
         */
        @SuppressWarnings("synthetic-access")
        private void handleWrite(TFTPWriteRequestPacket twrp) throws IOException, TFTPPacketException {
            OutputStream bos = null;
            try {
                if (mode_ == ServerMode.GET_ONLY) {
                    transferTftp_.bufferedSend(new TFTPErrorPacket(twrp.getAddress(), twrp.getPort(),
                            TFTPErrorPacket.ILLEGAL_OPERATION, "Write not allowed by server."));
                    return;
                }

                int lastBlock = 0;
                String fileName = twrp.getFilename();

                try {
                    File temp = buildSafeFile(serverWriteDirectory_, fileName, true);
                    if (temp.exists()) {
                        transferTftp_.bufferedSend(new TFTPErrorPacket(twrp.getAddress(), twrp.getPort(),
                                TFTPErrorPacket.FILE_EXISTS, "File already exists"));
                        return;
                    }
                    bos = new BufferedOutputStream(new FileOutputStream(temp));

                    if (twrp.getMode() == TFTP.NETASCII_MODE) {
                        bos = new FromNetASCIIOutputStream(bos);
                    }
                } catch (Exception e) {
                    transferTftp_.bufferedSend(new TFTPErrorPacket(twrp.getAddress(), twrp.getPort(), TFTPErrorPacket.UNDEFINED, e
                            .getMessage()));
                    return;
                }

                TFTPAckPacket lastSentAck = new TFTPAckPacket(twrp.getAddress(), twrp.getPort(), 0);
                transferTftp_.bufferedSend(lastSentAck);

                while (true) {
                    // get the response - ensure it is from the right place.
                    TFTPPacket dataPacket = null;

                    int timeoutCount = 0;

                    while (!shutdownTransfer
                            && (dataPacket == null || !dataPacket.getAddress().equals(twrp.getAddress()) || dataPacket.getPort() != twrp.getPort())) {
                        // listen for an answer.
                        if (dataPacket != null) {
                            // The data that we got didn't come from the
                            // expected source, fire back an error, and continue
                            // listening.
                            logger.warn("TFTP Server ignoring message from unexpected source.");
                            transferTftp_.bufferedSend(new TFTPErrorPacket(dataPacket.getAddress(), dataPacket.getPort(),
                                    TFTPErrorPacket.UNKNOWN_TID, "Unexpected Host or Port"));
                        }

                        try {
                            dataPacket = transferTftp_.bufferedReceive();
                        } catch (SocketTimeoutException e) {
                            if (timeoutCount >= maxTimeoutRetries_) {
                                throw e;
                            }
                            // It didn't get our ack. Resend it.
                            transferTftp_.bufferedSend(lastSentAck);
                            timeoutCount++;
                            continue;
                        }
                    }

                    if (dataPacket != null && dataPacket instanceof TFTPWriteRequestPacket) {
                        // it must have missed our initial ack. Send another.
                        lastSentAck = new TFTPAckPacket(twrp.getAddress(), twrp.getPort(), 0);
                        transferTftp_.bufferedSend(lastSentAck);
                    } else if (dataPacket == null || !(dataPacket instanceof TFTPDataPacket)) {
                        if (!shutdownTransfer) {
                            logger.warn("Unexpected response from tftp client during transfer (" + dataPacket + ") - aborted.");
                        }
                        break;
                    } else {
                        int block = ((TFTPDataPacket) dataPacket).getBlockNumber();
                        byte[] data = ((TFTPDataPacket) dataPacket).getData();
                        int dataLength = ((TFTPDataPacket) dataPacket).getDataLength();
                        int dataOffset = ((TFTPDataPacket) dataPacket).getDataOffset();

                        if (block > lastBlock || (lastBlock == 65535 && block == 0)) {
                            // it might resend a data block if it missed our ack
                            // - don't rewrite the block.
                            bos.write(data, dataOffset, dataLength);
                            lastBlock = block;
                        }

                        lastSentAck = new TFTPAckPacket(twrp.getAddress(), twrp.getPort(), block);
                        transferTftp_.bufferedSend(lastSentAck);
                        if (dataLength < TFTPDataPacket.MAX_DATA_LENGTH) {
                            // end of stream signal - The tranfer is complete.
                            bos.close();

                            // But my ack may be lost - so listen to see if I
                            // need to resend the ack.
                            for (int i = 0; i < maxTimeoutRetries_; i++) {
                                try {
                                    dataPacket = transferTftp_.bufferedReceive();
                                } catch (SocketTimeoutException e) {
                                    // this is the expected route - the client
                                    // shouldn't be sending any more packets.
                                    break;
                                }

                                if (dataPacket != null
                                        && (!dataPacket.getAddress().equals(twrp.getAddress()) || dataPacket.getPort() != twrp.getPort())) {
                                    // make sure it was from the right client...
                                    transferTftp_.bufferedSend(new TFTPErrorPacket(dataPacket.getAddress(), dataPacket.getPort(),
                                            TFTPErrorPacket.UNKNOWN_TID, "Unexpected Host or Port"));
                                } else {
                                    // This means they sent us the last
                                    // datapacket again, must have missed our
                                    // ack. resend it.
                                    transferTftp_.bufferedSend(lastSentAck);
                                }
                            }

                            // all done.
                            break;
                        }
                    }
                }
            } finally {
                if (bos != null) {
                    bos.close();
                }
            }
        }

        /*
         * Utility method to make sure that paths provided by tftp clients do
         * not get outside of the serverRoot directory.
         */
        private File buildSafeFile(File serverDirectory, String fileName, boolean createSubDirs) throws IOException {
            File temp = new File(serverDirectory, fileName);
            temp = temp.getCanonicalFile();

            if (!isSubdirectoryOf(serverDirectory, temp)) {
                throw new IOException("Cannot access files outside of tftp server root.");
            }

            // ensure directory exists (if requested)
            if (createSubDirs) {
                createDirectory(temp.getParentFile());
            }

            return temp;
        }

        /*
         * recursively create subdirectories
         */
        private void createDirectory(File file) throws IOException {
            File parent = file.getParentFile();
            if (parent == null) {
                throw new IOException("Unexpected error creating requested directory");
            }
            if (!parent.exists()) {
                // recurse...
                createDirectory(parent);
            }

            if (parent.isDirectory()) {
                if (file.isDirectory()) {
                    return;
                }
                boolean result = file.mkdir();
                if (!result) {
                    throw new IOException("Couldn't create requested directory");
                }
            } else {
                throw new IOException("Invalid directory path - file in the way of requested folder");
            }
        }

        /*
         * recursively check to see if one directory is a parent of another.
         */
        private boolean isSubdirectoryOf(File parent, File child) {
            File childsParent = child.getParentFile();
            if (childsParent == null) {
                return false;
            }
            if (childsParent.equals(parent)) {
                return true;
            } else {
                return isSubdirectoryOf(parent, childsParent);
            }
        }
    }
}