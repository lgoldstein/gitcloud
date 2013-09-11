/*
 * Copyright 2013 Lyor Goldstein
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io;

import java.io.File;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.SortedSet;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.collections15.set.UnmodifiableSortedSet;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedCharSequenceUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * @author Lyor G.
 */
public class ExtendedFilenameUtils extends FilenameUtils {
    /**
     * The pattern used to generate a timestamped file name
     */
    public static final String FILENAME_TIMESTAMP_PATTERN="yyyy-MM-dd-HH-mm-ss";

    /**
     * The {@link FastDateFormat} used to generate timestamped file names
     * @see #FILENAME_TIMESTAMP_PATTERN
     */
    public static final DateFormat  FILENAME_TIMESTAMP_FORMAT=new SimpleDateFormat(FILENAME_TIMESTAMP_PATTERN);

    public ExtendedFilenameUtils() {
        super();
    }

    /**
     * Builds a full file path given sub-components using the {@link File#separatorChar}
     * between them
     * @param pathComponents The path components in the <U>order</U> that they
     * should be used to build the path - ignored if <code>null</code>/empty
     * @return Result path - <code>null</code> if no components provided
     * @throws IllegalArgumentException If one of the components is <code>null</code>
     * or empty
     * @see #buildFilePath(Collection)
     */
    public static final String buildFilePath (CharSequence ... pathComponents) throws IllegalArgumentException {
        return buildFilePath(ExtendedArrayUtils.asList(pathComponents));
    }
    
    /**
     * Builds a full file path given sub-components using the {@link File#separatorChar}
     * between them
     * @param pathComponents The path components in the <U>order</U> that they
     * should be used to build the path - ignored if <code>null</code>/empty
     * @return Result path - <code>null</code> if no components provided
     * @throws IllegalArgumentException If one of the components is <code>null</code>
     * or empty
     */
    public static final String buildFilePath (Collection<? extends CharSequence> pathComponents) throws IllegalArgumentException {
        if (ExtendedCollectionUtils.size(pathComponents) <= 0) {
            return null;
        }

        StringBuilder   sb=new StringBuilder(pathComponents.size() * 16);
        for (CharSequence c : pathComponents) {
            if (StringUtils.isEmpty(c)) {
                throw new IllegalArgumentException("buildFilePath(" + pathComponents + ") null/empty component");
            }

            int sbLen=sb.length();
            if (c.charAt(0) != File.separatorChar) {
                /* 
                 * if previous component does not end in a file separator and the
                 * new component does not start with one then add separator
                 */
                if ((sbLen > 0) && (sb.charAt(sbLen-1) != File.separatorChar)) {
                    sb.append(File.separatorChar);
                }
            } else {
                /*
                 * If new component starts with file separator AND previous
                 * one ended with separator, remove the previous ending separator
                 */
                if ((sbLen > 0) && (sb.charAt(sbLen-1) == File.separatorChar)) {
                    sb.setLength(sbLen - 1);
                }
            }

            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * @param name The original name that may (or may not) contain a dot
     * @param withDot TRUE=retrieve the extension with the dot, FALSE=return
     * only the &quot;pure&quot; extension
     * @return The extension with/out the dot after the last dot (if any) -
     * <code>null</code> if no extension found
     */
    public static final String getExtension (final String name, final boolean withDot) {
        final int   nLen=ExtendedCharSequenceUtils.getSafeLength(name), ePos=(nLen <= 0) ? (-1) : name.lastIndexOf('.');
        if ((ePos < 0) || (ePos >= (nLen-1))) {
            return null;
        }

        return withDot ? name.substring(ePos) : name.substring(ePos + 1);
    }

    /**
     * An {@link ExtendedTransformer} that returns the extension of a filename
     * - including the leading dot
     * @see #getExtension(String, boolean)
     */
    public static final ExtendedTransformer<String,String>  DOTTED_EXTENSION_EXTRACTOR=
            new AbstractExtendedTransformer<String,String>(String.class, String.class) {
                @Override
                public String transform (String input) {
                    return getExtension(input, true);
                }
            };

    /**
     * An {@link ExtendedTransformer} that returns the extension of a filename
     * - <U>without</U> the leading dot
     * @see #getExtension(String, boolean)
     */
    public static final ExtendedTransformer<String,String>  PURE_EXTENSION_EXTRACTOR=
            new AbstractExtendedTransformer<String,String>(String.class, String.class) {
                @Override
                public String transform (String input) {
                    return getExtension(input);
                }
            };

    /**
     * @param name The original name that may (or may not) contain a dot
     * @return The name without the extension
     */
    public static final String stripExtension(String name) {
        final int   nLen=ExtendedCharSequenceUtils.getSafeLength(name), ePos=(nLen <= 0) ? (-1) : name.lastIndexOf('.');
        if (ePos < 0) {
            return name;
        } else {
            return name.substring(0, ePos);
        }
    }

    /**
     * @param prefix Filename prefix - ignored if <code>null</code>/empty
     * @param timestamp The timestamp value to use to generate the name
     * @param suffix Filename suffix - ignored if <code>null</code>/empty
     * @return The generated name
     * @see #generateTimestampedName(String, Date, String)
     */
    public static final String generateTimestampedName(String prefix, Calendar timestamp, String suffix) {
        return generateTimestampedName(prefix, timestamp.getTime(), suffix);
    }

    /**
     * @param prefix Filename prefix - ignored if <code>null</code>/empty
     * @param timestamp The timestamp value to use to generate the name
     * @param suffix Filename suffix - ignored if <code>null</code>/empty
     * @return The generated name
     * @see #generateTimestampedName(String, Date, String)
     */
    public static final String generateTimestampedName(String prefix, long timestamp, String suffix) {
        return generateTimestampedName(prefix, new Date(timestamp), suffix);
    }
    
    /**
     * @param prefix Filename prefix - ignored if <code>null</code>/empty
     * @param suffix Filename suffix - ignored if <code>null</code>/empty
     * @return An {@link ExtendedTransformer} that generates a timestamped
     * filename using its {@link Date} argument
     * @see #FILENAME_TIMESTAMP_FORMAT
     * @see #timestampedNameGenerator(String, String, DateFormat)
     */
    public static final ExtendedTransformer<Date,String> timestampedNameGenerator(String prefix, String suffix) {
        return timestampedNameGenerator(prefix, suffix, FILENAME_TIMESTAMP_FORMAT);
    }
    /**
     * @param prefix Filename prefix - ignored if <code>null</code>/empty
     * @param timestamp The timestamp value to use to generate the name
     * @param suffix Filename suffix - ignored if <code>null</code>/empty
     * @return The generated name
     * @see #FILENAME_TIMESTAMP_FORMAT
     * @see #generateTimestampedName(String, Date, String, DateFormat)
     */
    public static final String generateTimestampedName(String prefix, Date timestamp, String suffix) {
        return generateTimestampedName(prefix, timestamp, suffix, FILENAME_TIMESTAMP_FORMAT);
    }

    /**
     * @param prefix Filename prefix - ignored if <code>null</code>/empty
     * @param timestamp The timestamp value to use to generate the name
     * @param suffix Filename suffix - ignored if <code>null</code>/empty
     * @param formatter The {@link DateFormat} to use to generate the timestamped name
     * @return The generated name
     * @see #generateTimestampedName(String, Date, String)
     */
    public static final String generateTimestampedName(String prefix, Calendar timestamp, String suffix, DateFormat formatter) {
        return generateTimestampedName(prefix, timestamp.getTime(), suffix, formatter);
    }

    /**
     * @param prefix Filename prefix - ignored if <code>null</code>/empty
     * @param timestamp The timestamp value to use to generate the name
     * @param suffix Filename suffix - ignored if <code>null</code>/empty
     * @param formatter The {@link DateFormat} to use to generate the timestamped name
     * @return The generated name
     * @see #generateTimestampedName(String, Date, String)
     */
    public static final String generateTimestampedName(String prefix, long timestamp, String suffix, DateFormat formatter) {
        return generateTimestampedName(prefix, new Date(timestamp), suffix, formatter);
    }

    /**
     * @param prefix Filename prefix - ignored if <code>null</code>/empty
     * @param timestamp The timestamp value to use to generate the name
     * @param suffix Filename suffix - ignored if <code>null</code>/empty
     * @param formatter The {@link DateFormat} to use to generate the timestamped name
     * @return The generated name
     * @see #FILENAME_TIMESTAMP_FORMAT
     */
    public static final String generateTimestampedName(String prefix, Date timestamp, String suffix, DateFormat formatter) {
        int prefixLen=ExtendedCharSequenceUtils.getSafeLength(prefix);
        int suffixLen=ExtendedCharSequenceUtils.getSafeLength(suffix);
        StringBuffer    buf=new StringBuffer(prefixLen + suffixLen + FILENAME_TIMESTAMP_PATTERN.length() /* just some extra length */ + Byte.SIZE);
        if (prefixLen > 0) {
            buf.append(prefix);
        }

        synchronized(formatter) {   // need to sync. it since some formatters are stateful
            formatter.format(timestamp, buf, new FieldPosition(0));
        }

        if (suffixLen > 0) {
            buf.append(suffix);
        }
        
        return buf.toString();
    }
    
    /**
     * @param prefix Filename prefix - ignored if <code>null</code>/empty
     * @param suffix Filename suffix - ignored if <code>null</code>/empty
     * @param formatter The {@link DateFormat} to use to generate the timestamped name
     * @return An {@link ExtendedTransformer} that generates a timestamped
     * filename using its {@link Date} argument
     * @see #generateTimestampedName(String, Date, String, DateFormat)
     * @throws NullPointerException if no formatter provided 
     */
    public static final ExtendedTransformer<Date,String> timestampedNameGenerator(
            final String prefix, final String suffix, final DateFormat formatter) {
        ExtendedValidate.notNull(formatter, "No timestamp formatter provided");
        
        return new AbstractExtendedTransformer<Date, String>(Date.class, String.class) {
            @Override
            public String transform(Date input) {
                if (input == null) {
                    return null;
                } else {
                    return generateTimestampedName(prefix, input, suffix, formatter);
                }
            }
        };
    }

    /**
     * @param prefix Expected prefix
     * @param suffix Expected suffix
     * @return An {@link ExtendedTransformer} that recovers the timestamp
     * {@link Date} value from names generated by one of the <code>generateTimestampedName</code>
     * methods
     * @see #FILENAME_TIMESTAMP_FORMAT
     * @see #timestampedNameParser(String, String, DateFormat)
     */
    public static final ExtendedTransformer<String,Date> timestampedNameParser(String prefix, String suffix) {
        return timestampedNameParser(prefix, suffix, FILENAME_TIMESTAMP_FORMAT);
    }

    /**
     * Parses a timestamped filename value generated by one of the <code>generateTimestampedName</code>
     * methods
     * @param name The generated name
     * @param prefix Expected prefix
     * @param suffix Expected suffix
     * @return The recovered {@link Date} timestamp - <code>null</code> if no name or
     * mismatched prefix/suffix
     * @throws IllegalArgumentException if cannot parse the timestamp after
     * stripping the prefix/suffix
     * @see #FILENAME_TIMESTAMP_FORMAT
     * @see #parseTimestampedName(String, String, String, DateFormat)
     */
    public static final Date parseTimestampedName(String name, String prefix, String suffix) {
        return parseTimestampedName(name, prefix, suffix, FILENAME_TIMESTAMP_FORMAT);
    }

    /**
     * @param prefix Expected prefix
     * @param suffix Expected suffix
     * @param formatter The {@link DateFormat} to use to parse the timestamped name (should
     * be the same one used to generate it)
     * @return An {@link ExtendedTransformer} that recovers the timestamp
     * {@link Date} value from names generated by one of the <code>generateTimestampedName</code>
     * methods
     * @see #parseTimestampedName(String, String, String, DateFormat)
     * @throws NullPointerException if no formatter provided 
     */
    public static final ExtendedTransformer<String,Date> timestampedNameParser(
                final String prefix, final String suffix, final DateFormat formatter) {
        ExtendedValidate.notNull(formatter, "No timestamp formatter provided");
        
        return new AbstractExtendedTransformer<String, Date>(String.class, Date.class) {
            @Override
            public Date transform(String input) {
                if (StringUtils.isEmpty(input)) {
                    return null;
                } else {
                    return parseTimestampedName(input, prefix, suffix, formatter);
                }
            }
        };
    }

    /**
     * Parses a timestamped filename value generated by one of the <code>generateTimestampedName</code>
     * methods
     * @param name The generated name
     * @param prefix Expected prefix
     * @param suffix Expected suffix
     * @param formatter The {@link DateFormat} to use to parse the timestamped name (should
     * be the same one used to generate it)
     * @return The recovered {@link Date} timestamp - <code>null</code> if no name or
     * mismatched prefix/suffix. <B>Note:</B> the recovered timestamp may differ from the
     * original one due to formatting issues - e.g., if the milliseconds value was not
     * included when the timestamped name was generated, then it will differ from the
     * original
     * @throws IllegalArgumentException if cannot parse the timestamp after
     * stripping the prefix/suffix
     */
    public static final Date parseTimestampedName(String name, String prefix, String suffix, DateFormat formatter) {
        String  strippedName=name;
        if (StringUtils.isEmpty(strippedName)) {
            return null;
        }
        
        if (ExtendedCharSequenceUtils.getSafeLength(prefix) > 0) {
            if (!name.startsWith(prefix)) {
                return null;
            }
            
            strippedName = strippedName.substring(prefix.length());
        }

        if (ExtendedCharSequenceUtils.getSafeLength(suffix) > 0) {
            if (!name.endsWith(suffix)) {
                return null;
            }
            
            strippedName = strippedName.substring(0, strippedName.length() - suffix.length());
        }

        if (StringUtils.isEmpty(strippedName)) {
            return null;
        }

        try {
            synchronized(formatter) {   // need to sync. it since some formatters are stateful
                return formatter.parse(strippedName);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed (" + e.getClass().getSimpleName() + ") to parse " + name + ": " + e.getMessage());
        }
    }

    /**
     * A {@link IOFileFilter} that accepts files with hidden names
     * @see #isHiddenName(CharSequence)
     */
    public static final IOFileFilter	HIDDEN_NAME_FILTER=new IOFileFilter() {
			@Override
			public boolean accept (File dir, String name) {
				return isHiddenName(name);
			}

            @Override
            public boolean accept (File file) {
                return isHiddenName((file == null) ? null : file.getName());
            }

            @Override
            public String toString() {
                return "HIDDEN_NAME_FILTER";
            }
		};

    /**
     * @param name A file name to be considered
     * @return <code>true</code> if name starts with a dot (.) and is not
     * the current folder (.) or its parent (..)
     */
    public static final boolean isHiddenName(CharSequence name) {
    	if (StringUtils.isEmpty(name)) {
    		return false;
    	}
    	
    	if (name.charAt(0) != '.') {
    		return false;
    	}

   		if (name.length() == 1) {
   			return false;	// the '.' itself is not hidden
    	}
   		
   		if ((name.length() == 2) && (name.charAt(1) == '.')) {
   			return false;
   		}
   		
   		return true;
    }

    /**
     * Accepts filenames that have one of the Windows executable extensions
     * @see #isWindowsExecutableExtension(String)
     */
    public static final IOFileFilter WINDOWS_EXECUTABLE_FILTER=new IOFileFilter() {
			@Override
			public boolean accept (File dir, String name) {
				String	extension=getExtension(name);
				return isWindowsExecutableExtension(extension);
			}

            @Override
            public boolean accept (File file) {
                String  name=(file == null) ? null : file.getName(), extension=getExtension(name);
                return isWindowsExecutableExtension(extension);
            }

            @Override
            public String toString() {
                return "WINDOWS_EXECUTABLE_FILTER";
            }
		};
	
	/**
	 * A {@link SortedSet} of extensions associated with Windows executables
	 * (<B>Note:</B> the extensions are listed <U>without</U> the leading dot)
	 */
    public static final SortedSet<String>	WINDOWS_EXECUTABLE_SUFFIXES=
    		UnmodifiableSortedSet.decorate(ExtendedCollectionUtils.asSortedSet(String.CASE_INSENSITIVE_ORDER, "bat", "exe", "cmd"));

    /**
     * @param extension The extension to be evaluated (without leading dot)
     * @return <code>true</code> if the extension is associated with a Windows
     * executables
     * @see #WINDOWS_EXECUTABLE_SUFFIXES
     */
    public static final boolean isWindowsExecutableExtension(String extension) {
    	if (StringUtils.isEmpty(extension)) {
    		return false;
    	}
    	
    	if (WINDOWS_EXECUTABLE_SUFFIXES.contains(extension)) {
    		return true;	// debug breakpoint
    	} else {
    		return false;
    	}
    }

    /**
     * @param parent A parent path to be used if the specified
     * <code>path</code> denotes a relative location
     * @param path A path element to be checked - if absolute, then it is
     * the return value, otherwise it is assumed to be relative to the
     * provided <code>parent</code> parameter
     * @return A path result
     * @see #isRelativePath(CharSequence)
     * @see FilenameUtils#normalize(String)
     */
    public static String resolveAbsolutePath (String parent, String path) {
        if (isRelativePath(path)) {
            return normalizeNoEndSeparator(concat(parent, path));
        } else {
            return path;
        }
    }

    /**
     * @param path A file path
     * @return <P><code>true</code> if the path is <U>relative</U> - i.e., does
     * not specify an absolute location:</P></BR>
     * <UL>
     *      <LI>
     *      if <code>null</code>/empty then &quot;relative&quot;
     *      </LI>
     *      
     *      <LI>
     *      if starts with dot (.) then relative - e.g., &quot;./foo&quot,
     *      &quot;../../bar&quot;
     *      </LI>
     *      
     *      <LI>
     *      If starts with {@link File#separatorChar} then absolute
     *      </LI>
     *      
     *      <LI>
     *      <B>Note:</B> for <U>Windows</U> systems, it checks if the path
     *      starts with a drive letter followed by a colon - if so, then
     *      considered to be an absolute path
     *      </LI>
     * </UL>
     */
    public static boolean isRelativePath (CharSequence path) {
        if (StringUtils.isEmpty(path)) {
            return true;
        }

        final char  ch0=path.charAt(0);
        if (ch0 == '.') {
            return true;
        } else if (ch0 == File.separatorChar) {
            return false;    // this is also true for Windows where "\\" is a network path...
        }

        if (isSystemWindows()  // special handling for windows paths with drive letter
         && (path.length() >= 2) // at least drive letter + colon
         && (path.charAt(1) == ':')) {
            // make sure a valid drive letter
            if ((('a' <= ch0) && (ch0 <= 'z'))
              || (('A' <= ch0) && (ch0 <= 'Z'))) {
                return false;
            }
        }

        return true;
    }
}
