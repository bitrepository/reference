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

import static org.bitrepository.pillar.cache.database.DatabaseConstants.CHECKSUM_TABLE;

import java.io.File;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.DatabaseTestUtils;
import org.bitrepository.common.utils.FileUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChecksumDatabaseTest extends ExtendedTestCase {
    protected Settings settings;

    protected final String DATABASE_NAME = "checksumdb";
    protected final String DATABASE_DIRECTORY = "test-database";
    protected final String DATABASE_URL = "jdbc:derby:" + DATABASE_DIRECTORY + "/" + DATABASE_NAME;
    
    private File dbDir = null;
    private Connection dbCon;
    
    private static final String DEFAULT_FILE_ID = "TEST-FILE";
    private static final String DEFAULT_CHECKSUM = "TEST-checksum";
    private static final Date DEFAULT_DATE = new Date();

    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("ReferencePillarTest");
        
        File dbFile = new File("src/test/resources/checksumdb.jar");
        Assert.assertTrue(dbFile.isFile(), "The database file should exist");
        
        dbDir = FileUtils.retrieveDirectory(DATABASE_DIRECTORY);
        FileUtils.retrieveSubDirectory(dbDir, DATABASE_NAME);
        
        dbCon = DatabaseTestUtils.takeDatabase(dbFile, DATABASE_NAME, dbDir);
        dbCon.close();
        settings.getReferenceSettings().getPillarSettings().getChecksumDatabase().setDatabaseURL(DATABASE_URL);
    }
    
    @AfterMethod (alwaysRun = true)
    public void clearDatabase() throws Exception {
        DBConnector connector = new DBConnector(settings.getReferenceSettings().getPillarSettings().getChecksumDatabase());
        DatabaseUtils.executeStatement(connector, "DELETE FROM " + CHECKSUM_TABLE, new Object[0]);
    }

    @AfterClass (alwaysRun = true)
    public void cleanup() throws Exception {
        if(dbCon != null) {
            dbCon.close();
        }
        if(dbDir != null) {
            FileUtils.delete(dbDir);
        }
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void testChecksumDatabaseExtraction() {
        addDescription("Test the extraction of data from the checksum database.");
        ChecksumDAO cache = getCacheWithData();
        
        addStep("Check whether the default entry exists.", "It does!");
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID));
        
        addStep("Extract calculation date", "Should be identical to the default date.");
        Assert.assertEquals(cache.getCalculationDate(DEFAULT_FILE_ID), DEFAULT_DATE);
        
        addStep("Extract the checksum", "Should be identical to the default checksum");
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_CHECKSUM);
        
        addStep("Extract the whole entry", "Should have the default values.");
        ChecksumEntry entry = cache.getEntry(DEFAULT_FILE_ID);
        Assert.assertEquals(entry.getFileId(), DEFAULT_FILE_ID);
        Assert.assertEquals(entry.getChecksum(), DEFAULT_CHECKSUM);
        Assert.assertEquals(entry.getCalculationDate(), DEFAULT_DATE);
        
        addStep("Extract all entries", "Should only be the one default.");
        List<ChecksumEntry> entries = cache.getAllEntries();
        Assert.assertEquals(entries.size(), 1);
        Assert.assertEquals(entries.get(0).getFileId(), DEFAULT_FILE_ID);
        Assert.assertEquals(entries.get(0).getChecksum(), DEFAULT_CHECKSUM);
        Assert.assertEquals(entries.get(0).getCalculationDate(), DEFAULT_DATE);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testDeletion() {
        addDescription("Test that data can be deleted from the database.");
        ChecksumDAO cache = getCacheWithData();
        
        addStep("Check whether the default entry exists.", "It does!");
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID));
        Assert.assertEquals(cache.getFileIDs(), Arrays.asList(DEFAULT_FILE_ID));
        
        addStep("Remove the default entry", "Should no longer exist");
        cache.deleteEntry(DEFAULT_FILE_ID);
        Assert.assertFalse(cache.hasFile(DEFAULT_FILE_ID));
        Assert.assertEquals(cache.getFileIDs(), Arrays.asList());
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testReplacingExistingEntry() {
        addDescription("Test that an entry can be replaced by another in the database.");
        ChecksumDAO cache = getCacheWithData();
        
        String newChecksum = "new-checksum";
        Date newDate = new Date(System.currentTimeMillis() + 123456789L);
        
        addStep("Check whether the default entry exists.", "It does!");
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID));
        ChecksumEntry oldEntry = cache.getEntry(DEFAULT_FILE_ID);
        Assert.assertEquals(oldEntry.getFileId(), DEFAULT_FILE_ID);
        Assert.assertEquals(oldEntry.getChecksum(), DEFAULT_CHECKSUM);
        Assert.assertEquals(oldEntry.getCalculationDate(), DEFAULT_DATE);
        
        addStep("Replace the checksum and date", "Should still exist, but have different values.");
        cache.insertChecksumCalculation(DEFAULT_FILE_ID, newChecksum, newDate);
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID));
        ChecksumEntry newEntry = cache.getEntry(DEFAULT_FILE_ID);
        Assert.assertEquals(newEntry.getFileId(), DEFAULT_FILE_ID);
        Assert.assertEquals(newEntry.getChecksum(), newChecksum);
        Assert.assertFalse(oldEntry.getChecksum().equals(newEntry.getChecksum()));
        Assert.assertFalse(oldEntry.getCalculationDate().getTime() == newEntry.getCalculationDate().getTime());
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testExtractionOfMissingData() {
        addDescription("Test the handling of bad arguments.");
        ChecksumDAO cache = getCacheWithData();
        String badFileId = "BAD-FILE-ID";
        
        addStep("Try to get the date of a wrong file id.", "Should throw an exception");
        try {
            cache.getCalculationDate(badFileId);
            Assert.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // expected
        }
        
        addStep("Try to get the date of a wrong file id.", "Should throw an exception");
        try {
            cache.getChecksum(badFileId);
            Assert.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // expected
        }

        addStep("Try to remove a bad file id", "Should throw an exception");
        try {
            cache.deleteEntry(badFileId);
            Assert.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    private ChecksumDAO getCacheWithData() {
        ChecksumDAO res = new ChecksumDAO(settings);;
        res.insertChecksumCalculation(DEFAULT_FILE_ID, DEFAULT_CHECKSUM, DEFAULT_DATE);
        return res;
    }
}
