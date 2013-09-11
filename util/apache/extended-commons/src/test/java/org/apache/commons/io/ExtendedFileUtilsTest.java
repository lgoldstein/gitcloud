/**
 * 
 */
package org.apache.commons.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triplet;
import org.apache.commons.net.util.URLUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.apache.commons.test.MicroBenchmark;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;


/**
 * @author lgoldstein
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedFileUtilsTest extends AbstractTestSupport {
	public ExtendedFileUtilsTest() {
		super();
	}

    @Test
    public void testGetExtension() {
        /*
         * Triplets - X=initial value, X+1=addDot value, X+2 expected result
         */
        final Object[]  CONFIGS={
                null, Boolean.TRUE, null,
                "", Boolean.TRUE, null,
                null, Boolean.FALSE, null,
                "", Boolean.FALSE, null,
                ".a", Boolean.TRUE, ".a",
                "a", Boolean.TRUE, null,
                ".a", Boolean.FALSE, "a",
                "a", Boolean.FALSE, null,
                "a.b", Boolean.TRUE, ".b",
                "a.b", Boolean.FALSE, "b"
            };
        for (int    cIndex=0; cIndex < CONFIGS.length; cIndex += 3) {
            final String    orgValue=(String) CONFIGS[cIndex];
            final Boolean   dotValue=(Boolean) CONFIGS[cIndex + 1];
            final String    expValue=(String) CONFIGS[cIndex + 2];
            assertEquals("Mismatched result for '" + orgValue + "'[withDot=" + dotValue + "]", expValue, ExtendedFilenameUtils.getExtension(orgValue, dotValue.booleanValue()));
        }
    }

    @Test
    public void testBuildFile () throws IOException {
        File            testFile=ExtendedFileUtils.retrieveTempDirectory();
        List<String>    pathComponents=ExtendedFileUtils.breakDownFilePath(testFile);
        File            resultFile=ExtendedFileUtils.buildFile(pathComponents);
        assertEquals("Mismatched re-constructed path", testFile.getCanonicalPath(), resultFile.getCanonicalPath());
    }

    @Test
    public void testTmpDirCaching () {
    	File	tmpDir=ExtendedFileUtils.retrieveTempDirectory();
    	assertNotNull("No initial value", tmpDir);
    	for (int	index=0; index < Byte.SIZE; index++) {
    		File	dir=ExtendedFileUtils.retrieveTempDirectory();
    		assertSame("Mismatched instances at iteration #" + index, tmpDir, dir);
    	}
    }

    @Test
    public void testIsJarFileName () {
    	testIsJarFileName(true, "a.jar", "test-7.3.6.5.jar", "test.jar-blah.jar");
    	testIsJarFileName(false, "", null, ".jar", "foo.bar");
    }

    @Test
    public void testToURL () throws MalformedURLException {
        List<File>  files=new ArrayList<File>();
        File[]      roots=File.listRoots();
        if (ExtendedArrayUtils.length(roots) > 0) {
            files.addAll(Arrays.asList(roots));
        }
        files.add(ExtendedFileUtils.retrieveTempDirectory());

        List<URL>   urls=ExtendedFileUtils.toURL(files);
        assertEquals("Mismatched converted size", files.size(), urls.size());

        for (File f : files) {
            final URL	fileURL=f.toURI().toURL();
            assertNotNull(URLUtils.toString(fileURL) + " not found", 
            			  CollectionUtils.find(urls, new Predicate<URL>() {
							@Override
							public boolean evaluate(URL object) {
								if (URLUtils.BY_EXTERNAL_FORM_COMPARATOR.compare(object, fileURL) == 0) {
									return true;
								} else {
									return false;
								}
							}
            		   	  }));
        }
    }

    @Test
    public void testAsFile() throws MalformedURLException {
        List<File>  files=new ArrayList<File>();
        File[]      roots=File.listRoots();
        if (ExtendedArrayUtils.length(roots) > 0) {
            files.addAll(Arrays.asList(roots));
        }
        
        File    tempDir=ExtendedFileUtils.retrieveTempDirectory();
        files.add(tempDir);
        files.add(new File(tempDir, "With spaces.txt"));
        files.add(new File(tempDir, "With ampersand & spaces.txt"));
        
        for (File expected : files) {
        	URL		url=ExtendedFileUtils.toURL(expected);
        	File	actual=ExtendedFileUtils.asFile(url);
        	assertEquals("Mismatched re-constructed path", expected.getAbsolutePath(), actual.getAbsolutePath());
        }
    }

    @Test
    public void testToSourceFile() throws MalformedURLException {
    	Class<?>[]	jarClasses={ Assert.class, Mockito.class };
    	for (Class<?> clazz : jarClasses) {
    		File	expected=ExtendedClassUtils.getClassContainerLocationFile(clazz);
    		URL		url=ExtendedClassUtils.getClassBytesURL(clazz);
        	File	actual=ExtendedFileUtils.toFileSource(url);
        	assertEquals("Mismatched re-constructed path", expected.getAbsolutePath(), actual.getAbsolutePath());
    	}
    }

    @Test
    public void testToStringFile() {
    	assertNull("Unexpected null result", ExtendedFileUtils.toString((File) null));
    	
    	File	file=detectTargetFolder();
    	assertEquals("Mismatched toString result", file.getAbsolutePath(), ExtendedFileUtils.toString(file));
    }

    @Test
    public void testFindDifferenceOnBadArguments() throws IOException {
        File[]  values={
                null,
                ensureFolderExists(detectTargetFolder()),
                Validate.notNull(getTestJavaSourceFile(), "No source file", ArrayUtils.EMPTY_OBJECT_ARRAY) };
        for (File file1 : values) {
            for (File file2 : values) {
                if ((file1 != null) && file1.isFile() && (file2 != null) && file2.isFile()) {
                    continue;
                }
                
                try {
                    Triplet<Long,Byte,Byte> cmpRes=ExtendedFileUtils.findDifference(file1, file2);
                    fail("Unexpected success (" + cmpRes + ") for " + file1 + " vs. " + file2);
                } catch(RuntimeException e) {
                    if (file1 == null) {
                        assertObjectInstanceof("No NPE on null file1", NullPointerException.class, e);
                    } else if (file1.isDirectory()) {
                        assertObjectInstanceof("No IllegalArgExc on folder file1", IllegalArgumentException.class, e);
                    } else if (file2 == null) {
                        assertObjectInstanceof("No NPE on null file2", NullPointerException.class, e);
                    } else if (file2.isDirectory()) {
                        assertObjectInstanceof("No IllegalArgExc on folder file2", IllegalArgumentException.class, e);
                    } else {
                        fail("Unexpected exception (" + e.getClass().getSimpleName() + ") on file1=" + file1 + ", file2=" + file2 + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    @Test
    public void testFindDifference() throws IOException {
        final File      targetFolder=ensureFolderExists(new File(ensureFolderExists(detectTargetFolder()), getClass().getSimpleName()));
        final File      file1=new File(targetFolder, "testFindDifference1"), file2=new File(targetFolder, "testFindDifference2");
        final byte[]    bytes={ 3, 7, 7, 7, 3, 4, 7, 1, 0, 2, 8, 1, 7, 1, 3, 7, 3, 6, 5 };
        final String    a1=Arrays.toString(bytes);
        {
            OutputStream    out=new FileOutputStream(file1);
            try {
                out.write(bytes);
            } finally {
                out.close();
            }
        }

        for (int   offset=-1; offset <= bytes.length; offset++) {
            byte   modValue=0;
            if ((offset >= 0) && (offset < bytes.length)) {
                modValue = bytes[offset];
                bytes[offset] = (byte) (bytes[offset] + 1);
                assertNotEquals("Cannot modify data at offset=" + offset, bytes[offset], modValue);
            }
            
            String  a2=Arrays.toString(bytes); 
            try {
                OutputStream    out=new FileOutputStream(file2);
                try {
                    out.write(bytes);
                } finally {
                    out.close();
                }

                Triplet<Long,Byte,Byte>    cmpRes=ExtendedFileUtils.findDifference(file1, file2);
                if ((offset >= 0) && (offset < bytes.length)) {
                    assertNotNull("Unexpected equality for offset=" + offset + " on " + a1 + " vs. " + a2, cmpRes);
                    assertEquals("Mismatched offset on "+ a1 + " vs. " + a2, offset, cmpRes.getV1().longValue());
                    
                    Byte   v1=cmpRes.getV2(), v2=cmpRes.getV3();
                    assertEquals("Mismatched diff #1 values at offset=" + offset + " for " + a1 + " vs. " + a2, modValue, v1.byteValue());
                    assertEquals("Mismatched diff #2 values at offset=" + offset + " for " + a1 + " vs. " + a2, bytes[offset], v2.byteValue());
                    assertNotEquals("Unexpected equal values at offset=" + offset + " for " + a1 + " vs. " + a2, v1.byteValue(), v2.byteValue());
                } else {
                    assertNull("Unexpected difference for " + a1 + " vs. " + a2, cmpRes);
                }

            } finally {    // restore data arrays equality
                if ((offset >= 0) && (offset < bytes.length)) {
                    bytes[offset] = modValue;
                }
            }
        }
    }

    @Test
    public void testQuickCopyFile() throws IOException {
        URL     url=Validate.notNull(ExtendedClassUtils.getClassBytesURL(getClass()), "No class bytes", ArrayUtils.EMPTY_OBJECT_ARRAY);
        byte[]  bytes=IOUtils.toByteArray(url);
        File    targetFolder=ensureFolderExists(new File(ensureFolderExists(detectTargetFolder()), getClass().getSimpleName()));
        File    srcFile=new File(targetFolder, "testQuickCopySourceFile"), dstFile=new File(targetFolder, "testQuickCopyDestinationFile");
        {
            OutputStream    out=new FileOutputStream(srcFile);
            try {
                out.write(bytes);
            } finally {
                out.close();
            }
        }

        dstFile.delete();
        assertFalse("Destination file not deleted: " + dstFile, dstFile.exists());
        
        long    cpySize=ExtendedFileUtils.quickCopyFile(srcFile, dstFile);
        assertTrue("Destination file not created: " + dstFile, dstFile.exists());
        assertEquals("Mismatched copy size for " + dstFile, srcFile.length(), cpySize);
        
        Triplet<Long,Byte,Byte> cmpRes=ExtendedFileUtils.findDifference(srcFile, dstFile);
        assertNull("Unexpected difference", cmpRes);
    }

    @Test
    @Category(MicroBenchmark.class)
    @Ignore("Takes a lot of time to run from the IDE")
    public void testQuickCopyFilePerformance() throws IOException {
        boolean[]   modes={ true, false };

        for (int index=1; index <= Short.SIZE; index++) {
            for (boolean quickMode : modes) {
                testQuickCopyFilePerformance(quickMode, index * ExtendedIOUtils.DEFAULT_BUFFER_SIZE_VALUE);
            }
        }
    }

    private void testQuickCopyFilePerformance(boolean quickMode, long fileSize) throws IOException {
        URL     url=Validate.notNull(ExtendedClassUtils.getClassBytesURL(getClass()), "No class bytes", ArrayUtils.EMPTY_OBJECT_ARRAY);
        byte[]  bytes=IOUtils.toByteArray(url);
        File    targetFolder=ensureFolderExists(new File(ensureFolderExists(detectTargetFolder()), getClass().getSimpleName()));
        File    srcFile=new File(targetFolder, "testQuickCopySourceFilePerformance"), dstFile=new File(targetFolder, "testQuickCopyDestinationFilePerformance");
        {
            OutputStream    out=new FileOutputStream(srcFile);
            try {
                for (long curSize=0; curSize < fileSize; curSize += bytes.length) {
                    if (bytes.length > fileSize) {
                        out.write(bytes, 0, (int) fileSize);
                    } else {
                        out.write(bytes);
                    }
                }
            } finally {
                out.close();
            }
        }

        System.out.println(" ================ quick mode=" + quickMode + " (size=" + srcFile.length() + ") ================");
        System.out.printf("%10s %20s %20s", "Num. calls", "Duration (nano)", "Used memory (B)");
        System.out.println();

        final Runtime   RUNTIME=Runtime.getRuntime();
        for (final int NUM_CALLS : new int[] { 1, 5, 10, 15, 20 }) {
            long    totalTime=0L, totalUsed=0L;

            for (int cIndex=0; cIndex < NUM_CALLS; cIndex++) {
                dstFile.delete();
                assertFalse("Destination file not deleted at " + cIndex + " out of " + NUM_CALLS + ": " + dstFile, dstFile.exists());
                encourageGC();

                long    startFree=RUNTIME.freeMemory(), startTime=System.nanoTime();
                if (quickMode) {
                    ExtendedFileUtils.quickCopyFile(srcFile, dstFile);
                } else {
                    FileUtils.copyFile(srcFile, dstFile);
                }
                long    endFree=RUNTIME.freeMemory(), endTime=System.nanoTime();

                totalUsed += (endFree - startFree);
                totalTime += (endTime - startTime);
            }

            System.out.printf("%10d %20d %20d", Integer.valueOf(NUM_CALLS), Long.valueOf(totalTime), Long.valueOf(totalUsed));
            System.out.println();
        }
    }

    private void testIsJarFileName (boolean expected, String ... names) {
    	for (String n : names) {
    		assertEquals("Mismatched result for " + n, Boolean.valueOf(expected), Boolean.valueOf(ExtendedFileUtils.isJarFileName(n)));
    	}
    }
}
