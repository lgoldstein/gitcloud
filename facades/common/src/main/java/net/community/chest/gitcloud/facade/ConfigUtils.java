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

import java.io.File;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 3:52:33 PM
 */
public final class ConfigUtils {
    public static final String  GITCLOUD_BASE_PROP="gitcloud.base";
    public static final String  CONF_DIR_NAME="conf";
    public static final String  GITCLOUD_CONF_DIR_PROP="gitcloud.conf.dir";

    private ConfigUtils() {
        throw new UnsupportedOperationException("No instance");
    }

    /**
     * Checks that a give property value that represents a folder is valid as follows:</BR>
     * <UL>
     *      <LI>If exists, then it must be a folder with read/write/execute permissions</LI>
     *      <LI>Otherwise, it is created along with its parents</LI>
     * <UL>
     * @param propName The property name (used for meaningful exception message)
     * @param propValue The {@link File} to verify
     * @return <code>false</code> if this is an already existing folder, <code>true</code>
     * if had to create it (and its parents)
     * @throws IllegalStateException if any of the validation tests fails
     * @see File#mkdirs()
     */
    public static final boolean verifyFolderProperty(String propName, File propValue) {
        if (propValue.exists()) {
            if (!propValue.isDirectory()) {
                throw new IllegalStateException("verifyFolderProperty(" + propName + ") not a folder: " + ExtendedFileUtils.toString(propValue));
            }
            
            if (!propValue.canRead()) {
                throw new IllegalStateException("verifyFolderProperty(" + propName + ") non-readable: " + ExtendedFileUtils.toString(propValue));
            }

            if (!propValue.canWrite()) {
                throw new IllegalStateException("verifyFolderProperty(" + propName + ") non-writeable: " + ExtendedFileUtils.toString(propValue));
            }

            if (!propValue.canExecute()) {
                throw new IllegalStateException("verifyFolderProperty(" + propName + ") non-listable: " + ExtendedFileUtils.toString(propValue));
            }
            
            return false;
        } else {
            if (!propValue.mkdirs()) {
                throw new IllegalStateException("verifyFolderProperty(" + propName + ") failed to create: " + ExtendedFileUtils.toString(propValue));
            }
            
            return true;
        }
    }

    /**
     * Resolves the location of the <code>gitcloud.base</code> location as follows:</BR>
     * <OL>
     *      <LI>
     *      Checks if there is already a definition for the <code>gitcloud.base</code>
     *      property. If so, then uses it
     *      </LI>
     *      
     *      <LI>
     *      Ensures that there is a definition for the <code>catalina.base</code>
     *      property and defines <code>gitcloud.base</code> to be the <code>gitcloud</code>
     *      <U>sub-folder</U>
     *      </LI>
     * </OL>
     * @param resolver The {@link PlaceholderResolver} to use for the resolution of the
     * various required properties
     * @return A {@link Pair} where the left-hand is the resolved {@link File} location
     * of <code>gitcloud.base</code> and right-hand is a {@link Boolean} representing
     * whether the property was already defined (<code>true</code>) or had to
     * use the <code>catalina.base</code> property (<code>false</code>)
     */
    public static final Pair<File,Boolean> resolveGitcloudBase(PlaceholderResolver resolver) {
        String  baseValue=resolver.resolvePlaceholder(ConfigUtils.GITCLOUD_BASE_PROP);
        if (!StringUtils.isEmpty(baseValue)) {
            return Pair.of(new File(baseValue), Boolean.TRUE);
        }

        String  catalinaBase=resolver.resolvePlaceholder("catalina.base");
        if (StringUtils.isEmpty(catalinaBase)) {
            throw new IllegalStateException("No catalina.base value available");
        }
            
        return Pair.of(new File(catalinaBase, "gitcloud"), Boolean.FALSE);
    }
}
