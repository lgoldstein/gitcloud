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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.CloseShieldReader;
import org.apache.commons.io.input.ExtendedCloseShieldInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.ExtendedNumberUtils;
import org.apache.commons.net.ssh.keys.dss.DSSKeyDecoder;
import org.apache.commons.net.ssh.keys.rsa.RSAKeyDecoder;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 7:50:05 AM
 */
public class KeyUtils {
    private static final Map<String,KeyDecoder> byKeyTypeDecodersMap=new TreeMap<String,KeyDecoder>(String.CASE_INSENSITIVE_ORDER);
    private static final Map<String,KeyDecoder> byAlgorithmDecodersMap=new TreeMap<String,KeyDecoder>(String.CASE_INSENSITIVE_ORDER);

    // pre-register some built in decoders
    static {
        registerDecoder(RSAKeyDecoder.DECODER);
        registerDecoder(DSSKeyDecoder.DECODER);
    }

    /**
     * An {@link ExtendedTransformer} that extracts the {@link Key#getEncoded()} data bytes
     */
    public static final ExtendedTransformer<Key,byte[]> ENCODED_KEY_DATA_EXTRACTOR=
            new AbstractExtendedTransformer<Key,byte[]>(Key.class, byte[].class) {
                @Override
                public byte[] transform(Key input) {
                    if (input == null) {
                        return null;
                    } else {
                        return input.getEncoded();
                    }
                }
            };

    /**
     * Compares the {@link Key#getEncoded()} data bytes
     */
    public static final Comparator<Key> BY_ENCODED_FORM_KEY_COMPARATOR=
            new Comparator<Key>() {
                @Override
                public int compare(Key k1, Key k2) {
                    byte[]  d1=ENCODED_KEY_DATA_EXTRACTOR.transform(k1);
                    byte[]  d2=ENCODED_KEY_DATA_EXTRACTOR.transform(k2);
                    int     index=ExtendedArrayUtils.findFirstNonMatchingIndex(d1, d2);
                    if (index < 0) {
                        return 0;
                    }
                    
                    int l1=ExtendedArrayUtils.length(d1), l2=ExtendedArrayUtils.length(d2), cmnLen=Math.min(l1, l2);
                    // if within the common length, return the values difference - otherwise the shorter one comes first
                    if (index < cmnLen) {
                        byte    v1=d1[index], v2=d2[index];
                        return ExtendedNumberUtils.signOf(v1 - v2);
                    } else if (index == l1) {
                        return (-1);
                    } else {
                        return (+1);
                    }
                }
            };

    /**
     * An {@link ExtendedTransformer} that extracts the {@link Key#getAlgorithm()} value
     */
    public static final ExtendedTransformer<Key,String> KEY_ALGORITHM_EXTRACTOR=
            new AbstractExtendedTransformer<Key,String>(Key.class, String.class) {
                @Override
                public String transform(Key input) {
                    if (input == null) {
                        return null;
                    } else {
                        return input.getAlgorithm();
                    }
                }
            };

    /**
     * A {@link Comparator} that compares the {@link Key#getAlgorithm()} value
     * followed by the {@link Key#getEncoded()} data bytes
     */
    public static final Comparator<Key> BY_ALGORITHM_ENCODED_FORM_KEY_COMPARATOR=
            new Comparator<Key>() {
                @Override
                public int compare(Key k1, Key k2) {
                    String  a1=KEY_ALGORITHM_EXTRACTOR.transform(k1);
                    String  a2=KEY_ALGORITHM_EXTRACTOR.transform(k2);
                    int     nRes=ExtendedStringUtils.safeCompare(a1, a2, false);
                    if (nRes != 0) {
                        return nRes;
                    }
                    
                    if ((nRes=BY_ENCODED_FORM_KEY_COMPARATOR.compare(k1, k2)) != 0) {
                        return nRes;
                    }
                    
                    return 0;
                }
            };
    /**
     * @param decoder Registers the specified {@link KeyDecoder} in the cached
     * map(s) - replacing any previous one
     * @see #getKeyDecoderByAlgorithm(String)
     * @see #getKeyDecoderByKeyType(String)
     */
    public static final void registerDecoder(KeyDecoder decoder) {
        ExtendedValidate.notNull(decoder, "No decoder provided");
        
        synchronized(byKeyTypeDecodersMap) {
            byKeyTypeDecodersMap.put(decoder.getKeyType(), decoder);
        }

        synchronized(byAlgorithmDecodersMap) {
            byAlgorithmDecodersMap.put(decoder.getAlgorithm(), decoder);
        }
    }

    /**
     * @param keyType The key type (case insensitive)
     * @return The registered {@link KeyDecoder} for the specified type -
     * <code>null</code> if no match found
     * @see #registerDecoder(KeyDecoder)
     */
    public static final KeyDecoder getKeyDecoderByKeyType(String keyType) {
        synchronized(byKeyTypeDecodersMap) {
            return byKeyTypeDecodersMap.get(keyType);
        }
    }

    /**
     * @param algorithm The algorithm (case insensitive)
     * @return The registered {@link KeyDecoder} for the specified
     * algorithm - <code>null</code> if no match found
     * @see #registerDecoder(KeyDecoder)
     */
    public static final KeyDecoder getKeyDecoderByAlgorithm(String algorithm) {
        synchronized(byAlgorithmDecodersMap) {
            return byAlgorithmDecodersMap.get(algorithm);
        }
    }

    /**
     * @return A <U>copy</U> {@link SortedMap} of the currently registered
     * decoders where key=the key type (case insensitive), value=the {@link KeyDecoder}
     * @see #registerDecoder(KeyDecoder)
     * @see #getKeyDecoderByKeyType(String)
     */
    public static final SortedMap<String,KeyDecoder> getDecodersByKeyType() {
        SortedMap<String,KeyDecoder>  result=new TreeMap<String,KeyDecoder>(String.CASE_INSENSITIVE_ORDER);
        synchronized(byKeyTypeDecodersMap) {
            result.putAll(byKeyTypeDecodersMap);
        }
        
        return result;
    }

    /**
     * @return A <U>copy</U> {@link SortedMap} of the currently registered
     * decoders where key=the algorithm (case insensitive), value=the {@link KeyDecoder}
     * @see #registerDecoder(KeyDecoder)
     * @see #getKeyDecoderByKeyType(String)
     */
    public static final SortedMap<String,KeyDecoder> getDecodersByAlgorithm() {
        SortedMap<String,KeyDecoder>  result=new TreeMap<String,KeyDecoder>(String.CASE_INSENSITIVE_ORDER);
        synchronized(byAlgorithmDecodersMap) {
            result.putAll(byAlgorithmDecodersMap);
        }
        
        return result;
    }
    
    /**
     * Standard extension used by OpenSSH to store public key files
     */
    public static final String PUBLIC_KEYFILE_EXT=".pub";
    
    /**
     * Standard filename prefix used by OpenSSH to store key files
     */
    public static final String STD_KEYFILE_NAME_PREFIX="id_";

    /**
     * Standard folder name used by OpenSSH to hold key files
     */
    public static final String STD_KEYFILE_FOLDER_NAME=".ssh";

    /**
     * @param algorithm The required algorithm (case insensitive) - may not
     * be {@code null}/empty
     * @return The standard OpenSSH file name used to hold the public key for
     * the specified algorithm
     */
    public static final String getStandardPublicKeyFilename(String algorithm) {
        return getStandardKeyFilename(algorithm, true);
    }
    
    /**
     * @param algorithm The required algorithm (case insensitive) - may not
     * be {@code null}/empty
     * @return The standard OpenSSH file name used to hold the private key for
     * the specified algorithm
     */
    public static final String getStandardPrivateKeyFilename(String algorithm) {
        return getStandardKeyFilename(algorithm, false);
    }
    
    /**
     * @param algorithm The required algorithm (case insensitive) - may not
     * be {@code null}/empty
     * @param publicKey {@code true} retrieve the public key file name,
     * {@code false} the private one
     * @return The standard OpenSSH file name used to hold the public/private key for
     * the specified algorithm
     */
    public static final String getStandardKeyFilename(String algorithm, boolean publicKey) {
        Validate.notEmpty(algorithm, "No key algorithm specified", ArrayUtils.EMPTY_OBJECT_ARRAY);
        // TODO consider caching these values since they are constants in effect
        return new StringBuilder(STD_KEYFILE_NAME_PREFIX.length() + algorithm.length() + PUBLIC_KEYFILE_EXT.length() /* worst case */)
                        .append(STD_KEYFILE_NAME_PREFIX)
                        .append(algorithm.toLowerCase())
                        .append(publicKey ? PUBLIC_KEYFILE_EXT : "")
                    .toString();
    }

    /**
     * Loads a {@link KeyPair} represented by 2 {@link File}-s: a public one having
     * the {@link #PUBLIC_KEYFILE_EXT} and a private one with no extension (similar
     * to <code>ssh-keygen</code> results)
     * @param dir The directory where the 2 files reside
     * @param baseName The base name - the private key should reside in this file,
     * whereas the public one is assumed to reside in a file having the same name
     * but with the {@link #PUBLIC_KEYFILE_EXT} extension
     * @param password The private key password ({@code null}/empty if not encrypted)
     * @return A {@link KeyPair} with the loaded files
     * @throws IOException If failed to read or decode the keys
     * @see #loadPublicKey(File)
     * @see #loadPEMPrivateKey(File, String)
     */
    public static final KeyPair loadKeyPair(File dir, String baseName, String password) throws IOException {
        PublicKey   pubKey=KeyUtils.loadPublicKey(new File(dir, baseName + PUBLIC_KEYFILE_EXT));
        PrivateKey  prvKey=KeyUtils.loadPEMPrivateKey(new File(dir, baseName), password);
        return new KeyPair(pubKey, prvKey);
    }

    /**
     * Loads a {@link KeyPair} represented by 2 {@link File}-s: a public one having
     * the {@link #PUBLIC_KEYFILE_EXT} and a private one with no extension (similar
     * to <code>ssh-keygen</code> results)
     * @param url The base URL pointing to the <U>private</U> key - the public key
     * URL is derived by appending the {@link #PUBLIC_KEYFILE_EXT} extension to the URL
     * @param password The private key password ({@code null}/empty if not encrypted)
     * @return A {@link KeyPair} with the loaded files
     * @throws IOException If failed to read or decode the keys
     * @see #loadPublicKey(URL)
     * @see #loadPEMPrivateKey(URL, String)
     */
    public static final KeyPair loadKeyPair(URL url, String password) throws IOException {
        String      baseURL=url.toExternalForm();
        PublicKey   pubKey=KeyUtils.loadPublicKey(new URL(baseURL + PUBLIC_KEYFILE_EXT));
        PrivateKey  prvKey=KeyUtils.loadPEMPrivateKey(url, password);
        return new KeyPair(pubKey, prvKey);
    }

    private static final class LazyDefaultKeysFolderHolder {
        private static final File   folder=
                new File(SystemUtils.USER_HOME + File.separator + STD_KEYFILE_FOLDER_NAME);
    }
    /**
     * @return The default OpenSSH folder used to hold key files
     */
    @SuppressWarnings("synthetic-access")
    public static final File getDefaultKeysFolder() {
        return LazyDefaultKeysFolderHolder.folder;
    }

    /**
     * @param algorithm The required algorithm (case insensitive) - may not
     * be {@code null}/empty
     * @param password The private key password ({@code null}/empty if not encrypted)
     * @return The loaded {@link KeyPair} from the default OpenSSH location
     * @throws IOException If failed to load the keys
     * @see #getDefaultKeysFolder()
     */
    public static final KeyPair loadDefaultKeyPair(String algorithm, String password) throws IOException {
        return loadKeyPair(getDefaultKeysFolder(), getStandardPrivateKeyFilename(algorithm), password);
    }

    /**
     * @param algorithm The required algorithm (case insensitive) - may not
     * be {@code null}/empty
     * @return The loaded {@link PublicKey} from the default OpenSSH location
     * @throws IOException If failed to load the key
     * @see #getDefaultKeysFolder()
     */
    public static final PublicKey loadDefaultPublicKey(String algorithm) throws IOException {
        return loadPublicKey(new File(getDefaultKeysFolder(), getStandardPublicKeyFilename(algorithm)));
    }

    /**
     * @param algorithm The required algorithm (case insensitive) - may not
     * be {@code null}/empty
     * @param password The private key password ({@code null}/empty if not encrypted)
     * @return The loaded {@link PrivateKey} from the default OpenSSH location
     * @throws IOException If failed to load the key
     * @see #getDefaultKeysFolder()
     */
    public static final PrivateKey loadDefaultPrivateKey(String algorithm, String password) throws IOException {
        return loadPEMPrivateKey(new File(getDefaultKeysFolder(), getStandardPrivateKeyFilename(algorithm)), password);
    }

    /**
     * @param file The {@link File} to load the public key from
     * @return The decoded {@link PublicKey}
     * @throws IOException If failed to access the file or decode its contents
     */
    public static final PublicKey loadPublicKey(File file) throws IOException {
        BufferedReader  rdr=new BufferedReader(new FileReader(file));
        try {
            return loadPublicKey(rdr);
        } finally {
            rdr.close();
        }
    }

    /**
     * @param url The {@link URL} to load the public key from
     * @return The decoded {@link PublicKey}
     * @throws IOException If failed to access the resource or decode its contents
     * @see #loadPublicKey(InputStream, boolean)
     */
    public static final PublicKey loadPublicKey(URL url) throws IOException {
        return loadPublicKey(url.openStream(), true);
    }
    
    /**
     * @param in The {@link InputStream} containing the public key data
     * @param okToClose <code>true</code> if OK to close the stream regardless
     * of success or failure
     * @return The decoded {@link PublicKey}
     * @throws IOException If failed to access the data or decode its contents
     * @see #loadPublicKey(BufferedReader)
     */
    public static final PublicKey loadPublicKey(InputStream in, boolean okToClose) throws IOException {
        @SuppressWarnings("resource")
        BufferedReader  rdr=new BufferedReader(
                new InputStreamReader(okToClose ? in : new CloseShieldInputStream(in)));
        try {
            return loadPublicKey(rdr);
        } finally {
            rdr.close();
        }
    }

    /**
     * @param rdr The {@link BufferedReader} containing the public key data
     * @return The decoded {@link PublicKey}
     * @throws IOException If failed to access the data or decode its contents
     */
    public static final PublicKey loadPublicKey(BufferedReader rdr) throws IOException {
        Collection<CryptoKeyEntry>  entries=CryptoKeyEntry.readAuthorizedKeys(rdr);
        if (ExtendedCollectionUtils.isEmpty(entries)) {
            throw new StreamCorruptedException("No key data");
        }
        
        if (ExtendedCollectionUtils.size(entries) > 1) {
            throw new StreamCorruptedException("Too many keys");
        }
        
        CryptoKeyEntry  keyEntry=ExtendedCollectionUtils.getFirstMember(entries);
        return keyEntry.decodePublicKey();  
    }

    public static final String  SSH2_PUBLIC_KEY_START_MARKER="- BEGIN SSH2 PUBLIC KEY -";
        public static final String  SSH2_PUBLIC_KEY_END_MARKER="- END SSH2 PUBLIC KEY -";

    /**
     * Reads public key as specified by the <A HREF="http://www.ietf.org/rfc/rfc4716.txt">RFC-47176</A>
     * @param file The {@link File} to load the public key from
     * @return The decoded {@link PublicKey}
     * @throws IOException If failed to access the file or decode its contents
     */
    public static final PublicKey loadSSH2PublicKey(File file) throws IOException {
        BufferedReader  rdr=new BufferedReader(new FileReader(file));
        try {
            return loadSSH2PublicKey(rdr);
        } finally {
            rdr.close();
        }
    }

    /**
     * Reads public key as specified by the <A HREF="http://www.ietf.org/rfc/rfc4716.txt">RFC-47176</A>
     * @param url The {@link URL} to load the public key from
     * @return The decoded {@link PublicKey}
     * @throws IOException If failed to access the resource or decode its contents
     * @see #loadPublicKey(InputStream, boolean)
     */
    public static final PublicKey loadSSH2PublicKey(URL url) throws IOException {
        return loadSSH2PublicKey(url.openStream(), true);
    }
    
    /**
     * Reads public key as specified by the <A HREF="http://www.ietf.org/rfc/rfc4716.txt">RFC-47176</A>
     * @param in The {@link InputStream} containing the public key data
     * @param okToClose <code>true</code> if OK to close the stream regardless
     * of success or failure
     * @return The decoded {@link PublicKey}
     * @throws IOException If failed to access the data or decode its contents
     * @see #loadPublicKey(BufferedReader)
     */
    public static final PublicKey loadSSH2PublicKey(InputStream in, boolean okToClose) throws IOException {
        @SuppressWarnings("resource")
        BufferedReader  rdr=new BufferedReader(
                new InputStreamReader(okToClose ? in : new CloseShieldInputStream(in)));
        try {
            return loadSSH2PublicKey(rdr);
        } finally {
            rdr.close();
        }
    }

    /**
     * Reads public key as specified by the <A HREF="http://www.ietf.org/rfc/rfc4716.txt">RFC-47176</A>
     * @param rdr The {link BufferedReader} to read from
     * @return The {@link PublicKey}
     * @throws IOException If failed to parse and load the key
     */
    public static final PublicKey loadSSH2PublicKey(BufferedReader rdr) throws IOException {
        for (String    line=rdr.readLine(); line != null; line=rdr.readLine()) {
            line = line.trim();
            
            if (line.contains(SSH2_PUBLIC_KEY_START_MARKER)) {
                line = skipKeyFileHeaders(rdr);

                StringBuilder   keyData=new StringBuilder(256 + line.length()).append(line);
                for (line = rdr.readLine(); line != null; line=rdr.readLine()) {
                    line = line.trim();
                    
                    if (StringUtils.isEmpty(line)) {
                        continue;
                    }
                    
                    if (line.contains(SSH2_PUBLIC_KEY_END_MARKER)) {
                        return decodePublicKey(keyData.toString());
                    }
                    
                    keyData.append(line);
                }

                throw new StreamCorruptedException("Cannot find SSH2 key data end");
            }
        }
        
        throw new StreamCorruptedException("Bad/invalid SSH2 file format");
    }

    public static final String skipKeyFileHeaders(BufferedReader rdr) throws IOException {
        for (String line = rdr.readLine(); line != null; line=rdr.readLine()) {
            line = line.trim();
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            
            // skip continuation header lines
            if (line.charAt(line.length() - 1) == '\\') {
                String  firstLine=line;
                do {
                    line = rdr.readLine();
                    if (line == null) {
                        throw new StreamCorruptedException("Incomplete continued header: " + firstLine);
                    }
    
                    line = line.trim();
                } while((line.length() > 0) && (line.charAt(line.length() - 1) == '\\'));

                continue;
            }

            if (line.indexOf(':') < 0) {
                return line;
            }
        }
        
        throw new StreamCorruptedException("No start of data found");
    }

    /**
     * @param b64Data BASE64 encoded public key data - optionally, may also
     * contain the &quot;ssh-rsa&quot; prefix and username suffix (which are
     * automatically detected and stripped 
     * @return The decoded {@link PublicKey}
     * @throws IOException If required key algorithm not supported
     */
    public static final PublicKey decodePublicKey(String b64Data) throws IOException {
        String  effData=Validate.notEmpty(StringUtils.trimToEmpty(b64Data), "No key data", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        int pos=effData.indexOf(' ');
        if (pos < 0) {  // check if key type specified and known
            return decodePublicKey(Base64.decodeBase64(effData));
        }

        String      keyType=StringUtils.trimToEmpty(effData.substring(0, pos));
        KeyDecoder  decoder=getKeyDecoderByKeyType(keyType);
        if (decoder == null) {
            throw new StreamCorruptedException("Unknown key type to decode in: " + b64Data);
        }

        effData = StringUtils.trimToEmpty(effData.substring(pos + 1));
        
        if ((pos=effData.indexOf(' ')) > 0) {   // strip username if found
            effData = StringUtils.trimToEmpty(effData.substring(0, pos));
        }

        if (StringUtils.isEmpty(effData)) {
            throw new StreamCorruptedException("No BASE64 data in " + b64Data);
        }
        
        PublicKey   key=decodePublicKey(Base64.decodeBase64(effData));
        String      expectedAlgorithm=decoder.getAlgorithm(), actualAlgorithm=key.getAlgorithm();
        if (!expectedAlgorithm.equalsIgnoreCase(actualAlgorithm)) {
            throw new StreamCorruptedException("Mismatched public key algorithms"
                    + " (expected=" + expectedAlgorithm + ", actual=" + actualAlgorithm + ")"
                    + " in " + b64Data);
        }
        
        return key;
    }

    /**
     * @param keyBytes The public key bytes
     * @return The decoded {@link PublicKey} instance
     * @throws IOException If invalid bytes contents
     */
    public static final PublicKey decodePublicKey(byte ... keyBytes) throws IOException {
        return decodePublicKey(keyBytes, 0, keyBytes.length);
    }

    /**
     * @param keyBytes The public key bytes
     * @param off Offset of key bytes in the array
     * @param len Number of key bytes
     * @return The decoded {@link PublicKey} instance
     * @throws IOException If invalid bytes contents
     */
    public static final PublicKey decodePublicKey(byte[] keyBytes, int off, int len) throws IOException {
        return decodePublicKey(new ByteArrayInputStream(keyBytes, off, len));
    }

    /**
     * @param s The {@link InputStream} to read from
     * @return The decoded {@link PublicKey} instance
     * @throws IOException If invalid bytes contents
     */
    public static final PublicKey decodePublicKey(InputStream s) throws IOException {
        String      type=AbstractKeyDecoder.decodeString(s);
        KeyDecoder  decoder=getKeyDecoderByKeyType(type);
        if (decoder == null) {
            throw new StreamCorruptedException("No decoder found for key type=" + type);
        }
        
        return decoder.decodePublicKey(s);
    }
    
    /**
     * An {@link ExtendedTransformer} that invokes the {@link #recoverPublicKey(PrivateKey)}
     * method. Throws a {@link RuntimeException} if exception encountered during recovery
     */
    public static final ExtendedTransformer<PrivateKey,PublicKey> PUBLIC_KEY_RECOVERER=
            new AbstractExtendedTransformer<PrivateKey,PublicKey>(PrivateKey.class, PublicKey.class) {
                @Override
                public PublicKey transform(PrivateKey input) {
                    if (input == null) {
                        return null;
                    }
                    
                    try {
                        return recoverPublicKey(input);
                    } catch(GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
    /**
     * @param privateKey The {@link PrivateKey}
     * @return The matching {@link PublicKey}
     * @throws GeneralSecurityException If failed to recover the key
     * @see #getKeyDecoderByAlgorithm(String)
     * @see KeyDecoder#recoverPublicKey(PrivateKey)
     */
    public static final PublicKey recoverPublicKey(PrivateKey privateKey) throws GeneralSecurityException {
        String      algorithm=privateKey.getAlgorithm();
        KeyDecoder  decoder=getKeyDecoderByAlgorithm(algorithm);
        if (decoder == null) {
            throw new NoSuchAlgorithmException("recoverPublicKey(" + algorithm + ") unsupported");
        }
        return decoder.recoverPublicKey(privateKey);
    }

    public static final PrivateKey loadPEMPrivateKey(URL url, String password) throws IOException {
        return loadPEMPrivateKey(url.openStream(), true, password);
    }

    public static final PrivateKey loadPEMPrivateKey(InputStream in, boolean okToClose, String password) throws IOException {
        Reader  rdr=new InputStreamReader(ExtendedCloseShieldInputStream.resolveInputStream(in, okToClose));
        try {
            return loadPEMPrivateKey(rdr, true, password);
        } finally {
            rdr.close();
        }
    }

    public static final PrivateKey loadPEMPrivateKey(Reader rdr, boolean okToClose, String password) throws IOException {
        BufferedReader  buf=new BufferedReader(CloseShieldReader.resolveReader(rdr, okToClose));
        try {
            return loadPEMPrivateKey(buf, password);
        } finally {
            buf.close();
        }
    }
    /**
     * The usual extension associated with PEM files
     */
    public static final String PEM_KEYFILE_EXT=".pem";

    /**
     * @param file The {@link File} containing the PEM data
     * @return The loaded {@link PrivateKey}
     * @param password The private key password ({@code null}/empty if not encrypted)
     * @throws IOException If failed to read or decode data
     * @see #loadPEMPrivateKey(BufferedReader, String)
     */
    public static final PrivateKey loadPEMPrivateKey(File file, String password) throws IOException {
        BufferedReader  rdr=new BufferedReader(new FileReader(file));
        try {
            return loadPEMPrivateKey(rdr, password);
        } finally {
            rdr.close();
        }
    }

    public static final String PEM_PKCS8_BEGIN_MARKER= "-BEGIN PRIVATE KEY-";
        public static final String PEM_PKCS8_END_MARKER="-END PRIVATE KEY-";

    /**
     * @param rdr The {@link BufferedReader} through which to read the PEM data
     * @param password The private key password ({@code null}/empty if not encrypted)
     * @return The loaded {@link PrivateKey}
     * @throws IOException If failed to read or decode data
     */
    public static final PrivateKey loadPEMPrivateKey(BufferedReader rdr, String password) throws IOException {
        Map<String,KeyDecoder>  decodersMap=getDecodersByKeyType();
        Collection<KeyDecoder>  decoders=decodersMap.values();
        for (String    line=rdr.readLine(); line != null; line=rdr.readLine()) {
             line = line.trim();

             // special case
             if (line.contains(PEM_PKCS8_BEGIN_MARKER)) {
                 return loadPEMPrivateKeyPCKS8(rdr);
             }

             // NOTE !!! we ignore any headers and extra data
             for (KeyDecoder d : decoders) {
                 Predicate<? super String>  marker=d.getPEMBeginMarker();
                 if (marker.evaluate(line)) {
                     return d.decodePEMPrivateKey(rdr, password);
                 }
             }
         }

         throw new StreamCorruptedException("Invalid/Unsupported PEM file format");
    }

    // NOTE: assumes reader positioned AFTER begin marker
    public static final PrivateKey loadPEMPrivateKeyPCKS8(BufferedReader rdr) throws IOException {
        StringBuilder   sb=new StringBuilder(1024);
        for (String    line=rdr.readLine(); line != null; line=rdr.readLine()) {
            line = line.trim();
            
            if (line.contains(PEM_PKCS8_END_MARKER)) {
                return decodePEMPrivateKeyPKCS8(sb.toString());
            }
            
            sb.append(line);
        }
        
        throw new StreamCorruptedException("Missing PEM PKCS8 end marker");
    }

    public static final PrivateKey decodePEMPrivateKeyPKCS8(String keyData) throws IOException {
        return decodePEMPrivateKeyPKCS8(new Base64().decode(keyData));
    }

    public static final PrivateKey decodePEMPrivateKeyPKCS8(byte... keyBytes) throws IOException {
        try {
            return RSAKeyDecoder.DECODER.generatePrivateKey(new PKCS8EncodedKeySpec(keyBytes));
        } catch(GeneralSecurityException e) {
            throw new IOException("Failed (" + e.getClass().getSimpleName() + ") to generate key: " + e.getMessage(), e);
        }
    }
}
