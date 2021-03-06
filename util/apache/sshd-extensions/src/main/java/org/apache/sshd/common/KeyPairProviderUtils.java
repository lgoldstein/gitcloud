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

package org.apache.sshd.common;

import java.security.KeyPair;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 3, 2013 2:46:38 PM
 */
public class KeyPairProviderUtils {
    /**
     * A {@link KeyPairProvider} that has no keys
     */
    public static final KeyPairProvider EMPTY_KEYPAIR_PROVIDER=
            new KeyPairProvider() {
                private static final String TYPES=SSH_RSA + "," + SSH_DSS;

                @Override
                public KeyPair loadKey(String type) {
                    return null;
                }

                @Override
                public String getKeyTypes() {
                    return TYPES;
                }
            };
}
