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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private static final List<String> TEST_EXTS=Collections.unmodifiableList(Arrays.asList("", Constants.DOT_GIT_EXT ));

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
        
        for (String ext : TEST_EXTS) {
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
        
        for (String ext : TEST_EXTS) {
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
    
    @Test
    public void testDeepDownRepositoryResolution() throws Exception {
        final String    REPO_NAME="testDeepDownRepositoryResolution", GIT_NAME=REPO_NAME + Constants.DOT_GIT_EXT;
        final int       MAX_DEPTH=Byte.SIZE;
        StringBuilder   sb=new StringBuilder(MAX_DEPTH + Long.SIZE);
        File            parentDir=reposDir;
        for (int depth=0; depth < MAX_DEPTH; depth++) {
            String  subName=String.valueOf(depth);
            parentDir = new File(parentDir, subName);
            sb.append(subName).append('/');

            File    gitDir=new File(parentDir, GIT_NAME);
            if (!gitDir.exists()) {
                Repository      repo=new FileRepository(gitDir);
                try {
                    repo.create(true);
                } finally {
                    repo.close();
                }
            } else {
                assertTrue("Child repo not a folder: " + gitDir, gitDir.isDirectory());
            }

            int curLen=sb.length();
            try {
                sb.append(REPO_NAME);
                
                int baseLen=sb.length();
                for (String ext : TEST_EXTS) {
                    try {
                        Repository  repo=resolver.open(null, sb.append(ext).toString());
                        assertNotNull("No resolution result for ext=" + ext, repo);
        
                        try {
                            File    actual=repo.getDirectory();
                            assertEquals("Mismatched resolved location for ext=" + ext, gitDir, actual);
                        } finally {
                            repo.close();
                        }
                    } finally {
                        sb.setLength(baseLen);
                    }
                }
            } finally {
                sb.setLength(curLen);
            }
        }
    }
}
