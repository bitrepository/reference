package org.bitrepository.pillar.referencepillar;
/*
 * #%L
 * Bitrepository Reference Pillar
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

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.referencepillar.archive.ArchiveDirectory;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ArchiveDirectoryTest extends ExtendedTestCase {
    private static String DIR_NAME = "archive-directory";
    private static String FILE_DIR_NAME = DIR_NAME + "/fileDir";
    
    private static String FILE_ID = "file1";
    
    @AfterMethod (alwaysRun=true)
    public void shutdownTests() throws Exception {
        FileUtils.delete(new File(DIR_NAME));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testArchiveDirectoryExistingFile() throws Exception {
        addDescription("Test the ArchiveDirectory when the file exists");
        addStep("Setup", "Should place the 'existing file' in the directory.");
        
        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);
        createExistingFile();
        
        addStep("Validate the existence of the file", "Should exist and be retrievable.");
        Assert.assertTrue(directory.hasFile(FILE_ID));
        Assert.assertNotNull(directory.getFile(FILE_ID));
        Assert.assertEquals(directory.getFileIds(), Arrays.asList(FILE_ID));
        
        addStep("Delete the file.", "Should not be extractable.");
        directory.removeFileFromArchive(FILE_ID);
        Assert.assertFalse(directory.hasFile(FILE_ID));
        Assert.assertNull(directory.getFile(FILE_ID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testArchiveDirectoryMissingFile() throws Exception {
        addDescription("Test the ArchiveDirectory when the file is missing.");
        addStep("Setup", "No file added to the directory.");
        
        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);
        
        addStep("Validate the existence of the file", "Should exist and be retrievable.");
        Assert.assertFalse(directory.hasFile(FILE_ID));
        Assert.assertNull(directory.getFile(FILE_ID));
        Assert.assertEquals(directory.getFileIds(), Arrays.asList());
        
        addStep("Delete the file.", "exception since the file does not exist.");
        try {
            directory.removeFileFromArchive(FILE_ID);
            Assert.fail("Should not be possible to remove a non-existing file.");
        } catch (IllegalStateException e) {
            // exptected
        }
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testArchiveDirectoryNewFile() throws Exception {
        addDescription("Testing the ArchiveDirectory handling of a new file.");
        addStep("Setup", "No file added to the directory.");
        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);

        addStep("Retrieve tmp file", "Exception since files does not exist.");
        try {
            directory.getFileInTempDir(FILE_ID);
            Assert.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }
        
        addStep("Request a new file for the tmp dir", "Should be received and creatable.");
        File newFile = directory.getNewFileInTempDir(FILE_ID);
        Assert.assertTrue(newFile.createNewFile());
        
        addStep("Retrieve tmp file", "Should be the newly created file.");
        File tmpFile = directory.getFileInTempDir(FILE_ID);
        Assert.assertNotNull(tmpFile);
        Assert.assertEquals(tmpFile.getAbsolutePath(), newFile.getAbsolutePath());
        
        addStep("Request another new file with the same name", "Should throw exception, since it already exists.");
        try {
            directory.getNewFileInTempDir(FILE_ID);
            Assert.fail("Should throw exception, since the file already exists.");
        } catch (IllegalStateException e) {
            // expected
        }
        
        addStep("Move the file from tmp to archive", "Should exist in archive but not in tmp.");
        directory.moveFromTmpToArchive(FILE_ID);
        Assert.assertTrue(directory.hasFile(FILE_ID));
        Assert.assertFalse(directory.hasFileInTempDir(FILE_ID));
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void testArchiveDirectoryMoveFileToArchive() throws Exception {
        addDescription("Testing the error scenarios when moving a file from tmp to archive for the ArchiveDirectory.");
        addStep("Setup", "No file added to the directory.");
        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);

        addStep("Moving file from tmp to archive", "Exception since it does not exist in the tmp-dir");
        try {
            directory.moveFromTmpToArchive(FILE_ID);
            Assert.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }
        
        addStep("Create file in both tmp and archive.", "");
        createExistingFile();
        File newFile = directory.getNewFileInTempDir(FILE_ID);
        Assert.assertTrue(newFile.createNewFile());

        addStep("Moving file from tmp to archive", "Exception since the file already exists within the archive.");
        try {
            directory.moveFromTmpToArchive(FILE_ID);
            Assert.fail("Should throw exception since the file in archive already exists.");
        } catch (IllegalStateException e) {
            // exptected
        }
        
        addStep("Remove the file from archive and try again", "File in tmp moved to archive.");
        Assert.assertTrue(directory.hasFile(FILE_ID));
        Assert.assertTrue(directory.hasFileInTempDir(FILE_ID));
        directory.removeFileFromArchive(FILE_ID);
        Assert.assertFalse(directory.hasFile(FILE_ID));
        Assert.assertTrue(directory.hasFileInTempDir(FILE_ID));
        directory.moveFromTmpToArchive(FILE_ID);
        Assert.assertTrue(directory.hasFile(FILE_ID));
        Assert.assertFalse(directory.hasFileInTempDir(FILE_ID));
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void testArchiveDirectoryRemoveFile() throws Exception {
        addDescription("Testing the error scenarios when removing files from the archive.");
        addStep("Setup", "No file added to the directory.");
        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);
        File retainDir = new File(DIR_NAME + "/retainDir");
        
        addStep("Remove nonexisting file from archive", "Exception since it does not exist");
        try {
            directory.removeFileFromArchive(FILE_ID);
            Assert.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }

        addStep("Remove nonexisting file from tmp", "Exception since it does not exist");
        try {
            directory.removeFileFromTmp(FILE_ID);
            Assert.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }
        
        addStep("Create file in both tmp, archive and retain directories.", "");
        createExistingFile();
        File tmpFile = directory.getNewFileInTempDir(FILE_ID);
        Assert.assertTrue(tmpFile.createNewFile());
        File retainFile = new File(retainDir, FILE_ID);
        Assert.assertTrue(retainFile.createNewFile());
        Assert.assertEquals(retainDir.list().length, 1);

        addStep("Remove the file from archive and tmp", "all 3 files in retain dir.");
        directory.removeFileFromArchive(FILE_ID);
        directory.removeFileFromTmp(FILE_ID);
        Assert.assertEquals(retainDir.list().length, 3);
    }
    
    private void createExistingFile() throws Exception {
        FileWriter fw = new FileWriter(new File(FILE_DIR_NAME, FILE_ID), false);
        fw.write("test-data\n");
        fw.flush();
        fw.close();
    }
}
