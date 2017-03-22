/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.integrityservice.cache;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.IntegrityDatabaseTestCase;
import org.bitrepository.integrityservice.cache.database.DerbyIntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.service.database.DatabaseManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IntegrityDAOTest extends IntegrityDatabaseTestCase {
    String TEST_PILLAR_1 = "MY-TEST-PILLAR-1";
    String TEST_PILLAR_2 = "MY-TEST-PILLAR-2";
    String EXTRA_PILLAR = "MY-EXTRA-PILLAR";
    
    String TEST_FILE_ID = "TEST-FILE-ID";
    String TEST_CHECKSUM = "1234cccc4321";
    
    String TEST_COLLECTIONID;
    public static final String EXTRA_COLLECTION = "extra-collection";

    @BeforeMethod (alwaysRun = true)
    @Override
    public void setup() throws Exception {
        super.setup();
        TEST_COLLECTIONID = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
    }
    
    @Override 
    protected void customizeSettings() {
        org.bitrepository.settings.repositorysettings.Collection c0 = 
                settings.getRepositorySettings().getCollections().getCollection().get(0);
        c0.getPillarIDs().getPillarID().clear();
        c0.getPillarIDs().getPillarID().add(TEST_PILLAR_1);
        c0.getPillarIDs().getPillarID().add(TEST_PILLAR_2);
        settings.getRepositorySettings().getCollections().getCollection().clear();
        settings.getRepositorySettings().getCollections().getCollection().add(c0);
        
        org.bitrepository.settings.repositorysettings.Collection extraCollection = 
                new org.bitrepository.settings.repositorysettings.Collection();
        extraCollection.setID(EXTRA_COLLECTION);
        org.bitrepository.settings.repositorysettings.PillarIDs pids 
            = new org.bitrepository.settings.repositorysettings.PillarIDs();
        pids.getPillarID().add(TEST_PILLAR_1);
        pids.getPillarID().add(EXTRA_PILLAR);
        extraCollection.setPillarIDs(pids);
        settings.getRepositorySettings().getCollections().getCollection().add(extraCollection);
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void instantiationTest() throws Exception {
        addDescription("Testing the connection to the integrity database.");
        IntegrityDAO cache = createDAO();
        Assert.assertNotNull(cache);
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void reinitialiseDatabaseTest() throws Exception {
        addDescription("Testing the connection to the integrity database.");
        addStep("Setup manually.", "Should be created.");
        DatabaseManager dm = new IntegrityDatabaseManager(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
                        
        IntegrityDAO cache = new DerbyIntegrityDAO(dm.getConnector(), settings);
        Assert.assertNotNull(cache);

        addStep("Close the connection and create another one.", "Should not fail");
        dm.getConnector().getConnection().close();
        dm.getConnector().destroy();

        synchronized(this) {
            wait(100);
        }
        
        DatabaseManager newdm = new IntegrityDatabaseManager(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
         
        cache = new DerbyIntegrityDAO(newdm.getConnector(), settings);
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void initialStateExtractionTest() throws Exception {
        addDescription("Tests the initial state of the IntegrityModel. Should not contain any data.");
        IntegrityDAO cache = createDAO();
        
        List<String> pillersInDB = cache.getAllPillars();
        Assert.assertTrue(pillersInDB.containsAll(Arrays.asList(TEST_PILLAR_1, TEST_PILLAR_2, EXTRA_PILLAR)));
        Assert.assertEquals(pillersInDB.size(), 3);
        List<String> collectionsInDB = cache.getCollections();
        Assert.assertTrue(collectionsInDB.containsAll(Arrays.asList(TEST_COLLECTIONID, EXTRA_COLLECTION)));
        Assert.assertEquals(collectionsInDB.size(), 2);
        
        Assert.assertEquals(cache.getNumberOfFilesInCollection(TEST_COLLECTIONID), new Long(0));
        Assert.assertEquals(cache.getNumberOfFilesInCollection(EXTRA_COLLECTION), new Long(0));
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testCorrectDateHandling() throws ParseException  {
        addDescription("Testing the correct ingest and extraction of file and checksum dates");
        IntegrityDAO cache = createDAO();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date summertimeTS = sdf.parse("2015-10-25T02:59:54.000+02:00");
        Date summertimeUnix = new Date(1445734794000L);
        Assert.assertEquals(summertimeTS, summertimeUnix);
        
        Date wintertimeTS = sdf.parse("2015-10-25T02:59:54.000+01:00");
        Date wintertimeUnix = new Date(1445738394000L);
        Assert.assertEquals(wintertimeTS, wintertimeUnix);
        
        FileIDsData summertimeData = getFileIDsData("summertime");
        summertimeData.getFileIDsDataItems().getFileIDsDataItem().get(0)
            .setLastModificationTime(CalendarUtils.getXmlGregorianCalendar(summertimeTS));
        FileIDsData wintertimeData = getFileIDsData("wintertime");
        wintertimeData.getFileIDsDataItems().getFileIDsDataItem().get(0)
            .setLastModificationTime(CalendarUtils.getXmlGregorianCalendar(wintertimeTS));
        cache.updateFileIDs(summertimeData, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(wintertimeData, TEST_PILLAR_1, TEST_COLLECTIONID);
                
        List<ChecksumDataForChecksumSpecTYPE> summertimeCsData = getChecksumResults("summertime", TEST_CHECKSUM);
        summertimeCsData.get(0).setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(summertimeTS));
        List<ChecksumDataForChecksumSpecTYPE> wintertimeCsData = getChecksumResults("wintertime", TEST_CHECKSUM);
        wintertimeCsData.get(0).setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(wintertimeTS));
        cache.updateChecksums(summertimeCsData, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateChecksums(wintertimeCsData, TEST_PILLAR_1, TEST_COLLECTIONID);
        
        List<FileInfo> fis = cache.getFileInfosForFile("summertime", TEST_COLLECTIONID);
        Assert.assertEquals(fis.size(), 1, fis.toString());
        Assert.assertEquals(
                CalendarUtils.convertFromXMLGregorianCalendar(fis.get(0).getDateForLastChecksumCheck()), summertimeUnix);
        
        fis = cache.getFileInfosForFile("wintertime", TEST_COLLECTIONID);
        Assert.assertEquals(fis.size(), 1, fis.toString());
        Assert.assertEquals(
                CalendarUtils.convertFromXMLGregorianCalendar(fis.get(0).getDateForLastChecksumCheck()), wintertimeUnix);
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testIngestOfFileIDsData() throws Exception {
        addDescription("Tests the ingesting of file ids data");
        IntegrityDAO cache = createDAO();
        
        Assert.assertEquals(cache.getNumberOfFilesInCollection(TEST_COLLECTIONID), new Long(0));
        Assert.assertEquals(cache.getNumberOfFilesInCollection(EXTRA_COLLECTION), new Long(0));
        
        addStep("Create data", "Should be ingested into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        cache.updateFileIDs(data1, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(data1, TEST_PILLAR_2, TEST_COLLECTIONID);
        
        addStep("Extract the data", "Should be identical to the ingested data");
        Collection<FileInfo> fileinfos = cache.getFileInfosForFile(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fileinfos);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getFileId(), TEST_FILE_ID);
            Assert.assertNull(fi.getChecksum());
            Assert.assertEquals(fi.getDateForLastChecksumCheck(), CalendarUtils.getEpoch());
            Assert.assertEquals(fi.getDateForLastFileIDCheck(), data1.getFileIDsDataItems().getFileIDsDataItem().get(0).getLastModificationTime());
            Assert.assertEquals(fi.getFileSize(), new Long(data1.getFileIDsDataItems().getFileIDsDataItem().get(0).getFileSize().longValue()));
        }
        
        addStep("Check that the extra collection is untouched by the ingest", "should deliver an empty collection and no errors");
        Assert.assertEquals(cache.getNumberOfFilesInCollection(TEST_COLLECTIONID), new Long(1));
        Assert.assertEquals(cache.getNumberOfFilesInCollection(EXTRA_COLLECTION), new Long(0));
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testIngestOfChecksumsData() throws Exception {
        addDescription("Tests the ingesting of checksums data");
        IntegrityDAO cache = createDAO();
        
        Assert.assertEquals(cache.getNumberOfFilesInCollection(TEST_COLLECTIONID), new Long(0));
        Assert.assertEquals(cache.getNumberOfFilesInCollection(EXTRA_COLLECTION), new Long(0));
        
        addStep("Create data", "Should be ingested into the database");
        List<ChecksumDataForChecksumSpecTYPE> csData = getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM);
        cache.updateChecksums(csData, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateChecksums(csData, TEST_PILLAR_2, TEST_COLLECTIONID);
        
        addStep("Extract the data", "Should be identical to the ingested data");
        Collection<FileInfo> fileinfos = cache.getFileInfosForFile(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fileinfos);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getFileId(), TEST_FILE_ID);
            Assert.assertEquals(fi.getChecksum(), TEST_CHECKSUM);
            Assert.assertEquals(fi.getDateForLastChecksumCheck(), csData.get(0).getCalculationTimestamp());
        }
        
        addStep("Check that the extra collection is untouched by the ingest", "should deliver an empty collection and no errors");
        Assert.assertEquals(cache.getNumberOfFilesInCollection(TEST_COLLECTIONID), new Long(1));
        Assert.assertEquals(cache.getNumberOfFilesInCollection(EXTRA_COLLECTION), new Long(0));
        
    }
    
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testDeletingEntry() throws Exception {
        addDescription("Tests the deletion of an FileID entry from a collection. " +
        		"Checks that it does not effect another collection with a fileID equal to the deleted");
        IntegrityDAO cache = createDAO();
        Collection<FileInfo> fileinfos = cache.getFileInfosForFile(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 0);

        addStep("Create data", "Should be ingested into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        cache.updateFileIDs(data1, TEST_PILLAR_1, TEST_COLLECTIONID);       
        cache.updateFileIDs(data1, TEST_PILLAR_2, TEST_COLLECTIONID);
        
        cache.updateFileIDs(data1, TEST_PILLAR_1, EXTRA_COLLECTION);
        cache.updateFileIDs(data1, EXTRA_PILLAR, EXTRA_COLLECTION);
        
        addStep("Ensure that the data is present", "the data is present");
        fileinfos = cache.getFileInfosForFile(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        fileinfos = cache.getFileInfosForFile(TEST_FILE_ID, EXTRA_COLLECTION);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        
        addStep("Delete the entry for the first pillar", "No fileinfos should be extracted from the pillar in the collection.");
        cache.removeFile(TEST_COLLECTIONID, TEST_PILLAR_1, TEST_FILE_ID);
        List<FileInfo> fis = cache.getFileInfosForFile(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fis);
        Assert.assertEquals(fis.size(), 1);
        Assert.assertEquals(fis.get(0).getPillarId(), TEST_PILLAR_2);
        
        addStep("Delete the entry for the second pillar", "No fileinfos should be extracted from the collection.");
        cache.removeFile(TEST_COLLECTIONID, TEST_PILLAR_2, TEST_FILE_ID);
        fis = cache.getFileInfosForFile(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fis);
        Assert.assertEquals(fis.size(), 0);
        
        addStep("Check that the data in the extra collection is still present", "the data is present");
        fileinfos = cache.getFileInfosForFile(TEST_FILE_ID, EXTRA_COLLECTION);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testDeletingNonExistingEntry() throws Exception {
        addDescription("Tests the deletion of an nonexisting FileID entry.");
        IntegrityDAO cache = createDAO();
        
        String nonexistingFileEntry = "NON-EXISTING-FILE-ENTRY" + new Date().getTime();

        addStep("Create data", "Should be ingested into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        cache.updateFileIDs(data1, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(data1, TEST_PILLAR_2, TEST_COLLECTIONID);
        
        Collection<FileInfo> fileinfos = cache.getFileInfosForFile(nonexistingFileEntry, TEST_COLLECTIONID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 0);
        
        fileinfos = cache.getFileInfosForFile(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        
        addStep("Delete a nonexisting entry", "Should not change the state of the database.");
        cache.removeFile(TEST_COLLECTIONID, TEST_PILLAR_1, nonexistingFileEntry);
        cache.removeFile(TEST_COLLECTIONID, TEST_PILLAR_2, nonexistingFileEntry);
        fileinfos = cache.getFileInfosForFile(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testFindOrphanFiles() throws Exception {
        addDescription("Tests the ability to find orphan files.");
        IntegrityDAO cache = createDAO();
       
        addStep("Create data", "Should be ingested into the database");
        String orphanFile = "orphan";
        String existingFile = "existing";
        FileIDsData data1 = getFileIDsData(existingFile);
        FileIDsData data3 = getFileIDsData(orphanFile);
        cache.updateFileIDs(data1, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(data1, TEST_PILLAR_2, TEST_COLLECTIONID);
        cache.updateFileIDs(data3, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(data3, TEST_PILLAR_2, TEST_COLLECTIONID);
        Assert.assertEquals(cache.getNumberOfFilesInCollection(TEST_COLLECTIONID), new Long(2));
        Assert.assertEquals(cache.getNumberOfFilesInCollection(EXTRA_COLLECTION), new Long(0));
        Thread.sleep(100);
        Date updateTime = new Date();
        cache.updateFileIDs(data1, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(data1, TEST_PILLAR_2, TEST_COLLECTIONID);
        cache.updateFileIDs(data3, TEST_PILLAR_1, TEST_COLLECTIONID);
        
        List<String> orphanFilesPillar1 = getIssuesFromIterator(cache.getOrphanFilesOnPillar(TEST_COLLECTIONID, 
                TEST_PILLAR_1, updateTime));
        Assert.assertEquals(orphanFilesPillar1.size(), 0);
        List<String> orphanFilesPillar2 = getIssuesFromIterator(cache.getOrphanFilesOnPillar(TEST_COLLECTIONID, 
                TEST_PILLAR_2, updateTime));
        Assert.assertEquals(orphanFilesPillar2.size(), 1);
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testFindInconsistentChecksum() throws Exception {
        addDescription("Testing the localization of inconsistent checksums");
        IntegrityDAO cache = createDAO();
        
        String checksum1_1 = "11";
        String checksum1_2 = "12";
        String checksum3 = "33";
        String checksum2_1 = "21";
        String checksum2_2 = "22";
        
        String BAD_FILE_ID_1 = "BAD-FILE-1";
        String BAD_FILE_ID_2 = "BAD-FILE-2";
        String GOOD_FILE_ID = "GOOD-FILE";

        addStep("Update the database with 2 inconsistent files and one consistent file.", 
                "Ingesting the data into the database");
        List<ChecksumDataForChecksumSpecTYPE> csData1_1 = getChecksumResults(BAD_FILE_ID_1, checksum1_1);
        List<ChecksumDataForChecksumSpecTYPE> csData1_2 = getChecksumResults(BAD_FILE_ID_2, checksum1_2);
        List<ChecksumDataForChecksumSpecTYPE> csData1_3 = getChecksumResults(GOOD_FILE_ID, checksum3);
        cache.updateChecksums(csData1_1, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateChecksums(csData1_2, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateChecksums(csData1_3, TEST_PILLAR_1, TEST_COLLECTIONID);
        List<ChecksumDataForChecksumSpecTYPE> csData2_1 = getChecksumResults(BAD_FILE_ID_1, checksum2_1);
        List<ChecksumDataForChecksumSpecTYPE> csData2_2 = getChecksumResults(BAD_FILE_ID_2, checksum2_2);
        List<ChecksumDataForChecksumSpecTYPE> csData2_3 = getChecksumResults(GOOD_FILE_ID, checksum3);
        cache.updateChecksums(csData2_1, TEST_PILLAR_2, TEST_COLLECTIONID);
        cache.updateChecksums(csData2_2, TEST_PILLAR_2, TEST_COLLECTIONID);
        cache.updateChecksums(csData2_3, TEST_PILLAR_2, TEST_COLLECTIONID);

        addStep("Find the files with inconsistent checksums", "Bad file 1 and 2");
        List<String> filesWithChecksumError
            = getIssuesFromIterator(cache.findFilesWithChecksumInconsistincies(TEST_COLLECTIONID));
        Assert.assertEquals(filesWithChecksumError, Arrays.asList(BAD_FILE_ID_1, BAD_FILE_ID_2));
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testNoChecksums() throws Exception {
        addDescription("Testing the checksum validation, when no checksums exists.");
        IntegrityDAO cache = createDAO();
        
        addStep("Update the database with 2 inconsistent files and one consistent file.", 
                "Ingesting the data into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        cache.updateFileIDs(data1, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(data1, TEST_PILLAR_2, TEST_COLLECTIONID);

        addStep("Finding the files with inconsistent checksums", "No checksum thus no errors");
        List<String> filesWithChecksumError
            = getIssuesFromIterator(cache.findFilesWithChecksumInconsistincies(TEST_COLLECTIONID));
        Assert.assertEquals(filesWithChecksumError, Arrays.asList());
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testMissingChecksums() throws Exception {
        addDescription("Testing the checksum validation, when only one pillar has a checksum for a file.");
        IntegrityDAO cache = createDAO();

        Date testStart = new Date();
        addStep("Update the database with 1 file, missing its checksum on one pillar.", 
                "Ingesting the data into the database");
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID), TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID), TEST_PILLAR_2, TEST_COLLECTIONID);
        cache.updateChecksums(getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM),TEST_PILLAR_1, TEST_COLLECTIONID);
        
        addStep("Finding the files with inconsistent checksums", "No checksum thus no errors");
        List<String> filesWithChecksumError 
            = getIssuesFromIterator(cache.findFilesWithChecksumInconsistincies(TEST_COLLECTIONID));      
        Assert.assertEquals(filesWithChecksumError, Arrays.asList());
        
        List<String> fileWithMissingChecksumPillar1 
            = getIssuesFromIterator(cache.getFilesWithMissingChecksums(TEST_COLLECTIONID, TEST_PILLAR_1, testStart));
        Assert.assertEquals(fileWithMissingChecksumPillar1, Arrays.asList());
        List<String> fileWithMissingChecksumPillar2 
            = getIssuesFromIterator(cache.getFilesWithMissingChecksums(TEST_COLLECTIONID, TEST_PILLAR_2, testStart));
        Assert.assertEquals(fileWithMissingChecksumPillar2, Arrays.asList(TEST_FILE_ID));
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testMissingChecksumsChecksumNotUpdated() throws Exception {
        addDescription("Testing the checksum validation, when only one pillar has a checksum for a file.");
        IntegrityDAO cache = createDAO();

        Date testStart = new Date();
        addStep("Update the database with 1 file, no missing checksums.", 
                "Ingesting the data into the database");
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID), TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID), TEST_PILLAR_2, TEST_COLLECTIONID);
        cache.updateChecksums(getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM), TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateChecksums(getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM), TEST_PILLAR_2, TEST_COLLECTIONID);
        
        addStep("Finding the files with inconsistent checksums", "No checksum thus no errors");
        List<String> filesWithChecksumError 
            = getIssuesFromIterator(cache.findFilesWithChecksumInconsistincies(TEST_COLLECTIONID));      
        Assert.assertEquals(filesWithChecksumError, Arrays.asList());
        
        List<String> fileWithMissingChecksumPillar1 
            = getIssuesFromIterator(cache.getFilesWithMissingChecksums(TEST_COLLECTIONID, TEST_PILLAR_1, testStart));
        Assert.assertEquals(fileWithMissingChecksumPillar1, Arrays.asList());
        List<String> fileWithMissingChecksumPillar2 
            = getIssuesFromIterator(cache.getFilesWithMissingChecksums(TEST_COLLECTIONID, TEST_PILLAR_2, testStart));
        Assert.assertEquals(fileWithMissingChecksumPillar2, Arrays.asList());
        
        addStep("Updating the checksum for one pillar, and checking that the other pillars checksum is now missing",
                "The second pillar is reported to be missing the checksum for the file");
        Date secondUpdate = new Date();
        Thread.sleep(1000);
        cache.updateChecksums(getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM), TEST_PILLAR_1, TEST_COLLECTIONID);
        addStep("Finding the files with inconsistent checksums", "No checksum thus no errors");
        filesWithChecksumError = getIssuesFromIterator(cache.findFilesWithChecksumInconsistincies(TEST_COLLECTIONID));      
        Assert.assertEquals(filesWithChecksumError, Arrays.asList());
        
        fileWithMissingChecksumPillar1 
            = getIssuesFromIterator(cache.getFilesWithMissingChecksums(TEST_COLLECTIONID, TEST_PILLAR_1, secondUpdate));
        Assert.assertEquals(fileWithMissingChecksumPillar1, Arrays.asList());
        fileWithMissingChecksumPillar2 
            = getIssuesFromIterator(cache.getFilesWithMissingChecksums(TEST_COLLECTIONID, TEST_PILLAR_2, secondUpdate));
        Assert.assertEquals(fileWithMissingChecksumPillar2, Arrays.asList(TEST_FILE_ID));
    }
    
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testOutdatedChecksums() throws Exception {
        addDescription("Testing the checksum validation, when only one pillar has a checksum for a file.");
        IntegrityDAO cache = createDAO();
        
        Date maxDate = new Date(System.currentTimeMillis() - 10000);
        
        addStep("Update the database with one file, one pillar having an outdated checksum.", 
                "Ingesting the data into the database");
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID), TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID), TEST_PILLAR_2, TEST_COLLECTIONID);
        cache.updateChecksums(getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM),TEST_PILLAR_1, TEST_COLLECTIONID);
        
        List<ChecksumDataForChecksumSpecTYPE> checksumData = getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM);
        checksumData.get(0).setCalculationTimestamp(CalendarUtils.getEpoch());
        cache.updateChecksums(checksumData,TEST_PILLAR_2, TEST_COLLECTIONID);
        
        addStep("Finding the files with inconsistent checksums", "No checksum thus no errors");
        List<String> filesWithChecksumError 
            = getIssuesFromIterator(cache.findFilesWithChecksumInconsistincies(TEST_COLLECTIONID));      
        Assert.assertEquals(filesWithChecksumError, Arrays.asList());
        
        List<String> fileWithOutdatedChecksumsPillar1 
            = getIssuesFromIterator(cache.getFilesWithOutdatedChecksums(TEST_COLLECTIONID, TEST_PILLAR_1, maxDate));
        Assert.assertEquals(fileWithOutdatedChecksumsPillar1, Arrays.asList());
        
        List<String> fileWithOutdatedChecksumPillar2 
            = getIssuesFromIterator(cache.getFilesWithOutdatedChecksums(TEST_COLLECTIONID, TEST_PILLAR_2, maxDate));
        Assert.assertEquals(fileWithOutdatedChecksumPillar2, Arrays.asList(TEST_FILE_ID));
    }    
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testExtractingAllKnownFilesForPillars() throws Exception {
        addDescription("Tests that known files can be extracted for specific pillars.");
        IntegrityDAO cache = createDAO();
        String file2 = TEST_FILE_ID + "-2";
        String file3 = TEST_FILE_ID + "-3";
        
        addStep("Insert two files into database for a pillar", "Ingesting the data into the database");
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID, file2), TEST_PILLAR_1, TEST_COLLECTIONID);
        addStep("Insert a file to the extra collection for the common pillar", "Data is ingested into the database");
        cache.updateFileIDs(getFileIDsData(file3), TEST_PILLAR_1, EXTRA_COLLECTION);
        
        addStep("Extract all the existing file ids for the pillar for collection '" + TEST_COLLECTIONID + "'", "Both file ids is found.");
        IntegrityIssueIterator it = cache.getAllFileIDsOnPillar(TEST_COLLECTIONID, TEST_PILLAR_1, new Long(0), Long.MAX_VALUE);
        Collection<String> fileIDs = getIssuesFromIterator(it);
        Assert.assertTrue(fileIDs.size() == 2, "Number of files: " + fileIDs.size());
        Assert.assertTrue(fileIDs.contains(TEST_FILE_ID));
        Assert.assertTrue(fileIDs.contains(file2));
        Assert.assertFalse(fileIDs.contains(file3));

        addStep("Extract the single fileID for the extra collection", "Only the one file id exists");
        it = cache.getAllFileIDsOnPillar(EXTRA_COLLECTION, TEST_PILLAR_1,  new Long(0), Long.MAX_VALUE);
        fileIDs = getIssuesFromIterator(it);
        Assert.assertTrue(fileIDs.size() == 1, "Number of files: " + fileIDs.size());
        Assert.assertTrue(fileIDs.contains(file3));
        Assert.assertFalse(fileIDs.contains(file2));
        Assert.assertFalse(fileIDs.contains(TEST_FILE_ID));
               
        addStep("Extract all the existing file ids for another pillar", "No files are found.");
        it = cache.getAllFileIDsOnPillar(TEST_COLLECTIONID, TEST_PILLAR_2, new Long(0), Long.MAX_VALUE);
        fileIDs = getIssuesFromIterator(it);
        Assert.assertTrue(fileIDs.isEmpty());
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testExtractingAllKnownFilesForPillarsLimits() throws Exception {
        addDescription("Tests the limits for extracting files for specific pillars.");
        IntegrityDAO cache = createDAO();
        String file2 = TEST_FILE_ID + "-2";
        
        addStep("Insert two files into database for a pillar", "Ingesting the data into the database");
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID, file2), TEST_PILLAR_1, TEST_COLLECTIONID);

        addStep("Extract with a maximum of 1", "The first file.");
        IntegrityIssueIterator it = cache.getAllFileIDsOnPillar(TEST_COLLECTIONID, TEST_PILLAR_1, new Long(0), new Long(1));
        Collection<String> fileIDs = getIssuesFromIterator(it);
        Assert.assertEquals(fileIDs.size(), 1);
        Assert.assertTrue(fileIDs.contains(TEST_FILE_ID));
        
        addStep("Extract with a minimum of 1 and maximum of infinite", "The last file.");
        it = cache.getAllFileIDsOnPillar(TEST_COLLECTIONID, TEST_PILLAR_1, new Long(1), Long.MAX_VALUE);
        fileIDs = getIssuesFromIterator(it);
        Assert.assertEquals(fileIDs.size(), 1);
        Assert.assertTrue(fileIDs.contains(file2));
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testExtractingAllMissingFiles() throws Exception {
        addDescription("Tests that missing files can be extracted.");
        IntegrityDAO cache = createDAO();
        String file2 = TEST_FILE_ID + "-2";
        
        addStep("Insert two files into database for a pillar and mark them as missing", 
                "Ingesting the data into the database");
        
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID, file2), TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID), TEST_PILLAR_2, TEST_COLLECTIONID);
        
        addStep("Check the number of files in collection and on pillars", 
                "The collection should have two files, the first pillar two, the second one");
        Assert.assertTrue(cache.getNumberOfFilesInCollection(TEST_COLLECTIONID) == 2);
        Map<String, PillarCollectionMetric> metrics = cache.getPillarCollectionMetrics(TEST_COLLECTIONID);
        Assert.assertEquals(metrics.get(TEST_PILLAR_1).getPillarFileCount(), 2);
        Assert.assertEquals(metrics.get(TEST_PILLAR_2).getPillarFileCount(), 1);
    
        addStep("Extract missing files", "one file should be missing");
        List<String> missingFiles 
            = getIssuesFromIterator(cache.findFilesWithMissingCopies(TEST_COLLECTIONID, 2, 0L, 10L));
        Assert.assertEquals(missingFiles, Arrays.asList(file2));
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testExtractingAllMissingFilesForPillarsLimits() throws Exception {
        addDescription("Tests the limits for extracting missing files for specific pillars.");
        IntegrityDAO cache = createDAO();
        String file2 = TEST_FILE_ID + "-2";
        String file3 = TEST_FILE_ID + "-3";
        
        addStep("Insert two files into database for a pillar and set them to missing", 
                "Ingesting the data into the database");
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID, file2, file3), TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(getFileIDsData(TEST_FILE_ID), TEST_PILLAR_2, TEST_COLLECTIONID);

        addStep("Extract with a maximum of 1", "The first file.");
        IntegrityIssueIterator it = cache.findFilesWithMissingCopies(TEST_COLLECTIONID, 2, 0L, 1L);
        Collection<String> fileIDs = getIssuesFromIterator(it);
        Assert.assertEquals(fileIDs.size(), 1);
        Assert.assertTrue(fileIDs.contains(file2));
        
        addStep("Extract with a minimum of 1 and maximum of infinite", "The last file.");
        it = cache.findFilesWithMissingCopies(TEST_COLLECTIONID, 2, 1L, Long.MAX_VALUE);
        fileIDs = getIssuesFromIterator(it);
        Assert.assertEquals(fileIDs.size(), 1);
        Assert.assertTrue(fileIDs.contains(file3));
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testGetLatestFileDateEntryForCollection() throws Exception {
        addDescription("Tests that checksum date entries can be retrieved and manipulated.");
        IntegrityDAO cache = createDAO();

        addStep("Create data", "Should be ingested into the database");
        
        Assert.assertNull(cache.getLatestFileDate(TEST_COLLECTIONID, TEST_PILLAR_1));
        Assert.assertNull(cache.getLatestFileDate(TEST_COLLECTIONID, TEST_PILLAR_2));
        
        FileIDsData fidsPillar1 = getFileIDsData(TEST_FILE_ID);
        Date expectedLatestFileDatePillar1 = CalendarUtils.convertFromXMLGregorianCalendar(
                fidsPillar1.getFileIDsDataItems().getFileIDsDataItem().get(0).getLastModificationTime());
        cache.updateFileIDs(fidsPillar1, TEST_PILLAR_1, TEST_COLLECTIONID);
        
        FileIDsData fidsPillar2 = getFileIDsData(TEST_FILE_ID);
        Date expectedLatestFileDatePillar2 = new Date(expectedLatestFileDatePillar1.getTime() + 100);
        fidsPillar2.getFileIDsDataItems().getFileIDsDataItem().get(0)
            .setLastModificationTime(CalendarUtils.getXmlGregorianCalendar(expectedLatestFileDatePillar2));
        cache.updateFileIDs(fidsPillar2, TEST_PILLAR_2, TEST_COLLECTIONID);
        
        Assert.assertEquals(cache.getLatestFileDate(TEST_COLLECTIONID, TEST_PILLAR_1), expectedLatestFileDatePillar1);
        Assert.assertEquals(cache.getLatestFileDate(TEST_COLLECTIONID, TEST_PILLAR_2), expectedLatestFileDatePillar2);
        
        Assert.assertEquals(cache.getLatestFileDate(TEST_COLLECTIONID, TEST_PILLAR_2), 
                cache.getLatestFileDateInCollection(TEST_COLLECTIONID));
        
        cache.resetFileCollectionProgress(TEST_COLLECTIONID);
        Assert.assertNull(cache.getLatestFileDate(TEST_COLLECTIONID, TEST_PILLAR_1));
        Assert.assertNull(cache.getLatestFileDate(TEST_COLLECTIONID, TEST_PILLAR_2));
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testGetLatestChecksumDateEntryForCollection() throws Exception {
        addDescription("Tests that checksum date entries can be retrieved and manipulated.");
        IntegrityDAO cache = createDAO();

        addStep("Create data", "Should be ingested into the database");
        
        Assert.assertNull(cache.getLatestChecksumDate(TEST_COLLECTIONID, TEST_PILLAR_1));
        Assert.assertNull(cache.getLatestChecksumDate(TEST_COLLECTIONID, TEST_PILLAR_2));
        
        List<ChecksumDataForChecksumSpecTYPE> csData = getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM); 
        cache.updateChecksums(csData, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateChecksums(csData, TEST_PILLAR_2, TEST_COLLECTIONID);
        Date expectedLatestChecksum = CalendarUtils.convertFromXMLGregorianCalendar(csData.get(0).getCalculationTimestamp());
        Assert.assertEquals(cache.getLatestChecksumDate(TEST_COLLECTIONID, TEST_PILLAR_1), expectedLatestChecksum);
        Assert.assertEquals(cache.getLatestChecksumDate(TEST_COLLECTIONID, TEST_PILLAR_2), expectedLatestChecksum);
        
        cache.resetChecksumCollectionProgress(TEST_COLLECTIONID);
        Assert.assertNull(cache.getLatestChecksumDate(TEST_COLLECTIONID, TEST_PILLAR_1));
        Assert.assertNull(cache.getLatestChecksumDate(TEST_COLLECTIONID, TEST_PILLAR_2));
    }
   
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testExtractCollectionFileSize() throws Exception {
        addDescription("Tests that the accumulated size of the collection can be extracted");
        IntegrityDAO cache = createDAO();
        
        addStep("Insert test data into database", "Data is ingested");
        String file2 = TEST_FILE_ID + "-2";
        String file3 = TEST_FILE_ID + "-3";
        Long size1 = new Long(100);
        Long size2 = new Long(200);
        Long size3 = new Long(300);
        FileIDsData data1 = makeFileIDsDataWithGivenFileSize(TEST_FILE_ID, size1);
        FileIDsData data2 = makeFileIDsDataWithGivenFileSize(file2, size2);
        FileIDsData data3 = makeFileIDsDataWithGivenFileSize(file3, size3);
        cache.updateFileIDs(data1, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(data2, TEST_PILLAR_1, TEST_COLLECTIONID);      
        cache.updateFileIDs(data2, TEST_PILLAR_2, TEST_COLLECTIONID);
        cache.updateFileIDs(data3, TEST_PILLAR_2, TEST_COLLECTIONID);
        
        long pillar1Size = size1 + size2;
        long pillar2Size = size2 + size3;
        long collectionSize = size1 + size2 + size3;
        
        Map<String, PillarCollectionMetric> metrics = cache.getPillarCollectionMetrics(TEST_COLLECTIONID);
        addStep("Check the reported size of the first pillar in the collection", "The reported size matches the precalculated");
        Assert.assertEquals(metrics.get(TEST_PILLAR_1).getPillarCollectionSize(), pillar1Size);
        addStep("Check the reported size of the second pillar in the collection", "The reported size matches the precalculated");
        Assert.assertEquals(metrics.get(TEST_PILLAR_2).getPillarCollectionSize(), pillar2Size);
        addStep("Check the reported size of the whole collection", "The reported size matches the precalculated");
        Assert.assertEquals(cache.getCollectionSize(TEST_COLLECTIONID), collectionSize);   
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testGetFileIDAtIndex() throws Exception {
        addDescription("Tests that a fileID at a given index can be extracted.");
        IntegrityDAO cache = createDAO();
        
        addStep("Extract a fileID from the empty database", "Returns a null");
        Assert.assertNull(cache.getFileIdAtIndex(TEST_COLLECTIONID, 0L));
        
        addStep("Insert test data into database", "Data is ingested");
        FileIDsData data = makeFileIDsDataWithGivenFileSize(TEST_FILE_ID, 100L);
        cache.updateFileIDs(data, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.updateFileIDs(data, TEST_PILLAR_2, TEST_COLLECTIONID);

        addStep("Extract the first fileID", "The inserted fileID");
        Assert.assertEquals(cache.getFileIdAtIndex(TEST_COLLECTIONID, 0L), TEST_FILE_ID);
        
        addStep("Extract a fileID at an incomprehendable index from the database", "Returns a null");
        Assert.assertNull(cache.getFileIdAtIndex(TEST_COLLECTIONID, Long.MAX_VALUE));
    }
    
    private FileIDsData makeFileIDsDataWithGivenFileSize(String fileID, Long size) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        FileIDsDataItem dataItem = new FileIDsDataItem();
        dataItem.setFileID(fileID);
        dataItem.setFileSize(BigInteger.valueOf(size));
        dataItem.setLastModificationTime(CalendarUtils.getNow());
        items.getFileIDsDataItem().add(dataItem);
        res.setFileIDsDataItems(items);
        return res;
    }
    
    private List<ChecksumDataForChecksumSpecTYPE> getChecksumResults(String fileID, String checksum) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        
        ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
        csData.setChecksumValue(Base16Utils.encodeBase16(checksum));
        csData.setCalculationTimestamp(CalendarUtils.getNow());
        csData.setFileID(fileID);
        res.add(csData);
        return res;
    }
    
    private FileIDsData getFileIDsData(String... fileIDs) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        
        for(String fileID : fileIDs) {
            FileIDsDataItem dataItem = new FileIDsDataItem();
            dataItem.setFileID(fileID);
            dataItem.setFileSize(BigInteger.valueOf(items.getFileIDsDataItem().size() + 1));
            dataItem.setLastModificationTime(CalendarUtils.getNow());
            items.getFileIDsDataItem().add(dataItem);
        } 
        
        res.setFileIDsDataItems(items);
        return res;
    }
    
    private IntegrityDAO createDAO() {
        DatabaseManager dm = new IntegrityDatabaseManager(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        return new DerbyIntegrityDAO(dm.getConnector(), settings);
    }
    
    /**
     * This is not the way to handle the iterators, as the lists might grow really long. 
     * It's here to make the tests simple, and can be done as there's only small amounts of test data in the tests. 
     */
    private List<String> getIssuesFromIterator(IntegrityIssueIterator it) {
        List<String> issues = new ArrayList<String>();
        String issue = null;
        while((issue = it.getNextIntegrityIssue()) != null) {
            issues.add(issue);
        }
        
        return issues;
    }
}
