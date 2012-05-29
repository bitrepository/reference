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
package org.bitrepository.pillar.checksumpillar;

import static org.bitrepository.pillar.checksumpillar.cache.ChecksumEntry.CHECKSUM_SEPARATOR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.checksumpillar.cache.FilebasedChecksumStore;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileBasedCacheTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    private final String CHECKSUM_DIR = "test-output/checksumDir";
    private File csDir;
    
    @BeforeMethod (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
        csDir = FileUtils.retrieveDirectory(CHECKSUM_DIR);
        
        settings.getReferenceSettings().getPillarSettings().setFileDir(csDir.getAbsolutePath());
    }
    
    @AfterMethod (alwaysRun=true) 
    public void closeArchive() {
        if(csDir.isDirectory()) {
            FileUtils.delete(csDir);
        }
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void testFileBasedCacheFunctions() throws Exception {
        addDescription("Testing the file based cache.");
        addStep("Setup variables and cache", "No errors.");
        
        String GOOD_FILE_ID = "good-file-id";
        String BAD_FILE_ID = "bad-file-id";
        String GOOD_CHECKSUM = "1234cccc4321";
        String BAD_CHECKSUM = "cc43211234cc";
        
        FilebasedChecksumStore cache = new FilebasedChecksumStore(settings);
        Assert.assertTrue(csDir.isDirectory());
        
        addStep("Perform the put of a file. Several times with same and different checksums", 
                "Should be no exception when putting the file with same checksum, but an exception when different checksum");
        cache.putEntry(GOOD_FILE_ID, GOOD_CHECKSUM);
        cache.putEntry(GOOD_FILE_ID, GOOD_CHECKSUM);
        try {
            cache.putEntry(GOOD_FILE_ID, BAD_CHECKSUM);
            Assert.fail("Should throw an IllegalStateException here!");
        } catch (IllegalStateException e) {
            // expected.
        }
        
        addStep("Test the HasFile function for both an existing file and a non-existing file.", 
                "Should return true for the existing file and false for the non-existing file.");
        Assert.assertTrue(cache.hasFile(GOOD_FILE_ID));
        Assert.assertFalse(cache.hasFile(BAD_FILE_ID));

        addStep("Perform the getChecksum for a file. Both for an existing file an a bad file.", 
                "Should return the good checksum for the existing file and throw an exception for the bad file id.");
        String checksum = cache.getChecksum(GOOD_FILE_ID);
        Assert.assertEquals(checksum, GOOD_CHECKSUM);
        try {
            cache.getChecksum(BAD_FILE_ID);
            Assert.fail("Should throw an IllegalStateException here!");
        } catch (IllegalStateException e) {
            // expected.
        }
        
        addStep("Test the getFileIDs function for the cases 'all files', 'one existing file' and "
                + "'one non-existing file'.", "Should return a collection with 'GOOD_FILE' for 'all files' and the "
                + "'existing file', and an empty collection for the 'non-existing file'.");
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");
        Collection<String> files = cache.getFileIDs(fileIDs);
        Assert.assertEquals(files.size(), 1);
        Assert.assertTrue(files.contains(GOOD_FILE_ID));

        fileIDs = new FileIDs();
        fileIDs.setFileID(GOOD_FILE_ID);
        files = cache.getFileIDs(fileIDs);
        Assert.assertEquals(files.size(), 1);
        Assert.assertTrue(files.contains(GOOD_FILE_ID));

        fileIDs = new FileIDs();
        fileIDs.setFileID(BAD_FILE_ID);
        files = cache.getFileIDs(fileIDs);
        Assert.assertEquals(files.size(), 0);

        addStep("Test the getLastModifiedDate function for the cases 'all files', 'one existing file' and "
                + "'one non-existing file'.", "Should return a collection with 'GOOD_FILE' for 'all files' and the "
                + "'existing file', and an empty collection for the 'non-existing file'. "
                + "All the dates should be approximately now.");
        Date now = new Date();
        fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");
        Map<String, Date> fileDates = cache.getLastModifiedDate(fileIDs);
        Assert.assertEquals(fileDates.size(), 1);
        Assert.assertTrue(fileDates.containsKey(GOOD_FILE_ID));
        Assert.assertTrue(fileDates.get(GOOD_FILE_ID).getTime() - now.getTime() < 1000, 
                "Should be less than 1 second since update.");

        fileIDs = new FileIDs();
        fileIDs.setFileID(GOOD_FILE_ID);
        fileDates = cache.getLastModifiedDate(fileIDs);
        Assert.assertEquals(fileDates.size(), 1);
        Assert.assertTrue(fileDates.containsKey(GOOD_FILE_ID));
        Assert.assertTrue(fileDates.get(GOOD_FILE_ID).getTime() - now.getTime() < 1000, 
                "Should be less than 1 second since update.");

        fileIDs = new FileIDs();
        fileIDs.setFileID(BAD_FILE_ID);
        fileDates = cache.getLastModifiedDate(fileIDs);
        Assert.assertEquals(fileDates.size(), 0);

        addStep("Check the Replace function. Twice try to change the checksum from one to the other.", 
                "Should work when the correct checksum is given.");
        String NEW_CHECKSUM = "1c23c44c32c1";
        cache.replaceEntry(GOOD_FILE_ID, GOOD_CHECKSUM, NEW_CHECKSUM);
        try {
            cache.replaceEntry(GOOD_FILE_ID, GOOD_CHECKSUM, NEW_CHECKSUM);
            Assert.fail("Should throw an IllegalStateException here!");
        } catch (IllegalStateException e) {
            // expected.
        }

        try {
            cache.replaceEntry(BAD_FILE_ID, GOOD_CHECKSUM, NEW_CHECKSUM);
            Assert.fail("Should throw an IllegalStateException here!");
        } catch (IllegalStateException e) {
            // expected.
        }
        
        addStep("Check the Delete function. Twice for the existing file and for a non-existing file.", 
                "Should be good for the existing file and throw an exception for the non-existing file.");
        cache.deleteEntry(GOOD_FILE_ID);
        try {
            cache.deleteEntry(GOOD_FILE_ID);
            Assert.fail("Should throw an IllegalStateException here!");
        } catch (IllegalStateException e) {
            // expected.            
        }
        
        try {
            cache.deleteEntry(BAD_FILE_ID);
            Assert.fail("Should throw an IllegalStateException here!");
        } catch (IllegalStateException e) {
            // expected.            
        }
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testFileBasedCacheLoading() throws Exception {
        addDescription("Testing the file based cache loading of an existing cache.");
        addStep("Setup variables and cache", "No errors.");
        String FILE_ID1 = "file1";
        String FILE_ID2 = "file2";
        String CHECKSUM = "1234cccc4321";
        
        File csFile = new File(csDir,
                "checksum_" + settings.getReferenceSettings().getPillarSettings().getPillarID() + ".checksum");
        Assert.assertTrue(csFile.createNewFile());
        File wrongFile = new File(csDir,
                "removed_" + settings.getReferenceSettings().getPillarSettings().getPillarID() + ".checksum");
        Assert.assertTrue(wrongFile.createNewFile());
        FileOutputStream out = new FileOutputStream(csFile, false);
        out.write(new String(FILE_ID1 + "##" + CHECKSUM + "\n").getBytes());
        out.write(new String(FILE_ID2 + "##" + CHECKSUM + "\n").getBytes());
        out.write(new String("WRONG-ENTY-WITHOUT-HASHS" + "\n").getBytes());
        out.flush();
        out.close();
        
        System.out.println("PATH:" + csFile.getAbsolutePath());
        FilebasedChecksumStore cache = new FilebasedChecksumStore(settings);
        Assert.assertTrue(cache.hasFile(FILE_ID1));
        Assert.assertTrue(cache.hasFile(FILE_ID2));
        
        Assert.assertTrue(cache.hasEnoughSpace());
        settings.getReferenceSettings().getPillarSettings().setMinimumSizeLeft(Long.MAX_VALUE);
        Assert.assertFalse(cache.hasEnoughSpace());
        
        Assert.assertEquals(cache.getChecksumFilePath(), csFile.getCanonicalPath());
        Assert.assertTrue(cache.getWrongEntryFilePath().startsWith(csDir.getCanonicalPath()));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testInvalidFile() throws Exception {
        addDescription("Test an invalid checksum file");
        addStep("Create the file as a directory.", "Should be createable.");
        File csFile = new File(csDir,
                "checksum_" + settings.getReferenceSettings().getPillarSettings().getPillarID() + ".checksum");
        Assert.assertTrue(csFile.mkdir());
        
        addStep("Start the store", "Should throw exception, since the file is a directory");
        try {
            new FilebasedChecksumStore(settings);
            Assert.fail("Should throw an '" + IllegalStateException.class + "' exception here.");
        } catch (IllegalStateException e) {
            // expected
        }
        FileUtils.delete(csFile);
    }
        
    @Test( groups = {"regressiontest", "pillartest"})
    public void testReadonlyCSdir() throws Exception {
        addDescription("Test the case, when the checksum dir is readonly.");
        addStep("Setup variables", "");

        addStep("Try make the directory unwritable.", "Should also fail.");
        csDir.setReadOnly();
        try {
            new FilebasedChecksumStore(settings);
            Assert.fail("Should throw an '" + IllegalStateException.class + "' exception here.");
        } catch (IllegalStateException e) {
            // expected
        } finally {
            csDir.setExecutable(true);
            csDir.setWritable(true);
        }

    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testDeletingFile() throws Exception {
        addDescription("Test the case, when the checksum file is deleted.");
        addStep("Setup", "");
        String FILE_NAME = "filename";
        String FILE_NAME2 = "filename2";
        String CHECKSUM = "checksum";
        File csFile = new File(csDir,
                "checksum_" + settings.getReferenceSettings().getPillarSettings().getPillarID() + ".checksum");
        Assert.assertFalse(csFile.exists());

        addStep("Instantiate the store and populate it", "Should create the file with content.");
        FilebasedChecksumStore cache = new FilebasedChecksumStore(settings);
        Assert.assertTrue(csFile.exists());
        cache.putEntry(FILE_NAME, CHECKSUM);
        Assert.assertTrue(csFile.exists());
        Assert.assertTrue(csFile.length() > 0, "The file should have content");
        
        addStep("Delete the file", "The file should not exist.");
        FileUtils.delete(csFile);
        Assert.assertFalse(csFile.exists());
        Assert.assertFalse(cache.hasEnoughSpace());
        
        addStep("Try to add another file.", "");
        cache.putEntry(FILE_NAME2, CHECKSUM);
        Assert.assertTrue(csFile.exists());
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void testModifiedFile() throws Exception {
        addDescription("Test the case, when the checksum file is modified.");
        addStep("Setup", "");
        String FILE_NAME = "filename";
        String FILE_NAME2 = "filename2";
        String CHECKSUM = "checksum";
        File csFile = new File(csDir,
                "checksum_" + settings.getReferenceSettings().getPillarSettings().getPillarID() + ".checksum");
        Assert.assertFalse(csFile.exists());

        addStep("Instantiate the store and populate it", "Should create the file with content.");
        FilebasedChecksumStore cache = new FilebasedChecksumStore(settings);
        Assert.assertTrue(csFile.exists());
        cache.putEntry(FILE_NAME, CHECKSUM);
        Assert.assertTrue(csFile.exists());
        Assert.assertTrue(csFile.length() > 0, "The file should have content");
        
        Assert.assertFalse(cache.hasFile(FILE_NAME2));
        
        synchronized(this) {
            // has to wait 1 sec for 'last modified' to be able to change (it is tight to 'seconds')
            wait(1000);
        }
        
        addStep("Modify the file", "Should be reloaded by the cache");
        appendEntryToFile(csFile, FILE_NAME2, CHECKSUM);
        Assert.assertTrue(cache.hasFile(FILE_NAME2));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testReadonlyWrongFileFile() throws Exception {
        addDescription("Test the case, when the 'wrong file is read-only.");
        addStep("Setup", "");
        String FILE_NAME = "filename";
        String CHECKSUM = "checksum";
        File wrongFile = new File(csDir,
                "removed_" + settings.getReferenceSettings().getPillarSettings().getPillarID() + ".checksum");
        
        addStep("Instantiate the store and populate it", "Should create the file with content.");
        FilebasedChecksumStore cache = new FilebasedChecksumStore(settings);
        cache.putEntry(FILE_NAME, CHECKSUM);
        
        addStep("Make the wrongFile readonly", "Should not be able to ");
        wrongFile.setReadOnly();
        try {
            cache.deleteEntry(FILE_NAME);
            Assert.fail("Should throw an " + IllegalStateException.class);
        } catch (IllegalStateException e) {
            // expected
        } finally {
            wrongFile.setWritable(true);
            wrongFile.setExecutable(true);
        }
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void testReadonlyCSFile() throws Exception {
        addDescription("Test the case, when the 'wrong file is read-only.");
        addStep("Setup", "");
        String FILE_NAME = "filename";
        String CHECKSUM = "checksum";
        File csFile = new File(csDir,
                "checksum_" + settings.getReferenceSettings().getPillarSettings().getPillarID() + ".checksum");
        
        addStep("Try making it readonly after instantiation", "Should throw an exception, when a file is put");
        FilebasedChecksumStore cache = new FilebasedChecksumStore(settings);
        csFile.setReadOnly();
        try {
            cache.putEntry(FILE_NAME, CHECKSUM);
            Assert.fail("Should throw an '" + IllegalStateException.class + "' exception here.");
        } catch (IllegalStateException e) {
            // expected
        } finally {
            csFile.setExecutable(true);
            csFile.setWritable(true);
        }
    }
    
    private synchronized void appendEntryToFile(File file, String filename, String checksum) {
        String record = filename + CHECKSUM_SEPARATOR + checksum + "\n";
        boolean appendToFile = true;
        synchronized(file) {
            try {
                FileWriter fwrite = new FileWriter(file, appendToFile);
                try {
                    fwrite.append(record);
                } finally {
                    // close fileWriter.
                    fwrite.flush();
                    fwrite.close();
                }
            } catch(IOException e) {
                throw new IllegalStateException("An error occurred while appending an entry to the archive file.", e);
            }
        }
    }

}
