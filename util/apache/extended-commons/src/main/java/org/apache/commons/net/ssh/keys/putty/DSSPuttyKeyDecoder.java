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
import java.security.spec.DSAPrivateKeySpec;

import org.apache.commons.net.ssh.keys.DSSKeyDecoder;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 11:31:52 AM
 */
public class DSSPuttyKeyDecoder extends AbstractPuttyKeyDecoder {
    public static final DSSPuttyKeyDecoder  DECODER=new DSSPuttyKeyDecoder();

    public DSSPuttyKeyDecoder() {
        super(DSSKeyDecoder.SSH_DSS, DSSKeyDecoder.DSS_ALGORITHM);
    }

    @Override
    protected PrivateKey decodePrivateKey(PuttyKeyReader pubReader, PuttyKeyReader prvReader) throws IOException {
        pubReader.skip();   // skip version

        BigInteger  p=pubReader.readInt();
        BigInteger  q=pubReader.readInt();
        BigInteger  g=pubReader.readInt();
        @SuppressWarnings("unused")
        BigInteger  y=pubReader.readInt();  // don't need it, but have to read it to get to x
        BigInteger  x=prvReader.readInt();


        try {
            return generatePrivateKey(new DSAPrivateKeySpec(x, p, q, g));
        } catch(GeneralSecurityException e) {
            throw new IOException("Failed (" + e.getClass().getSimpleName() + ") to generate key: " + e.getMessage(), e);
        }
    }
}
