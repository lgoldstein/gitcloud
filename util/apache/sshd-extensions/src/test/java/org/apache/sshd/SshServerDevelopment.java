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

package org.apache.sshd;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URL;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.io.output.CloseShieldWriter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ssh.keys.KeyUtils;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.file.nativefs.ExtendedNativeFileSystemFactory;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.server.AbstractCommand;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.PasswordAuthenticatorUtils;
import org.apache.sshd.server.PublickeyAuthenticatorUtils;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.test.AbstractSshdTestSupport;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 3, 2013 2:29:04 PM
 */
public class SshServerDevelopment extends AbstractSshdTestSupport {
    public static final Class<?>    ANCHOR=SshServerDevelopment.class;
    private static final ExecutorService    EXECUTORS=Executors.newFixedThreadPool(Long.SIZE);
    private static final EchoShellFactory   ECHO_SHELL=new EchoShellFactory(EXECUTORS);
    private static final KeyPairProvider    HOST_KEYS_PROVIDER=
            new KeyPairProvider() {
                private final Log   log=LogFactory.getLog(KeyPairProvider.class);
                private final Map<String,KeyPair>   pairsMap=new TreeMap<String,KeyPair>(String.CASE_INSENSITIVE_ORDER);
                private final String    KEYS=StringUtils.join(new String[] { SSH_RSA, SSH_DSS }, ',');
        
                @Override
                public KeyPair loadKey(String type) {
                    Validate.notEmpty(type, "No key type specified", ArrayUtils.EMPTY_OBJECT_ARRAY);
                    
                    KeyPair kp=null;
                    final String  resourceName;
                    synchronized(pairsMap) {
                        if ((kp=pairsMap.get(type)) != null) {
                            return kp;
                        }
                        
                        resourceName = ANCHOR.getSimpleName() + "-" + type;
                        URL     url=ANCHOR.getResource(resourceName);
                        Validate.notNull(url, resourceName, ArrayUtils.EMPTY_OBJECT_ARRAY);
                        
                        try {
                            kp = KeyUtils.loadOpenSSHKeyPair(url, null);
                        } catch (IOException e) {
                            log.error("loadKey(" + resourceName + ") failed (" + e.getClass().getSimpleName() + "): " + e.getMessage(), e);
                            return null;
                        }
                        
                        pairsMap.put(type, kp);
                    }
        
                    log.info("loadKey(" + resourceName + ")");
                    return kp;
                }
                
                @Override
                public String getKeyTypes() {
                    return KEYS;
                }
            };

    public SshServerDevelopment() {
        super();
    }

    protected static final void testSftpAccess(BufferedReader in, PrintStream out, SshServer sshd) throws Exception {
        for ( ; ; ) {
            String  ans=getval(out, in, "root folder [u]ser/c(w)d/(r)oot/XXX path/(q)uit");
            if (isQuit(ans)) {
                break;
            }

            char    op=StringUtils.isEmpty(ans) ? '\0' : Character.toLowerCase(ans.charAt(0));
            String  path=null;
            switch(op) {
                case '\0'   :
                case 'u'    :
                    path = SystemUtils.USER_HOME;
                    break;
                case 'w'    :
                    path = SystemUtils.USER_DIR;
                    break;

                case 'r'    : {
                        File[]  roots=File.listRoots();
                        if (ArrayUtils.isEmpty(roots)) {
                            System.err.println("No roots listed");
                            break;
                        }
                        
                        if (roots.length == 1) {
                            path = roots[0].getAbsolutePath();
                            break;
                        }
                        
                        File    selected=inputListChoice(out, in, "Roots", Arrays.asList(roots), null);
                        if (selected != null) {
                            path = selected.getAbsolutePath();
                        }
                    }
                    break;

                default     :  // assume a path
                    path = ans;
            }

            if (StringUtils.isEmpty(path)) {
                continue;
            }

            final File  rootDir=new File(path);
            NativeFileSystemFactory factory=new ExtendedNativeFileSystemFactory() {
                    @Override
                    protected File getSessionRootDir(Session session) {
                        return rootDir;
                    }
                };
            if (!rootDir.exists()) {
                ans = getval(out, in, "create " + rootDir + " y/[n]/q");
                if (isQuit(ans)) {
                    continue;
                }
                
                if (!StringUtils.isEmpty(ans)) {
                    factory.setCreateHome('y' == Character.toLowerCase(ans.charAt(0)));
                }
            }
            sshd.setFileSystemFactory(factory);
            break;
        }

        sshd.setSubsystemFactories(Arrays.<NamedFactory<Command>>asList(new SftpSubsystem.Factory()));
        sshd.setCommandFactory(new ScpCommandFactory());
        sshd.setShellFactory(ECHO_SHELL);
        waitForExit(in, out, sshd);
    }

    protected static final void testAuthorizedPublicKeysAuthenticator(BufferedReader in, PrintStream out, SshServer sshd) throws Exception {
        /*
        {
            URL url=ANCHOR.getResource(ANCHOR.getSimpleName() + "-authorized_keys");
            assertNotNull("Cannot find authorized keys resource", url);
            Collection<CryptoKeyEntry>  entries=CryptoKeyEntry.readAuthorizedKeys(url);
            server.setPublickeyAuthenticator(PublickeyAuthenticatorUtils.authorizedKeysAuthenticator(entries));
        }
        */
        
        final CommandFactory    cmdFactory=new CommandFactory() {
            @Override
            public Command createCommand(final String command) {
                    return new AbstractCommand(command) {
                        @Override
                        public void start(Environment env) throws IOException {
                            Map<String,String>  values=env.getEnv();
                            if (ExtendedMapUtils.isEmpty(values)) {
                                logger.warn("start(" + command + ") no environment");
                            } else {
                                for (Map.Entry<String,String> e : values.entrySet()) {
                                    logger.info("start(" + command + ")[" + e.getKey() + "]: " + e.getValue());
                                }
                            }

                            try {
                                Writer  err=new CloseShieldWriter(new OutputStreamWriter(getErrorStream()));
                                try {
                                    err.append(command).append(": N/A").append(SystemUtils.LINE_SEPARATOR);
                                } finally {
                                    err.close();
                                }
                            } finally {
                                ExitCallback    cbExit=getExitCallback();
                                cbExit.onExit(-1, "Failed");
                            }
                        }
                    };
                }
            };
        sshd.setCommandFactory(cmdFactory);
        sshd.setShellFactory(ECHO_SHELL);
        waitForExit(in, out, sshd);
    }

    /* --------------------------------------------------------------------- */

    private static final void waitForExit(BufferedReader in, PrintStream out, SshServer sshd) throws IOException {
        out.append("Listening on ").println(sshd.getPort());

        for (sshd.start(); ; ) {
            String  ans=getval(out, in, "(q)uit");
            if (isQuit(ans)) {
                break;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    
    public static void main(String[] args) {
        SshServer   sshd=SshServer.setUpDefaultServer();
        sshd.setPort(2222);
        sshd.setPasswordAuthenticator(PasswordAuthenticatorUtils.ACCEPT_ALL_AUTHENTICATOR);
        sshd.setPublickeyAuthenticator(PublickeyAuthenticatorUtils.ACCEPT_ALL_AUTHENTICATOR);
        // provides the host key(s) - e.g., on Ubuntu these reside in the "/etc/ssh" folder
        sshd.setKeyPairProvider(HOST_KEYS_PROVIDER);

        try {
            try {
//              testAuthorizedPublicKeysAuthenticator(getStdin(), System.out, sshd);
                testSftpAccess(getStdin(), System.out, sshd);
            } finally {
                System.out.println("Stopping...");
                sshd.stop();
                System.out.println("Stopped...");
            }
        } catch(Exception e) {
            System.err.append(e.getClass().getSimpleName()).append(": ").println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
