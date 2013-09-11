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

package org.apache.commons.codec.signatures;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.net.ssh.keys.RSAKeyDecoder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Sep 9, 2013 1:00:01 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SignerWriterTest extends AbstractSignatureTestSupport {
    public SignerWriterTest() {
        super();
    }

    @Test
    public void testSignRSAWrittenData() throws Exception {
        testSignData(RSAKeyDecoder.RSA_ALGORITHM, 1024);
    }
    
    private void testSignData(String algorithm, int numBits)
            throws GeneralSecurityException, IOException {
        KeyPair     kp=generateKeyPair(algorithm, numBits);
        PrivateKey  prvKey=kp.getPrivate();
        File        file=getTestJavaSourceFile();
        byte[]      expected=SignatureUtils.signData(prvKey, file);

        SignerWriter    writer=new SignerWriter(SignatureUtils.getInstance(prvKey), prvKey, NullWriter.NULL_WRITER);
        try {
            FileReader  rdr=new FileReader(file);
            try {
                long    cpySize=IOUtils.copyLarge(rdr, writer);
                assertEquals("Mismatched copy size", file.length(), cpySize);
            } finally {
                rdr.close();
            }
        } finally {
            writer.close();
        }

        byte[]  actual=writer.sign();
        assertArrayEquals("Mismatched signature content", expected, actual);
    }
}
