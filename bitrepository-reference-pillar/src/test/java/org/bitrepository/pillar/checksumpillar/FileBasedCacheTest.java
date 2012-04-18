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

import java.io.File;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FileBasedCacheTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    private final String CHECKSUM_DIR = "test-output/checksumDir";
    private File csDir = new File(CHECKSUM_DIR);
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
        
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
}
