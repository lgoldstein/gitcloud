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

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

import javax.servlet.ServletContext;

import org.apache.commons.logging.AbstractJULWrapper;
import org.apache.commons.logging.Log;
import org.springframework.util.ExtendedPlaceholderResolverUtils;
import org.springframework.util.NamedExtendedPlaceholderResolver;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 4:17:32 PM
 */
public class ServletUtils {
    // TODO move this to some util(s) artifact
    public static final NamedExtendedPlaceholderResolver toPlaceholderResolver(final ServletContext context) {
        if (context == null) {
            return ExtendedPlaceholderResolverUtils.EMPTY_RESOLVER;
        } else {
            return new NamedExtendedPlaceholderResolver() {

                @Override
                public String resolvePlaceholder(String placeholderName, String defaultValue) {
                    String  value=resolvePlaceholder(placeholderName);
                    if (value == null) {
                        return defaultValue;
                    } else {
                        return value;
                    }
                }

                @Override
                public String resolvePlaceholder(String placeholderName) {
                    return context.getInitParameter(placeholderName);
                }

                @Override
                public Collection<String> getPlaceholderNames() {
                    return Collections.list(context.getInitParameterNames());
                }
            };
        }
    }

    // TODO move this to some util(s) artifact
    public static final Log wrapServletContext(final ServletContext context, final Level thresholdLevel) {
        if ((context == null) || (thresholdLevel == null)) {
            throw new IllegalArgumentException("Incomplete wrapper specification");
        }
        
        return new AbstractJULWrapper() {
            @Override
            public void log(Level level, Object message, Throwable t) {
                if (isEnabled(level)) {
                    if (t == null) {
                        context.log(level.getName() + ": " + message);
                    } else {
                        context.log(level.getName() + ": " + message, t);
                    }
                }
            }

            @Override
            public boolean isEnabled(Level level) {
                if (Level.OFF.equals(thresholdLevel)) {
                    return false;
                }
                
                if (Level.ALL.equals(thresholdLevel)) {
                    return true;
                }
                
                if (level.intValue() >= thresholdLevel.intValue()) {
                    return true;
                } else {
                    return false;   // debug breakpoint
                }
            }
        };
    }
}
