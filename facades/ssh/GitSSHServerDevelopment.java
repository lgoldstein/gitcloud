/**
 * 
 */
package com.vmware.devenv.git.ssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ssh.keys.CryptoKeyEntry;
import org.apache.commons.net.ssh.keys.KeyUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.apache.mina.core.session.IoSession;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.NullShellFactory;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;

/**
 * @author lgoldstein
 */
public class GitSSHServerDevelopment extends AbstractTestSupport {
    public static final Class<?>   anchor=GitSSHServerDevelopment.class;

    public GitSSHServerDevelopment() {
        super();
    }

    public static final void testAuthorizedPublicKeysAuthenticator(BufferedReader in, PrintStream out, SshServer server) throws Exception {
        // provides the host key(s) - e.g., on Ubuntu these reside in the "/etc/ssh" folder
        server.setKeyPairProvider(new KeyPairProvider() {
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
                        
                        resourceName = anchor.getSimpleName() + "-" + type;
                        URL     url=anchor.getResource(resourceName);
                        Validate.notNull(url, resourceName, ArrayUtils.EMPTY_OBJECT_ARRAY);
                        
                        try {
                            kp = KeyUtils.loadKeyPair(url);
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
            });
        server.setPublickeyAuthenticator(new PublickeyAuthenticator() {
                private final Log   log=LogFactory.getLog(PublickeyAuthenticator.class);
                private final Map<String,List<Pair<String,byte[]>>>   userKeys=
                        new TreeMap<String,List<Pair<String,byte[]>>>(String.CASE_INSENSITIVE_ORDER);
                {
                    URL url=anchor.getResource(CryptoKeyEntry.STD_AUTHORIZED_KES_FILENAME);
                    assertNotNull("Not found " + CryptoKeyEntry.STD_AUTHORIZED_KES_FILENAME, url);

                    Collection<CryptoKeyEntry>  entries=CryptoKeyEntry.readAuthorizedKeys(url);
                    assertFalse("No authorized keys found", entries.isEmpty());
                    
                    for (CryptoKeyEntry ke : entries) {
                        String      username=ke.getUser();
                        PublicKey   key=ke.decodePublicKey();
                        String      algorithm=key.getAlgorithm();
                        byte[]      data=key.getEncoded();

                        // do not allow the same key to be registered for multiple users or even the same one
                        for (Map.Entry<String,List<Pair<String,byte[]>>> ue : userKeys.entrySet()) {
                            String                      name=ue.getKey();
                            List<Pair<String,byte[]>>   list=ue.getValue();
                            for (Pair<String,byte[]> kd : list) {
                                String  ka=kd.getLeft();
                                if (!algorithm.equalsIgnoreCase(ka)) {
                                    continue;
                                }
                                
                                byte[]  kb=kd.getRight();
                                if (Arrays.equals(data, kb)) {
                                    throw new IllegalStateException(username + "[" + name + "] duplicate " + algorithm + " key data");
                                }
                            }
                        }
                        
                        List<Pair<String,byte[]>>   list=userKeys.get(username);
                        if (list == null) {
                            list = new ArrayList<Pair<String,byte[]>>();
                            userKeys.put(username, list);
                        }
                        list.add(Pair.of(algorithm, data));
                    }
                }
    
                @Override
                public boolean authenticate(String username, PublicKey key, ServerSession session) {
                    Validate.notEmpty(username, "No username to authenticate", ArrayUtils.EMPTY_OBJECT_ARRAY);
                    Validate.notNull(key, "No key to authenticate", ArrayUtils.EMPTY_OBJECT_ARRAY);
                    Validate.notNull(session, "No session authenticate", ArrayUtils.EMPTY_OBJECT_ARRAY);

                    Pair<String,byte[]> keyData=Pair.of(key.getAlgorithm(), key.getEncoded());
                    IoSession       ioSession=session.getIoSession();
                    SocketAddress   remoteAddress=ioSession.getRemoteAddress();
                    if (remoteAddress instanceof InetSocketAddress) {
                        InetSocketAddress   inetSock=(InetSocketAddress) remoteAddress;
                        log.info("authenticate(" + username + ")[" + keyData.getLeft() + "] from " + inetSock.getHostString());
                    } else {
                        log.info("authenticate(" + username + ")[" + keyData.getLeft() + "]");
                    }
                    
                    List<Pair<String,byte[]>>   candidates=userKeys.get(username);
                    String                      resolvedName=null;
                    if (candidates != null) {
                        if (validatePublicKey(username, keyData, candidates)) {
                            resolvedName = username;
                        }
                    } else {
                        for (Map.Entry<String,List<Pair<String,byte[]>>> ue : userKeys.entrySet()) {
                            String  name=ue.getKey();
                            
                            candidates = ue.getValue();
                            if (validatePublicKey(name, keyData, candidates)) {
                                resolvedName = name;
                                break;
                            }
                            
                            candidates = null;
                        }
                        
                    }
                    
                    if (StringUtils.isEmpty(resolvedName)) {
                        return false;
                    }

                    session.setAttribute(AbstractGitCommand.USER_ID_ATTRIBUTE, resolvedName);
                    return true;
                }
                
                private boolean validatePublicKey(
                        String user, Pair<String,byte[]> keyData, Collection<? extends Pair<String,byte[]>> candidates) {
                    if (ExtendedCollectionUtils.isEmpty(candidates)) {
                        return false;
                    }
                    
                    for (Pair<String,byte[]> kp : candidates) {
                        if (ExtendedStringUtils.safeCompare(keyData.getLeft(), kp.getLeft(), false) != 0) {
                            continue;
                        }
                        
                        if (ExtendedArrayUtils.findFirstNonMatchingIndex(keyData.getRight(), kp.getRight()) < 0) {
                            log.info("validatePublicKey(" + user + ")[" + keyData.getLeft() + "] found");
                            return true;
                        }
                    }

                    log.warn("validatePublicKey(" + user + ")[" + keyData.getLeft() + "] no match");
                    return false;
                }
            });

        final String    GITBLIT_PREFIX="/gitblit";
        SSHRepositoryResolver<? extends AbstractGitCommand> resolver=
            new GitCommandFileRepositoryResolver<AbstractGitCommand>(new File("C:\\Projects\\devenv-aas\\trunk\\Servers\\apache-tomcat-7.0.42\\webapps\\gitblit\\WEB-INF\\data"), true) {
                @Override
                public Repository open(AbstractGitCommand req, String name)
                        throws RepositoryNotFoundException, ServiceNotEnabledException {
                    if (name.startsWith(GITBLIT_PREFIX)) {
                        name = name.substring(GITBLIT_PREFIX.length());
                    }
                    return super.open(req, name);
                }
            
            };
        
        ExecutorService service=Executors.newFixedThreadPool(Byte.SIZE);
        try {
            server.setCommandFactory(new GitCommandFactory(service, resolver));
            server.start();
            try {
                out.append("Listening on ").println(server.getPort());
                for ( ; ; ) {
                    String  ans=getval(out, in, "(q)uit");
                    if (isQuit(ans)) {
                        break;
                    }
                }
            } finally {
                out.println("Stopping...");
                server.stop();
                out.println("Stopped...");
            }
        } finally {
            service.shutdownNow();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    
    public static void main(String[] args) {
        SshServer   server=SshServer.setUpDefaultServer();
        server.setPort(2222);
        server.setShellFactory(NullShellFactory.INSTANCE);
        server.setPasswordAuthenticator(new PasswordAuthenticator() {
                private final Log   log=LogFactory.getLog(PasswordAuthenticator.class);

                @Override
                public boolean authenticate(String username, String password, ServerSession session) {
                    log.info("authenticate(" + username + ")[" + password + "]");
                    return true;
                }
            });
        try {
            testAuthorizedPublicKeysAuthenticator(getStdin(), System.out, server);
        } catch(Exception e) {
            System.err.append(e.getClass().getSimpleName()).append(": ").println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

}
