/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.common.utils;

import org.apache.activemq.util.ByteArrayInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.bitrepository.protocol.utils.AllureTestUtils.addDescription;
import static org.bitrepository.protocol.utils.AllureTestUtils.addStep;

public class FileUtilsTest {
    String DIR = "test-directory";
    String SUB_DIR = "sub-directory";
    String TEST_FILE_NAME = "test.file.name";
    String MOVED_FILE_NAME = "moved.file.name";
    String DATA = "The data for the stream.";

    @BeforeEach
    public void setupTest() throws Exception {
        File dir = new File(DIR);
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
    }
    @AfterEach
    public void teardownTest() throws Exception {
        File dir = new File(DIR);
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
    }

    @Test
    @Tag("regressiontest")
    public void createDirectoryTester() throws Exception {
        addDescription("Test the ability to create directories.");
        addStep("Test the ability to create a directory", "Should be created by utility.");
        File dir = new File(DIR);
        Assertions.assertFalse(dir.exists());
        File madeDir = FileUtils.retrieveDirectory(DIR);
        Assertions.assertTrue(madeDir.exists());
        Assertions.assertTrue(madeDir.isDirectory());
        Assertions.assertTrue(dir.isDirectory());
        Assertions.assertEquals(dir.getAbsolutePath(), madeDir.getAbsolutePath());
        
        addStep("Test error scenarios, when the directory path is a file", "Should throw exception");
        File testFile = new File(dir, TEST_FILE_NAME);
        Assertions.assertTrue(testFile.createNewFile());

        try {
            FileUtils.retrieveDirectory(testFile.getPath());
            Assertions.fail("Should throw an exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test
    @Tag("regressiontest")
    public void createSubDirectoryTester() throws Exception {
        addDescription("Test the ability to create sub directories.");
        addStep("Test the ability to create sub-directories", "Should be created by utility");
        File dir = FileUtils.retrieveDirectory(DIR);
        File subdir = new File(dir, SUB_DIR);
        Assertions.assertFalse(subdir.exists());
        File madeSubdir = FileUtils.retrieveSubDirectory(dir, SUB_DIR);
        Assertions.assertTrue(madeSubdir.exists());
        Assertions.assertTrue(madeSubdir.isDirectory());
        Assertions.assertTrue(subdir.isDirectory());
        Assertions.assertEquals(subdir.getAbsolutePath(), madeSubdir.getAbsolutePath());
        
        addStep("Test that it fails if the 'directory' is actually a file", "Throws exception");
        File testFile = new File(dir, TEST_FILE_NAME);
        Assertions.assertTrue(testFile.createNewFile());

        try {
            FileUtils.retrieveSubDirectory(testFile, SUB_DIR);
            Assertions.fail("Should throw an exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        addStep("Test that it fails, if the parent directory does not allow writing", "Throws exception");
        FileUtils.delete(subdir);
        try {
            dir.setWritable(false);
            FileUtils.retrieveSubDirectory(dir, SUB_DIR);
            Assertions.fail("Should throw an exception");
        } catch (IllegalStateException e) {
            // expected
        } finally {
            dir.setWritable(true);
        }
    }
    
    @Test
    @Tag("regressiontest")
    public void createDeleteDirectoryTester() throws Exception {
        addDescription("Test the ability to delete directories.");
        addStep("Test deleting a directory with file and subdirectory", "Removes directory, sub-directory and file");
        File dir = FileUtils.retrieveDirectory(DIR);
        File subdir = FileUtils.retrieveSubDirectory(dir, SUB_DIR);
        Assertions.assertTrue(dir.exists());
        Assertions.assertTrue(subdir.exists());
        File testFile = new File(dir, TEST_FILE_NAME);
        Assertions.assertTrue(testFile.createNewFile());
        Assertions.assertTrue(testFile.exists());
        
        FileUtils.delete(dir);
        Assertions.assertFalse(dir.exists());
        Assertions.assertFalse(subdir.exists());
        Assertions.assertFalse(testFile.exists());
    }
    
    @Test
    @Tag("regressiontest")
    public void deprecateFileTester() throws Exception {
        addDescription("Test the deprecation of a file.");
        addStep("Setup", "");
        File dir = FileUtils.retrieveDirectory(DIR);
        File testFile = new File(dir, TEST_FILE_NAME);
        Assertions.assertFalse(testFile.exists());
        Assertions.assertTrue(testFile.createNewFile());
        Assertions.assertTrue(testFile.exists());
        
        addStep("Deprecate the file", "Should be move to '*.old'");
        FileUtils.deprecateFile(testFile);
        Assertions.assertFalse(testFile.exists());
        File deprecatedFile = new File(dir, TEST_FILE_NAME + ".old");
        Assertions.assertTrue(deprecatedFile.exists());
    }
    
    @Test
    @Tag("regressiontest")
    public void moveFileTester() throws Exception {
        addDescription("Test the moving of a file.");
        addStep("Setup", "");
        File dir = FileUtils.retrieveDirectory(DIR);
        File testFile = new File(dir, TEST_FILE_NAME);
        File movedFile = new File(dir, MOVED_FILE_NAME);
        Assertions.assertFalse(testFile.exists());
        Assertions.assertFalse(movedFile.exists());
        Assertions.assertTrue(testFile.createNewFile());
        Assertions.assertTrue(testFile.exists());
        
        addStep("Move the file", "The 'moved' should exist, whereas the other should not.");
        FileUtils.moveFile(testFile, movedFile);
        Assertions.assertFalse(testFile.exists());
        Assertions.assertTrue(movedFile.exists());
    }
    
    @Test
    @Tag("regressiontest")
    public void writeInputstreamTester() throws Exception {
        addDescription("Test writing an inputstream to a file.");
        addStep("Setup", "");
        File dir = FileUtils.retrieveDirectory(DIR);
        File testFile = new File(dir, TEST_FILE_NAME);
        Assertions.assertFalse(testFile.exists());
        ByteArrayInputStream in = new ByteArrayInputStream(DATA.getBytes(StandardCharsets.UTF_8));

        addStep("Write the input stream to the file", "The file should exist and have same size as the data.");
        FileUtils.writeStreamToFile(in, testFile);
        Assertions.assertTrue(testFile.exists());
        Assertions.assertEquals(testFile.length(), DATA.length());
    }

    @Test
    @Tag("regressiontest")
    public void unzipFileTester() throws Exception {
        addDescription("Test unzipping a file.");
        addStep("Setup", "");
        File dir = FileUtils.retrieveDirectory(DIR);
        File zipFile = new File("src/test/resources/test-files/test.jar");
        Assertions.assertTrue(zipFile.isFile(), zipFile.getAbsolutePath());
        Assertions.assertEquals(dir.listFiles().length, 0);

        addStep("Unzip the zipfile to the directory", "Should place a file and a directory inside the dir");
        FileUtils.unzip(zipFile, dir);
        Assertions.assertEquals(dir.listFiles().length, 2);
    }
    
    @Test @Tag("regressiontest")
    public void cleanupEmptyDirectoriesTester() throws Exception {
        addDescription("Test the cleanup of empty directories.");
        File dir = FileUtils.retrieveDirectory(DIR);
        File subDir = FileUtils.retrieveSubDirectory(dir, SUB_DIR);
        File subSubDir = FileUtils.retrieveSubDirectory(subDir, SUB_DIR);
        
        addStep("Cleanup non-empty folder", "Should not do anything");
        Assertions.assertTrue(subSubDir.isDirectory());
        FileUtils.cleanupEmptyDirectories(subDir, dir);
        Assertions.assertTrue(subSubDir.isDirectory());
        Assertions.assertTrue(subDir.isDirectory());
        Assertions.assertTrue(dir.isDirectory());
        
        addStep("Cleanup when dir and root are the same", "Should do nothing");
        Assertions.assertTrue(subSubDir.isDirectory());
        FileUtils.cleanupEmptyDirectories(subSubDir, subSubDir);
        Assertions.assertTrue(subSubDir.isDirectory());
        
        addStep("Test succes case, when the directory is empty", "Removes sub-dir");
        Assertions.assertTrue(subSubDir.isDirectory());
        FileUtils.cleanupEmptyDirectories(subSubDir, dir);
        Assertions.assertFalse(subSubDir.isDirectory());
        Assertions.assertFalse(subDir.isDirectory());
        Assertions.assertTrue(dir.isDirectory());
        
        addStep("Test with a file.", "Should do nothing.");
        File file = new File(dir, TEST_FILE_NAME);
        Assertions.assertTrue(file.createNewFile());
        FileUtils.cleanupEmptyDirectories(file, dir);
        Assertions.assertTrue(file.exists());
    }
    
}
