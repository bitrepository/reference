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
package org.bitrepository.pillar.store.checksumcache;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.common.ChecksumDatabaseCreator;
import org.bitrepository.pillar.store.checksumdatabase.*;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.bitrepository.settings.repositorysettings.PillarIDs;
import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public class ChecksumDatabaseTest extends ExtendedTestCase {
    private String collectionID;
    protected Settings settings;

    private static final String DEFAULT_FILE_ID = "TEST-FILE";
    private static final String DEFAULT_CHECKSUM = "abcdef0110fedcba";
    private static final Date DEFAULT_DATE = new Date();

    @BeforeEach
    public void setup() throws Exception {
        loadSettings();
        collectionID = settings.getCollections().get(0).getID();

        DatabaseSpecifics checksumDB =
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(checksumDB);

        ChecksumDatabaseCreator checksumDatabaseCreator = new ChecksumDatabaseCreator();
        checksumDatabaseCreator.createChecksumDatabase(settings, null);
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testChecksumDatabaseExtraction() {
        addDescription("Test the extraction of data from the checksum database.");
        ChecksumDAO cache = getCacheWithData();

        addStep("Check whether the default entry exists.", "It does!");
        Assertions.assertTrue(cache.hasFile(DEFAULT_FILE_ID, collectionID));

        addStep("Extract calculation date", "Should be identical to the default date.");
        Assertions.assertEquals(DEFAULT_DATE, cache.getCalculationDate(DEFAULT_FILE_ID, collectionID));

        addStep("Extract the checksum", "Should be identical to the default checksum");
        Assertions.assertEquals(DEFAULT_CHECKSUM, cache.getChecksum(DEFAULT_FILE_ID, collectionID));

        addStep("Extract the whole entry", "Should have the default values.");
        ChecksumEntry entry = cache.getEntry(DEFAULT_FILE_ID, collectionID);
        Assertions.assertEquals(DEFAULT_FILE_ID, entry.getFileId());
        Assertions.assertEquals(DEFAULT_CHECKSUM, entry.getChecksum());
        Assertions.assertEquals(DEFAULT_DATE, entry.getCalculationDate());

        addStep("Extract all entries", "Should only be the one default.");
        List<ChecksumDataForChecksumSpecTYPE> entries = cache.getChecksumResults(null, null, null,
                collectionID).getEntries();
        Assertions.assertEquals(1, entries.size());
        Assertions.assertEquals(DEFAULT_FILE_ID, entries.get(0).getFileID());
        Assertions.assertEquals(DEFAULT_CHECKSUM, Base16Utils.decodeBase16(entries.get(0).getChecksumValue()));
        Assertions.assertEquals(DEFAULT_DATE, CalendarUtils.convertFromXMLGregorianCalendar(entries.get(0).getCalculationTimestamp()));
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testDeletion() {
        addDescription("Test that data can be deleted from the database.");
        ChecksumDAO cache = getCacheWithData();

        addStep("Check whether the default entry exists.", "It does!");
        Assertions.assertTrue(cache.hasFile(DEFAULT_FILE_ID, collectionID));
        ExtractedFileIDsResultSet res = cache.getFileIDs(null, null, null, null, collectionID);
        Assertions.assertEquals(1, res.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());
        Assertions.assertEquals(DEFAULT_FILE_ID, res.getEntries().getFileIDsDataItems().getFileIDsDataItem().get(0).getFileID());

        addStep("Remove the default entry", "Should no longer exist");
        cache.deleteEntry(DEFAULT_FILE_ID, collectionID);
        Assertions.assertFalse(cache.hasFile(DEFAULT_FILE_ID, collectionID));
        res = cache.getFileIDs(null, null, null, null, collectionID);
        Assertions.assertEquals(0, res.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testReplacingExistingEntry() {
        addDescription("Test that an entry can be replaced by another in the database.");
        ChecksumDAO cache = getCacheWithData();

        String newChecksum = "new-checksum";
        Date newDate = new Date(System.currentTimeMillis() + 123456789L);

        addStep("Check whether the default entry exists.", "It does!");
        Assertions.assertTrue(cache.hasFile(DEFAULT_FILE_ID, collectionID));
        ChecksumEntry oldEntry = cache.getEntry(DEFAULT_FILE_ID, collectionID);
        Assertions.assertEquals(DEFAULT_FILE_ID, oldEntry.getFileId());
        Assertions.assertEquals(DEFAULT_CHECKSUM, oldEntry.getChecksum());
        Assertions.assertEquals(DEFAULT_DATE, oldEntry.getCalculationDate());

        addStep("Replace the checksum and date", "Should still exist, but have different values.");
        cache.insertChecksumCalculation(DEFAULT_FILE_ID, collectionID, newChecksum, newDate);
        Assertions.assertTrue(cache.hasFile(DEFAULT_FILE_ID, collectionID));
        ChecksumEntry newEntry = cache.getEntry(DEFAULT_FILE_ID, collectionID);
        Assertions.assertEquals(DEFAULT_FILE_ID, newEntry.getFileId());
        Assertions.assertEquals(newChecksum, newEntry.getChecksum());
        Assertions.assertNotEquals(oldEntry.getChecksum(), newEntry.getChecksum());
        Assertions.assertNotEquals(oldEntry.getCalculationDate().getTime(), newEntry.getCalculationDate().getTime());
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testExtractionOfMissingData() {
        addDescription("Test the handling of bad arguments.");
        ChecksumDAO cache = getCacheWithData();
        String badFileId = "BAD-FILE-ID";

        addStep("Try to get the date of a wrong file id.", "Should throw an exception");
        try {
            cache.getCalculationDate(badFileId, collectionID);
            Assertions.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // expected
        }

        addStep("Try to get the date of a wrong file id.", "Should throw an exception");
        try {
            cache.getChecksum(badFileId, collectionID);
            Assertions.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // expected
        }

        addStep("Try to remove a bad file id", "Should throw an exception");
        try {
            cache.deleteEntry(badFileId, collectionID);
            Assertions.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testSpecifiedEntryExtraction() {
        addDescription("Test that specific entries can be extracted. Has two entries in the database: "
                + "one for the current timestamp and one for the epoch.");
        addStep("Instantiate database with appropriate data.", "");
        Date beforeTest = new Date(System.currentTimeMillis() - 100000);
        String oldFile = "VeryOldFile";
        ChecksumDAO cache = getCacheWithData();
        cache.insertChecksumCalculation(oldFile, collectionID, DEFAULT_CHECKSUM, new Date(0));

        addStep("Extract with out restrictions", "Both entries.");
        ExtractedChecksumResultSet extractedResults = cache.getChecksumResults(null, null, null, collectionID);
        Assertions.assertEquals(2, extractedResults.getEntries().size());

        addStep("Extract with a maximum of 1 entry", "The oldest entry");
        extractedResults = cache.getChecksumResults(null, null, 1L, collectionID);
        Assertions.assertEquals(1, extractedResults.getEntries().size());
        ChecksumDataForChecksumSpecTYPE dataEntry = extractedResults.getEntries().get(0);
        Assertions.assertEquals(0, CalendarUtils.convertFromXMLGregorianCalendar(dataEntry.getCalculationTimestamp()).getTime());
        Assertions.assertEquals(oldFile, dataEntry.getFileID());

        addStep("Extract all dates older than this tests instantiation", "The oldest entry");
        extractedResults = cache.getChecksumResults(null, CalendarUtils.getXmlGregorianCalendar(beforeTest), null, collectionID);
        Assertions.assertEquals(1, extractedResults.getEntries().size());
        dataEntry = extractedResults.getEntries().get(0);
        Assertions.assertEquals(0, CalendarUtils.convertFromXMLGregorianCalendar(dataEntry.getCalculationTimestamp()).getTime());
        Assertions.assertEquals(oldFile, dataEntry.getFileID());

        addStep("Extract all dates newer than this tests instantiation", "The default entry");
        extractedResults = cache.getChecksumResults(CalendarUtils.getXmlGregorianCalendar(beforeTest), null, null, collectionID);
        Assertions.assertEquals(1, extractedResults.getEntries().size());
        dataEntry = extractedResults.getEntries().get(0);
        Assertions.assertEquals(DEFAULT_DATE, CalendarUtils.convertFromXMLGregorianCalendar(dataEntry.getCalculationTimestamp()));
        Assertions.assertEquals(DEFAULT_FILE_ID, dataEntry.getFileID());

        addStep("Extract all dates older than the newest instance", "Both entries");
        extractedResults = cache.getChecksumResults(null, CalendarUtils.getXmlGregorianCalendar(DEFAULT_DATE), null, collectionID);
        Assertions.assertEquals(2, extractedResults.getEntries().size());

        addStep("Extract all dates newer than the oldest instantiation", "Both entries");
        extractedResults = cache.getChecksumResults(CalendarUtils.getEpoch(), null, null, collectionID);
        Assertions.assertEquals(2, extractedResults.getEntries().size());
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testGetFileIDsRestrictions() {
        addDescription("Tests the restrictions on the GetFileIDs call to the database.");
        addStep("Instantiate database with appropriate data.", "");
        ChecksumDAO cache = new ChecksumDAO(new ChecksumDatabaseManager(settings));
        String FILE_ID_1 = DEFAULT_FILE_ID + "_1";
        String FILE_ID_2 = DEFAULT_FILE_ID + "_2";
        Date FILE_1_DATE = new Date(12345);
        Date FILE_2_DATE = new Date(34567);
        Date MIDDLE_DATE = new Date(23456);
        cache.insertChecksumCalculation(FILE_ID_1, collectionID, DEFAULT_CHECKSUM, FILE_1_DATE);
        cache.insertChecksumCalculation(FILE_ID_2, collectionID, DEFAULT_CHECKSUM, FILE_2_DATE);

        addStep("Test with no time restrictions and 10000 max_results", "Delivers both files.");
        ExtractedFileIDsResultSet efirs = cache.getFileIDs(null, null, 100000L, null, collectionID);
        Assertions.assertEquals(2, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());

        addStep("Test with minimum-date earlier than first file", "Delivers both files.");
        efirs = cache.getFileIDs(CalendarUtils.getFromMillis(0), null, 100000L, null, collectionID);
        Assertions.assertEquals(2, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());

        addStep("Test with maximum-date earlier than first file", "Delivers no files.");
        efirs = cache.getFileIDs(null, CalendarUtils.getFromMillis(0), 100000L, null, collectionID);
        Assertions.assertEquals(0, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());

        addStep("Test with minimum-date set to later than second file.", "Delivers no files.");
        efirs = cache.getFileIDs(CalendarUtils.getXmlGregorianCalendar(new Date()), null, 100000L, null, collectionID);
        Assertions.assertEquals(0, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());

        addStep("Test with maximum-date set to later than second file.", "Delivers both files.");
        efirs = cache.getFileIDs(null, CalendarUtils.getXmlGregorianCalendar(new Date()), 100000L, null, collectionID);
        Assertions.assertEquals(2, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());

        addStep("Test with minimum-date set to middle date.", "Delivers second file.");
        efirs = cache.getFileIDs(CalendarUtils.getXmlGregorianCalendar(MIDDLE_DATE), null, 100000L, null, collectionID);
        Assertions.assertEquals(1, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());
        Assertions.assertEquals(FILE_ID_2, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().get(0).getFileID());

        addStep("Test with maximum-date set to middle date.", "Delivers first file.");
        efirs = cache.getFileIDs(null, CalendarUtils.getXmlGregorianCalendar(MIDDLE_DATE), 100000L, null, collectionID);
        Assertions.assertEquals(1, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());
        Assertions.assertEquals(FILE_ID_1, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().get(0).getFileID());

        addStep("Test with both minimum-date and maximum-date set to middle date.", "Delivers no files.");
        efirs = cache.getFileIDs(CalendarUtils.getXmlGregorianCalendar(MIDDLE_DATE),
                CalendarUtils.getXmlGregorianCalendar(MIDDLE_DATE), 100000L, null, collectionID);
        Assertions.assertEquals(0, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());

        addStep("Test the first file-id, with no other restrictions", "Only delivers the requested file-id");
        efirs = cache.getFileIDs(null, null, 100000L, FILE_ID_1, collectionID);
        Assertions.assertEquals(1, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());
        Assertions.assertEquals(FILE_ID_1, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().get(0).getFileID());

        addStep("Test the second file-id, with no other restrictions", "Only delivers the requested file-id");
        efirs = cache.getFileIDs(null, null, 100000L, FILE_ID_2, collectionID);
        Assertions.assertEquals(1, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());
        Assertions.assertEquals(FILE_ID_2, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().get(0).getFileID());

        addStep("Test the date for the first file-id, while requesting the second file-id",
                "Should not deliver anything");
        efirs = cache.getFileIDs(CalendarUtils.getFromMillis(0), CalendarUtils.getXmlGregorianCalendar(MIDDLE_DATE),
                100000L, FILE_ID_2, collectionID);
        Assertions.assertEquals(0, efirs.getEntries().getFileIDsDataItems().getFileIDsDataItem().size());
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testGetChecksumResult() {
        addDescription("Tests the restrictions on the GetChecksumResult call to the database.");
        addStep("Instantiate database with appropriate data.", "");
        ChecksumDAO cache = getCacheWithData();

        addStep("Test with no time restrictions", "Retrieves the file");
        ExtractedChecksumResultSet extractedChecksums = cache.getChecksumResult(null, null,
                DEFAULT_FILE_ID, collectionID);
        Assertions.assertEquals(1, extractedChecksums.getEntries().size());
        Assertions.assertEquals(DEFAULT_FILE_ID, extractedChecksums.getEntries().get(0).getFileID());

        addStep("Test with time restrictions from epoc to now", "Retrieves the file");
        extractedChecksums = cache.getChecksumResult(CalendarUtils.getEpoch(), CalendarUtils.getNow(),
                DEFAULT_FILE_ID, collectionID);
        Assertions.assertEquals(1, extractedChecksums.getEntries().size());

        addStep("Test with very strict time restrictions around the default date", "Retrieves the file");
        extractedChecksums = cache.getChecksumResult(CalendarUtils.getFromMillis(DEFAULT_DATE.getTime() - 1),
                CalendarUtils.getFromMillis(DEFAULT_DATE.getTime() + 1), DEFAULT_FILE_ID, collectionID);
        Assertions.assertEquals(1, extractedChecksums.getEntries().size());

        addStep("Test with too new a lower limit", "Does not retrieve the file");
        extractedChecksums = cache.getChecksumResult(CalendarUtils.getFromMillis(DEFAULT_DATE.getTime() + 1),
                CalendarUtils.getNow(), DEFAULT_FILE_ID, collectionID);
        Assertions.assertEquals(0, extractedChecksums.getEntries().size());

        addStep("Test with exact date as both upper and lower limit", "Does not retrieve the file");
        extractedChecksums = cache.getChecksumResult(CalendarUtils.getFromMillis(DEFAULT_DATE.getTime()),
                CalendarUtils.getFromMillis(DEFAULT_DATE.getTime()), DEFAULT_FILE_ID, collectionID);
        Assertions.assertEquals(0, extractedChecksums.getEntries().size());

        addStep("Test with date limit from 1 millis before as lower and exact date a upper limit",
                "Does retrieve the file");
        extractedChecksums = cache.getChecksumResult(CalendarUtils.getFromMillis(DEFAULT_DATE.getTime() - 1),
                CalendarUtils.getFromMillis(DEFAULT_DATE.getTime()), DEFAULT_FILE_ID, collectionID);
        Assertions.assertEquals(1, extractedChecksums.getEntries().size());

        addStep("Test with date limit from exact date as lower and 1 millis after date a upper limit",
                "Does not retrieve the file");
        extractedChecksums = cache.getChecksumResult(CalendarUtils.getFromMillis(DEFAULT_DATE.getTime()),
                CalendarUtils.getFromMillis(DEFAULT_DATE.getTime() + 1), DEFAULT_FILE_ID, collectionID);
        Assertions.assertEquals(0, extractedChecksums.getEntries().size());

        addStep("Test with too old an upper limit", "Does not retrieve the file");
        extractedChecksums = cache.getChecksumResult(CalendarUtils.getEpoch(),
                CalendarUtils.getFromMillis(DEFAULT_DATE.getTime() - 1), DEFAULT_FILE_ID, collectionID);
        Assertions.assertEquals(0, extractedChecksums.getEntries().size());
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testGetFileIDsWithOldChecksums() {
        addDescription("Tests the restrictions on the GetFileIDsWithOldChecksums call to the database.");
        addStep("Instantiate database with appropriate data.", "");
        ChecksumDAO cache = new ChecksumDAO(new ChecksumDatabaseManager(settings));
        String FILE_ID_1 = DEFAULT_FILE_ID + "_1";
        String FILE_ID_2 = DEFAULT_FILE_ID + "_2";
        Date FILE_1_DATE = new Date(12345);
        Date FILE_2_DATE = new Date(34567);
        Date MIDDLE_DATE = new Date(23456);
        cache.insertChecksumCalculation(FILE_ID_1, collectionID, DEFAULT_CHECKSUM, FILE_1_DATE);
        cache.insertChecksumCalculation(FILE_ID_2, collectionID, DEFAULT_CHECKSUM, FILE_2_DATE);

        addStep("Extract all entries with checksum date older than now", "Returns both file ids");
        List<String> extractedFileIDs = cache.getFileIDsWithOldChecksums(Instant.now(), collectionID);
        Assertions.assertEquals(2, extractedFileIDs.size());
        Assertions.assertTrue(extractedFileIDs.contains(FILE_ID_1));
        Assertions.assertTrue(extractedFileIDs.contains(FILE_ID_2));

        addStep("Extract all entries with checksum date older than epoch", "Returns no file ids");
        extractedFileIDs = cache.getFileIDsWithOldChecksums(Instant.EPOCH, collectionID);
        Assertions.assertEquals(0, extractedFileIDs.size());

        addStep("Extract all entries with checksum date older than middle date",
                "Returns the first file id");
        extractedFileIDs = cache.getFileIDsWithOldChecksums(MIDDLE_DATE.toInstant(), collectionID);
        Assertions.assertEquals(1, extractedFileIDs.size());
        Assertions.assertTrue(extractedFileIDs.contains(FILE_ID_1));
    }


    private ChecksumDAO getCacheWithData() {
        ChecksumDAO res = new ChecksumDAO(new ChecksumDatabaseManager(settings));
        for (String fileID : res.getAllFileIDs(collectionID)) {
            res.deleteEntry(fileID, collectionID);
        }
        res.insertChecksumCalculation(DEFAULT_FILE_ID, collectionID, DEFAULT_CHECKSUM, DEFAULT_DATE);
        return res;
    }

    /**
     * Replaces the pillarID references in the settings will test specific pillarIDs.
     */
    protected Settings loadSettings() {
        settings = TestSettingsProvider.reloadSettings(getPillarID());
        settings.getReferenceSettings().getPillarSettings().setPillarID(getPillarID());
        updateSettingsWithSpecificPillarName(settings, getPillarID());
        return settings;
    }

    private void updateSettingsWithSpecificPillarName(Settings settings, String pillarID) {
        PillarIDs pillars = settings.getCollections().get(0).getPillarIDs();
        pillars.getPillarID().clear();
        pillars.getPillarID().add(pillarID);
    }

    private String getPillarID() {
        return "ReferencePillarTest";
    }
}
