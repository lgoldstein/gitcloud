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

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import org.apache.commons.lang3.ExtendedValidate;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 11:15:02 AM
 */
public abstract class AbstractKeyLoader implements KeyLoader {
    private final String    type, algorithm;

    protected AbstractKeyLoader(String keyType, String algName) {
        type = ExtendedValidate.notEmpty(keyType, "No key type specified");
        algorithm = ExtendedValidate.notEmpty(algName, "No algorithm specified");
    }

    @Override
    public final String getAlgorithm() {
        return algorithm;
    }

    @Override
    public final String getKeyType() {
        return type;
    }

    @Override
    public KeyFactory getKeyFactoryInstance() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance(getAlgorithm());
    }

    @Override
    public KeyFactory getKeyFactoryInstance(String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        return KeyFactory.getInstance(getAlgorithm(), provider);
    }

    @Override
    public KeyFactory getKeyFactoryInstance(Provider provider) throws NoSuchAlgorithmException {
        return KeyFactory.getInstance(getAlgorithm(), provider);
    }

    @Override
    public PublicKey generatePublicKey(KeySpec keySpec) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory  factory=getKeyFactoryInstance();
        return factory.generatePublic(keySpec);
    }

    @Override
    public PrivateKey generatePrivateKey(KeySpec keySpec) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory  factory=getKeyFactoryInstance();
        return factory.generatePrivate(keySpec);
    }
}
