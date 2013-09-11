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
package net.community.chest.gitcloud.users;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.ExtendedEnumUtils;

/**
 * Represents the access permissions associated with a given repository for a user
 * @author Lyor Goldstein
 * @since Sep 11, 2013 2:43:33 PM
 */
public enum UserRepoAccessPermission {
    PULL,
    PUSH;
    
    /**
     * The full {@link Set} of permissions 
     */
    public static final Set<UserRepoAccessPermission>    FULL=
            Collections.unmodifiableSet(EnumSet.allOf(UserRepoAccessPermission.class));

    /**
     * The empty {@link Set} of permissions
     */
    public static final Set<UserRepoAccessPermission>    NONE=
            Collections.unmodifiableSet(Collections.<UserRepoAccessPermission>emptySet());
    
    /**
     * A {@link Transformer} that converts a {@link String} into the matching
     * {@link UserRepoAccessPermission} constant case <U>insensitive</U> (<B>Note:</B>
     * returns {@code null} if no match found or if {@code null}/empty string)
     */
    public static final Transformer<String,UserRepoAccessPermission> NAME_TO_PERMISSION_XFORMER=
            ExtendedEnumUtils.fromNameTransformer(false, FULL);
}
