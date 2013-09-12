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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.ExtendedIOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExtendedExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.ExtendedLogUtils;
import org.apache.commons.logging.Log;
import org.springframework.util.AggregatedExtendedPlaceholderResolver;
import org.springframework.util.ExtendedPlaceholderResolverUtils;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

/**
 * @author Lyor Goldstein
 * @since Sep 12, 2013 8:09:45 AM
 */
public abstract class AbstractEnvironmentInitializer implements ServletContextListener {
    protected volatile Log    logger;
    private   byte[]      workBuf;

    protected AbstractEnvironmentInitializer() {
        logger = ExtendedLogUtils.wrapJULLoggger(Logger.getLogger(getClass().getName()));
    }
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Log curLogger=logger;
        try {
            ServletContext  context=sce.getServletContext();
            logger = ServletUtils.wrapServletContext(context, Level.CONFIG);
            contextInitialized(context);
            logger.info("contextInitialized(" + context.getContextPath() + ")");
        } catch(Throwable t) {
            logger.error("Failed (" + t.getClass().getSimpleName() + ") to initialize: " + t.getMessage(), t);
            throw ExtendedExceptionUtils.toRuntimeException(t, true);
        } finally {
            logger = curLogger;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Log curLogger=logger;
        try {
            ServletContext  context=sce.getServletContext();
            logger = ServletUtils.wrapServletContext(context, Level.CONFIG);
            contextDestroyed(context);
            logger.info("contextDestroyed(" + context.getContextPath() + ")");
        } catch(Throwable t) {
            logger.error("Failed (" + t.getClass().getSimpleName() + ") to destroy: " + t.getMessage(), t);
            throw ExtendedExceptionUtils.toRuntimeException(t, true);
        } finally {
            logger = curLogger;
        }
    }

    protected void contextInitialized(ServletContext context) {
        PlaceholderResolver contextResolver=ServletUtils.toPlaceholderResolver(context);
        Pair<File,Boolean>  result=ConfigUtils.resolveGitcloudBase(
                new AggregatedExtendedPlaceholderResolver(contextResolver, ExtendedPlaceholderResolverUtils.SYSPROPS_RESOLVER, ExtendedPlaceholderResolverUtils.ENVIRON_RESOLVER));
        File                rootDir=result.getLeft();
        Boolean             baseExists=result.getRight();
        if (!baseExists.booleanValue()) {
            System.setProperty(ConfigUtils.GITCLOUD_BASE_PROP, rootDir.getAbsolutePath());
            logger.info("contextInitialized(" + context.getContextPath() + ") - added " + ConfigUtils.GITCLOUD_BASE_PROP + ": " + ExtendedFileUtils.toString(rootDir));
        } else {
            logger.info("contextInitialized(" + context.getContextPath() + ") using " + ConfigUtils.GITCLOUD_BASE_PROP + ": " + ExtendedFileUtils.toString(rootDir));
        }

        extractConfigFiles(new File(rootDir, ConfigUtils.CONF_DIR_NAME));
    }

    protected void contextDestroyed(ServletContext context) {
        Validate.notNull(context, "No context to destroy", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    protected abstract void extractConfigFiles(File confDir);

    protected void extractConfigFiles(File confDir, Collection<Pair<String,Collection<String>>> filesList) {
        for (Pair<String,Collection<String>> p : filesList) {
            extractConfigFiles(confDir, p.getLeft(), p.getRight());
        }
    }

    protected void extractConfigFiles(File confDir, String resPrefix, String ... names) {
        extractConfigFiles(confDir, resPrefix, ExtendedArrayUtils.asList(names));
    }

    protected void extractConfigFiles(File confDir, String resPrefix, Collection<String> names) {
        if (ConfigUtils.verifyFolderProperty(ConfigUtils.CONF_DIR_NAME, confDir)) {
            logger.info("extractConfigFiles(" + resPrefix + ") - created " + ExtendedFileUtils.toString(confDir));
        }

        ClassLoader cl=ExtendedClassUtils.getDefaultClassLoader(getClass());
        for (String fileName : names) {
            File    targetFile=new File(confDir, fileName);
            if (targetFile.exists()) {
                logger.info("extractConfigFiles(" + fileName + ")[" + resPrefix + "] skip - already exists: " + ExtendedFileUtils.toString(targetFile));
                continue;
            }

            try {
                long    copyLength=extractConfigFile(
                            cl.getResourceAsStream(resPrefix + "/" + fileName), targetFile, getWorkBuf(ExtendedIOUtils.DEFAULT_BUFFER_SIZE_VALUE));
                if (copyLength <= 0L) {
                    throw new StreamCorruptedException("Bad copy count: " + copyLength);
                }

                logger.info("extractConfigFiles(" + resPrefix + ")[" + fileName + "] " + copyLength + " bytes: " + ExtendedFileUtils.toString(targetFile));
            } catch(IOException e) {
                RuntimeException    thrown=
                        new RuntimeException("extractConfigFiles(" + resPrefix + ")[" + fileName + "]"
                                           + " failed (" + e.getClass().getSimpleName() + ")"
                                           + " to extract contents: " + e.getMessage(), e);
                logger.warn(thrown.getMessage(), e);
                throw thrown;
            }
        }
    }

    protected long extractConfigFile(InputStream srcData, File targetFile, byte[] cpyBuf) throws IOException {
        if (srcData == null) {
            throw new FileNotFoundException("No input stream");
        }

        try {
            return ExtendedIOUtils.copyLarge(srcData, targetFile, cpyBuf);
        } finally {
            srcData.close();
        }
    }

    protected byte[] getWorkBuf(int initialSize) {
        if (workBuf == null) {
            workBuf = new byte[initialSize];
        }
        
        return workBuf;
    }
}
