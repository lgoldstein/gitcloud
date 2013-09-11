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

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedTransformer;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 8:32:24 AM
 */
public interface KeyLoader {
    /**
     * @return The key type name - e.g., &quot;ssh-rsa&quot;, &quot;ssh-dss&quot; 
     */
    String getKeyType();
    
    /**
     * An {@link ExtendedTransformer} that invokes {@link #getKeyType()}
     */
    static final ExtendedTransformer<KeyLoader,String> KEY_TYPE_EXTRACTOR=
            new AbstractExtendedTransformer<KeyLoader,String>(KeyLoader.class, String.class) {
                @Override
                public String transform(KeyLoader l) {
                    if (l == null) {
                        return null;
                    } else {
                        return l.getKeyType();
                    }
                }
            };

    /**
     * @return The encryption algorithm name - e.g., &quot;RSA&quot;, &quot;DSA&quot;
     */
    String getAlgorithm();

    /**
     * An {@link ExtendedTransformer} that invokes {@link #getAlgorithm()}
     */
    static final ExtendedTransformer<KeyLoader,String> ALGORITHM_EXTRACTOR=
            new AbstractExtendedTransformer<KeyLoader,String>(KeyLoader.class, String.class) {
                @Override
                public String transform(KeyLoader l) {
                    if (l == null) {
                        return null;
                    } else {
                        return l.getAlgorithm();
                    }
                }
            };

    /**
     * @return The {@link KeyFactory} for the specified algorithm
     * @throws NoSuchAlgorithmException If algorithm not supported
     * @see #getAlgorithm()
     */
    KeyFactory getKeyFactoryInstance() throws NoSuchAlgorithmException;
    KeyFactory getKeyFactoryInstance(String provider) throws NoSuchAlgorithmException, NoSuchProviderException;
    KeyFactory getKeyFactoryInstance(Provider provider) throws NoSuchAlgorithmException;
    
    /**
     * @param keySpec The {@link KeySpec}
     * @return The generated {@link PublicKey}
     * @throws NoSuchAlgorithmException If failed to instantiate a {@link KeyFactory}
     * @throws InvalidKeySpecException If bad key specification
     * @see #getKeyFactoryInstance()
     */
    PublicKey generatePublicKey(KeySpec keySpec) throws NoSuchAlgorithmException, InvalidKeySpecException;

    /**
     * @param keySpec The {@link KeySpec}
     * @return The generated {@link PrivateKey}
     * @throws NoSuchAlgorithmException If failed to instantiate a {@link KeyFactory}
     * @throws InvalidKeySpecException If bad key specification
     * @see #getKeyFactoryInstance()
     */
    PrivateKey generatePrivateKey(KeySpec keySpec) throws NoSuchAlgorithmException, InvalidKeySpecException;
}
