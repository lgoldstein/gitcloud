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

package org.apache.commons.net.ssh.keys;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.ExtendedHex;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ssh.keys.rsa.RSAKeyDecoder;
import org.apache.commons.test.AbstractTestSupport;

/**
 * @author Lyor G.
 * @since Sep 15, 2013 3:33:51 PM
 */
public class KeyUtilsDevelopment extends AbstractTestSupport {

    protected static final String encodeToHex(String data, PublicKey pubKey) throws GeneralSecurityException {
        Cipher  cipher=getCipherInstance(pubKey);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);

        byte[]  encBytes=cipher.doFinal(data.getBytes());
        return ExtendedHex.encodeHexString(true, encBytes);
    }

    protected static final String decodeFromHex(String data, PrivateKey prvKey) throws GeneralSecurityException, DecoderException {
        Cipher  cipher=getCipherInstance(prvKey);
        cipher.init(Cipher.DECRYPT_MODE, prvKey);
        
        byte[]  encBytes=Hex.decodeHex(data.toCharArray());
        byte[]  decBytes=cipher.doFinal(encBytes);
        return new String(decBytes);
    }

    protected static final Cipher getCipherInstance(Key key) throws GeneralSecurityException {
        return Cipher.getInstance(key.getAlgorithm() + "/ECB/PKCS1Padding");
    }

    protected static final <S extends PrintStream> S printPrivateKeyDetails(S stdout, PrivateKey  prvKey) {
        if (prvKey instanceof RSAPrivateKey) {
            RSAPrivateKey   key=(RSAPrivateKey) prvKey;
            stdout.append('\t').append(prvKey.getAlgorithm()).append(" modulus: ").println(key.getModulus());
            stdout.append('\t').append(prvKey.getAlgorithm()).append(" d: ").println(key.getPrivateExponent());
            
            if (key instanceof RSAPrivateCrtKey) {
                RSAPrivateCrtKey    crt=(RSAPrivateCrtKey) key;
                stdout.append('\t').append(prvKey.getAlgorithm()).append(" p: ").println(crt.getPrimeP());
                stdout.append('\t').append(prvKey.getAlgorithm()).append(" q: ").println(crt.getPrimeQ());
            }
        }

        return stdout;
    }

    protected static final <S extends PrintStream> S printPublicKeyDetails(S stdout, PublicKey  pubKey) {
        if (pubKey instanceof RSAPublicKey) {
            RSAPublicKey    key=(RSAPublicKey) pubKey;
            stdout.append('\t').append(pubKey.getAlgorithm()).append(" modulus: ").println(key.getModulus());
            stdout.append('\t').append(pubKey.getAlgorithm()).append(" e: ").println(key.getPublicExponent());
        }

        return stdout;
    }

    //////////////////////////////////////////////////////////////////////////

    protected static final void testEncodeDecode(BufferedReader stdin, PrintStream stdout, PrintStream stderr, String ... args) throws Exception {
        KeyPair     kp=KeyUtils.loadDefaultKeyPair(RSAKeyDecoder.RSA_ALGORITHM, null);
        PublicKey   pubKey=kp.getPublic();
        PrivateKey  prvKey=kp.getPrivate();
        printPublicKeyDetails(stdout, pubKey);
        printPrivateKeyDetails(stdout, prvKey);

        for (int argIndex=0; ; argIndex++) {
            String  expected=(argIndex < ArrayUtils.getLength(args)) ? args[argIndex] : getval(stdout, stdin, "clear text (or Quit)");
            if (StringUtils.isEmpty(expected)) {
                continue;
            }
            if (isQuit(expected)) {
                break;
            }
            
            String      ans=getval(stdout, stdin, "first [e]ncode/(d)ecode/(q)uit");
            if (isQuit(ans)) {
                break;
            }

            final char  op=StringUtils.isEmpty(ans) ? 'e' : Character.toLowerCase(ans.charAt(0));
            final String  encData;
            try {
                switch(op) {
                    case 'e'    :
                        encData = encodeToHex(expected, pubKey);
                        break;

                    case 'd'    :
                        encData = decodeFromHex(Hex.encodeHexString(expected.getBytes()), prvKey);
                        break;
                        
                    default     :
                        throw new UnsupportedOperationException("Unkown action: " + ans);
                }

                stdout.append('\t').append(expected).append(": ").println(encData);
            } catch(Exception e) {
                stderr.append(e.getClass().getSimpleName()).append(" while encrypting ").append(expected).append(": ").println(e.getMessage());
                e.printStackTrace(stderr);
                continue;
            }

            try {
                final String    actual;
                switch(op) {
                    case 'e'    :
                        actual = decodeFromHex(encData, prvKey);
                        break;
                     
                    case 'd'    :
                        actual = encodeToHex(encData, pubKey);
                        break;

                    default     :
                        throw new UnsupportedOperationException("Unkown action: " + ans);
                }

                if (expected.equals(actual)) {
                    stdout.append('\t').append(encData).append(": ").println(actual);
                } else {
                    stderr.append("Mismatched decryption result: expected=").append(expected).append(", actual=").println(actual);
                }
            } catch(Exception e) {
                stderr.append(e.getClass().getSimpleName()).append(" while decrypting ").append(expected).append(": ").println(e.getMessage());
                e.printStackTrace(stderr);
                continue;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    protected static final void testPasswordlessDecode(BufferedReader stdin, PrintStream stdout, PrintStream stderr, String ... args) throws Exception {
        KeyPair     kp=KeyUtils.loadDefaultKeyPair(RSAKeyDecoder.RSA_ALGORITHM, null);
        PublicKey   pubKey=kp.getPublic();
        PrivateKey  prvKey=kp.getPrivate();
        printPublicKeyDetails(stdout, pubKey);
        printPrivateKeyDetails(stdout, prvKey);

        for (int argIndex=0; ; argIndex++) {
            String  decrypted=(argIndex < ArrayUtils.getLength(args)) ? args[argIndex] : getval(stdout, stdin, "encoded text (or Quit)");
            if (StringUtils.isEmpty(decrypted)) {
                continue;
            }
            if (isQuit(decrypted)) {
                break;
            }
            
            try {
                byte[]  encBytes=Hex.decodeHex(decrypted.toCharArray());
                Cipher  cipher=Cipher.getInstance(pubKey.getAlgorithm() + "/ECB/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, pubKey);

                byte[]  decBytes=cipher.doFinal(encBytes);
                String  encrypted=null;
                for (int index=0; index < decBytes.length; index++) {
                    if (decBytes[index] != 0) {
                        encrypted = new String(decBytes, index, decBytes.length - index);

                        for (index++; index < decBytes.length; index++) {
                            if (decBytes[index] == 0) {
                                stderr.append("Zero value at index=").println(index);
                            }
                        }
                        break;
                    }
                }
                stdout.append('\t').println(encrypted);
            } catch(Exception e) {
                stderr.append(e.getClass().getSimpleName()).append(" while decrypting ").append(decrypted).append(": ").println(e.getMessage());
                e.printStackTrace(stderr);
                continue;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    protected static final void testRecoverPEMPublicKeys(BufferedReader stdin, PrintStream stdout, PrintStream stderr, String ... args) throws Exception {
        for (int argIndex=0; ; argIndex++) {
            String  pemPath=(argIndex < ArrayUtils.getLength(args)) ? args[argIndex] : getval(stdout, stdin, "PEM path (or Quit)");
            if (StringUtils.isEmpty(pemPath)) {
                continue;
            }
            if (isQuit(pemPath)) {
                break;
            }

            String  password=getval(stdout, stdin, "password [ENTER=none]/(Q)uit");
            if (isQuit(password)) {
                continue;
            }

            try {
                PrivateKey  prvKey=KeyUtils.loadPEMPrivateKey(new File(pemPath), password);
                printPrivateKeyDetails(stdout, prvKey);

                PublicKey   pubKey=KeyUtils.recoverPublicKey(prvKey);
                printPublicKeyDetails(stdout, pubKey);
                
                KeyUtils.appendOpenSSHPublicKey(stdout, pubKey, "community@localhost").println();
            } catch(Exception e) {
                stderr.append(e.getClass().getSimpleName()).append(" while loading ").append(pemPath).append(": ").println(e.getMessage());
                e.printStackTrace(stderr);
                continue;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
        try {
//            testEncodeDecode(getStdin(), System.out, System.err, args);
//            testPasswordlessDecode(getStdin(), System.out, System.err, args);
            testRecoverPEMPublicKeys(getStdin(), System.out, System.err, args);
        } catch(Throwable t) {
            System.err.append(t.getClass().getSimpleName()).append(": ").println(t.getMessage());
            t.printStackTrace(System.err);
        }
    }
}
