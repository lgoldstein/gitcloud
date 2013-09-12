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
package net.community.chest.gitcloud.facade.backend;

import java.io.File;

import org.apache.commons.io.ExtendedFileUtils;
import org.springframework.util.ExtendedPlaceholderResolver;

import net.community.chest.gitcloud.facade.AbstractContextInitializer;
import net.community.chest.gitcloud.facade.ConfigUtils;

/**
 * A special initializer that make sure that the required folders exist
 * TODO: remove it once a real distribution is created
 * @author Lyor G.
 */
public class FacadeContextInitializer extends AbstractContextInitializer {
    public FacadeContextInitializer() {
        super();
    }

    @Override
    protected File getApplicationConfigFile(File appBase, ExtendedPlaceholderResolver sourcesResolver) {
        return ExtendedFileUtils.buildRelativeFile(appBase, ConfigUtils.CONF_DIR_NAME, FacadeEnvironmentInitializer.PROPS_FILE_NAME);
    }   
}
