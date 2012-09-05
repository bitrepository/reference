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

import java.io.File;

import org.apache.activemq.util.ByteArrayInputStream;
import org.bitrepository.common.TestValidationUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileUtilsTest extends ExtendedTestCase {
    String DIR = "test-directory";
    String SUB_DIR = "sub-directory";
    String TEST_FILE_NAME = "test.file.name";
    String MOVED_FILE_NAME = "moved.file.name";
    String DATA = "The data for the stream.";

    @BeforeMethod(alwaysRun = true)
    public void setupTest() throws Exception {
        File dir = new File(DIR);
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
    }
    @AfterMethod(alwaysRun = true)
    public void teardownTest() throws Exception {
        File dir = new File(DIR);
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
    }
    
    @Test(groups = { "regressiontest" })
    public void utilityTester() throws Exception {
        addDescription("Test that the utility class is a proper utility class.");
        TestValidationUtils.validateUtilityClass(FileUtils.class);
    }

    @Test(groups = {"regressiontest"})
    public void createDirectoryTester() throws Exception {
        addDescription("Test the ability to create directories and delete them.");
        addStep("Test the ability to create a directory", "Should be created by utility.");
        File dir = new File(DIR);
        Assert.assertFalse(dir.exists());
        File madeDir = FileUtils.retrieveDirectory(DIR);
        Assert.assertTrue(madeDir.exists());
        Assert.assertTrue(madeDir.isDirectory());
        Assert.assertTrue(dir.isDirectory());
        Assert.assertEquals(dir.getAbsolutePath(), madeDir.getAbsolutePath());
        
        addStep("Test the ability to create sub-directories", "Should be created by utility");
        File subdir = new File(dir, SUB_DIR);
        Assert.assertFalse(subdir.exists());
        File madeSubdir = FileUtils.retrieveSubDirectory(dir, SUB_DIR);
        Assert.assertTrue(madeSubdir.exists());
        Assert.assertTrue(madeSubdir.isDirectory());
        Assert.assertTrue(subdir.isDirectory());
        Assert.assertEquals(subdir.getAbsolutePath(), madeSubdir.getAbsolutePath());
        
        addStep("Test delete", "Should remove both directory and sub-directory");
        FileUtils.delete(dir);
        Assert.assertFalse(dir.exists());
        Assert.assertFalse(subdir.exists());
    }
    
    @Test(groups = {"regressiontest"})
    public void deprecateFileTester() throws Exception {
        addDescription("Test the deprecation of a file.");
        addStep("Setup", "");
        File dir = FileUtils.retrieveDirectory(DIR);
        File testFile = new File(dir, TEST_FILE_NAME);
        Assert.assertFalse(testFile.exists());
        Assert.assertTrue(testFile.createNewFile());
        Assert.assertTrue(testFile.exists());
        
        addStep("Deprecate the file", "Should be move to '*.old'");
        FileUtils.deprecateFile(testFile);
        Assert.assertFalse(testFile.exists());
        File deprecatedFile = new File(dir, TEST_FILE_NAME + ".old");
        Assert.assertTrue(deprecatedFile.exists());
    }
    
    @Test(groups = {"regressiontest"})
    public void moveFileTester() throws Exception {
        addDescription("Test the moving of a file.");
        addStep("Setup", "");
        File dir = FileUtils.retrieveDirectory(DIR);
        File testFile = new File(dir, TEST_FILE_NAME);
        File movedFile = new File(dir, MOVED_FILE_NAME);
        Assert.assertFalse(testFile.exists());
        Assert.assertFalse(movedFile.exists());
        Assert.assertTrue(testFile.createNewFile());
        Assert.assertTrue(testFile.exists());
        
        addStep("Move the file", "The 'moved' should exist, whereas the other should not.");
        FileUtils.moveFile(testFile, movedFile);
        Assert.assertFalse(testFile.exists());
        Assert.assertTrue(movedFile.exists());
    }
    
    @Test(groups = {"regressiontest"})
    public void writeInputstreamTester() throws Exception {
        addDescription("Test writing an inputstream to a file.");
        addStep("Setup", "");
        File dir = FileUtils.retrieveDirectory(DIR);
        File testFile = new File(dir, TEST_FILE_NAME);
        Assert.assertFalse(testFile.exists());
        ByteArrayInputStream in = new ByteArrayInputStream(DATA.getBytes());

        addStep("Write the input stream to the file", "The file should exist and have same size as the data.");
        FileUtils.writeStreamToFile(in, testFile);
        Assert.assertTrue(testFile.exists());
        Assert.assertEquals(testFile.length(), DATA.length());
    }

    @Test(groups = {"regressiontest"})
    public void unzipFileTester() throws Exception {
        addDescription("Test unzipping a file.");
        addStep("Setup", "");
        File dir = FileUtils.retrieveDirectory(DIR);
        File zipFile = new File("src/test/resources/test-files/test.jar");
        Assert.assertTrue(zipFile.isFile(), zipFile.getAbsolutePath());
        Assert.assertEquals(dir.listFiles().length, 0);

        addStep("Unzip the zipfile to the directory", "Should place a file and a directory inside the dir");
        FileUtils.unzip(zipFile, dir);
        Assert.assertEquals(dir.listFiles().length, 2);
    }
}
