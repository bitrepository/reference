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

import java.util.Date;
import java.util.List;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.cache.database.ExtractedChecksumResultSet;
import org.bitrepository.pillar.cache.database.ExtractedFileIDsResultSet;
import org.bitrepository.pillar.common.ChecksumDatabaseCreator;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChecksumDatabaseTest extends ExtendedTestCase {
    protected Settings settings;

    private static final String DEFAULT_FILE_ID = "TEST-FILE";
    private static final String DEFAULT_CHECKSUM = "abcdef0110fedcba";
    private static final Date DEFAULT_DATE = new Date();
    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettingsForPillar("ReferencePillarTest");

        DatabaseSpecifics checksumDB =
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(checksumDB);

        ChecksumDatabaseCreator checksumDatabaseCreator = new ChecksumDatabaseCreator();
        checksumDatabaseCreator.createChecksumDatabase(settings, null);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void testChecksumDatabaseExtraction() {
        addDescription("Test the extraction of data from the checksum database.");
        ChecksumDAO cache = getCacheWithData();
        
        addStep("Check whether the default entry exists.", "It does!");
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID, settings.getCollectionID()));
        
        addStep("Extract calculation date", "Should be identical to the default date.");
        Assert.assertEquals(cache.getCalculationDate(DEFAULT_FILE_ID, settings.getCollectionID()), DEFAULT_DATE);
        
        addStep("Extract the checksum", "Should be identical to the default checksum");
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID, settings.getCollectionID()), DEFAULT_CHECKSUM);
        
        addStep("Extract the whole entry", "Should have the default values.");
        ChecksumEntry entry = cache.getEntry(DEFAULT_FILE_ID, settings.getCollectionID());
        Assert.assertEquals(entry.getFileId(), DEFAULT_FILE_ID);
        Assert.assertEquals(entry.getChecksum(), DEFAULT_CHECKSUM);
        Assert.assertEquals(entry.getCalculationDate(), DEFAULT_DATE);
        
        addStep("Extract all entries", "Should only be the one default.");
        List<ChecksumDataForChecksumSpecTYPE> entries = cache.getEntries(null, null, null, 
                settings.getCollectionID()).getEntries();
        Assert.assertEquals(entries.size(), 1);
        Assert.assertEquals(entries.get(0).getFileID(), DEFAULT_FILE_ID);
        Assert.assertEquals(Base16Utils.decodeBase16(entries.get(0).getChecksumValue()), DEFAULT_CHECKSUM);
        Assert.assertEquals(CalendarUtils.convertFromXMLGregorianCalendar(entries.get(0).getCalculationTimestamp()),
                DEFAULT_DATE);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testDeletion() {
        addDescription("Test that data can be deleted from the database.");
        ChecksumDAO cache = getCacheWithData();
        
        addStep("Check whether the default entry exists.", "It does!");
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID, settings.getCollectionID()));
        ExtractedFileIDsResultSet res = cache.getFileIDs(null, null, null, settings.getCollectionID());
        Assert.assertEquals(res.getEntries().getFileIDsDataItems().getFileIDsDataItem().size(), 1);
        Assert.assertEquals(res.getEntries().getFileIDsDataItems().getFileIDsDataItem().get(0).getFileID(), 
                DEFAULT_FILE_ID);
        
        addStep("Remove the default entry", "Should no longer exist");
        cache.deleteEntry(DEFAULT_FILE_ID, settings.getCollectionID());
        Assert.assertFalse(cache.hasFile(DEFAULT_FILE_ID, settings.getCollectionID()));
        res = cache.getFileIDs(null, null, null, settings.getCollectionID());
        Assert.assertEquals(res.getEntries().getFileIDsDataItems().getFileIDsDataItem().size(), 0);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testReplacingExistingEntry() {
        addDescription("Test that an entry can be replaced by another in the database.");
        ChecksumDAO cache = getCacheWithData();
        
        String newChecksum = "new-checksum";
        Date newDate = new Date(System.currentTimeMillis() + 123456789L);
        
        addStep("Check whether the default entry exists.", "It does!");
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID, settings.getCollectionID()));
        ChecksumEntry oldEntry = cache.getEntry(DEFAULT_FILE_ID, settings.getCollectionID());
        Assert.assertEquals(oldEntry.getFileId(), DEFAULT_FILE_ID);
        Assert.assertEquals(oldEntry.getChecksum(), DEFAULT_CHECKSUM);
        Assert.assertEquals(oldEntry.getCalculationDate(), DEFAULT_DATE);
        
        addStep("Replace the checksum and date", "Should still exist, but have different values.");
        cache.insertChecksumCalculation(DEFAULT_FILE_ID, settings.getCollectionID(), newChecksum, newDate);
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID, settings.getCollectionID()));
        ChecksumEntry newEntry = cache.getEntry(DEFAULT_FILE_ID, settings.getCollectionID());
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
            cache.getCalculationDate(badFileId, settings.getCollectionID());
            Assert.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // expected
        }
        
        addStep("Try to get the date of a wrong file id.", "Should throw an exception");
        try {
            cache.getChecksum(badFileId, settings.getCollectionID());
            Assert.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // expected
        }

        addStep("Try to remove a bad file id", "Should throw an exception");
        try {
            cache.deleteEntry(badFileId, settings.getCollectionID());
            Assert.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void testSpecifiedEntryExtraction() {
        addDescription("Test that specific entries can be extracted. Has two entries in the database: "
                + "one for the current timestamp and one for the epoch.");
        addStep("Instantiate database with appropriate data.", "");
        Date beforeTest = new Date(System.currentTimeMillis() - 100000);
        String oldFile = "VeryOldFile";
        ChecksumDAO cache = getCacheWithData();
        cache.insertChecksumCalculation(oldFile, settings.getCollectionID(), DEFAULT_CHECKSUM, new Date(0));
        
        addStep("Extract with out restrictions", "Both entries.");
        ExtractedChecksumResultSet extractedResults = cache.getEntries(null, null, null, settings.getCollectionID());
        Assert.assertEquals(extractedResults.getEntries().size(), 2);
        
        addStep("Extract with a maximum of 1 entry", "The oldest entry");
        extractedResults = cache.getEntries(null, null, 1L, settings.getCollectionID());
        Assert.assertEquals(extractedResults.getEntries().size(), 1);
        ChecksumDataForChecksumSpecTYPE dataEntry = extractedResults.getEntries().get(0);
        Assert.assertEquals(CalendarUtils.convertFromXMLGregorianCalendar(dataEntry.getCalculationTimestamp()).getTime(), 0);
        Assert.assertEquals(dataEntry.getFileID(), oldFile);
        
        addStep("Extract all dates older than this tests instantiation", "The oldest entry");
        extractedResults = cache.getEntries(null, CalendarUtils.getXmlGregorianCalendar(beforeTest), null, settings.getCollectionID());
        Assert.assertEquals(extractedResults.getEntries().size(), 1);
        dataEntry = extractedResults.getEntries().get(0);
        Assert.assertEquals(CalendarUtils.convertFromXMLGregorianCalendar(dataEntry.getCalculationTimestamp()).getTime(), 0);
        Assert.assertEquals(dataEntry.getFileID(), oldFile);
        
        addStep("Extract all dates newer than this tests instantiation", "The default entry");
        extractedResults = cache.getEntries(CalendarUtils.getXmlGregorianCalendar(beforeTest), null, null, settings.getCollectionID());
        Assert.assertEquals(extractedResults.getEntries().size(), 1);
        dataEntry = extractedResults.getEntries().get(0);
        Assert.assertEquals(CalendarUtils.convertFromXMLGregorianCalendar(dataEntry.getCalculationTimestamp()), 
                DEFAULT_DATE);
        Assert.assertEquals(dataEntry.getFileID(), DEFAULT_FILE_ID);
        
        addStep("Extract all dates older than the newest instance", "Both entries");
        extractedResults = cache.getEntries(null, CalendarUtils.getXmlGregorianCalendar(DEFAULT_DATE), null, settings.getCollectionID());
        Assert.assertEquals(extractedResults.getEntries().size(), 2);
        
        addStep("Extract all dates newer than the oldest instantiation", "Both entries");
        extractedResults = cache.getEntries(CalendarUtils.getEpoch(), null, null, settings.getCollectionID());
        Assert.assertEquals(extractedResults.getEntries().size(), 2);
    }
    
    private ChecksumDAO getCacheWithData() {
        ChecksumDAO res = new ChecksumDAO(settings);
        res.insertChecksumCalculation(DEFAULT_FILE_ID, settings.getCollectionID(), DEFAULT_CHECKSUM, DEFAULT_DATE);
        return res;
    }
}
