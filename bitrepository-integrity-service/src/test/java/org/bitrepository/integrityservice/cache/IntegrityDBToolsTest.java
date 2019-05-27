/*
 * #%L
 * Bitrepository Integrity Service
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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.FileInfosDataItem;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.IntegrityDatabaseTestCase;
import org.bitrepository.integrityservice.cache.database.DerbyIntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDBStateException;
import org.bitrepository.integrityservice.cache.database.IntegrityDBTools;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IntegrityDBToolsTest extends IntegrityDatabaseTestCase {

    String TEST_PILLAR_1 = "MY-TEST-PILLAR-1";
    String TEST_PILLAR_2 = "MY-TEST-PILLAR-2";
    String EXTRA_PILLAR = "MY-EXTRA-PILLAR";
    String EXTRA_COLLECTION = "extra-collection";
    String TEST_FILE_ID = "TEST-FILE-ID";
    String TEST_COLLECTIONID;
    
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
        pids.getPillarID().add(TEST_PILLAR_2);
        extraCollection.setPillarIDs(pids);
        settings.getRepositorySettings().getCollections().getCollection().add(extraCollection);
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testAddCollectionSuccess() {
        addDescription("Tests that a new collection can be added to the integrity database");
        String newCollectionID = "new-collectionid";
        DatabaseManager dm = new IntegrityDatabaseManager(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        DBConnector dbCon = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        IntegrityDAO integrityDAO = new DerbyIntegrityDAO(dm.getConnector());
        IntegrityDBTools tool = new IntegrityDBTools(dbCon);
        List<String> collections = integrityDAO.getCollections();
        addStep("Extract initial list of collections", "The list contains the expected collections");
        assertTrue(collections.contains(TEST_COLLECTIONID));
        assertTrue(collections.contains(EXTRA_COLLECTION));
        assertFalse(collections.contains(newCollectionID));
        
        addStep("Add the new collection", "The new collection is found in the list of collections");
        tool.addCollection(newCollectionID);
        collections = integrityDAO.getCollections();
        assertTrue(collections.contains(TEST_COLLECTIONID));
        assertTrue(collections.contains(EXTRA_COLLECTION));
        assertTrue(collections.contains(newCollectionID));
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testAddExistingCollection() {
        addDescription("Tests that an existing collectionID cannot be added to the integrity database.");
        DatabaseManager dm = new IntegrityDatabaseManager(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        DBConnector dbCon = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        IntegrityDAO integrityDAO = new DerbyIntegrityDAO(dm.getConnector());
        IntegrityDBTools tool = new IntegrityDBTools(dbCon);
        List<String> collections = integrityDAO.getCollections();
        addStep("Extract initial list of collections.", "The list contains the expected collections.");
        assertTrue(collections.contains(TEST_COLLECTIONID));
        assertTrue(collections.contains(EXTRA_COLLECTION));
        
        addStep("Attempt to add the new collection.", "An exception is thrown, and the collection list is uneffected.");
        try {
            tool.addCollection(TEST_COLLECTIONID);
            fail("addCollection did not fail as expected");
        } catch(IntegrityDBStateException e) {
            
        }
        collections = integrityDAO.getCollections();
        assertTrue(collections.contains(TEST_COLLECTIONID));
        assertTrue(collections.contains(EXTRA_COLLECTION));
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testRemoveNonExistingCollection() {
        addDescription("Tests that a non existing collection can't be removed from the integrity database.");
        String nonExistingCollectionID = "non-existing-collectionid";
        DatabaseManager dm = new IntegrityDatabaseManager(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        DBConnector dbCon = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        IntegrityDAO integrityDAO = new DerbyIntegrityDAO(dm.getConnector());
        IntegrityDBTools tool = new IntegrityDBTools(dbCon);
        List<String> collections = integrityDAO.getCollections();
        addStep("Extract initial list of collections.", "The list contains the expected collections.");
        assertTrue(collections.contains(TEST_COLLECTIONID));
        assertTrue(collections.contains(EXTRA_COLLECTION));
        assertFalse(collections.contains(nonExistingCollectionID));

        addStep("Attempt to remove the non-existing collection.", "An exception is thrown, the collection list is uneffected.");
        try {
            tool.removeCollection(nonExistingCollectionID);
            fail("removeCollection did not fail as expected");
        } catch (IntegrityDBStateException e) {
            
        }
        collections = integrityDAO.getCollections();
        assertTrue(collections.contains(TEST_COLLECTIONID));
        assertTrue(collections.contains(EXTRA_COLLECTION));
    }
    
    /*@Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testRemoveExistingCollection() {
        addDescription("Tests the removal of an existing collection and references to it in the integrity database");
        DatabaseManager dm = new IntegrityDatabaseManager(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        DBConnector dbCon = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        IntegrityDAO2 integrityDAO = new DerbyIntegrityDAO2(dm.getConnector(), settings);
        IntegrityDBTools tool = new IntegrityDBTools(dbCon);
        
        List<String> collections = integrityDAO.getCollections();
        addStep("Extract initial list of collections.", "The list contains the expected collections.");
        assertTrue(collections.contains(TEST_COLLECTIONID));
        assertTrue(collections.contains(EXTRA_COLLECTION));
        
        addStep("Populate the database", 
                "The databse contains entries for the collection that is to be removed, and the one that stays.");
        populateCollection(integrityDAO, TEST_COLLECTIONID);
        populateCollection(integrityDAO, EXTRA_COLLECTION);
        
        Long collectionFileCount = integrityDAO.getNumberOfFilesInCollection(TEST_COLLECTIONID);
        assertNotNull(collectionFileCount, "Number of files for the collection");
        assertTrue(collectionFileCount > 0);
        assertNotNull(integrityDAO.getLatestPillarStats(TEST_COLLECTIONID));
        assertFalse(integrityDAO.getLatestPillarStats(TEST_COLLECTIONID).isEmpty());
        assertNotNull(integrityDAO.getLatestCollectionStats(TEST_COLLECTIONID, 1L));
        assertFalse(integrityDAO.getLatestCollectionStats(TEST_COLLECTIONID, 1L).isEmpty());

        collectionFileCount = integrityDAO.getNumberOfFilesInCollection(EXTRA_COLLECTION);
        assertNotNull(collectionFileCount, "Number of files for the collection");
        assertTrue(collectionFileCount > 0);
        assertNotNull(integrityDAO.getLatestPillarStats(EXTRA_COLLECTION));
        assertFalse(integrityDAO.getLatestPillarStats(EXTRA_COLLECTION).isEmpty());
        assertNotNull(integrityDAO.getLatestCollectionStats(EXTRA_COLLECTION, 1L));
        assertFalse(integrityDAO.getLatestCollectionStats(EXTRA_COLLECTION, 1L).isEmpty());
        
        
        addStep("Remove the collection TEST_COLLECTIONID", 
                "The collection is removed, references to the collection does not exist anymore. "
                + "The other collection is untouched");
        tool.removeCollection(TEST_COLLECTIONID);
        collections = integrityDAO.retrieveCollectionsInDatabase();
        assertFalse(collections.contains(TEST_COLLECTIONID));
        assertTrue(collections.contains(EXTRA_COLLECTION));
        
        collectionFileCount = integrityDAO.getNumberOfFilesInCollection(TEST_COLLECTIONID);
        assertNotNull(collectionFileCount, "Number of files for the collection");
        assertTrue(collectionFileCount == 0);
        assertTrue(integrityDAO.getLatestPillarStats(TEST_COLLECTIONID).isEmpty());
        assertTrue(integrityDAO.getLatestCollectionStats(TEST_COLLECTIONID, 1L).isEmpty());
        
        collectionFileCount = integrityDAO.getNumberOfFilesInCollection(EXTRA_COLLECTION);
        assertNotNull(collectionFileCount, "Number of files for the collection");
        assertTrue(collectionFileCount > 0);
        assertNotNull(integrityDAO.getLatestPillarStats(EXTRA_COLLECTION));
        assertFalse(integrityDAO.getLatestPillarStats(EXTRA_COLLECTION).isEmpty());
        assertNotNull(integrityDAO.getLatestCollectionStats(EXTRA_COLLECTION, 1L));
        assertFalse(integrityDAO.getLatestCollectionStats(EXTRA_COLLECTION, 1L).isEmpty());
    }*/
    
    private void populateCollection(IntegrityDAO dao, String collectionID) {
        String file2 = TEST_FILE_ID + "-2";
        String file3 = TEST_FILE_ID + "-3";
        String file4 = TEST_FILE_ID + "-4";
        String file5 = TEST_FILE_ID + "-5";
        Long size1 = new Long(100);
        Long size2 = new Long(200);
        Long size3 = new Long(300);
        Long size4 = new Long(400);
        Long size5 = new Long(500);
        String checksum1 = "abcd";
        String checksum2 = "acbd";
        String checksum3 = "aacc";
        String checksum3bad = "baad";
        String checksum4 = "ccaa";
        String checksum5 = "ddaa";
        FileInfosDataItem data1 = makeFileInfoDataItem(checksum1, TEST_FILE_ID, size1);
        FileInfosDataItem data2 = makeFileInfoDataItem(checksum2, file2, size2);
        FileInfosDataItem data3 = makeFileInfoDataItem(checksum3, file3, size3);
        FileInfosDataItem data3bad = makeFileInfoDataItem(checksum3bad, file3, size3);
        FileInfosDataItem data4 = makeFileInfoDataItem(checksum4, file4, size4);
        FileInfosDataItem data5 = makeFileInfoDataItem(checksum5, file5, size5);
        
        List<FileInfosDataItem> dataPillar1 = new ArrayList<>();
        dataPillar1.add(data1);
        dataPillar1.add(data3bad);
        dataPillar1.add(data4);
        dataPillar1.add(data5);

        List<FileInfosDataItem> dataPillar2 = new ArrayList<>();
        dataPillar2.add(data1);
        dataPillar2.add(data2);
        dataPillar2.add(data3);
        dataPillar2.add(data4);
        
        
        dao.updateFileInfos(dataPillar1, TEST_PILLAR_1, collectionID);
        dao.updateFileInfos(dataPillar2, TEST_PILLAR_2, collectionID);
        
        /*
        FileIDsData data1 = makeFileIDsDataWithGivenFileSize(TEST_FILE_ID, size1);
        FileIDsData data2 = makeFileIDsDataWithGivenFileSize(file2, size2);
        FileIDsData data3 = makeFileIDsDataWithGivenFileSize(file3, size3);
        FileIDsData data4 = makeFileIDsDataWithGivenFileSize(file4, size4);
        FileIDsData data5 = makeFileIDsDataWithGivenFileSize(file5, size5);
        List<ChecksumDataForChecksumSpecTYPE> csData1 = getChecksumResults(TEST_FILE_ID, checksum1);
        List<ChecksumDataForChecksumSpecTYPE> csData2 = getChecksumResults(file2, checksum2);
        List<ChecksumDataForChecksumSpecTYPE> csData3 = getChecksumResults(file3, checksum3);
        List<ChecksumDataForChecksumSpecTYPE> csData3bad = getChecksumResults(file3, checksum3bad);
        List<ChecksumDataForChecksumSpecTYPE> csData4 = getChecksumResults(file4, checksum4);
        List<ChecksumDataForChecksumSpecTYPE> csData5 = getChecksumResults(file5, checksum5);

        List<ChecksumDataForChecksumSpecTYPE> csDataPillar1 = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        csDataPillar1.addAll(csData1);
        csDataPillar1.addAll(csData3bad);
        csDataPillar1.addAll(csData4);
        csDataPillar1.addAll(csData5);
        List<ChecksumDataForChecksumSpecTYPE> csDataPillar2 = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        csDataPillar2.addAll(csData1);
        csDataPillar2.addAll(csData2);
        csDataPillar2.addAll(csData3);
        csDataPillar2.addAll(csData4);
        dao.updateFileIDs(data1, TEST_PILLAR_1, collectionID);
        dao.updateFileIDs(data3, TEST_PILLAR_1, collectionID);
        dao.updateFileIDs(data4, TEST_PILLAR_1, collectionID);
        dao.updateFileIDs(data5, TEST_PILLAR_1, collectionID);
        
        dao.updateFileIDs(data1, TEST_PILLAR_2, collectionID);
        dao.updateFileIDs(data2, TEST_PILLAR_2, collectionID);
        dao.updateFileIDs(data3, TEST_PILLAR_2, collectionID);
        dao.updateFileIDs(data4, TEST_PILLAR_2, collectionID);

        dao.updateChecksums(csDataPillar1, TEST_PILLAR_1, collectionID);
        dao.updateChecksums(csDataPillar2, TEST_PILLAR_2, collectionID);
        */
    }
    
    private FileInfosDataItem makeFileInfoDataItem(String checksum, String fileID, long size) {
        FileInfosDataItem item = new FileInfosDataItem();
        item.setLastModificationTime(CalendarUtils.getNow());
        item.setCalculationTimestamp(CalendarUtils.getNow());
        item.setChecksumValue(Base16Utils.encodeBase16(checksum));
        item.setFileSize(BigInteger.valueOf(0L));
        item.setFileID(fileID);
                
        return item;
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
}
