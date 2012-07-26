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
package org.bitrepository.pillar.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.cache.ChecksumEntry;
import org.bitrepository.pillar.cache.FilebasedChecksumStore;
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
        settings = TestSettingsProvider.reloadSettings("FileBasedCacheUnderTest");
        csDir = FileUtils.retrieveDirectory(CHECKSUM_DIR);
        
        settings.getReferenceSettings().getPillarSettings().getFileDir().clear();
        settings.getReferenceSettings().getPillarSettings().getFileDir().add(csDir.getAbsolutePath());
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
        
        FilebasedChecksumStore cache = new FilebasedChecksumStore(settings);
        Assert.assertTrue(csDir.isDirectory());
        
        addStep("Perform the put of a file. Several times with same checksum", 
                "Should be no exception when putting the file several times");
        cache.insertChecksumCalculation(GOOD_FILE_ID, GOOD_CHECKSUM, new Date());
        cache.insertChecksumCalculation(GOOD_FILE_ID, GOOD_CHECKSUM, new Date());
        
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
        
        addStep("Test the getFileIDs function for the cases 'all files'", 
                "A collection with the only ingested file.");
        Collection<String> files = cache.getFileIDs();
        Assert.assertEquals(files.size(), 1);
        Assert.assertTrue(files.contains(GOOD_FILE_ID));

        addStep("Test the getLastModifiedDate function for the cases 'all files', 'one existing file' and "
                + "'one non-existing file'.", "Should return a collection with 'GOOD_FILE' for 'all files' and the "
                + "'existing file', and an empty collection for the 'non-existing file'. "
                + "All the dates should be approximately now.");
        Date now = new Date();
        List<ChecksumEntry> entries = new ArrayList<ChecksumEntry>(cache.getAllEntries());
        Assert.assertEquals(entries.size(), 1);
        Assert.assertEquals(entries.get(0).getFileId(), (GOOD_FILE_ID));
        Assert.assertTrue(entries.get(0).getCalculationDate().getTime() - now.getTime() < 1000, 
                "Should be less than 1 second since update.");

        ChecksumEntry entry = cache.getEntry(GOOD_FILE_ID);
        Assert.assertNotNull(entry);
        Assert.assertEquals(entry.getFileId(), GOOD_FILE_ID);
        Assert.assertTrue(entry.getCalculationDate().getTime() - now.getTime() < 1000, 
                "Should be less than 1 second since update.");

        addStep("Check the Replace function. Twice try to change the checksum from one to the other.", 
                "Should work when the correct checksum is given.");
        String NEW_CHECKSUM = "1c23c44c32c1";
        cache.insertChecksumCalculation(GOOD_FILE_ID, NEW_CHECKSUM, new Date());

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
        out.write(new String(FILE_ID1 + "##" + CHECKSUM + "##" + System.currentTimeMillis() + "\n").getBytes());
        out.write(new String(FILE_ID2 + "##" + CHECKSUM + "##" + System.currentTimeMillis() + "\n").getBytes());
        out.write(new String("WRONG-ENTY-WITHOUT-HASHS" + "\n").getBytes());
        out.flush();
        out.close();
        
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
        cache.insertChecksumCalculation(FILE_NAME, CHECKSUM, new Date());
        Assert.assertTrue(csFile.exists());
        Assert.assertTrue(csFile.length() > 0, "The file should have content");
        
        addStep("Delete the file", "The file should not exist.");
        FileUtils.delete(csFile);
        Assert.assertFalse(csFile.exists());
        Assert.assertFalse(cache.hasEnoughSpace());
        
        addStep("Try to add another file.", "");
        cache.insertChecksumCalculation(FILE_NAME2, CHECKSUM, new Date());
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
        cache.insertChecksumCalculation(FILE_NAME, CHECKSUM, new Date());
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
        cache.insertChecksumCalculation(FILE_NAME, CHECKSUM, new Date());
        
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
            cache.insertChecksumCalculation(FILE_NAME, CHECKSUM, new Date());
            Assert.fail("Should throw an '" + IllegalStateException.class + "' exception here.");
        } catch (IllegalStateException e) {
            // expected
        } finally {
            csFile.setExecutable(true);
            csFile.setWritable(true);
        }
    }
    
    private synchronized void appendEntryToFile(File file, String filename, String checksum) {
        String record = filename + "##" + checksum + "##" + System.currentTimeMillis() + "\n";
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
