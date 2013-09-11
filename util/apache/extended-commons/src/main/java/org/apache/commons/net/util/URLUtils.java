/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.util;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedCharSequenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author lgoldstein
 */
public class URLUtils {
    /**
     * Separator used in URL(s) that reference a resource inside a JAR
     * to denote the sub-path inside the JAR
     */
    public static final char    RESOURCE_SUBPATH_SEPARATOR='!';
    /**
     * Separator used to separate relative resource name components
     */
    public static final char    RESOURCE_PATH_SEPARATOR='/';

	public URLUtils() {
		super();
	}
    
    /**
     * An {@link ExtendedTransformer} implementation that converts a {@link URL}
     * to a {@link String} using the {@link #toString(URL)} method
     */
    public static final ExtendedTransformer<URL, String> URL2STRING_TRANSFORMER=
            new AbstractExtendedTransformer<URL, String>(URL.class,String.class) {
                @Override
                public String transform(URL src) {
                    return URLUtils.toString(src);
                }
            };

    /**
     * A <code>null</code>-safe way to retrieve a {@link String} representation
     * of a {@link URL} instance. <B>Caveat emptor:</B> this method <B><U>should
     * not be used instead of {@link URL#toExternalForm()}</U></B> where the
     * external form is required as its implementation may change in the future 
     * @param url The {@link URL} instance
     * @return The {@link URL#toExternalForm()} - <code>null</code> if
     * <code>null</code> URL instance
     */
    public static final String toString(URL url) {
        if (url == null) {
            return null;
        } else {
            return url.toExternalForm();
        }
    }

    /**
     * @param uri The {@link URI} value - ignored if <code>null</code>
     * @return The URI(s) source path where {@link ExtendedFileUtils#JAR_URL_PREFIX} and
     * any sub-resource are stripped
     * @see #getURLSource(String)
     */
    public static final String getURLSource (URI uri) {
        return getURLSource((uri == null) ? null : uri.toString());
    }

    /**
     * A {@link ExtendedTransformer} implementation that converts a {@link URL}
     * to a {@link String} using the {@link #getURLSource(URL)} method
     */
    public static final ExtendedTransformer<URL, String> URL2SOURCE_TRANSFORMER=
            new AbstractExtendedTransformer<URL, String>(URL.class,String.class) {
                @Override
                public String transform(URL src) {
                    return URLUtils.getURLSource(src);
                }
            };

    /**
     * @param url The {@link URL} value - ignored if <code>null</code>
     * @return The URL(s) source path where {@link ExtendedFileUtils#JAR_URL_PREFIX} and
     * any sub-resource are stripped
     * @see #getURLSource(String)
     */
    public static final String getURLSource (URL url) {
        return getURLSource((url == null) ? null : url.toExternalForm());
    }
    
    /**
     * @param urls A {@link Collection} of URL external form strings
     * @return A {@link List} of the matching source values
     * @see #getURLSource(String)
     */
    public static List<String> getURLSourceList(Collection<String> urls) {
        if (ExtendedCollectionUtils.size(urls) <= 0) {
            return Collections.emptyList();
        }
        
        List<String>    result=new ArrayList<String>(urls.size());
        for(String url : urls) {
            result.add(getURLSource(url));
        }
        
        return result;
    }

    /**
     * @param externalForm The {@link URL#toExternalForm()} string - ignored if
     * <code>null</code>/empty
     * @return The URL(s) source path where {@link ExtendedFileUtils#JAR_URL_PREFIX} and
     * any sub-resource are stripped
     */
    public static final String getURLSource (String externalForm) {
        String  url=externalForm;
        if (StringUtils.isEmpty(url)) {
            return url;
        }

        url = stripJarURLPrefix(externalForm);
        if (StringUtils.isEmpty(url)){
            return url;
        }
        
        int sepPos=url.indexOf(RESOURCE_SUBPATH_SEPARATOR);
        if (sepPos < 0) {
            return adjustURLPathValue(url);
        } else {
            return adjustURLPathValue(url.substring(0, sepPos));
        }
    }

    public static final String stripJarURLPrefix(String externalForm) {
        String  url=externalForm;
        if (StringUtils.isEmpty(url)) {
            return url;
        }

        if (url.startsWith(ExtendedFileUtils.JAR_URL_PREFIX)) {
            return url.substring(ExtendedFileUtils.JAR_URL_PREFIX.length());
        }       
        
        return url;
    }

    /**
     * Compares 2 {@link URL}-s by using their {@link URL#toExternalForm()} values
     * <U>after</U> applying {@link #adjustURLPathValue(String)} to the them 
     */
    public static final Comparator<URL> BY_EXTERNAL_FORM_COMPARATOR=new Comparator<URL>() {
            @Override
            public int compare (URL o1, URL o2) {
            	if (o1 == o2) {
            		return 0;
            	}

            	final String    f1=adjustURLPathValue((o1 == null) ? null : o1.toExternalForm()),
                                f2=adjustURLPathValue((o2 == null) ? null : o2.toExternalForm());
                if (f1 == null) {   // push null(s) to end
                    return (f2 == null) ? 0 : (+1);
                } else if (f2 == null) {
                    return (-1);
                } else {
                    return f1.compareTo(f2);
                }
            }
        };

    /**
     * Compares 2 {@link URL}-s by using their {@link URL#getPath()} values
     * <U>after</U> applying {@link #adjustURLPathValue(String)} to them 
     */
    public static final Comparator<URL> BY_PATH_ONLY_COMPARATOR=new Comparator<URL>() {
            @Override
            public int compare (URL u1, URL u2) {
            	if (u1 == u2) {
            		return 0;
            	}

                final String    p1=adjustURLPathValue((u1 == null) ? null : u1.getPath()),
                                p2=adjustURLPathValue((u2 == null) ? null : u2.getPath());
                // push null(s) to end
                if (p1 == null) {
                    return (p2 == null) ? 0 : (+1);
                } else if (p2 == null) {
                    return (-1);
                } else {
                    return p1.compareTo(p2);
                }
            }
        };

    /**
     * @param url A {@link URL} - ignored if {@code null}
     * @return The path after stripping any trailing '/' provided the path
     * is not '/' itself
     * @see #adjustURLPathValue(String)
     */
    public static final String adjustURLPathValue(URL url) {
        return adjustURLPathValue((url == null) ? null : url.getPath());
    }

    /**
     * @param path A URL path value - ignored if {@code null}/empty
     * @return The path after stripping any trailing '/' provided the path
     * is not '/' itself
     */
    public static final String adjustURLPathValue(final String path) {
        final int   pathLen=ExtendedCharSequenceUtils.getSafeLength(path);
        if ((pathLen <= 1) || (path.charAt(pathLen - 1) != '/')) {
            return path;
        }

        return path.substring(0, pathLen - 1);
    }

    /**
     * Adds an &quot;extension&quot; to an existing URL &quot;base&quot;
     * @param base The base URL - may not be {@code null}/empty
     * @param extension The extension to be appended - ignored if {@code null}
     * or empty
     * @return The concatenation result - <B>Note:</B> takes care of any
     * trailing/preceding '/' in either the base or the extension - e.g.,</BR></BR>
     * <code>
     * concat(&quot;http://a/b/c/&quot;, &quot;d/e/f&quot;) = &quot;http://a/b/c/d/e/f&quot;</br>
     * concat(&quot;http://a/b/c&quot;, &quot;/d/e/f&quot;) = &quot;http://a/b/c/d/e/f&quot;</br>
     * concat(&quot;http://a/b/c/&quot;, &quot;/d/e/f&quot;) = &quot;http://a/b/c/d/e/f&quot;</br>
     * </code>
     */
    public static final String concat(String base, String extension) {
        Validate.notEmpty(base, "No base URL", ArrayUtils.EMPTY_OBJECT_ARRAY);
        if (StringUtils.isEmpty(extension)) {
            return base;
        }
        
        char    endChar=base.charAt(base.length() - 1), startChar=extension.charAt(0);
        if (endChar == RESOURCE_PATH_SEPARATOR) {
            if (startChar == RESOURCE_PATH_SEPARATOR) {
                if (extension.length() == 1) {
                    return base;
                } else {
                    return base + extension.substring(1);
                }
            } else {
                return base + extension;
            }
        } else if (startChar == RESOURCE_PATH_SEPARATOR) {
            return base + extension;
        } else {
            return base + String.valueOf(RESOURCE_PATH_SEPARATOR) + extension;
        }
    }

    /**
     * Compares <U>case insensitive</U> 2 {@link URL}-s by their {@link URL#getProtocol()} values
     */
    public static final Comparator<URL> BY_PROTOCOL_COMPARATOR=
            new Comparator<URL>() {
                @Override
                public int compare(URL o1, URL o2) {
                    if (o1 == o2) {
                        return 0;
                    }

                    String  s1=(o1 == null) ? null : o1.getProtocol();
                    String  s2=(o2 == null) ? null : o2.getProtocol();
                    return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
                }
        };

    /**
     * Compares <U>case insensitive</U> 2 {@link URI}-s by their {@link URI#getScheme()} values
     */
    public static final Comparator<URI> BY_SCHEME_COMPARATOR=
            new Comparator<URI>() {
                @Override
                public int compare(URI o1, URI o2) {
                	if (o1 == o2) {
                		return 0;
                	}

                	String  s1=(o1 == null) ? null : o1.getScheme();
                    String	s2=(o2 == null) ? null : o2.getScheme();
                    return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
                }
        };
}
