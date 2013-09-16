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

package org.apache.commons.net.ssh.keys.putty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.input.CloseShieldReader;
import org.apache.commons.io.input.ExtendedCloseShieldInputStream;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ssh.keys.KeyUtils;
import org.apache.commons.net.ssh.keys.dss.DSSPuttyKeyDecoder;
import org.apache.commons.net.ssh.keys.rsa.RSAPuttyKeyDecoder;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 11:11:37 AM
 */
public class PuttyKeyUtils {
    private static final Map<String,PuttyKeyDecoder> byKeyTypeDecodersMap=new TreeMap<String,PuttyKeyDecoder>(String.CASE_INSENSITIVE_ORDER);
    private static final Map<String,PuttyKeyDecoder> byAlgorithmDecodersMap=new TreeMap<String,PuttyKeyDecoder>(String.CASE_INSENSITIVE_ORDER);

    // pre-register some built in decoders
    static {
        registerDecoder(RSAPuttyKeyDecoder.DECODER);
        registerDecoder(DSSPuttyKeyDecoder.DECODER);
    }

    /**
     * @param decoder Registers the specified {@link PuttyKeyDecoder} in the cached
     * map(s) - replacing any previous one
     * @see #getPuttyKeyDecoderByAlgorithm(String)
     * @see #getPuttyKeyDecoderByKeyType(String)
     */
    public static final void registerDecoder(PuttyKeyDecoder decoder) {
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
     * @return The registered {@link PuttyKeyDecoder} for the specified type -
     * <code>null</code> if no match found
     * @see #registerDecoder(PuttyKeyDecoder)
     */
    public static final PuttyKeyDecoder getPuttyKeyDecoderByKeyType(String keyType) {
        synchronized(byKeyTypeDecodersMap) {
            return byKeyTypeDecodersMap.get(keyType);
        }
    }

    /**
     * @param algorithm The algorithm (case insensitive)
     * @return The registered {@link PuttyKeyDecoder} for the specified
     * algorithm - <code>null</code> if no match found
     * @see #registerDecoder(PuttyKeyDecoder)
     */
    public static final PuttyKeyDecoder getPuttyKeyDecoderByAlgorithm(String algorithm) {
        synchronized(byAlgorithmDecodersMap) {
            return byAlgorithmDecodersMap.get(algorithm);
        }
    }

    /**
     * @return A <U>copy</U> {@link SortedMap} of the currently registered
     * decoders where key=the key type (case insensitive), value=the {@link PuttyKeyDecoder}
     * @see #registerDecoder(PuttyKeyDecoder)
     * @see #getPuttyKeyDecoderByKeyType(String)
     */
    public static final SortedMap<String,PuttyKeyDecoder> getDecodersByKeyType() {
        SortedMap<String,PuttyKeyDecoder>  result=new TreeMap<String,PuttyKeyDecoder>(String.CASE_INSENSITIVE_ORDER);
        synchronized(byKeyTypeDecodersMap) {
            result.putAll(byKeyTypeDecodersMap);
        }
        
        return result;
    }

    /**
     * @return A <U>copy</U> {@link SortedMap} of the currently registered
     * decoders where key=the algorithm (case insensitive), value=the {@link PuttyKeyDecoder}
     * @see #registerDecoder(PuttyKeyDecoder)
     * @see #getPuttyKeyDecoderByKeyType(String)
     */
    public static final SortedMap<String,PuttyKeyDecoder> getDecodersByAlgorithm() {
        SortedMap<String,PuttyKeyDecoder>  result=new TreeMap<String,PuttyKeyDecoder>(String.CASE_INSENSITIVE_ORDER);
        synchronized(byAlgorithmDecodersMap) {
            result.putAll(byAlgorithmDecodersMap);
        }
        
        return result;
    }

    /**
     * The standard file extension for PuTTY key files
     */
    public static final String  PPK_FILE_EXT=".ppk";
    
    /**
     * Loads a {@link KeyPair} from PuTTY's &quot;.ppk&quot; file.
     * @param file The PuTTY key {@link File}
     * @param password The password used to protect the private key - if
     * {@code null}/empty then no password protection is used
     * @return The loaded {@link KeyPair}
     * @throws IOException If unable to read or decode the data
     * @see #loadPuttyKeyPair(BufferedReader,String)
     */
    public static final KeyPair loadPuttyKeyPair(File file, String password) throws IOException {
        BufferedReader  rdr=new BufferedReader(new FileReader(file));
        try {
            return loadPuttyKeyPair(rdr, password);
        } finally {
            rdr.close();
        }
    }

    /**
     * Loads a {@link KeyPair} from PuTTY's &quot;.ppk&quot; file.
     * @param url The PuTTY key {@link URL}
     * @param password The password used to protect the private key - if
     * {@code null}/empty then no password protection is used
     * @return The loaded {@link KeyPair}
     * @throws IOException If unable to read or decode the data
     * @see #loadPuttyKeyPair(InputStream, boolean, String)
     */
    public static final KeyPair loadPuttyKeyPair(URL url, String password) throws IOException {
        return loadPuttyKeyPair(url.openStream(), true, password);
    }

    /**
     * Loads a {@link KeyPair} from PuTTY's &quot;.ppk&quot; file.
     * @param s The PuTTY key {@link InputStream}.
     * @param okToClose <code>true</code> if OK to close the input stream
     * regardless of success or failure
     * @param password The password used to protect the private key - if
     * {@code null}/empty then no password protection is used
     * @return The loaded {@link KeyPair}
     * @throws IOException If unable to read or decode the data
     * @see #loadPuttyKeyPair(Reader,boolean,String)
     */
    public static final KeyPair loadPuttyKeyPair(InputStream s, boolean okToClose, String password) throws IOException {
        Reader  rdr=new InputStreamReader(ExtendedCloseShieldInputStream.resolveInputStream(s, okToClose));
        try {
            return loadPuttyKeyPair(rdr, true, password);
        } finally {
            rdr.close();
        }
    }

    /**
     * Loads a {@link KeyPair} from PuTTY's &quot;.ppk&quot; data
     * @param rdr The PuTTY key data {@link Reader}
     * @param okToClose <code>true</code> if OK to close the input stream
     * regardless of success or failure
     * @param password The password used to protect the private key - if
     * {@code null}/empty then no password protection is used
     * @return The loaded {@link KeyPair}
     * @throws IOException If unable to read or decode the data
     * @see #loadPuttyKeyPair(BufferedReader, String)
     */
    public static final KeyPair loadPuttyKeyPair(Reader rdr, boolean okToClose, String password) throws IOException {
        BufferedReader  buf=new BufferedReader(CloseShieldReader.resolveReader(rdr, okToClose));
        try {
            return loadPuttyKeyPair(buf, password);
        } finally {
            buf.close();
        }
    }

    public static final String  PPK_ALGORITHM_HEADER="PuTTY-User-Key-File-2";
    public static final String  PPK_PUBLIC_DATA_HEADER="Public-Lines";
    public static final String  PPK_PRIVATE_DATA_HEADER="Private-Lines";
    public static final String  PPK_KEY_ENCRYPTION_HEADER="Encryption";
    
    /**
     * Loads a {@link KeyPair} from PuTTY's &quot;.ppk&quot; file.
     * <P>Note:</P>
     * <UL>
     *      <LI>
     *      The file appears to be a text file but it doesn't have the fixed encoding.
     *      So we just use the platform default encoding, which is what PuTTY seems to use.
     *      Fortunately, the important part is all ASCII, so this shouldn't really hurt
     *      the interpretation of the key.
     *      </LI>
     *      
     *      <LI>
     *      Does not supported pass-phrase protected keys (yet)
     *      </LI>
     *      
     *      <LI>
     *      Based on code from <A HREF="http://www.jarvana.com/jarvana/view/org/kohsuke/trilead-putty-extension/1.0/trilead-putty-extension-1.0-sources.jar!/org/kohsuke/putty/PuTTYKey.java?format=ok">here</A>
     *      </LI>
     * </UL>
     *
     * <P>Sample PuTTY file format</P>
     * <PRE>
     * PuTTY-User-Key-File-2: ssh-rsa
     * Encryption: none
     * Comment: rsa-key-20080514
     * Public-Lines: 4
     * AAAAB3NzaC1yc2EAAAABJQAAAIEAiPVUpONjGeVrwgRPOqy3Ym6kF/f8bltnmjA2
     * BMdAtaOpiD8A2ooqtLS5zWYuc0xkW0ogoKvORN+RF4JI+uNUlkxWxnzJM9JLpnvA
     * HrMoVFaQ0cgDMIHtE1Ob1cGAhlNInPCRnGNJpBNcJ/OJye3yt7WqHP4SPCCLb6nL
     * nmBUrLM=
     * Private-Lines: 8
     * AAAAgGtYgJzpktzyFjBIkSAmgeVdozVhgKmF6WsDMUID9HKwtU8cn83h6h7ug8qA
     * hUWcvVxO201/vViTjWVz9ALph3uMnpJiuQaaNYIGztGJBRsBwmQW9738pUXcsUXZ
     * 79KJP01oHn6Wkrgk26DIOsz04QOBI6C8RumBO4+F1WdfueM9AAAAQQDmA4hcK8Bx
     * nVtEpcF310mKD3nsbJqARdw5NV9kCxPnEsmy7Sy1L4Ob/nTIrynbc3MA9HQVJkUz
     * 7V0va5Pjm/T7AAAAQQCYbnG0UEekwk0LG1Hkxh1OrKMxCw2KWMN8ac3L0LVBg/Tk
     * 8EnB2oT45GGeJaw7KzdoOMFZz0iXLsVLNUjNn2mpAAAAQQCN6SEfWqiNzyc/w5n/
     * lFVDHExfVUJp0wXv+kzZzylnw4fs00lC3k4PZDSsb+jYCMesnfJjhDgkUA0XPyo8
     * Emdk
     * Private-MAC: 50c45751d18d74c00fca395deb7b7695e3ed6f77
     * </PRE>
     * @param rdr The {@link BufferedReader} to read from
     * @param password The password used to protect the private key - if
     * {@code null}/empty then no password protection is used
     * @return The loaded {@link KeyPair}
     * @throws IOException if failed to read or decode the data
     */
    public static final KeyPair loadPuttyKeyPair(BufferedReader rdr, String password) throws IOException {
        /*
         * NOTE: we use case insensitive map even though it is unlikely
         * we will encounter the headers in a different case
         */
        Map<String,String>  headers=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        StringBuilder       sb=new StringBuilder(8 * 64);
        for (String    line=rdr.readLine(); line != null; line=rdr.readLine()) {
            line = line.trim();
            
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            
            int idx = line.indexOf(':');
            if (idx <= 0) {
                throw new StreamCorruptedException("Unknown line format at: " + line);
            }
            
            String  hdrName=line.substring(0, idx).trim();
            String  hdrValue=line.substring(idx+1).trim();
            if (StringUtils.isEmpty(hdrValue)) {
                continue;   // should not happen, but we are lenient
            }
            
            if (PPK_PUBLIC_DATA_HEADER.equalsIgnoreCase(hdrName) || PPK_PRIVATE_DATA_HEADER.equalsIgnoreCase(hdrName)) {
                int numLines=(-1);
                try {
                    if ((numLines=Integer.parseInt(hdrValue)) <= 0) {
                        throw new NumberFormatException("Non-positive value");
                    }
                } catch(NumberFormatException e) {
                    throw new StreamCorruptedException("Invalid line count for " + hdrName + "=" + hdrValue + ": " + e.getMessage());
                }
                
                sb.setLength(0);
                for (int index=0; index < numLines; index++) {
                    if ((line=rdr.readLine()) == null) {
                        throw new StreamCorruptedException("Premature EOF while read line #" + index + " for " + hdrName);
                    }

                    line = line.trim();
                    sb.append(line);
                }
                
                hdrValue = sb.toString();
            }
            
            String  prev=headers.put(hdrName, hdrValue);
            if (prev != null) {
                throw new StreamCorruptedException("Multiple values for header=" + hdrName + ": new=" + hdrValue + ", prev=" + prev);
            }
        }

        try {
            return decodePuttyKeyPair(headers, password);
        } catch(GeneralSecurityException e) {
            throw new StreamCorruptedException("Failed (" + e.getClass().getSimpleName() + ") to decode data: " + e.getMessage());
        }
    }

    public static final KeyPair decodePuttyKeyPair(Map<String,String> headers, String password)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        String      pubData=headers.get(PPK_PUBLIC_DATA_HEADER);
        PublicKey   pubKey;
        try {
            pubKey = KeyUtils.decodePublicKey(pubData);
        } catch(IOException e) {
            throw new InvalidKeySpecException("Failed (" + e.getClass().getSimpleName() + ")"
                                            + " to decode public key:" + e.getMessage(), e);
        }

        PrivateKey  prvKey=decodePuttyPrivateKey(headers.get(PPK_ALGORITHM_HEADER),
                                                 pubData,
                                                 headers.get(PPK_PRIVATE_DATA_HEADER),
                                                 headers.get(PPK_KEY_ENCRYPTION_HEADER),
                                                 password);
        return new KeyPair(pubKey, prvKey);
    }

    public static final PrivateKey decodePuttyPrivateKey(String algorithm, String pubData, String prvData, String prvEncryption, String password)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        PuttyKeyDecoder decoder=getPuttyKeyDecoderByKeyType(algorithm);
        if (decoder == null) {
            throw new NoSuchAlgorithmException("No decoder found for " + algorithm);
        }

        try {
            return decoder.decodePrivateKey(pubData, prvData, prvEncryption, password);
        } catch(IOException e) {
            throw new InvalidKeySpecException("decodePrivateKey(" + algorithm + ")"
                                            + " failed (" + e.getClass().getSimpleName() + ")"
                                            + " to decode: " + e.getMessage());
        }
    }
}
