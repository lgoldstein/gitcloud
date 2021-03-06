/* Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.community.chest.gitcloud.facade;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedSetUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.manifest.ManifestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedCharSequenceUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.AbstractLoggingBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.AggregatedExtendedPlaceholderResolver;
import org.springframework.util.ExtendedPlaceholderResolver;
import org.springframework.util.ExtendedPlaceholderResolverUtils;
import org.springframework.util.NamedExtendedPlaceholderResolver;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.SystemPropertyUtils;

/**
 * @author Lyor Goldstein
 * @since Sep 12, 2013 8:43:05 AM
 */
public abstract class AbstractContextInitializer
                extends AbstractLoggingBean
                implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    protected AbstractContextInitializer() {
        super();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment     environment=applicationContext.getEnvironment();
        MutablePropertySources      propSources=environment.getPropertySources();
        ExtendedPlaceholderResolver sourcesResolver=ExtendedPlaceholderResolverUtils.toPlaceholderResolver(propSources);
        File                        appBase=resolveApplicationBase(propSources, sourcesResolver);
        File                        configFile=getApplicationConfigFile(appBase, sourcesResolver);
        Collection<String>          activeProfiles=resolveActiveProfiles(sourcesResolver);
        if (ExtendedCollectionUtils.size(activeProfiles) > 0) {
            environment.setActiveProfiles(activeProfiles.toArray(new String[activeProfiles.size()]));
        }

        try {
            ensureFoldersExistence(appBase, configFile, sourcesResolver);
        } catch (IOException e) {
            logger.error("ensureFoldersExistence(" + ExtendedFileUtils.toString(appBase) + ")"
                       + " " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        
        if (logger.isDebugEnabled()) {
            showArtifactsVersions();
        }
    }
    
    protected abstract File getApplicationConfigFile(File appBase, ExtendedPlaceholderResolver sourcesResolver);

    /*
     * Checks if profiles are already activated via the spring.profiles.active or
     * spring.profiles.default, then invokes the specific resolver.
     * returns a non-empty collection if an OVERRIDE of the active profiles is required
     */
    protected Collection<String> resolveActiveProfiles(ExtendedPlaceholderResolver sourcesResolver) {
        String  activeValues=sourcesResolver.resolvePlaceholder(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
        if (!StringUtils.isEmpty(activeValues)) {
            logger.info("resolveActiveProfiles(" + AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME + "): " + activeValues);
            return Collections.emptyList();
        }
        
        activeValues = sourcesResolver.resolvePlaceholder(AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME);
        if (!StringUtils.isEmpty(activeValues)) {
            logger.info("resolveActiveProfiles(" + AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME + "): " + activeValues);
            return Collections.emptyList();
        }

        return resolveActiveApplicationProfiles(sourcesResolver);
    }

    protected Collection<String> resolveActiveApplicationProfiles(ExtendedPlaceholderResolver sourcesResolver) {
        Validate.notNull(sourcesResolver, "No resolver", ArrayUtils.EMPTY_OBJECT_ARRAY);
        return Collections.emptyList();
    }

    protected SortedSet<File> ensureFoldersExistence(File appBase, File configFile, ExtendedPlaceholderResolver sourcesResolver) throws IOException {
        Properties  props=new Properties(); 
        InputStream propsStream=new BufferedInputStream(new FileInputStream(configFile));
        try {
            props.load(propsStream);
        } finally {
            propsStream.close();
        }

        String  prev=props.getProperty(ConfigUtils.GITCLOUD_BASE_PROP);
        if (!StringUtils.isEmpty(prev)) {
            throw new StreamCorruptedException(ConfigUtils.GITCLOUD_BASE_PROP + " property re-specified: " + prev);
        }

        return ensureFoldersExistence(appBase, ExtendedPlaceholderResolverUtils.toPlaceholderResolver(props), sourcesResolver);
    }

    protected SortedSet<File> ensureFoldersExistence(File appBase, NamedExtendedPlaceholderResolver propsResolver, ExtendedPlaceholderResolver sourcesResolver) {
        final SortedSet<File>               createdFiles=
                verifyFolderProperty(ConfigUtils.GITCLOUD_BASE_PROP, appBase, ExtendedSetUtils.<File>sortedSet(ExtendedFileUtils.BY_ABSOLUTE_PATH_COMPARATOR));
        final String                        appBasePrefix=appBase.getAbsolutePath();
        final PropertyPlaceholderHelper     helper=new PropertyPlaceholderHelper(
                SystemPropertyUtils.PLACEHOLDER_PREFIX, SystemPropertyUtils.PLACEHOLDER_SUFFIX, SystemPropertyUtils.VALUE_SEPARATOR, true);
        final ExtendedPlaceholderResolver   resolver=new AggregatedExtendedPlaceholderResolver(propsResolver, sourcesResolver);
        ExtendedPlaceholderResolverUtils.forAllEntriesDo(propsResolver, new Closure<Map.Entry<String,String>>() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void execute(Entry<String,String> e) {
                    String  propName=e.getKey(), propValue=e.getValue();
                    if (StringUtils.isEmpty(propName) || (!propName.endsWith(".dir")) || StringUtils.isEmpty(propValue)) {
                        return;
                    }
    
                    String  resolvedValue=helper.replacePlaceholders(propValue, resolver);
                    if (ExtendedCharSequenceUtils.getSafeLength(resolvedValue) < appBasePrefix.length()) {
                        return;
                    }
                    
                    String  pathValue=resolvedValue.replace('/', File.separatorChar);   // just in case using '/'
                    if (logger.isDebugEnabled()) {
                        logger.debug("ensureFoldersExistence(" + propName + "): " + pathValue);
                    }

                    if (pathValue.startsWith(appBasePrefix)) {
                        verifyFolderProperty(propName, new File(pathValue), createdFiles); 
                    }
                }
            });
        return createdFiles;
    }

    protected <C extends Collection<File>> C verifyFolderProperty(String propName, File propValue, C outputCollection) {
        if (ConfigUtils.verifyFolderProperty(propName, propValue)) {
            logger.info("verifyFolderProperty(" + propName + ") created: " + ExtendedFileUtils.toString(propValue));
            outputCollection.add(propValue);
        }

        return outputCollection;
    }

    protected File resolveApplicationBase(MutablePropertySources propSources, ExtendedPlaceholderResolver sourcesResolver) {
        Pair<File,Boolean>  result=ConfigUtils.resolveGitcloudBase(sourcesResolver);
        File                rootDir=result.getLeft();
        Boolean             baseExists=result.getRight();
        if (!baseExists.booleanValue()) {
            propSources.addFirst(new MapPropertySource("gitcloudBase", Collections.<String,Object>singletonMap(ConfigUtils.GITCLOUD_BASE_PROP, rootDir.getAbsolutePath())));
            System.setProperty(ConfigUtils.GITCLOUD_BASE_PROP, rootDir.getAbsolutePath());
            logger.info("resolveApplicationBase - added " + ConfigUtils.GITCLOUD_BASE_PROP + ": " + ExtendedFileUtils.toString(rootDir));
        }

        return rootDir;
    }

    protected void showArtifactsVersions() {
        scanArtifactsManifests(new Predicate<Pair<URL,Manifest>>() {
            @Override
            @SuppressWarnings("synthetic-access")
            public boolean evaluate(Pair<URL, Manifest> e) {
                URL         url=e.getKey();
                Manifest    manifest=e.getValue();
                Pair<Attributes.Name,String>    versionInfo=
                        ManifestUtils.findFirstManifestAttributeValue(manifest, ManifestUtils.STANDARD_VERSION_ATTRS_NAMES);
                if (versionInfo == null) {
                    logger.info("showArtifactsVersions(" + url.toExternalForm() + ") no version information extracted");
                } else {
                    logger.info("showArtifactsVersions(" + url.toExternalForm() + ")[" + versionInfo.getLeft() + "]: " + versionInfo.getRight());
                }
                
                return false;   // don't stop
            }
        });
    }

    protected void scanArtifactsManifests(Predicate<Pair<URL,Manifest>> manifestHandler) {
        ClassLoader loader=ExtendedClassUtils.getDefaultClassLoader(getClass());
        try {
            for (Enumeration<URL> manifests=loader.getResources(JarFile.MANIFEST_NAME);
                 (manifests != null) && manifests.hasMoreElements(); ) {
                URL url=manifests.nextElement();
                try {
                    Manifest manifest=ManifestUtils.loadManifest(url);
                    if (manifestHandler.evaluate(Pair.of(url, manifest))) {
                        logger.info("Scanning stopped by handler at URL=" + url.toExternalForm());
                        break;
                    }
                } catch(Exception e) {
                    logger.warn(e.getClass().getSimpleName() + " while handle URL=" + url.toExternalForm() + ": " + e.getMessage());
                }
            }
        } catch(IOException e) {
            logger.warn("Failed (" + e.getClass().getSimpleName() + ") to get manifests URLs: " + e.getMessage());
        }
    }
}
