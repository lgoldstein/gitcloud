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
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ExtendedCharSequenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.AbstractJULWrapper;
import org.apache.commons.logging.Log;
import org.springframework.util.ExtendedPlaceholderResolverUtils;
import org.springframework.util.NamedExtendedPlaceholderResolver;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 4:17:32 PM
 */
public class ServletUtils {
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
    
    public static final Map<String,String> getRequestHeaders(HttpServletRequest req) {
        // NOTE: map must be case insensitive as per HTTP requirements
        Map<String,String>  hdrsMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        for (Enumeration<String> hdrs=req.getHeaderNames(); (hdrs != null) && hdrs.hasMoreElements(); ) {
            String  hdrName=hdrs.nextElement(), hdrValue=req.getHeader(hdrName);
            hdrsMap.put(capitalizeHttpHeaderName(hdrName), StringUtils.trimToEmpty(hdrValue));
        }

        return hdrsMap;
    }
    
    public static final Map<String,String> getResponseHeaders(HttpServletResponse rsp) {
        // NOTE: map must be case insensitive as per HTTP requirements
        Map<String,String>  hdrsMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        for (String hdrName : rsp.getHeaderNames()) {
            String  hdrValue=rsp.getHeader(hdrName);
            hdrsMap.put(capitalizeHttpHeaderName(hdrName), StringUtils.trimToEmpty(hdrValue));
        }

        return hdrsMap;
    }

    public static final String capitalizeHttpHeaderName(String hdrName) {
        if (StringUtils.isEmpty(hdrName)) {
            return hdrName;
        }

        int curPos=hdrName.indexOf('-');
        if (curPos < 0) {
            return ExtendedCharSequenceUtils.capitalize(hdrName);
        }

        StringBuilder   sb=null;
        for (int  lastPos=0; ; ) {
            char    ch=hdrName.charAt(lastPos), tch=Character.toTitleCase(ch);
            if (ch != tch) {
                if (sb == null) {
                    sb = new StringBuilder(hdrName.length());
                    // append the data that was OK
                    if (lastPos > 0) {
                        sb.append(hdrName.substring(0, lastPos));
                    }
                }
                
                sb.append(tch);
                
                if (curPos > lastPos) {
                    sb.append(hdrName.substring(lastPos + 1 /* excluding the capital letter */, curPos + 1 /* including the '-' */));
                } else {    // last component in string
                    sb.append(hdrName.substring(lastPos + 1 /* excluding the capital letter */));
                }
            }

            if (curPos < lastPos) {
                break;
            }

            if ((lastPos=curPos + 1) >= hdrName.length()) {
                break;
            }
            
            curPos = hdrName.indexOf('-', lastPos);
        }

        if (sb == null) {   // There was no need to modify anything
            return hdrName;
        } else {
            return sb.toString();
        }
    }
}
