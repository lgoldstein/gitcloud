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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.io.filefilter.CanExecuteFileFilter;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.CanWriteFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.ExistFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.lang3.ExtendedValidate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExtendedExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triplet;
import org.apache.commons.net.util.URLUtils;

/**
 * @author lgoldstein
 *
 */
public class ExtendedFileUtils extends FileUtils {
    public ExtendedFileUtils() {
        super();
    }

    public static final String  JAR_FILE_SUFFIX=".jar";
    public static final File[]  EMPTY_FILES={ };

    /**
     * URL/URI scheme that refers to a JAR
     */
    public static final String  JAR_URL_SCHEME="jar";
    /**
     * Prefix used in URL(s) that reference a resource inside a JAR
     */
    public static final String  JAR_URL_PREFIX=JAR_URL_SCHEME + ":";
    /**
     * URL/URI scheme that refers to a file 
     */
    public static final String  FILE_URL_SCHEME="file";
    /**
     * Prefix used in URL(s) that reference a file resource
     */
    public static final String  FILE_URL_PREFIX=FILE_URL_SCHEME + ":";

    public static final char	READ_ACCESS_CHAR='r', WRITE_ACCESS_CHAR='w', EXECUTE_ACCESS_CHAR='x', FOLDER_ACCESS_CHAR='d', NO_ACCESS_CHAR='-';

    /**
     * @param file The {@link File} to be read
     * @return The {@code char[]} with the file's contents
     * @throws IOException If failed to read from the file
     */
    public static final char[] readFileToCharArray(File file) throws IOException {
        long    size=file.length();
        Validate.isTrue((size >= 0L) && (size < Integer.MAX_VALUE), "Bad file size for %s: %d", file, Long.valueOf(size));
        char[]  chars=new char[(int) size];

        Reader  rdr=new FileReader(file);
        try {
            IOUtils.readFully(rdr, chars);
        } finally {
            rdr.close();
        }
        
        return chars;
    }

    /**
     * Copies 2 {@link File}-s using an efficient internal Java mechanism
     * (better than read-write cycle)
     * @param srcFile source file object
     * @param dstFile destination file object - if destination folder does not
     * exist it is created using {@link File#mkdirs()}
     * @return number of copied bytes - Note: if un-successful, some partial
     * content may have been copied.
     * @throws IOException if read/write error
     */
    public static final long quickCopyFile (File srcFile, File dstFile) throws IOException {
        return quickCopyFile(srcFile, dstFile, (-1L));
    }

    /**
     * Copies 2 {@link File}-s using an efficient internal Java mechanism
     * (better than read-write cycle)
     * @param srcFile source file object
     * @param dstFile destination file object - if destination folder does not
     * exist it is created using {@link File#mkdirs()}
     * @param cpySize Max. number of bytes to copy - if negative then till EOF
     * @return number of copied bytes - Note: if un-successful, some partial
     * content may have been copied.
     * @throws IOException if read/write error
     */
    @SuppressWarnings("resource")
    public static final long quickCopyFile (File srcFile, File dstFile, long cpySize) throws IOException {
        Validate.notNull(srcFile, "No source file", ArrayUtils.EMPTY_OBJECT_ARRAY);
        ExtendedValidate.isTrue(srcFile.isFile(), "Not a source file: %s", srcFile);

        Validate.notNull(dstFile, "No destination file", ArrayUtils.EMPTY_OBJECT_ARRAY);
        ExtendedValidate.isTrue((!dstFile.exists()) || dstFile.isFile(), "Not a destination file: %s", dstFile);

        File  dstFolder=dstFile.getParentFile();
        if ((!dstFolder.exists()) && (!dstFolder.mkdirs()))
            throw new IOException("Failed to created destination folder(s): " + dstFolder);

        FileChannel srcChannel=null, dstChannel=null;
        try
        {
            srcChannel = new FileInputStream(srcFile).getChannel();
            dstChannel = new FileOutputStream(dstFile).getChannel();

            // Copy file contents from source to destination
            long  srcLen=srcFile.length(), copyLen=dstChannel.transferFrom(srcChannel, 0, (cpySize < 0L) ? srcLen : cpySize);
            if ((cpySize < 0L) && (copyLen != srcLen)) { // make sure full copy
                throw new StreamCorruptedException("Mismatched copy length: expected=" + srcLen + ", actual=" + copyLen);
            }

            return copyLen;
        } finally {
            ExtendedIOUtils.closeAll(srcChannel, dstChannel);
        }
    }

	/**
	 * A {@link IOFileFilter} that <code>accept</code>s all non-<code>null</code>
	 * {@link File}s that are JAR(s)
	 * @see #isJarFileName(File)
	 */
	public static final IOFileFilter	JAR_FILE_FILTER=new IOFileFilter() {
			@Override
			public boolean accept (File dir, String name) {
			    if (!isJarFileName(name)) {
			        return false;    // debug breakpoint
			    } else {
			        return true;
			    }
			}

            @Override
            public boolean accept(File f) {
				if (!isJarFileName(f)) {
					return false;	// debug breakpoint
				} else {
					return true;
				}
			}
            
            @Override
            public String toString() {
                return "JAR_FILE_FILTER";
            }
		};

	public static final boolean isJarFileName (File f) {
		return (f != null) && isJarFileName(f.getName());
	}

	public static final boolean isJarFileName (String name) {
		if (StringUtils.isEmpty(name) || (name.length() <= JAR_FILE_SUFFIX.length())) {
			return false;
		} else {
			return name.endsWith(JAR_FILE_SUFFIX);
		}
	}

    public static final URL toJarURL(File file, String name) throws MalformedURLException {
    	if ((file == null) || StringUtils.isEmpty(name)) {
    		throw new MalformedURLException("toJarURL(" + file + ")[" + name + "] incomplete specification");
    	}
    	
    	URL		baseURL=toURL(file);
    	String	urlPrefix=baseURL.toExternalForm();
    	String	resourceURL=new StringBuilder(JAR_URL_PREFIX.length() + urlPrefix.length() + name.length() + 2)
    							.append(JAR_URL_PREFIX)
    							.append(urlPrefix)
    							.append(URLUtils.RESOURCE_SUBPATH_SEPARATOR)
    							.append(URLUtils.RESOURCE_PATH_SEPARATOR)
    							.append(name)
    						.toString();
    	return new URL(resourceURL);
	}

	/**
	 * Converts a URL to a file
	 * @param url The {@link URL} - ignored if <code>null</code>
	 * @return The matching {@link File}
	 * @throws MalformedURLException If URL does not refer to a file location
	 * @see #asFile(URL)
	 */
	public static File asFile(URL url) throws MalformedURLException {
		if (url == null) {
			return null;
		}

		try {
			return asFile(url.toURI());
		} catch(URISyntaxException e) {
			throw new MalformedURLException("asFile(" + URLUtils.toString(url) + ")"
										  + " cannot (" + e.getClass().getSimpleName() + ")"
										  + " convert to URI: " + e.getMessage());
		}
	}
	
	/**
	 * Converts a URI to a file
	 * @param uri The {@link URI} - ignored if <code>null</code>
	 * @return The matching {@link File}
	 * @throws MalformedURLException If URI does not refer to a file location
	 * @see #FILE_URL_SCHEME
	 */
	public static File asFile(URI uri) throws MalformedURLException {
		if (uri == null) {
			return null;
		}
		
		if (!FILE_URL_SCHEME.equals(uri.getScheme())) {
			throw new MalformedURLException("asFile(" + uri + ") not a '" + FILE_URL_SCHEME + "' scheme");
		}
		
        // TODO consider using decodeUrl(...)
		return new File(uri);
	}

	public static final List<URL> toURL(File ... files) throws MalformedURLException {
		return toURL(ExtendedArrayUtils.asList(files));
	}
	
	public static final List<URL> toURL(Collection<? extends File> files) throws MalformedURLException {
		try {
			return ExtendedCollectionUtils.collectToList(files, FILE_TO_URL_TRANSFORMER);
		} catch(RuntimeException e) {
			Throwable	t=ExtendedExceptionUtils.getSafeCause(e);
			if (t instanceof MalformedURLException) {
				throw (MalformedURLException) t;
			} else {
				throw e;
			}
		}
	}
	/**
	 * Transforms a {@link File} to a {@link URL} via the {@link #toURL(File)}
	 * call. If a {@link MalformedURLException} is thrown then it is caught and
	 * re-thrown as a {@link RuntimeException} whose cause is the original
	 * {@link MalformedURLException}
	 */
	public static final ExtendedTransformer<File,URL> FILE_TO_URL_TRANSFORMER=
			new AbstractExtendedTransformer<File, URL>(File.class,URL.class) {
				@Override
				public URL transform(File input) {
					try {
						return toURL(input);
					} catch(MalformedURLException e) {
						throw new RuntimeException(e);
					}
				}
			};

    public static URL toURL(File f) throws MalformedURLException {
    	if (f == null) {
    		return null;
    	} else {
    		return f.toURI().toURL();
    	}
    }
    
    /**
     * Compares 2 {@link File}s using case <U>insensitive</U> comparison
     * of their {@link File#getAbsolutePath()} value(s). <B>Note:</B> this
     * choice was made since in Windows paths are case insensitive. Furthermore,
     * it is bad practice to use paths that differ only in their case
     */
    public static final Comparator<File> BY_ABSOLUTE_PATH_COMPARATOR=
            new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1 == o2) {
                        return 0;
                    }
                    
                    String  p1=(o1 == null) ? null : o1.getAbsolutePath();
                    String  p2=(o2 == null) ? null : o2.getAbsolutePath();
                    return ExtendedStringUtils.safeCompare(p1, p2, false);
                }
        
        };

	/**
	 * A <code>null</code>-safe way to retrieve a {@link String} representation
	 * of a {@link File} instance. <B>Caveat emptor:</B> this method <B><U>should
	 * not be used instead of {@link File#getAbsolutePath()}</U></B> where the
	 * absolute path is required as its implementation may change in the future 
	 * @param f The {@link File} instance
	 * @return The {@link File#getAbsolutePath()} - <code>null</code> if
	 * <code>null</code> file instance
	 */
	public static final String toString(File f) {
		if (f == null) {
			return null;
		} else {
			return f.getAbsolutePath();
		}
	}

	private static final class LazyTempFolderHolder {
		private static final File	tmpDir=FileUtils.getTempDirectory();
	}

	// a more efficient replacement for getTempDirectory
	@SuppressWarnings("synthetic-access")
	public static final File retrieveTempDirectory() {
		return LazyTempFolderHolder.tmpDir;
	}

	/**
	 * Converts a group of {@link URL}s to their {@link File} &quot;sources&quot;
	 * equivalents
	 * @param urls The initial {@link Collection} of {@link URL}s - may be
	 * <code>null</code>/empty
	 * @return A {@link List} of the converted {@link File} equivalents
	 * @see #toFileSource(URL)
	 * @throws MalformedURLException if failed to convert a {@link URL} to a {@link File}
	 */
	public static final List<File> toFileSource(final Collection<? extends URL> urls) throws MalformedURLException {
		try {
			return ExtendedCollectionUtils.collectToList(urls, URL2SRCFILE_TRANSFORMER);
		} catch(RuntimeException e) {
			Throwable	t=ExtendedExceptionUtils.getSafeCause(e);
			if (t instanceof MalformedURLException) {
				throw (MalformedURLException) t;
			} else {
				throw e;
			}
		}
	}
	
	public static final ExtendedTransformer<URL,File>	URL2SRCFILE_TRANSFORMER=
			new AbstractExtendedTransformer<URL, File>(URL.class,File.class) {

				@Override
				public File transform(URL input) {
					try {
						return toFileSource(input);
					} catch(MalformedURLException e) {
						throw new RuntimeException(e);
					}
				}
				
			};
	/**
	 * Converts a {@link URL} that may refer to an internal resource to
	 * a {@link File} representing is &quot;source&quot; container (e.g.,
	 * if it is a resource in a JAR, then the result is the JAR's path)
	 * @param url The {@link URL} - ignored if <code>null</code>
	 * @return The matching {@link File}
	 * @throws MalformedURLException If source URL does not refer to a
	 * file location
	 * @see #toFileSource(URI)
	 */
	public static File toFileSource(URL url) throws MalformedURLException {
		if (url == null) {
			return null;
		}
		
		try {
			return toFileSource(url.toURI());
		} catch(URISyntaxException e) {
			throw new MalformedURLException("toFileSource(" + URLUtils.toString(url) + ")"
										  + " cannot (" + e.getClass().getSimpleName() + ")"
										  + " convert to URI: " + e.getMessage());
		}
	}
	
	/**
	 * Converts a {@link URI} that may refer to an internal resource to
	 * a {@link File} representing is &quot;source&quot; container (e.g.,
	 * if it is a resource in a JAR, then the result is the JAR's path)
	 * @param uri The {@link URI} - ignored if <code>null</code>
	 * @return The matching {@link File}
	 * @throws MalformedURLException If source URI does not refer to a
	 * file location
	 * @see URLUtils#getURLSource(URI)
	 */
	public static File toFileSource(URI uri) throws MalformedURLException {
		String	src=URLUtils.getURLSource(uri);
		if (StringUtils.isEmpty(src)) {
			return null;
		}
		
		if (!src.startsWith(FILE_URL_PREFIX)) {
			throw new MalformedURLException("toFileSource(" + src + ") not a '" + FILE_URL_SCHEME + "' scheme");
		}

		try {
			return new File(new URI(src));
		} catch(URISyntaxException e) {
			throw new MalformedURLException("toFileSource(" + src + ")"
										  + " cannot (" + e.getClass().getSimpleName() + ")"
										  + " convert to URI: " + e.getMessage());
		}
	}

	/*

    public static final File getURLSourceFile(URL url) throws MalformedURLException  {
    	return getURLSourceFile((url == null) ? null : url.toExternalForm());
    }

    public static final File getURLSourceFile(URI uri) throws MalformedURLException  {
    	return getURLSourceFile((uri == null) ? null : uri.toString());
    }
    
    public static final File getURLSourceFile(String url) throws MalformedURLException {
    	String	src=getURLSource(url);
    	if (StringUtils.isEmpty(src)) {
    		return null;
    	} else {
    		try {
    			return asFile(new URI(src));
    		} catch(URISyntaxException e) {
    			throw new MalformedURLException("getURLSourceFile(" + url + ")"
					    					  + " failed (" + e.getClass().getSimpleName() + ")"
					    					  + " to create URI=" + src
					    					  + ": " + e.getMessage());
    		}
    	}
    }

    */

	/**
	 * An {@link ExtendedTransformer} implementation that converts a {@link File}
	 * to a {@link String} using the {@link #getURLSource(File)} method
	 */
	public static final ExtendedTransformer<File, String> FILE2SOURCE_TRANSFORMER=
			new AbstractExtendedTransformer<File, String>(File.class,String.class) {
				@Override
                public String transform(File src) {
					return ExtendedFileUtils.getURLSource(src);
				}
			};

    /**
     * @param file The {@link File} value - ignored if <code>null</code>
     * @return The file source path where {@link #JAR_URL_PREFIX} and
     * any sub-resource are stripped
     * @see URLUtils#getURLSource(URI)
     */
    public static final String getURLSource (File file) {
    	return URLUtils.getURLSource((file == null) ? null : file.toURI());
    }

	/**
	 * A {@link IOFileFilter} that uses O/S specific knowledge to determine if
	 * a {@link File} is executable - e.g., for Windows it checks the file
	 * extension
	 * @see CanExecuteFileFilter#CAN_EXECUTE
	 * @see ExtendedFilenameUtils#isWindowsExecutableExtension(String)
	 */
	public static final IOFileFilter OS_EXECUTABLE_FILTER=new IOFileFilter() {
    		@Override
            public boolean accept (File dir, String name) {
                if (FilenameUtils.isSystemWindows()) {
                    return ExtendedFilenameUtils.isWindowsExecutableExtension(name);
                } else {
                    return CanExecuteFileFilter.CAN_EXECUTE.accept(dir, name);
                }
            }
    
            @Override
    		public boolean accept (File pathname) {
    			if (FilenameUtils.isSystemWindows()) {
    				return ExtendedFilenameUtils.isWindowsExecutableExtension(FilenameUtils.getExtension(pathname.getName()));
    			} else {
    				return CanExecuteFileFilter.CAN_EXECUTE.accept(pathname);
    			}
    		}
    
            @Override
            public String toString() {
                return "OS_EXECUTABLE_FILTER";
            }
    	};

    /**
     * A {@link IOFileFilter} that accepts files with hidden names
     * @see ExtendedFilenameUtils#isHiddenName(CharSequence)
     */
    public static final IOFileFilter	HIDDEN_NAME_FILTER=new IOFileFilter() {
			@Override
			public boolean accept (File dir, String name) {
			    return ExtendedFilenameUtils.isHiddenName(name);
			}

            @Override
			public boolean accept (File pathname) {
				if (pathname == null) {
					return false;
				} else {
					return ExtendedFilenameUtils.isHiddenName(pathname.getName());
				}
			}
            
            @Override
            public String toString() {
                return "HIDDEN_NAME_FILTER";
            }
		};
	public static final IOFileFilter	NON_HIDDEN_NAME_FILTER=new NotFileFilter(HIDDEN_NAME_FILTER);

	/**
	 * A {@link IOFileFilter} that accepts files that are accepted either by
	 * the {@link HiddenFileFilter#HIDDEN} or the {@link #HIDDEN_NAME_FILTER}.
	 * In other words, either files declared by the file system as hidden
	 * or ones that follow the dot prefix naming convention
	 */
	public static final IOFileFilter COMPOUND_HIDDEN_FILE_FILTER=new OrFileFilter(HiddenFileFilter.HIDDEN, HIDDEN_NAME_FILTER);
	public static final IOFileFilter COMPOUND_NON_HIDDEN_FILE_FILTER=new NotFileFilter(COMPOUND_HIDDEN_FILE_FILTER);

	/**
     * Creates a {@link File} instance referencing the path created from the
     * given sub-components. This is a more efficient version of {@link FileUtils#getFile(String...)}
     * @param pathComponents The path components in the <U>order</U> that they
     * should be used to build the path - ignored if <code>null</code>/empty
     * @return Result {@link File} - <code>null</code> if no components provided
     * @throws IllegalArgumentException If one of the components is <code>null</code>
     * or empty
     * @see #buildFile(Collection)
     */
    public static File buildFile (CharSequence ... pathComponents) throws IllegalArgumentException {
        return buildFile(ExtendedArrayUtils.asList(pathComponents));
    }

    /**
     * Creates a {@link File} instance referencing the path created from the
     * given sub-components
     * @param pathComponents The path components in the <U>order</U> that they
     * should be used to build the path - ignored if <code>null</code>/empty
     * @return Result {@link File} - <code>null</code> if no components provided
     * @throws IllegalArgumentException If one of the components is <code>null</code>
     * or empty
     * @see ExtendedFilenameUtils#buildFilePath(Collection)
     */
    public static final File buildFile (Collection<? extends CharSequence> pathComponents) throws IllegalArgumentException {
        String  path=ExtendedFilenameUtils.buildFilePath(pathComponents);
        if (StringUtils.isEmpty(path)) {
            return null;
        } else {
            return new File(path);
        }
    }

    /**
     * Creates a new {@link File} from a root folder and some path components.
     *  This is a more efficient version of {@link FileUtils#getFile(File, String...)}
     * @param rootFolder The root folder
     * @param pathComponents The sub-path components - can be <code>null</code>/empty,
     * in which case the root folder is returned as the result
     * @return The {@link File} instance representing the <U>relative</U> location
     * from the root folder with the sub-path appended to it (if exists)
     * @throws IllegalArgumentException If no root folder specified
     * @see #buildRelativeFile(File, Collection)
     */
    public static File buildRelativeFile (File rootFolder, CharSequence ... pathComponents) throws IllegalArgumentException {
        return buildRelativeFile(rootFolder, ExtendedArrayUtils.asList(pathComponents));
    }

    /**
     * Creates a new {@link File} from a root folder and some path components
     * @param rootFolder The root folder
     * @param pathComponents The sub-path components - can be <code>null</code>/empty,
     * in which case the root folder is returned as the result
     * @return The {@link File} instance representing the <U>relative</U> location
     * from the root folder with the sub-path appended to it (if exists)
     * @throws IllegalArgumentException If no root folder specified
     * @see ExtendedFilenameUtils#buildFilePath(Collection)
     */
    public static File buildRelativeFile (File rootFolder, Collection<? extends CharSequence> pathComponents) throws IllegalArgumentException {
        if (rootFolder == null) {
            throw new IllegalArgumentException("No root folder provided");
        }

        String  subPath=ExtendedFilenameUtils.buildFilePath(pathComponents);
        if (StringUtils.isEmpty(subPath)) {
            return rootFolder;
        } else {
            return new File(rootFolder, subPath);
        }
    }
    
    /**
     * @param file The input {@link File} - ignored if <code>null</code>
     * @return A {@link List} of the name components comprising the full
     * path - in the same order as they appear in the hierarchy
     */
    public static final List<String> breakDownFilePath (File file) {
        if (file == null) {
            return Collections.emptyList();
        }

        List<String>    pathComponents=new ArrayList<String>();
        for (File curFile=file; curFile != null; curFile=curFile.getParentFile()) {
            String  name=curFile.getName();
            if (StringUtils.isEmpty(name)) { // this happens for Windows root drive
                name = curFile.getAbsolutePath();
            }
            pathComponents.add(0, name);
        }
        
        return pathComponents;
    }

    /**
     * @param f The {@link File} to be examined - ignored if <code>null</code>
     * @return A {@link String} representing the file's access according to Linux format
     * @see #appendFileAccess(Appendable, File)
     */
    public static final String getFileAccess(File f) {
    	if ((f == null) || (!f.exists())) {
    		return "";
    	}

    	try {
    		return appendFileAccess(new StringBuilder(3), f).toString();
    	} catch(IOException e) {
    		throw new RuntimeException("getFileAccess(" + toString(f) + ") " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
    	}
    }
    
    public static final ExtendedTransformer<File,Character>	FOLDER_FILE_ACCESS_CHAR=
    		new AccessCharTransformer(DirectoryFileFilter.DIRECTORY, FOLDER_ACCESS_CHAR);
    public static final ExtendedTransformer<File,Character>	READABLE_FILE_ACCESS_CHAR=
    		new AccessCharTransformer(CanReadFileFilter.CAN_READ, READ_ACCESS_CHAR);
    public static final ExtendedTransformer<File,Character>	WRITABLE_FILE_ACCESS_CHAR=
    		new AccessCharTransformer(CanWriteFileFilter.CAN_WRITE, WRITE_ACCESS_CHAR);
    public static final ExtendedTransformer<File,Character>	EXECUTABLE_FILE_ACCESS_CHAR=
    		new AccessCharTransformer(OS_EXECUTABLE_FILTER, EXECUTE_ACCESS_CHAR);
    public static final List<ExtendedTransformer<File,Character>>	FILE_ACCESS_CHARS=
    		Collections.unmodifiableList(
    				Arrays.asList(
    						FOLDER_FILE_ACCESS_CHAR,
    						READABLE_FILE_ACCESS_CHAR,
    						WRITABLE_FILE_ACCESS_CHAR,
    						EXECUTABLE_FILE_ACCESS_CHAR));

    /**
     * Appends the Linux-format access data of the examined file
     * @param sb The {@link Appendable} instance to use
     * @param f The {@link File} to be examined - ignored if <code>null</code>
     * @return The updated {@link Appendable} instance
     * @throws IOException If failed to append the data
     * @see #FOLDER_ACCESS_CHAR
     * @see #READ_ACCESS_CHAR
     * @see #WRITABLE_FILE_ACCESS_CHAR
     * @see #EXECUTABLE_FILE_ACCESS_CHAR
     * @see #OS_EXECUTABLE_FILTER
     */
    public static final <A extends Appendable> A appendFileAccess(final A sb, final File f) throws IOException {
    	if ((f == null) || (!f.exists())) {
    		return sb;
    	}
    	
    	for (Transformer<File,Character> xformer : FILE_ACCESS_CHARS) {
			sb.append(xformer.transform(f).charValue());
    	}
    	
    	return sb;
    }

    public static final void copyDirectory(File srcDir, File destDir, Predicate<Pair<File,File>> predicate) throws IOException {
        copyDirectory(srcDir, destDir, predicate, true);
    }

    public static final void copyDirectory(File srcDir, File destDir, Predicate<Pair<File,File>> predicate, boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, null, predicate, preserveFileDate);
    }

    public static final void copyDirectory(File srcDir, File destDir, FileFilter filter, Predicate<Pair<File,File>> predicate) throws IOException {
        copyDirectory(srcDir, destDir, filter, predicate, true);
    }

    public static final void copyDirectory(File srcDir, File destDir, FileFilter filter, Predicate<Pair<File,File>> predicate, boolean preserveFileDate) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source '" + srcDir + "' directory does not exist");
        }
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        
        String  srcPath=srcDir.getCanonicalPath(), dstPath=destDir.getCanonicalPath();
        if (FilenameUtils.isSystemWindows()) {
            if (srcPath.equalsIgnoreCase(dstPath)) {
                throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
            }
        } else {
            if (srcPath.equals(dstPath)) {
                throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
            }
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (dstPath.startsWith(srcPath)) {
            File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<String>(srcFiles.length);
                for (int i = 0; i < srcFiles.length; i++) {
                    File copiedFile = new File(destDir, srcFiles[i].getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, filter, predicate, preserveFileDate, exclusionList);
    }

    private static void doCopyDirectory(File srcDir, File destDir, FileFilter filter, Predicate<Pair<File,File>> predicate,
            boolean preserveFileDate, Collection<String> exclusionList) throws IOException {
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
            if (preserveFileDate) {
                destDir.setLastModified(srcDir.lastModified());
            }
        }
        if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        
        // recurse
        File[] files = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + srcDir);
        }
        for (File   srcFile : files) {
            File copiedFile = new File(destDir, srcFile.getName());
            if ((exclusionList != null) && exclusionList.contains(srcFile.getCanonicalPath())) {
                continue;
            }

            if ((predicate != null) && (!predicate.evaluate(Pair.of(srcFile, copiedFile)))) {
                continue;
            }

            if (srcFile.isDirectory()) {
                doCopyDirectory(srcFile, copiedFile, filter, predicate, preserveFileDate, exclusionList);
            } else {
                copyFile(srcFile, copiedFile, preserveFileDate);
            }
        }
    }

    public static final void copyFile(File srcFile, File destFile, Predicate<Pair<File,File>> predicate) throws IOException {
        copyFile(srcFile, destFile, predicate, true);
    }

    public static final void copyFile(File srcFile, File destFile, Predicate<Pair<File,File>> predicate, boolean preserveFileDate) throws IOException {
        if ((predicate == null) || predicate.evaluate(Pair.of(srcFile, destFile))) {
            copyFile(srcFile, destFile, preserveFileDate);
        }
    }

    public static final void moveFile(File srcFile, File destFile, Predicate<Pair<File,File>> predicate) throws IOException {
        if ((predicate == null) || predicate.evaluate(Pair.of(srcFile, destFile))) {
            if (destFile.exists() && destFile.isDirectory()) {
                moveFileToDirectory(srcFile, destFile, false);
            } else {
                moveFile(srcFile, destFile);
            }
        }
    }

    public static final void deleteFile(File file, Predicate<File> predicate) throws IOException {
        if (!file.exists()) {
            return; // nothing to do
        }
        
        if ((predicate != null) && (!predicate.evaluate(file))) {
            return;
        }

        if (file.isDirectory()) {
            File[]    files=file.listFiles();
            if (ExtendedArrayUtils.length(files) > 0) {
                for (File f : files) {
                    deleteFile(f, predicate);
                }
            }
            
            // directory might not be empty due to the predicate
            if (predicate != null) {
                files = file.listFiles();

                if (ExtendedArrayUtils.length(files) > 0) {
                    return;
                }
            }
        }

        if (!file.delete()) {
            throw new IOException("Failed to delete " + toString(file));
        }
    }

    /**
     * Compares the contents of the {@link File}-s
     * @param srcFile First file
     * @param dstFile Second file
     * @param maxRead Max. number of bytes to compare - if negative then
     * <U>all</U> bytes are compared
     * @param readSize work buffer size to be used to read data from the files
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference (File srcFile, File dstFile, long maxRead, int readSize) throws IOException {
        Validate.notNull(srcFile, "No 1st file", ArrayUtils.EMPTY_OBJECT_ARRAY);
        ExtendedValidate.isTrue(srcFile.isFile(), "1st file not a file: %s", srcFile);

        Validate.notNull(dstFile, "No 2nd file", ArrayUtils.EMPTY_OBJECT_ARRAY);
        ExtendedValidate.isTrue(dstFile.isFile(), "2nd file not a file: %s", dstFile);

        InputStream src=null, dst=null;
        try {
            src = new FileInputStream(srcFile);
            dst = new FileInputStream(dstFile);

            return ExtendedIOUtils.findDifference(src, dst, maxRead, readSize);
        } finally {
            ExtendedIOUtils.closeAll(src, dst);
        }
    }

    /**
     * Compares the contents of the {@link File}-s
     * @param srcFile First file
     * @param dstFile Second file
     * @param maxRead Max. number of bytes to compare - if negative then
     * <U>all</U> bytes are compared
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference (File srcFile, File dstFile, long maxRead) throws IOException {
        return findDifference(srcFile, dstFile, maxRead, ExtendedIOUtils.DEFAULT_BUFFER_SIZE_VALUE);
    }

    /**
     * Compares the contents of the {@link File}-s
     * @param srcFile Source file
     * @param dstFile Destination file
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference (File srcFile, File dstFile) throws IOException {
        return findDifference(srcFile, dstFile, (-1L), ExtendedIOUtils.DEFAULT_BUFFER_SIZE_VALUE);
    }

	private static final class AccessCharTransformer extends AbstractExtendedTransformer<File,Character> {
		private static final Character	NO_ACCESS=Character.valueOf(NO_ACCESS_CHAR);
		private final FileFilter	filter;
		private final Character		successChar;

		protected AccessCharTransformer(FileFilter accessFilter, char accessChar) {
			super(File.class, Character.class);
			if ((filter=accessFilter) == null) {
				throw new IllegalStateException("No file access filter provided");
			}
			
			successChar = Character.valueOf(accessChar);
		}

		@Override
		public Character transform (File f) {
			if (!ExistFileFilter.EXIST.accept(f)) {
				return NO_ACCESS;
			}
			
			if (filter.accept(f)) {
				return successChar;
			} else {
				return NO_ACCESS;
			}
		}
	}
}
