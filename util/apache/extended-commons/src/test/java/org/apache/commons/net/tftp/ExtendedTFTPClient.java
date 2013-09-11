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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

/**
 * @author Lyor G.
 * @since Sep 10, 2013 10:19:33 AM
 */
public class ExtendedTFTPClient extends TFTPClient {
    public ExtendedTFTPClient() {
        super();
    }

    public void sendFile(String filename, int mode, File input, InetAddress host, int port) throws IOException {
        InputStream    in=new FileInputStream(input);
        try {
            sendFile(filename, mode, in, host, port);
        } finally {
            in.close();
        }
    }

    public void sendFile(String filename, int mode, File input, String hostname, int port) throws IOException {
        sendFile(filename, mode, input, InetAddress.getByName(hostname), port);
    }

    public void sendFile(String filename, int mode, File input, InetAddress host)throws IOException {
        sendFile(filename, mode, input, host, DEFAULT_PORT);
    }

    public void sendFile(String filename, int mode, File input, String hostname) throws IOException {
        sendFile(filename, mode, input, InetAddress.getByName(hostname));
    }

    public int receiveFile(String filename, int mode, File output, InetAddress host, int port) throws IOException {
        OutputStream    out=new FileOutputStream(output);
        try {
            return receiveFile(filename, mode, out, host, port);
        } finally {
            out.close();
        }
    }

    public int receiveFile(String filename, int mode, File output, String hostname, int port) throws IOException {
        return receiveFile(filename, mode, output, InetAddress.getByName(hostname), port);
    }

    public int receiveFile(String filename, int mode, File output, InetAddress host)throws IOException {
        return receiveFile(filename, mode, output, host, DEFAULT_PORT);
    }

    public int receiveFile(String filename, int mode, File output, String hostname) throws IOException {
        return receiveFile(filename, mode, output, InetAddress.getByName(hostname));
    }
}
