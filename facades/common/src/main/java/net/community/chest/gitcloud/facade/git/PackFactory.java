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
package net.community.chest.gitcloud.facade.git;

import org.springframework.context.RefreshedContextAttacher;

/**
 * @author Lyor Goldstein
 * @since Sep 12, 2013 9:29:15 AM
 */
public abstract class PackFactory<C> extends RefreshedContextAttacher {
    public static final int DEFAULT_TIMEOUT_SEC=30;

    protected PackFactory() {
        super();
    }
}
