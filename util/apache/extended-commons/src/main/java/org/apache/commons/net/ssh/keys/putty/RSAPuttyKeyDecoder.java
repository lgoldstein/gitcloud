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

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateCrtKeySpec;

import org.apache.commons.net.ssh.keys.RSAKeyDecoder;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 11:28:09 AM
 */
public class RSAPuttyKeyDecoder extends AbstractPuttyKeyDecoder {
    public static final RSAPuttyKeyDecoder  DECODER=new RSAPuttyKeyDecoder();

    public RSAPuttyKeyDecoder() {
        super(RSAKeyDecoder.SSH_RSA, RSAKeyDecoder.RSA_ALGORITHM);
    }

    @Override
    protected PrivateKey decodePrivateKey(PuttyKeyReader pubReader, PuttyKeyReader prvReader) throws IOException {
        pubReader.skip();

        BigInteger publicExp=pubReader.readInt(), modulus=pubReader.readInt();
        BigInteger privateExp = prvReader.readInt();
        BigInteger prime1 = prvReader.readInt();
        BigInteger prime2 = prvReader.readInt();
        BigInteger crtCoef = prvReader.readInt();

        BigInteger exp1 = privateExp.mod(prime1.subtract(BigInteger.ONE));
        BigInteger exp2 = privateExp.mod(prime2.subtract(BigInteger.ONE));

        try {
            return generatePrivateKey(new RSAPrivateCrtKeySpec(modulus, publicExp, privateExp, prime1, prime2, exp1, exp2, crtCoef));
        } catch(GeneralSecurityException e) {
            throw new IOException("Failed (" + e.getClass().getSimpleName() + ") to generate key: " + e.getMessage(), e);
        }
    }
}
