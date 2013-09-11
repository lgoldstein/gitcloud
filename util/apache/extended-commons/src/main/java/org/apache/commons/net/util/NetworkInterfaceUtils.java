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

package org.apache.commons.net.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.commons.collections15.AbstractExtendedClosure;
import org.apache.commons.collections15.Closure;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Lyor G.
 */
public class NetworkInterfaceUtils {
    public static final void doWithNetworkInterfaces(Closure<NetworkInterface> closure) throws IOException {
        for (Enumeration<NetworkInterface> interfaces=NetworkInterface.getNetworkInterfaces();
             (interfaces != null) && interfaces.hasMoreElements(); ) {
            closure.execute(interfaces.nextElement());
        }
    }
    
    public static final void doWithNetworkInterfacesAddresses(final Closure<Pair<NetworkInterface,InetAddress>> closure) throws IOException {
        doWithNetworkInterfaces(new AbstractExtendedClosure<NetworkInterface>(NetworkInterface.class) {
            @Override
            public void execute(NetworkInterface ifNet) {
                for (Enumeration<InetAddress> addrs=ifNet.getInetAddresses();
                    (addrs != null) && addrs.hasMoreElements(); ) {
                    closure.execute(Pair.of(ifNet, addrs.nextElement()));
                }                
            }
        });
    }
}
