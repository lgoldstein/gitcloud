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

package org.apache.commons.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.ExtendedFileUtils;
import org.apache.commons.io.ExtendedIOUtils;
import org.apache.commons.io.input.ExtendedCloseShieldInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.ExtendedAssert;

/**
 * Serves as a useful base class for jUnit tests
 * @author lgoldstein
 */
public abstract class AbstractTestSupport extends ExtendedAssert {
	public static final String TEMP_SUBFOLDER_NAME="temp";

	protected final Log	logger;
	private File	targetFolder;
	private File	testTempFolder;

	protected AbstractTestSupport() {
		logger = LogFactory.getLog(getClass());
	}

	@Before
    public void setUpTempDirectory() {
    	final File	tmpDir=ExtendedFileUtils.retrieveTempDirectory();
    	if (!tmpDir.exists()) {
    		if (!tmpDir.mkdirs()) {
    			logger.error("Failed to ensure existence of " + ExtendedFileUtils.toString(tmpDir));
    		} else {
    			logger.info("Created " + ExtendedFileUtils.toString(tmpDir));
    		}
    	} else {
    		assertTrue("Temp directory not a folder: " + ExtendedFileUtils.toString(tmpDir), tmpDir.isDirectory());
    	}
    	
    	assertTrue("Cannot access " + ExtendedFileUtils.toString(tmpDir), tmpDir.canRead());
    }

    protected InputStream getClassResourceAsStream (final String name) {
        return getClass().getResourceAsStream(name);
    }

    protected InputStream getLoaderResourceAsStream (final String name) {
        final ClassLoader   cl=getDefaultClassLoader();
        return cl.getResourceAsStream(name);
    }

    protected File getClassResourceFile (final String name) throws MalformedURLException {
        return toResourceFile(getClassResource(name));
    }

    protected URL getClassResource (final String name) {
        return getClass().getResource(name);
    }

    protected File getLoaderResourceFile (final String name) throws MalformedURLException {
        return toResourceFile(getLoaderResource(name));
    }

    protected URL getLoaderResource (final String name) {
        final ClassLoader   cl=getDefaultClassLoader();
        return cl.getResource(name);
    }

    protected File toResourceFile (final URL url) throws MalformedURLException {
        return ExtendedFileUtils.asFile(url);
    }

    protected File toResourceFile (final URI uri) throws MalformedURLException {
        return ExtendedFileUtils.asFile(uri);
    }

	protected ClassLoader getDefaultClassLoader() {
	    return ExtendedClassUtils.getDefaultClassLoader(getClass());
	}

    protected File ensureTempFolderExists () throws IllegalStateException {
    	synchronized(TEMP_SUBFOLDER_NAME) {
    		if (testTempFolder == null) {
    			final File	parent=detectTargetFolder();
    			testTempFolder = new File(parent, TEMP_SUBFOLDER_NAME);
    		}
    	}

    	return ensureFolderExists(testTempFolder);
    }

    public static final File ensureFolderExists (final File folder) throws IllegalStateException {
        Validate.notNull(folder, "No folder to ensure existence", ArrayUtils.EMPTY_OBJECT_ARRAY);
    	if ((!folder.exists()) && (!folder.mkdirs())) {
    		throw new IllegalStateException("Failed to create " + folder.getAbsolutePath());
    	}

    	return folder;
    }

    protected File detectTargetFolder () throws IllegalStateException {
    	synchronized(TEMP_SUBFOLDER_NAME) {
    		if (targetFolder == null) {
    			if ((targetFolder=detectTargetFolder(getClass())) == null) {
    				throw new IllegalStateException("Failed to detect target folder");
    			}
    		}
    	}

    	return targetFolder;
    }

    public static final String  JAVA_TYPE="java", JAVA_SUFFIX="." + JAVA_TYPE;
    protected File getMainJavaSourceFile() {
        return getMainSourceFile(JAVA_TYPE, JAVA_SUFFIX);
    }

    protected File getTestJavaSourceFile() {
        return getTestSourceFile(JAVA_TYPE, JAVA_SUFFIX);
    }

    public static final String  TEST_MODE="test", MAIN_MODE="main";
    protected File getMainSourceFile(String type, String suffix) {
        return getSourceFile(MAIN_MODE, type, suffix);
    }

    protected File getTestSourceFile(String type, String suffix) {
        return getSourceFile(TEST_MODE, type, suffix);
    }

    protected File getSourceFile(String mode, String type, String suffix) {
        return getSourceFile(mode, type, suffix, getClass());
    }

    public static final File  getMainJavaSourceFile(Class<?> c) {
        return getMainSourceFile(JAVA_TYPE, JAVA_SUFFIX, c);
    }

    public static final File getTestJavaSourceFile(Class<?> c) {
        return getTestSourceFile(JAVA_TYPE, JAVA_SUFFIX, c);
    }

    public static final File getMainSourceFile(String type, String suffix, Class<?> c) {
        return getSourceFile(MAIN_MODE, type, suffix, c);
    }

    public static final File getTestSourceFile(String type, String suffix, Class<?> c) {
        return getSourceFile(TEST_MODE, type, suffix, c);
    }

    /**
     * @param mode &quot;test&quot;, &quot;main&quot;
     * @param type &quot;java&quot;, &quot;resources&quot;, &quot;aspect&quot;
     * @param suffix &quot;.java&quot;, &quot;.groovy&quot;, &quot;.aj&quot;
     * @param c The anchor {@link Class} from which to derive the full path.
     * <B>Note:</B> if inner class, the the <U>container</U> source is path
     * is used
     * @return A {@link File} representing the expected location of the file
     * ({@code null} if cannot detect the &quot;target&quot; folder)
     * @see #detectTargetFolder(Class)
     */
    public static final File getSourceFile(String mode, String type, String suffix, Class<?> c) {
        Validate.notEmpty(mode,  "No mode", ArrayUtils.EMPTY_OBJECT_ARRAY);
        Validate.notEmpty(type,  "No type", ArrayUtils.EMPTY_OBJECT_ARRAY);

        File    target=detectTargetFolder(c);
        if (target == null) {
            return null;
        }
        
        File        root=ExtendedFileUtils.buildRelativeFile(target.getParentFile(), "src", mode, type);
        String      fqcp=Validate.notEmpty(c.getCanonicalName(), "No canonical name", ArrayUtils.EMPTY_OBJECT_ARRAY);
        int         ipos=fqcp.indexOf('$');
        if (ipos > 0) { // if internal class then get the container
            fqcp = fqcp.substring(0, ipos);
        }

        String[]    comps=StringUtils.split(fqcp, '.');
        if (!StringUtils.isEmpty(suffix)) {
            comps[comps.length - 1] += suffix;
        }
        
        return ExtendedFileUtils.buildRelativeFile(root, comps);
    }
    /**
     * @param anchor An anchor {@link Class} whose container we want to use
     * as the starting point for the &quot;target&quot; folder lookup up the
     * hierarchy
     * @return The &quot;target&quot; <U>folder</U> - <code>null</code> if not found
     * @see #detectTargetFolder(File)
     */
    public static final File detectTargetFolder (Class<?> anchor) {
    	return detectTargetFolder(ExtendedClassUtils.getClassContainerLocationFile(anchor));
    }
    
    /**
     * @param anchorFile An anchor {@link File} we want to use
     * as the starting point for the &quot;target&quot; folder lookup up the
     * hierarchy
     * @return The &quot;target&quot; <U>folder</U> - <code>null</code> if not found
     */
    public static final File detectTargetFolder (File anchorFile) {
    	for (File	file=anchorFile; file != null; file=file.getParentFile()) {
    		if ("target".equals(file.getName()) && file.isDirectory()) {
    			return file;
    		}
    	}

    	return null;
    }

    protected File createTempFile (final String prefix, final String suffix) throws IOException {
    	final File	destFolder=ensureTempFolderExists();
    	final File	file=File.createTempFile(prefix, suffix, destFolder);
        file.deleteOnExit();
        return file;
    }

	public static final boolean isQuit (final String s)
	{
		return "q".equalsIgnoreCase(s) || "quit".equalsIgnoreCase(s);
	}

	public static final String getval (final String prompt)
	{
		return getval(System.out, getStdin(), prompt);
	}

	// returns null if Quit, otherwise returns answer + index of NEXT argument
	public static final Pair<String,Integer> getNextValue(BufferedReader in, PrintStream out, String prompt, boolean allowEmpty, Pair<?,? extends Number> prevValue, String ... args) {
		return getNextValue(in, out, prompt, allowEmpty, prevValue.getRight().intValue(), args);
	}

	// returns null if Quit, otherwise returns answer + index of NEXT argument
    public static final Pair<String,Integer> getNextValue(BufferedReader in, PrintStream out, String prompt, boolean allowEmpty, int startIndex, String ... args) {
        if (startIndex < ExtendedArrayUtils.length(args)) {
            return Pair.of(args[startIndex], Integer.valueOf(startIndex+1));
        }
        
        String  ans=allowEmpty ? getval(out, in, prompt + " or (Q)uit") : getNonEmptyValue(out, in, prompt + " or (Q)uit");
        if (isQuit(ans)) {
            return null;
        } else {
            return Pair.of(ans, Integer.valueOf(startIndex));
        }
    }

	public static final String getNonEmptyValue (final PrintStream out, final BufferedReader in, final String prompt)
    {
	    for ( ; ; ) {
	        String ans=getval(out, in, prompt);
	        if (!StringUtils.isEmpty(ans)) {
	            return ans;
	        }
	    }
    }

	public static final String getval (final PrintStream out, final BufferedReader in, final String prompt)
	{
		if (out != null)
			out.print("Enter " + prompt + ": ");
		try
		{
			return in.readLine();
		}
		catch(IOException e)
		{
			return e.getClass().getName() + ": " + e.getMessage();
		}
	}

	/*----------------------------------------------------------------------*/

	public static final <V> V inputListChoice (
			final PrintStream out, final BufferedReader in, final String title,
			final List<? extends V> valsList, final V defValue /* null=none */)
	{
		final int	numVals=(null == valsList) ? 0 : valsList.size();
		if (numVals <= 0)
			return defValue;

		final String	prompt="selected index "
			+ ((defValue != null) ? "[ENTER=" + defValue + "]" : "")
			+ "/(Q)uit"
			;
		for ( ; ; )
		{
			out.println(title);
			for (int	vIndex=0; vIndex < numVals; vIndex++)
				out.println("\t" + vIndex + ": " + valsList.get(vIndex));

			final String	ans=getval(out, in, prompt);
			if (StringUtils.isEmpty(ans))
			{
				if (defValue != null)
					return defValue;
				continue;
			}

			if (isQuit(ans))
				return null;

			try
			{
				final int	vIndex=Integer.parseInt(ans);
				if ((vIndex >= 0) && (vIndex < numVals))
					return valsList.get(vIndex);
			}
			catch(NumberFormatException e)
			{
				// ignored
			}
		}
	}

	public static final int    DEFAULT_GC_ROUNDS=20;
	public static final void encourageGC() {
	    encourageGC(DEFAULT_GC_ROUNDS);
    }

	public static final void encourageGC(int numRounds) {
        System.runFinalization();
        for (int i= 0 ; i < numRounds; i++)
        {
            Thread.yield();
            System.gc();
        }
	}

	private static BufferedReader	_stdin	/* =null */;
	public static final synchronized BufferedReader getStdin ()
	{
		if (null == _stdin)
			_stdin = new BufferedReader(new InputStreamReader(new ExtendedCloseShieldInputStream(System.in)));
		return _stdin;
	}

	/**
	 * Makes sure that serializing and then de-serializing an object yields
	 * equal results
	 * @param message Prefix to be added to the assertions
	 * @param clazz The expected type of the de-serialized object
	 * @param expected The object instance to serialize
	 * @return The de-serialization result
	 * @see #reserialize(Class, Object)
	 */
	public static final <T> T assertSerializationEquals(String message, Class<T> clazz, Object expected) {
		try {
			T	actual=reserialize(clazz, expected);
			assertEquals(message, expected, actual);
			return actual;
		} catch(IOException e) {
			String	failMessage=message + ": " + e.getClass().getSimpleName() + ": " + e.getMessage();
			fail(failMessage);
			throw new RuntimeException(failMessage);	// dead code, but the compiler insists...
		}
	}

	/**
	 * Serializes an object in memory and then de-serializes it
	 * @param clazz The expected type of the de-serialized object
	 * @param instance The object instance to serialize
	 * @return The de-serialization result
	 * @throws IOException If failed to serialize/de-serialize it
	 */
	public static final <T> T reserialize(Class<T> clazz, Object instance) throws IOException {
		assertNotNull(clazz.getSimpleName() + ": No instance to serialize");
		assertObjectInstanceof(
				instance.getClass().getSimpleName() + ": not marked as " + Serializable.class.getSimpleName(),
				Serializable.class, instance);

        ByteArrayOutputStream bOs = new ByteArrayOutputStream(ExtendedIOUtils.DEFAULT_BUFFER_SIZE_VALUE);
        try {
	        ObjectOutputStream oosStream = new ObjectOutputStream(bOs);
	        try {
	        	oosStream.writeObject(instance);
	        } finally {
	        	oosStream.close();
	        }
        } finally {
        	bOs.close();
        }

        byte[] data = bOs.toByteArray();
        ByteArrayInputStream bIs = new ByteArrayInputStream(data);
        try {
        	ObjectInputStream iisStream = new ObjectInputStream(bIs);
        	try {
	        	Object deserialized = iisStream.readObject();
	        	if (deserialized == null) {
	        		return null;	// debug breakpoint
	        	} else {
	        		return clazz.cast(deserialized);
	        	}
        	} finally {
        		iisStream.close();
        	}
        } catch(ClassNotFoundException e) {
        	throw new StreamCorruptedException("Cannot re-load class: " + instance.getClass().getName());
        } finally {
        	bIs.close();
        }
	}
	
	public static final String shuffleCase(CharSequence cs) {
		if (StringUtils.isEmpty(cs)) {
			return "";
		}
		
		StringBuilder	sb=new StringBuilder(cs.length());
		for (int index=0; index < cs.length(); index++) {
			char	ch=cs.charAt(index);
			double	v=Math.random();
			if (Double.compare(v, 0.3d) < 0) {
				ch = Character.toUpperCase(ch);
			} else if ((Double.compare(v, 0.3d) >= 0) && (Double.compare(v, 0.6d) < 0)) {
				ch = Character.toLowerCase(ch);
			}
			sb.append(ch);
		}
		
		return sb.toString();
	}
	
	public static final <A extends Appendable> A appendRandomPadding(A sb, char padChar, int maxPadLen) throws IOException {
	    final int  padLen;
	    synchronized(RANDOMIZER) {
	        padLen = RANDOMIZER.nextInt(maxPadLen);
	    }
	    
	    for (int index=0; index < padLen; index++) {
	        sb.append(padChar);
	    }
	    
	    return sb;
	}

	public static final <K,V,M extends Map<K,V>> M assertUnmodifiableMap(M map, K testKey, V testValue) {
		return assertUnmodifiableMap(map, testKey, testValue, UnsupportedOperationException.class);
	}

	public static final <K,V,M extends Map<K,V>> M assertUnmodifiableMap(
			M map, K testKey, V testValue, Class<? extends RuntimeException> expectedException) {
		try {
			map.clear();
			fail("Map.clear(): - unexpected success");
		} catch(RuntimeException e) {
			assertObjectInstanceof("Map.clear()", expectedException, e);
		}

		try {
			Object	value=map.remove(testKey);
			fail("Map.remove(" + testKey + "): - unexpected success: " + value);
		} catch(RuntimeException e) {
			assertObjectInstanceof("Map.remove(" + testKey + ")", expectedException, e);
		}

		try {
			Object	value=map.put(testKey, testValue);
			fail("Map.put(" + testKey + ")[" + testValue + "]: - unexpected success: " + value);
		} catch(RuntimeException e) {
			assertObjectInstanceof("Map.put(" + testKey + ")[" + testValue + "]", expectedException, e);
		}

		try {
			map.putAll(Collections.singletonMap(testKey, testValue));
			fail("Map.putAll(" + testKey + ")[" + testValue + "]: - unexpected success");
		} catch(RuntimeException e) {
			assertObjectInstanceof("Map.putAll(" + testKey + ")[" + testValue + "]", expectedException, e);
		}

		return map;
	}

	/**
	 * @param name The property name - used in case of exception
	 * @param pType The property value type
	 * @return An {@link Object} compatible with the required type
	 * @throws UnsupportedOperationException if unable to create one
	 */
	public static Object createTestPropertyValue(String name, Class<?> pType) {
        final double    value=Math.random();
        if ((Boolean.TYPE == pType) || (Boolean.class == pType)) {
            return Boolean.valueOf(value <= 0.5d);
        } else if ((Byte.TYPE == pType) || (Byte.class == pType)) {
            return Byte.valueOf((byte) (value * 10));
        } else if ((Short.TYPE == pType) || (Short.class == pType)) {
            return Short.valueOf((short) (value * 100));
        } else if ((Integer.TYPE == pType) || (Integer.class == pType)) {
            return Integer.valueOf((int) (value * 1000));
        } else if ((Long.TYPE == pType) || (Long.class == pType)) {
            return Long.valueOf(System.nanoTime());
        } else if ((Float.TYPE == pType) || (Float.class == pType)) {
            return Float.valueOf((float) value);
        } else if ((Double.TYPE == pType) || (Double.class == pType)) {
            return Double.valueOf(value);
        } else if (CharSequence.class.isAssignableFrom(pType)) {
            return String.valueOf(value);
        } else if (Date.class.isAssignableFrom(pType)) {
            return new Date(System.currentTimeMillis() + Math.round(value * 1000000d));
        } else if (Calendar.class.isAssignableFrom(pType)) {
            return Calendar.getInstance();
        } else if (Enum.class.isAssignableFrom(pType)) {
            return TimeUnit.NANOSECONDS;
        } else {
            throw new UnsupportedOperationException("createTestPropertyValue(" + name + ") unsupported type: " + pType.getSimpleName());
        }
    }

    public static final Random  RANDOMIZER=new Random(System.nanoTime());
    public static final long randomSleep (final int  maxSleep)
    {
        if (maxSleep <= 0)
            return 0L;

        final long  sleepTime;
        synchronized(RANDOMIZER)
        {
            if ((sleepTime=RANDOMIZER.nextInt(maxSleep)) <= 0L)
                return 0L;
        }

        try
        {
            Thread.sleep(sleepTime);
        }
        catch(InterruptedException e)
        {
            // ignored
        }

        return sleepTime;
    }
}
