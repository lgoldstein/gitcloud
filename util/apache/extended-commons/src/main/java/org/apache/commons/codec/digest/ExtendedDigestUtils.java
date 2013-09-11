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

package org.apache.commons.codec.digest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections15.ExtendedSetUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author Lyor G.
 * @since Sep 3, 2013 9:52:07 AM
 */
public class ExtendedDigestUtils extends DigestUtils {
    public static final String  PROVIDER_DIGEST_KEY_PREFIX="MessageDigest.";

    // inspired by http://www.java2s.com/Code/Java/Security/Listtheavailablealgorithmnamesforcipherskeyagreementmacsmessagedigestsandsignatures.htm
    /**
     * @return A {@link SortedSet} of the currently registered digest providers names
     */
    public static final SortedSet<String> getDigestAlgorithms() {
        Provider[] providers=Security.getProviders();
        if (ArrayUtils.isEmpty(providers)) {
            return ExtendedSetUtils.emptySortedSet();
        }
        
        SortedSet<String>   result=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (Provider p : providers) {
            for (String key: p.stringPropertyNames()) {
                if (!key.startsWith(PROVIDER_DIGEST_KEY_PREFIX)) {
                    continue;
                }
                
                String  name=key.substring(PROVIDER_DIGEST_KEY_PREFIX.length());
                if (name.indexOf(' ') > 0) {
                    continue;   // some keys have this quirk...
                }
                
                if (!result.add(name)) {
                    continue;   // debug breakpoint
                }
            }
        }
        
        return result;
    }
    
    /**
     * @param digest The {@link MessageDigest} to use
     * @param file The file whose digest is requested
     * @return The digest value
     * @throws IOException If failed to access the file
     */
    public static final byte[] digest(MessageDigest digest, File file) throws IOException {
        InputStream input=new FileInputStream(file);
        try {
            return digest(digest, input);
        } finally {
            input.close();
        }
    }
    
    /**
     * @param digest The {@link MessageDigest} to use
     * @param url The {@link URL} to the data be digested
     * @return The digest value
     * @throws IOException If failed to access the file
     */
    public static final byte[] digest(MessageDigest digest, URL url) throws IOException {
        InputStream input=Validate.notNull(url, "No URL", ArrayUtils.EMPTY_OBJECT_ARRAY).openStream();
        try {
            return digest(digest, input);
        } finally {
            input.close();
        }
    }

    /**
     * @param digest The {@link MessageDigest} to use
     * @param chars The character(s) data be digested
     * @return The digest value
     * @throws IOException If failed to digest
     */
    public static final byte[] digest(MessageDigest digest, char ... chars) throws IOException {
        return digest(digest, chars, 0, chars.length);
    }

    /**
     * @param digest The {@link MessageDigest} to use
     * @param chars The character(s) data be digested
     * @param off Offset to start digest
     * @param len Number of characters to digest
     * @return The digest value
     * @throws IOException If failed to digest
     */
    public static final byte[] digest(MessageDigest digest, char[] chars, int off, int len) throws IOException {
        return digest(digest, CharBuffer.wrap(chars, off, len));
    }

    /**
     * @param digest The {@link MessageDigest} to use
     * @param csq The {@link CharSequence} data be digested
     * @return The digest value
     * @throws IOException If failed to digest
     */
    public static final byte[] digest(MessageDigest digest, CharSequence csq) throws IOException {
        return digest(digest, csq, 0, csq.length());
    }

    /**
     * @param digest The {@link MessageDigest} to use
     * @param csq The {@link CharSequence} data be digested
     * @param start Start offset to digest (inclusive)
     * @param end End offset to digest (exclusive)
     * @return The digest value
     * @throws IOException If failed to digest
     */
    public static final byte[] digest(MessageDigest digest, CharSequence csq, int start, int end) throws IOException {
        DigestWriter    output=new DigestWriter(digest, NullWriter.NULL_WRITER);
        try {
            output.append(csq, start, end);
        } finally {
            output.close();
        }

        return output.getDigestValue();
    }

    /**
     * @param digest The {@link MessageDigest} to use
     * @param input The {@link Reader} whose data is to be digested
     * @return The digest value
     * @throws IOException If failed to access the file
     */
    public static final byte[] digest(MessageDigest digest, Reader input) throws IOException {
        DigestWriter    output=new DigestWriter(digest, NullWriter.NULL_WRITER);
        try {
            IOUtils.copyLarge(input, output);
        } finally {
            output.close();
        }
        
        return output.getDigestValue();
    }

    /**
     * @param digest The {@link MessageDigest} to use
     * @param input The {@link InputStream} whose data is to be digested
     * @return The digest value
     * @throws IOException If failed to access the file
     */
    public static final byte[] digest(MessageDigest digest, InputStream input) throws IOException {
        DigestOutputStream  output=new DigestOutputStream(digest, NullOutputStream.NULL_OUTPUT_STREAM);
        try {
            IOUtils.copyLarge(input, output);
        } finally {
            output.close();
        }
        
        return output.getDigestValue();
    }
}
