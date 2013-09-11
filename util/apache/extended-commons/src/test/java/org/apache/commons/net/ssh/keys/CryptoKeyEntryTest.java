/*
 * 
 */
package org.apache.commons.net.ssh.keys;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.util.Collection;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Jul 10, 2013 7:59:20 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CryptoKeyEntryTest extends AbstractTestSupport {
    public CryptoKeyEntryTest() {
        super();
    }

    @Test
    public void testReadAuthorizedKeys() throws Exception {
        URL url=getClassResource("KeyUtilsTest.authorized_keys");
        assertNotNull("Missing test file", url);
        
        testReadAuthorizedKeys(CryptoKeyEntry.readAuthorizedKeys(url));
    }
    
    @Test
    public void testReadDefaultAuthorizedKeysFile() throws Exception {
        File    keysFile=CryptoKeyEntry.getDefaultAuthorizedKeysFile();
        assertNotNull("No default location", keysFile);
        if (!keysFile.exists()) {
            logger.info("testReadDefaultAuthorizedKeysFile: verify non-existing " + keysFile.getAbsolutePath());
            Collection<CryptoKeyEntry> entries=CryptoKeyEntry.readDefaultAuthorizedKeys();
            assertTrue("Non-empty keys even though file not found: " + entries, ExtendedCollectionUtils.isEmpty(entries));
        } else {
            assertTrue("Not a file: " + keysFile.getAbsolutePath(), keysFile.isFile());
            testReadAuthorizedKeys(CryptoKeyEntry.readDefaultAuthorizedKeys());
        }
    }

    private Collection<CryptoKeyEntry> testReadAuthorizedKeys(Collection<CryptoKeyEntry> entries) throws IOException {
        assertFalse("No entries read", ExtendedCollectionUtils.isEmpty(entries));
        
        for (CryptoKeyEntry entry : entries) {
            PublicKey   key=entry.decodePublicKey();
            String      eValue=entry.toString();
            assertNotNull("No key: " + eValue, key);
            assertEquals("Mismatched algorithm: " + eValue,
                         ExtendedStringUtils.TO_UPPERCASE_XFORMER.transform(entry.getAlgorithm()),
                         ExtendedStringUtils.TO_UPPERCASE_XFORMER.transform(key.getAlgorithm()));
        }
        
        return entries;
    }
}
