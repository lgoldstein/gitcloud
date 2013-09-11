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

package org.apache.commons.io.manifest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.net.util.URLUtils;

/**
 * @author Lyor G.
 */
public class ManifestUtils {
    public ManifestUtils() {
        super();
    }

    /**
     * A {@link List} of standard attributes used in manifests to provide version information
     */
    public static final List<Name> STANDARD_VERSION_ATTRS_NAMES=
            Collections.unmodifiableList(Arrays.asList(Name.SPECIFICATION_VERSION, Name.IMPLEMENTATION_VERSION));

    /**
     * Attempts to locate the main class specification in a JAR's manifest
     * main attributes section
     * @param manifest The {@link Manifest} data - ignored if <code>null</code>
     * @return The located main class value - <code>null</code>/empty if
     * no manifest or no main class specified
     * @see java.util.jar.Attributes.Name#MAIN_CLASS
     */
    public static final String getMainClass(Manifest manifest) {
        Attributes  attrs=(manifest == null) ? null : manifest.getMainAttributes();
        if (attrs == null) {
            return null;
        } else {
            return attrs.getValue(Attributes.Name.MAIN_CLASS);
        }
    }

    /**
     * Attempts to locate the main class specification in the specified
     * JAR file's manifest 
     * @param jarFile The {@link JarFile}
     * @return The located main class value - <code>null</code>/empty if
     * no manifest or no main class specified
     * @throws IOException If cannot access the JAR or read its manifest
     * @see JarFile#getManifest()
     * @see #getMainClass(Manifest)
     */
    public static final String getMainClass(JarFile jarFile) throws IOException {
        if (jarFile == null) {
            throw new IllegalArgumentException("No JAR file specified");
        }
        
        return getMainClass(jarFile.getManifest());
    }
    
    /**
     * Scans the an attributes section of a JAR file manifest for the 1st
     * non-empty attribute value
     * @param attrs The section's {@link Attributes} - ignored if <code>null</code>
     * or empty
     * @param names A {@link Collection} of {@link java.util.jar.Attributes.Name}-s to be checked
     * for non-empty value. <B>Note:</B> the <U>order</U> of the names in the
     * collection dictates the 1st non-empty attribute scan
     * @return  The key,value &quot;pair&quot; of the 1st non-empty attribute from
     * the collection - <code>null</code> if no non-empty match found
     */
    public static final Pair<Attributes.Name,String> findFirstManifestAttributeValue (Attributes attrs, Collection<? extends Attributes.Name> names) {
        if ((ExtendedMapUtils.size(attrs) <= 0) || (ExtendedCollectionUtils.size(names) <= 0)) {
            return null;
        }

        for (Attributes.Name n : names) {
            String  value=attrs.getValue(n);
            if (StringUtils.isEmpty(value)) {
                continue;   // debug breakpoint
            } else {
                return Pair.of(n,value);
            }
        }

        return null;    // no match found
    }

    /**
     * Scans the <U><code>main</code></U> section of a JAR file manifest for
     * the 1st non-empty attribute value
     * @param manifest The {@link Manifest} to be scanned - ignored if <code>null</code>
     * @param names A {@link Collection} of {@link java.util.jar.Attributes.Name}-s to be checked
     * for non-empty value. <B>Note:</B> the <U>order</U> of the names in the
     * collection dictates the 1st non-empty attribute scan
     * @return  The key,value &quot;pair&quot; of the 1st non-empty main attribute
     * from the collection - <code>null</code> if no non-empty match found
     * @see #findFirstManifestAttributeValue(Attributes, Collection)
     */
    public static final Pair<Attributes.Name,String> findFirstManifestAttributeValue (Manifest manifest, Collection<? extends Attributes.Name> names) {
        if ((manifest == null) || (ExtendedCollectionUtils.size(names) <= 0)) {
            return null;
        } else {
            return findFirstManifestAttributeValue(manifest.getMainAttributes(), names);
        }
    }

    /**
     * Scans the <U><code>main</code></U> section of a JAR file manifest for
     * the 1st non-empty attribute value
     * @param jarFile The {@link JarFile} to be examined
     * @param names A {@link Collection} of {@link java.util.jar.Attributes.Name}-s to be checked
     * for non-empty value. <B>Note:</B> the <U>order</U> of the names in the
     * collection dictates the 1st non-empty attribute scan
     * @return The name,value &quot;pair&quot; of the 1st non-empty main attribute
     * from the collection - <code>null</code> if no non-empty match found
     * @throws IOException If failed to access the JAR file for extracting its
     * manifest. <B>Note:</B> if the file does not exist or is not a file or no
     * names specified, then no access is attempted to begin with
     * @see #findFirstManifestAttributeValue(Manifest, Collection)
     */
    public static final Pair<Attributes.Name,String> findFirstManifestAttributeValue (
                JarFile jarFile, Collection<? extends Attributes.Name> names) throws IOException {
        return findFirstManifestAttributeValue((jarFile == null) ? null : jarFile.getManifest(), names);
    }
    
    /**
     * Loads a manifest from the given URL
     * @param url The {@link URL} - ignored if <code>null</code>
     * @return The loaded {@link Manifest}
     * @throws IOException If failed to access the URL or load the manifest
     * @see URL#openStream()
     * @see Manifest#Manifest(InputStream)
     */
    public static final Manifest loadManifest(URL url) throws IOException {
        if (url == null) {
            return null;
        }

        InputStream in=url.openStream();
        try {
            return new Manifest(in);
        } finally {
            in.close();
        }
    }

    /**
     * @param anchor The anchor {@link Class} that is assumed to be packed
     * in the same JAR as the manifest file
     * @return The {@link Manifest} - <code>null</code> if cannot determine it
     * @throws IOException If failed to read the manifest file
     */
    public static final Manifest loadContainerManifest (Class<?> anchor) throws IOException {
        URL     classBytesURL=ExtendedClassUtils.getClassBytesURL(anchor);
        String  scheme=(classBytesURL == null) ? null : classBytesURL.getProtocol();
        if (StringUtils.isEmpty(scheme)) {
            return null;
        }

        String          classPath=classBytesURL.toExternalForm();
        int             sepPos=classPath.lastIndexOf(URLUtils.RESOURCE_SUBPATH_SEPARATOR);
        final String    pathPrefix;
        if (sepPos < 0) {
            String  className=anchor.getName().replace('.', '/');
            if ((sepPos=classPath.indexOf(className)) <= 1) {   // should be at least a:b
                return null;
            }

            if (classPath.charAt(sepPos - 1) == '/') {
                sepPos--;
            }

            pathPrefix = classPath.substring(0, sepPos);
        } else {
            pathPrefix = classPath.substring(0, sepPos + 1);
        }

        return loadManifest(new URL(pathPrefix + "/" + JarFile.MANIFEST_NAME));
    }
}
