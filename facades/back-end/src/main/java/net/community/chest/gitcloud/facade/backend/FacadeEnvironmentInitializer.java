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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.community.chest.gitcloud.facade.AbstractEnvironmentInitializer;
import net.community.chest.gitcloud.facade.ConfigUtils;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Lyor G.
 */
public class FacadeEnvironmentInitializer extends AbstractEnvironmentInitializer {
    public static final String  PROPS_FILE_NAME="gitcloud-backend.properties";

    public FacadeEnvironmentInitializer() {
        super();
    }

    @Override
    protected void extractConfigFiles(File confDir) {
        // TODO use some automatic detection mechanism for "META-INF/conf"
        extractConfigFiles(confDir,
                Collections.singletonList(Pair.<String,Collection<String>>of("META-INF/" + ConfigUtils.CONF_DIR_NAME, Arrays.asList(PROPS_FILE_NAME, "gitcloud-backend-log4j.xml"))));
    }
}
