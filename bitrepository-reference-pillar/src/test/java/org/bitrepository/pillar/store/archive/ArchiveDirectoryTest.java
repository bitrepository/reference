package org.bitrepository.pillar.store.archive;
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

import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.store.filearchive.ArchiveDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

class ArchiveDirectoryTest {
    private static final String DIR_NAME = "archive-directory";
    private static final String FILE_DIR_NAME = DIR_NAME + "/fileDir";
    private static final String FOLDER_DIR_NAME = DIR_NAME + "/" + ArchiveDirectory.FOLDER_DIR;

    private static final String FILE_ID = "file1";
    private static final String FOLDER_FILE_ID = "folder1/folder2/file1";

    @AfterEach
    void shutdownTests() throws Exception {
        File dir = new File(DIR_NAME);
        if (dir.exists()) {
            FileUtils.delete(new File(DIR_NAME));
        }
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testArchiveDirectoryExistingFile() throws Exception {
        addDescription("Test the ArchiveDirectory when the file exists");
        addStep("Setup", "Should place the 'existing file' in the directory.");

        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);
        createExistingFile();

        addStep("Validate the existence of the file", "Should exist and be retrievable.");
        Assertions.assertTrue(directory.hasFile(FILE_ID));
        Assertions.assertNotNull(directory.retrieveFile(FILE_ID));
        Assertions.assertEquals(Collections.singletonList(FILE_ID), directory.getFileIds());

        addStep("Delete the file.", "Should not be extractable.");
        directory.removeFileFromArchive(FILE_ID);
        Assertions.assertFalse(directory.hasFile(FILE_ID));
        Assertions.assertNull(directory.retrieveFile(FILE_ID));
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testArchiveDirectoryMissingFile() throws Exception {
        addDescription("Test the ArchiveDirectory when the file is missing.");
        addStep("Setup", "No file added to the directory.");

        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);

        addStep("Validate the existence of the file", "Should exist and be retrievable.");
        Assertions.assertFalse(directory.hasFile(FILE_ID));
        Assertions.assertNull(directory.retrieveFile(FILE_ID));
        Assertions.assertEquals(List.of(), directory.getFileIds());

        addStep("Delete the file.", "exception since the file does not exist.");
        try {
            directory.removeFileFromArchive(FILE_ID);
            Assertions.fail("Should not be possible to remove a non-existing file.");
        } catch (IllegalStateException e) {
            // exptected
        }
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testArchiveDirectoryNewFile() throws Exception {
        addDescription("Testing the ArchiveDirectory handling of a new file.");
        addStep("Setup", "No file added to the directory.");
        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);

        addStep("Retrieve tmp file", "Exception since files does not exist.");
        try {
            directory.getFileInTempDir(FILE_ID);
            Assertions.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }

        addStep("Request a new file for the tmp dir", "Should be received and creatable.");
        File newFile = directory.getNewFileInTempDir(FILE_ID);
        Assertions.assertTrue(newFile.createNewFile());

        addStep("Retrieve tmp file", "Should be the newly created file.");
        File tmpFile = directory.getFileInTempDir(FILE_ID);
        Assertions.assertNotNull(tmpFile);
        Assertions.assertEquals(newFile.getAbsolutePath(), tmpFile.getAbsolutePath());

        addStep("Request another new file with the same name", "Should throw exception, since it already exists.");
        try {
            directory.getNewFileInTempDir(FILE_ID);
            Assertions.fail("Should throw exception, since the file already exists.");
        } catch (IllegalStateException e) {
            // expected
        }

        addStep("Move the file from tmp to archive", "Should exist in archive but not in tmp.");
        directory.moveFromTmpToArchive(FILE_ID);
        Assertions.assertTrue(directory.hasFile(FILE_ID));
        Assertions.assertFalse(directory.hasFileInTempDir(FILE_ID));
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testArchiveDirectoryMoveFileToArchive() throws Exception {
        addDescription("Testing the error scenarios when moving a file from tmp to archive for the ArchiveDirectory.");
        addStep("Setup", "No file added to the directory.");
        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);

        addStep("Moving file from tmp to archive",
                "Exception since it does not exist in the tmp-dir");
        try {
            directory.moveFromTmpToArchive(FILE_ID);
            Assertions.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }

        addStep("Create file in both tmp and archive.", "");
        createExistingFile();
        File newFile = directory.getNewFileInTempDir(FILE_ID);
        Assertions.assertTrue(newFile.createNewFile());

        addStep("Moving file from tmp to archive",
                "Exception since the file already exists within the archive.");
        try {
            directory.moveFromTmpToArchive(FILE_ID);
            Assertions.fail("Should throw exception since the file in archive already exists.");
        } catch (IllegalStateException e) {
            // exptected
        }

        addStep("Remove the file from archive and try again",
                "File in tmp moved to archive.");
        Assertions.assertTrue(directory.hasFile(FILE_ID));
        Assertions.assertTrue(directory.hasFileInTempDir(FILE_ID));
        directory.removeFileFromArchive(FILE_ID);
        Assertions.assertFalse(directory.hasFile(FILE_ID));
        Assertions.assertTrue(directory.hasFileInTempDir(FILE_ID));
        directory.moveFromTmpToArchive(FILE_ID);
        Assertions.assertTrue(directory.hasFile(FILE_ID));
        Assertions.assertFalse(directory.hasFileInTempDir(FILE_ID));
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testArchiveDirectoryRemoveFile() throws Exception {
        addDescription("Testing the error scenarios when removing files from the archive.");
        addStep("Setup", "No file added to the directory.");
        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);
        File retainDir = new File(DIR_NAME + "/retainDir");

        addStep("Remove nonexisting file from archive", "Exception since it does not exist");
        try {
            directory.removeFileFromArchive(FILE_ID);
            Assertions.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }

        addStep("Remove nonexisting file from tmp", "Exception since it does not exist");
        try {
            directory.removeFileFromTmp(FILE_ID);
            Assertions.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }

        addStep("Create file in both tmp, archive and retain directories.", "");
        createExistingFile();
        File tmpFile = directory.getNewFileInTempDir(FILE_ID);
        Assertions.assertTrue(tmpFile.createNewFile());
        File retainFile = new File(retainDir, FILE_ID);
        Assertions.assertTrue(retainFile.createNewFile());
        Assertions.assertEquals(1, Objects.requireNonNull(retainDir.list()).length);

        addStep("Remove the file from archive and tmp", "all 3 files in retain dir.");
        directory.removeFileFromArchive(FILE_ID);
        directory.removeFileFromTmp(FILE_ID);
        Assertions.assertEquals(3, Objects.requireNonNull(retainDir.list()).length);
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testArchiveDirectoryExistingFolderFile() throws Exception {
        addDescription("Test the ArchiveDirectory when the file exists");
        addStep("Setup", "Should place the 'existing file' in the directory.");

        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);
        createExistingFolderFile();

        addStep("Validate the existence of the file", "Should exist and be retrievable.");
        Assertions.assertTrue(directory.hasFile(FOLDER_FILE_ID));
        Assertions.assertNotNull(directory.retrieveFile(FOLDER_FILE_ID));
        Assertions.assertEquals(Collections.singletonList(FOLDER_FILE_ID), directory.getFileIds());

        addStep("Delete the file.", "Should not be retrievable.");
        directory.removeFileFromArchive(FOLDER_FILE_ID);
        Assertions.assertFalse(directory.hasFile(FOLDER_FILE_ID));
        Assertions.assertNull(directory.retrieveFile(FOLDER_FILE_ID));
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testArchiveDirectoryMissingFolderFile() throws Exception {
        addDescription("Test the ArchiveDirectory when the file is missing.");
        addStep("Setup", "No file added to the directory.");

        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);

        addStep("Validate the existence of the file", "Should neither exist nor be retrievable.");
        Assertions.assertFalse(directory.hasFile(FOLDER_FILE_ID));
        Assertions.assertNull(directory.retrieveFile(FOLDER_FILE_ID));
        Assertions.assertEquals(Collections.EMPTY_LIST, directory.getFileIds());

        addStep("Delete the file.", "exception since the file does not exist.");
        try {
            directory.removeFileFromArchive(FOLDER_FILE_ID);
            Assertions.fail("Should not be possible to remove a non-existing file.");
        } catch (IllegalStateException e) {
            // exptected
        }
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testArchiveDirectoryNewFolderFile() throws Exception {
        addDescription("Testing the ArchiveDirectory handling of a new file.");
        addStep("Setup", "No file added to the directory.");
        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);

        addStep("Retrieve tmp file", "Exception since files does not exist.");
        try {
            directory.getFileInTempDir(FOLDER_FILE_ID);
            Assertions.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }

        addStep("Request a new file for the tmp dir", "Should be received and creatable.");
        File newFile = directory.getNewFileInTempDir(FOLDER_FILE_ID);
        Assertions.assertTrue(newFile.createNewFile());

        addStep("Retrieve tmp file", "Should be the newly created file.");
        File tmpFile = directory.getFileInTempDir(FOLDER_FILE_ID);
        Assertions.assertNotNull(tmpFile);
        Assertions.assertEquals(newFile.getAbsolutePath(), tmpFile.getAbsolutePath());

        addStep("Request another new file with the same name",
                "Should throw exception, since it already exists.");
        try {
            directory.getNewFileInTempDir(FOLDER_FILE_ID);
            Assertions.fail("Should throw exception, since the file already exists.");
        } catch (IllegalStateException e) {
            // expected
        }

        addStep("Move the file from tmp to archive", "Should exist in archive but not in tmp.");
        directory.moveFromTmpToArchive(FOLDER_FILE_ID);
        Assertions.assertTrue(directory.hasFile(FOLDER_FILE_ID));
        Assertions.assertFalse(directory.hasFileInTempDir(FOLDER_FILE_ID));
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testArchiveDirectoryMoveFolderFileToArchive() throws Exception {
        addDescription("Testing the error scenarios when moving a file from tmp to archive for the ArchiveDirectory.");
        addStep("Setup", "No file added to the directory.");
        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);

        addStep("Moving file from tmp to archive",
                "Exception since it does not exist in the tmp-dir");
        try {
            directory.moveFromTmpToArchive(FOLDER_FILE_ID);
            Assertions.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }

        addStep("Create file in both tmp and archive.", "");
        createExistingFolderFile();
        File newFile = directory.getNewFileInTempDir(FOLDER_FILE_ID);
        Assertions.assertTrue(newFile.createNewFile());

        addStep("Moving file from tmp to archive",
                "Exception since the file already exists within the archive.");
        try {
            directory.moveFromTmpToArchive(FOLDER_FILE_ID);
            Assertions.fail("Should throw exception since the file in archive already exists.");
        } catch (IllegalStateException e) {
            // exptected
        }

        addStep("Remove the file from archive and try again", "File in tmp moved to archive.");
        Assertions.assertTrue(directory.hasFile(FOLDER_FILE_ID));
        Assertions.assertTrue(directory.hasFileInTempDir(FOLDER_FILE_ID));

        directory.removeFileFromArchive(FOLDER_FILE_ID);
        Assertions.assertFalse(directory.hasFile(FOLDER_FILE_ID));
        Assertions.assertTrue(directory.hasFileInTempDir(FOLDER_FILE_ID));

        directory.moveFromTmpToArchive(FOLDER_FILE_ID);
        Assertions.assertTrue(directory.hasFile(FOLDER_FILE_ID));
        Assertions.assertFalse(directory.hasFileInTempDir(FOLDER_FILE_ID));
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testArchiveDirectoryRemoveFolderFile() throws Exception {
        addDescription("Testing the error scenarios when removing files from the archive.");
        addStep("Setup", "No file added to the directory.");
        ArchiveDirectory directory = new ArchiveDirectory(DIR_NAME);
        File retainDir = new File(DIR_NAME + "/retainDir");

        addStep("Remove nonexisting file from archive", "Exception since it does not exist");
        try {
            directory.removeFileFromArchive(FOLDER_FILE_ID);
            Assertions.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }

        addStep("Remove nonexisting file from tmp", "Exception since it does not exist");
        try {
            directory.removeFileFromTmp(FOLDER_FILE_ID);
            Assertions.fail("Should throw exception since the file does not exist.");
        } catch (IllegalStateException e) {
            // exptected
        }

        addStep("Create file in both tmp, archive and retain directories.", "");
        createExistingFolderFile();
        File tmpFile = directory.getNewFileInTempDir(FOLDER_FILE_ID);
        Assertions.assertTrue(tmpFile.createNewFile());
        File retainFile = new File(retainDir, FOLDER_FILE_ID);
        if (!retainFile.getParentFile().isDirectory()) {
            Assertions.assertTrue(retainFile.getParentFile().mkdirs());
        }
        Assertions.assertTrue(retainFile.createNewFile());
        Assertions.assertEquals(1, Objects.requireNonNull(retainDir.list()).length);

        addStep("Remove the file from archive and tmp", "all 3 files in retain dir.");
        directory.removeFileFromArchive(FOLDER_FILE_ID);
        directory.removeFileFromTmp(FOLDER_FILE_ID);
        List<File> retainFiles = TestFileHelper.getAllFilesFromSubDirs(retainDir);
        Assertions.assertEquals(3, retainFiles.size());
    }

    private void createExistingFile() throws Exception {
        OutputStreamWriter osw = new OutputStreamWriter(
                new FileOutputStream(new File(FILE_DIR_NAME, FILE_ID), false), StandardCharsets.UTF_8);
        osw.write("test-data\n");
        osw.flush();
        osw.close();
    }

    private void createExistingFolderFile() throws Exception {
        File f = new File(FOLDER_DIR_NAME, FOLDER_FILE_ID);
        f.getParentFile().mkdirs();
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(f, false), StandardCharsets.UTF_8);
        osw.write("test-data\n");
        osw.flush();
        osw.close();
    }
}
