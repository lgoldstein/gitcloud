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

package org.apache.commons.net.ssh.keys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.io.input.CloseShieldReader;
import org.apache.commons.io.input.ExtendedCloseShieldInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents an entry in the keys file
 */
public class CryptoKeyEntry implements Serializable {
    private static final long serialVersionUID = -4961735053733585992L;

    /**
     * An {@link ExtendedTransformer} that invokes the {@link CryptoKeyEntry#decodePublicKey()}
     * <B>Note:</B> throws a {@linkplain RuntimeException} if failed to decode the key
     */
    public static final ExtendedTransformer<CryptoKeyEntry,PublicKey> PUBLIC_KEY_DECODER=
            new AbstractExtendedTransformer<CryptoKeyEntry,PublicKey>(CryptoKeyEntry.class, PublicKey.class) {
                @Override
                public PublicKey transform(CryptoKeyEntry entry) {
                    if (entry == null) {
                        return null;
                    }
                    
                    try {
                        return entry.decodePublicKey();
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

    /**
     * An {@link ExtendedTransformer} that invokes {@link CryptoKeyEntry#getUser()}
     */
    public static final ExtendedTransformer<CryptoKeyEntry,String>  USERNAME_EXTRACTOR=
            new AbstractExtendedTransformer<CryptoKeyEntry,String>(CryptoKeyEntry.class, String.class) {
                @Override
                public String transform(CryptoKeyEntry entry) {
                    if (entry == null) {
                        return null;
                    } else {
                        return entry.getUser();
                    }
                }
            };

    private String  algorithm;
    private String  keyType;
    private String  user;
    private String  host;
    private byte[]  keyData;
    
    public CryptoKeyEntry () {
        super();
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String value) {
        this.algorithm = value;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String value) {
        this.keyType = value;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String value) {
        this.user = value;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String value) {
        this.host = value;
    }

    public byte[] getKeyData() {
        return keyData;
    }

    public void setKeyData(byte[] value) {
        this.keyData = value;
    }

    public PublicKey decodePublicKey() throws IOException {
        return KeyUtils.decodePublicKey(getKeyData());
    }

    @Override
    public int hashCode() {
        return ExtendedStringUtils.hashCode(getAlgorithm(), Boolean.TRUE)
             + ExtendedStringUtils.hashCode(getKeyType(), Boolean.FALSE)
             + ExtendedStringUtils.hashCode(getHost(), Boolean.FALSE)
             + ExtendedStringUtils.hashCode(getUser(), Boolean.FALSE)
             + Arrays.hashCode(getKeyData())
             ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        CryptoKeyEntry  other=(CryptoKeyEntry) obj;
        return (0 == ExtendedStringUtils.safeCompare(getAlgorithm(), other.getAlgorithm(), false))
            && (0 == ExtendedStringUtils.safeCompare(getKeyType(), other.getKeyType(), false))
            && (0 == ExtendedStringUtils.safeCompare(getHost(), other.getHost(), false))
            && (0 == ExtendedStringUtils.safeCompare(getUser(), other.getUser(), false))
            && Arrays.equals(getKeyData(), other.getKeyData())
            ;
    }

    @Override
    public String toString() {
        String  kt=getKeyType();
        byte[]  kd=getKeyData();
        return "ssh-" + getAlgorithm()
            + (StringUtils.isEmpty(kt) ? "" : ("-" + kt))
            + " " + (ArrayUtils.isEmpty(kd) ? "<no-key>" : new Base64().encodeToString(kd))
            + " " + getUser() + "@" + getHost()
            ;
    }

    public static final String  IGNORED_HOST_VALUE="ignored", IGNORED_USER_VALUE="ignored";
    public boolean matchUserAndHost (String expHost, String expUser) {
        if ((StringUtils.isEmpty(expUser) || IGNORED_USER_VALUE.equalsIgnoreCase(expUser))
         && (StringUtils.isEmpty(expHost) || IGNORED_HOST_VALUE.equalsIgnoreCase(expHost))) {
            return true;    // no specific user and no specific host
        }
        
        if ((!StringUtils.isEmpty(expUser))
         && (!IGNORED_USER_VALUE.equalsIgnoreCase(expUser))
         && (!expUser.equalsIgnoreCase(getUser()))) {
            return false;   // have a specific user
        }
        
        if (StringUtils.isEmpty(expHost)
         || IGNORED_HOST_VALUE.equalsIgnoreCase(expHost)) {
            return true;    // if no specified host then assume a match
        }

        if (expHost.equalsIgnoreCase(getHost())) {
            return true;
        }
        
        return false;
    }

    /**
     * Standard OpenSSH authorized keys file name
     */
    public static final String  STD_AUTHORIZED_KES_FILENAME="authorized_keys";
    private static final class LazyDefaultAuthorizedKeysFileHolder {
        private static final File   keysFile=new File(KeyUtils.getDefaultKeysFolder(), STD_AUTHORIZED_KES_FILENAME);
    }

    /**
     * @return The {@link File} location of the OpenSSH authorized keys
     */
    @SuppressWarnings("synthetic-access")
    public static final File getDefaultAuthorizedKeysFile() {
        return LazyDefaultAuthorizedKeysFileHolder.keysFile;
    }

    /**
     * Reads read the contents of the default OpenSSH <code>authorized_keys</code> file
     * @return A {@link Collection} of all the {@link CryptoKeyEntry}-ies found there - 
     * or empty if file does not exist 
     * @throws IOException If failed to read keys from file
     */
    public static final Collection<CryptoKeyEntry> readDefaultAuthorizedKeys() throws IOException {
        File    keysFile=getDefaultAuthorizedKeysFile();
        if (keysFile.exists()) {
            return readAuthorizedKeys(keysFile);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Reads read the contents of an <code>authorized_keys</code> file
     * @param url The {@link URL} to read from
     * @return A {@link Collection} of all the {@link CryptoKeyEntry}-ies found there 
     * @throws IOException If failed to read or parse the entries
     * @see #readAuthorizedKeys(InputStream, boolean)
     */
    public static final Collection<CryptoKeyEntry> readAuthorizedKeys(URL url) throws IOException {
        return readAuthorizedKeys(url.openStream(), true);
    }

    /**
     * Reads read the contents of an <code>authorized_keys</code> file
     * @param in The {@link InputStream}
     * @param okToClose <code>true</code> if method may close the input stream
     * regardless of whether successful or failed
     * @return A {@link Collection} of all the {@link CryptoKeyEntry}-ies found there 
     * @throws IOException If failed to read or parse the entries
     * @see #readAuthorizedKeys(Reader, boolean)
     */
    public static final Collection<CryptoKeyEntry> readAuthorizedKeys(InputStream in, boolean okToClose) throws IOException {
        Reader  rdr=new InputStreamReader(ExtendedCloseShieldInputStream.resolveInputStream(in, okToClose));
        try {
            return readAuthorizedKeys(rdr, true);
        } finally {
            rdr.close();
        }
    }

    /**
     * Reads read the contents of an <code>authorized_keys</code> file
     * @param file The {@link File} to read from
     * @return A {@link Collection} of all the {@link CryptoKeyEntry}-ies found there 
     * @throws IOException If failed to read or parse the entries
     * @see #readAuthorizedKeys(Reader, boolean)
     */
    public static final Collection<CryptoKeyEntry> readAuthorizedKeys(File file) throws IOException {
        return readAuthorizedKeys(new FileReader(file), true);
    }

    /**
     * Reads read the contents of an <code>authorized_keys</code> file
     * @param rdr The {@link Reader}
     * @param okToClose <code>true</code> if method may close the input stream
     * regardless of whether successful or failed
     * @return A {@link Collection} of all the {@link CryptoKeyEntry}-ies found there 
     * @throws IOException If failed to read or parse the entries
     * @see #readAuthorizedKeys(BufferedReader)
     */
    public static final Collection<CryptoKeyEntry> readAuthorizedKeys(Reader rdr, boolean okToClose) throws IOException {
        BufferedReader  buf=new BufferedReader(CloseShieldReader.resolveReader(rdr, okToClose));
        try {
            return readAuthorizedKeys(buf);
        } finally {
            buf.close();
        }
    }

    /**
     * @param rdr The {@link BufferedReader} to use to read the contents of
     * an <code>authorized_keys</code> file
     * @return A {@link Collection} of all the {@link CryptoKeyEntry}-ies found there 
     * @throws IOException If failed to read or parse the entries
     * @see #parseCryptoKeyEntry(String)
     */
    public static final Collection<CryptoKeyEntry> readAuthorizedKeys(BufferedReader rdr) throws IOException {
        Collection<CryptoKeyEntry>  entries=null;

        for (String line=rdr.readLine(); line != null; line=rdr.readLine()) {
            CryptoKeyEntry  entry=parseCryptoKeyEntry(line.trim());
            if (entry == null) {
                continue;
            }
            
            if (entries == null) {
                entries = new LinkedList<CryptoKeyEntry>();
            }
            
            entries.add(entry);
        }
        
        if (entries == null) {
            return Collections.emptyList();
        } else {
            return entries;
        }
    }

    /**
     * Character used to denote a comment line in the keys file
     */
    public static final char COMMENT_CHAR='#';

    /**
     * @param line Original line from an <code>authorized_keys</code> file
     * @return {@link CryptoKeyEntry} or <code>null</code> if the line is
     * <code>null</code>/empty or a comment line
     * @throws IOException If failed to parse/decode the line
     * @see #COMMENT_CHAR
     */
    public static final CryptoKeyEntry parseCryptoKeyEntry (String line) throws IOException {
        if (StringUtils.isEmpty(line) || (line.charAt(0) == COMMENT_CHAR) /* comment ? */) {
            return null;
        }

        int startPos=line.indexOf(' '), endPos=line.lastIndexOf(' ');
        if ((startPos <= 0) || (endPos <= startPos)) {
            throw new StreamCorruptedException("Bad format (no key data delimiters): " + line);
        }
        
        String  algorithmAndType=line.substring(0, startPos).trim();
        int     algStartPos=algorithmAndType.indexOf('-'), algEndPos=algorithmAndType.lastIndexOf('-');
        if (algStartPos <= 0) {
            throw new StreamCorruptedException("Bad algorithm (" + algorithmAndType + "): " + line);
        }

        String  algorithm=(algStartPos != algEndPos)
                    ? algorithmAndType.substring(algStartPos + 1, algEndPos)
                    : algorithmAndType.substring(algStartPos + 1)
                    ;
        String  keyType=(algStartPos != algEndPos) ? algorithmAndType.substring(algEndPos + 1) : null;
        String  encData=line.substring(startPos + 1, endPos).trim();
        if (StringUtils.isEmpty(encData)) {
            throw new StreamCorruptedException("Missing key bytes for algorithm=" + algorithmAndType + ": " + line);
        }

        String  userAndHost=(endPos < (line.length() - 1)) ? line.substring(endPos + 1).trim() : null;
        if (StringUtils.isEmpty(userAndHost)) {
            throw new StreamCorruptedException("Missing user and/or host for algorithm=" + algorithmAndType + ": " + line);
        }

        int             sepPos=userAndHost.indexOf('@');
        CryptoKeyEntry  entry=new CryptoKeyEntry();
        entry.setAlgorithm(algorithm);
        entry.setKeyType(keyType);
        entry.setHost((sepPos < 0) ? userAndHost : userAndHost.substring(sepPos + 1));
        entry.setUser((sepPos < 0) ? null : userAndHost.substring(0, sepPos));
        entry.setKeyData(new Base64().decode(encData));
        return entry;
    }
}