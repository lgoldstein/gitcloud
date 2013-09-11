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

package org.apache.commons.lang3;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.net.util.URLUtils;

/**
 * @author lgoldstein
 */
public class ExtendedClassUtils extends ClassUtils {
	public static final List<Class<?>>	EXTRA_BASE_TYPES=
			Collections.unmodifiableList(Arrays.asList((Class<?>) Enum.class, (Class<?>) String.class));
	
	public ExtendedClassUtils() {
		super();
	}
	/**
	 * Compares 2 {@link Class}-es using their <U>full</U> name
	 * @see Class#getName()
	 */
	public static final Comparator<Class<?>>	BY_FULL_NAME_COMPARATOR=
			new Comparator<Class<?>>() {
				@Override
				public int compare(Class<?> o1, Class<?> o2) {
					if (o1 == o2) {
						return 0;
					}
					String	n1=(o1 == null) ? null : o1.getName();
					String	n2=(o2 == null) ? null : o2.getName();
					return ExtendedStringUtils.safeCompare(n1, n2);
				}
			};
	/**
	 * @param type The type to check - ignored if <code>null</code>
	 * @return <code>true</code> if the given type is either a primitive,
	 * its wrapper or one of the types in the {@link #EXTRA_BASE_TYPES}
	 * @see ClassUtils#isPrimitiveOrWrapper(Class)
	 */
	public static final boolean isBaseType(Class<?> type) {
		if (type == null) {
			return false;
		}
		
		if (isPrimitiveOrWrapper(type)) {
			return true;
		}
		
		for (Class<?> baseType : EXTRA_BASE_TYPES) {
			if (baseType.isAssignableFrom(type)) {
				return true;
			}
		}
		
		return false;
	}

    /**
     * @param clazz A {@link Class} object
     * @return A {@link File} of the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     * @throws IllegalArgumentException If location is not a valid
     * {@link File} location
     * @see #getClassContainerLocationURI(Class)
     * @see ExtendedFileUtils#asFile(URI) 
     */
    public static final File getClassContainerLocationFile (Class<?> clazz)
            throws IllegalArgumentException {
        try {
            URI uri=getClassContainerLocationURI(clazz);
            return ExtendedFileUtils.asFile(uri);
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link URI} to the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     * @throws URISyntaxException if location is not a valid URI
     * @see #getClassContainerLocationURL(Class)
     */
    public static final URI getClassContainerLocationURI (Class<?> clazz) throws URISyntaxException {
        URL url=getClassContainerLocationURL(clazz);
        return (url == null) ? null : url.toURI();
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link URL} to the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     */
    public static final URL getClassContainerLocationURL (Class<?> clazz) {
        ProtectionDomain    pd=clazz.getProtectionDomain();
        CodeSource          cs=(pd == null) ? null : pd.getCodeSource();
        URL					url=(cs == null) ? null : cs.getLocation();
        if (url == null) {
        	if ((url=getClassBytesURL(clazz)) == null) {
        		return null;
        	}
        	
        	String	srcForm=URLUtils.getURLSource(url);
        	if (StringUtils.isEmpty(srcForm)) {
        		return null;
        	}

        	try {
        		url = new URL(srcForm);
        	} catch(MalformedURLException e) {
        		throw new IllegalArgumentException("getClassContainerLocationURL(" + clazz.getName() + ")"
        										 + "Failed to create URL=" + srcForm + " from " + url.toExternalForm()
        										 + ": " + e.getMessage());
        	}
        }

        return url;
    }
    
    /**
     * @param clazz The request {@link Class}
     * @return A {@link URL} to the location of the <code>.class</code> file
     * - <code>null</code> if location could not be resolved
     */
    public static final URL getClassBytesURL (Class<?> clazz) {
    	String	className=clazz.getName();
    	int		sepPos=className.indexOf('$');
    	// if this is an internal class, then need to use its parent as well
    	if (sepPos > 0) {
    		if ((sepPos=className.lastIndexOf('.')) > 0) {
    			className = className.substring(sepPos + 1);
    		}
    	} else {
    		className = clazz.getSimpleName();
    	}

        return clazz.getResource(className + ".class");
    }

    /**
     * @return A {@link ClassLoader} to be used by the caller. The loader is
     * resolved in the following manner:</P></BR>
     * <UL>
     *      <LI>
     *      If a non-<code>null</code> loader is returned from the
     *      {@link Thread#getContextClassLoader()} call then use it.
     *      </LI>
     *      
     *      <LI>
     *      Otherwise, use the same loader that was used to load this class.
     *      </LI>
     * </UL>
     * @see #getDefaultClassLoader(Class)
     */
    public static final ClassLoader getDefaultClassLoader() {
        return getDefaultClassLoader(ExtendedClassUtils.class);
    }
    
    /**
     * @param anchor An &quot;anchor&quot; {@link Class} to be used in case
     * no thread context loader is available
     * @return A {@link ClassLoader} to be used by the caller. The loader is
     * resolved in the following manner:</P></BR>
     * <UL>
     *      <LI>
     *      If a non-<code>null</code> loader is returned from the
     *      {@link Thread#getContextClassLoader()} call then use it.
     *      </LI>
     *      
     *      <LI>
     *      Otherwise, use the same loader that was used to load the anchor class.
     *      </LI>
     * </UL>
     * @throws IllegalArgumentException if no anchor class provided (regardless of
     * whether it is used or not) 
     */
    public static final ClassLoader getDefaultClassLoader(Class<?> anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("No anchor class provided");
        }

        Thread      t=Thread.currentThread();
        ClassLoader cl=t.getContextClassLoader();
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = anchor.getClassLoader();
        }

        if (cl == null) {	// no class loader - assume system
        	cl = ClassLoader.getSystemClassLoader();
        }

        return cl;
    }

    /**
     * Attempts to locate the <U>first</U> class in the hierarchy whose name
     * matches the specified one - in other words, answers the question
     * whether the specified class is a descendant of the named class.
     * @param clazz The &quot;root&quot; {@link Class} to start from - ignored
     * if <code>null</code>
     * @param className The class name to be located
     * @return The matching {@link Class} - <code>null</code> if not found
     * @see #findDescendantOf(Class, String, Class)
     * @throws IllegalArgumentException if <code>null</code>/empty class name
     */
    public static Class<?> findDescendantOf(Class<?> clazz, String className) {
        return findDescendantOf(clazz, className, null);
    }

    /**
     * Attempts to locate the <U>first</U> class in the hierarchy whose name
     * matches the specified one - in other words, answers the question
     * whether the specified class is a descendant of the named class - up to
     * the specified &quot;stop&quot; class.
     * @param clazz The &quot;root&quot; {@link Class} to start from - ignored
     * if <code>null</code>
     * @param className The class name to be located
     * @param stopClass The class beyond which to stop looking (inclusive -
     * i.e., the stop class <U>is checked</U> for match). If <code>null</code>
     * then same as looking all the way through to {@link Object} (including)
     * @return The matching {@link Class} - <code>null</code> if not found
     * @throws IllegalArgumentException if <code>null</code>/empty class name
     */
    public static Class<?> findDescendantOf(Class<?> clazz, String className, Class<?> stopClass) {
        if (StringUtils.isEmpty(className)) {
            throw new IllegalArgumentException("No instance class name specified");
        }
        
        for (Class<?> match=clazz; match != null; match=match.getSuperclass()) {
            if (className.equals(match.getName())) {
                return match;
            }
            
            if (match == stopClass) {
                break;
            }
        }
        
        return null;
    }
}
