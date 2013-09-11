/**
 * 
 */
package org.apache.commons.io.manifest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author Lyor G.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ManifestUtilsTest extends AbstractTestSupport {
    private static final List<Attributes.Name>  TEST_NAMES=
            Collections.unmodifiableList(Arrays.asList(
                Name.SPECIFICATION_TITLE,
                Name.SPECIFICATION_VERSION,
                Name.SPECIFICATION_VENDOR,
                Name.IMPLEMENTATION_TITLE,
                Name.IMPLEMENTATION_VERSION,
                Name.IMPLEMENTATION_VENDOR));

    public ManifestUtilsTest() {
        super();
    }

    @Test
    public void testLoadManifestFromPackedJar () throws IOException {
        testLoadContainerManifest(Assert.class);
    }

    @Test
    public void testLoadManifestFromUnpackedClass() throws IOException {
        testLoadContainerManifest(getClass());
    }

    @Test
    public void testFindFirstManifestAttributeValue () {
        List<Attributes.Name>   names=new ArrayList<Attributes.Name>(TEST_NAMES);
        Attributes.Name         selectedName=names.get(0);
        String                  selectedValue=String.valueOf(Math.random());
        Manifest                manifest=createTestManifest(selectedName, selectedValue);
        for (int    index=0; index < Byte.SIZE; index++) {
            Collections.shuffle(names);

            Pair<Attributes.Name,String>  result=ManifestUtils.findFirstManifestAttributeValue(manifest, names);
            assertNotNull("No result for names=" + names, result);

            Attributes.Name foundName=result.getKey();
            assertSame("Mismatched attribute name for names=" + names, selectedName, foundName);

            String  foundValue=result.getValue();
            assertSame("Mismatched attribute value for names=" + names, selectedValue, foundValue);
        }
    }

    @Test
    public void testFindFirstManifestAttributeValueOnMultipleMatches () {
        List<Attributes.Name>       names=new ArrayList<Attributes.Name>(TEST_NAMES);
        Map<Attributes.Name,String> attrsMap=new HashMap<Attributes.Name, String>(names.size());
        for (int    index=0; index < names.size(); index++) {
            // skip even index to create "holes" in the manifest
            if ((index & 0x01) != 0) {
                Attributes.Name n=names.get(index);
                attrsMap.put(n, n.toString() + "=" + Math.random());
            }
        }

        Manifest    manifest=createTestManifest(attrsMap);
        for (int    index=0; index < Byte.SIZE; index++) {
            Collections.shuffle(names);

            Pair<Attributes.Name,String>  result=ManifestUtils.findFirstManifestAttributeValue(manifest, names);
            assertNotNull("No result for names=" + names, result);

            Attributes.Name foundName=result.getKey();
            String          foundValue=result.getValue(), mappedValue=attrsMap.get(foundName);
            assertSame("Mismatched value for " + foundName + " in shuffle=" + names, mappedValue, foundValue);

            int nameIndex=names.indexOf(foundName);
            assertTrue("Result name (" + foundName + ") not found in current shuffle=" + names, nameIndex >= 0);

            // we expect all names below this index NOT to have any mapped value
            for (nameIndex--; nameIndex >= 0; nameIndex--) {
                Attributes.Name n=names.get(nameIndex);
                String          v=attrsMap.get(n);
                assertNull("Preceding non-empty value (" + v + ") found for " + n + " before " + foundName + " in shuffle=" + names, v);
            }
        }
    }

    private static Manifest createTestManifest (Attributes.Name name, String value) {
        return createTestManifest(Collections.singletonMap(name, value));
    }
    
    private static Manifest createTestManifest (Map<Attributes.Name,String> attrsMap) {
        Manifest    manifest=new Manifest();
        Attributes  attrs=manifest.getMainAttributes();
        // can't call putAll since it checks if attrsMap is an instanceof Attributes
        for (Map.Entry<Attributes.Name,String> ae : attrsMap.entrySet()) {
            attrs.put(ae.getKey(), ae.getValue());
        }

        return manifest;
    }

    private Manifest testLoadContainerManifest(Class<?> anchor) throws IOException {
        Manifest    man=ManifestUtils.loadContainerManifest(anchor);
        assertNotNull(anchor.getSimpleName() + ": No manifest loaded", man);

        logger.info(anchor.getSimpleName() + " manifest attributes:");
        for (Map.Entry<?,?> attr : man.getMainAttributes().entrySet()) {
            Object  name=attr.getKey(), value=attr.getValue();
            logger.info("\t" + name + "=" + value);
        }
        
        return man;
    }

}
