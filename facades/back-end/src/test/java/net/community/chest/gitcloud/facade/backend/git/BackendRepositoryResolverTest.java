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
package net.community.chest.gitcloud.facade.backend.git;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.AbstractSpringTestSupport;

/**
 * @author Lyor Goldstein
 * @since Sep 12, 2013 11:06:53 AM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BackendRepositoryResolverTest extends AbstractSpringTestSupport {
    private final File  baseDir, reposDir;
    private final BackendRepositoryResolver<Void>   resolver;

    public BackendRepositoryResolverTest() {
        baseDir = ensureFolderExists(
                    new File(Validate.notNull(detectTargetFolder(), "Cannot detect target folder", ArrayUtils.EMPTY_OBJECT_ARRAY), getClass().getSimpleName()));
        reposDir = ensureFolderExists(new File(baseDir, "repos"));
        resolver = new BackendRepositoryResolver<Void>(reposDir);
    }

    @After
    public void tearDown() {
        BackendRepositoryResolver.clearInstance();
    }
    
    @Test
    public void testDisableDoubleRegistration() {
        try {
            BackendRepositoryResolver<Void> another=new BackendRepositoryResolver<Void>(reposDir);
            fail("Unexpected creationg success: " + another);
        } catch(IllegalStateException e) {
            // expected - ignored
        }
    }

    @Test
    public void testOpenExistingRepository() throws Exception {
        final String    REPO_NAME="testOpenExistingRepository";
        File            expected=new File(reposDir, REPO_NAME + Constants.DOT_GIT_EXT);
        if (!expected.exists()) {
            Repository      repo=new FileRepository(expected);
            try {
                repo.create(true);
            } finally {
                repo.close();
            }
        } else {
            assertTrue("Test repo not a folder: " + expected, expected.isDirectory());
        }
        
        for (String ext : new String[] { "", Constants.DOT_GIT_EXT }) {
            Repository  repo=resolver.open(null, REPO_NAME + ext);
            assertNotNull("No resolution result for ext=" + ext, repo);

            try {
                File    actual=repo.getDirectory();
                assertEquals("Mismatched resolved location for ext=" + ext, expected, actual);
            } finally {
                repo.close();
            }
        }
    }
    
    @Test
    public void testOpenNonExistingRepository() throws Exception {
        final String    REPO_NAME="testOpenNonExistingRepository";
        File            gitDir=new File(reposDir, REPO_NAME + Constants.DOT_GIT_EXT);
        FileUtils.deleteDirectory(gitDir);
        assertFalse("Failed to delete " + gitDir, gitDir.exists());
        
        for (String ext : new String[] { "", Constants.DOT_GIT_EXT }) {
            try {
                Repository repo=resolver.open(null, REPO_NAME + ext);
                try {
                    fail("Unexpected success for ext=" + ext + ": " + repo.getDirectory());
                } finally {
                    repo.close();
                }
            } catch(RepositoryNotFoundException e) {
                // expected - ignored
            }
        }
    }
}
