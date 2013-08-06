package org.bitrepository.integrityservice.cache;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.IntegrityDatabaseTestCase;
import org.bitrepository.integrityservice.cache.database.DerbyIntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDBStateException;
import org.bitrepository.integrityservice.cache.database.IntegrityDBTools;
import org.bitrepository.service.database.DBConnector;
import org.junit.Assert;
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
        DBConnector dbCon = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        IntegrityDAO integrityDAO = new DerbyIntegrityDAO(dbCon, settings.getRepositorySettings().getCollections());
        IntegrityDBTools tool = new IntegrityDBTools(dbCon);
        List<String> collections = integrityDAO.retrieveCollectionsInDatabase();
        addStep("Extract initial list of collections", "The list contains the expected collections");
        Assert.assertTrue(collections.contains(TEST_COLLECTIONID));
        Assert.assertTrue(collections.contains(EXTRA_COLLECTION));
        Assert.assertFalse(collections.contains(newCollectionID));
        
        addStep("Add the new collection", "The new collection is found in the list of collections");
        tool.addCollection(newCollectionID);
        collections = integrityDAO.retrieveCollectionsInDatabase();
        Assert.assertTrue(collections.contains(TEST_COLLECTIONID));
        Assert.assertTrue(collections.contains(EXTRA_COLLECTION));
        Assert.assertTrue(collections.contains(newCollectionID));
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testAddExistingCollection() {
        addDescription("Tests that an existing collectionID cannot be added to the integrity database.");
        DBConnector dbCon = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        IntegrityDAO integrityDAO = new DerbyIntegrityDAO(dbCon, settings.getRepositorySettings().getCollections());
        IntegrityDBTools tool = new IntegrityDBTools(dbCon);
        List<String> collections = integrityDAO.retrieveCollectionsInDatabase();
        addStep("Extract initial list of collections.", "The list contains the expected collections.");
        Assert.assertTrue(collections.contains(TEST_COLLECTIONID));
        Assert.assertTrue(collections.contains(EXTRA_COLLECTION));
        
        addStep("Attempt to add the new collection.", "An exception is thrown, and the collection list is uneffected.");
        try {
            tool.addCollection(TEST_COLLECTIONID);
            Assert.fail("addCollection did not fail as expected");
        } catch(IntegrityDBStateException e) {
            
        }
        collections = integrityDAO.retrieveCollectionsInDatabase();
        Assert.assertTrue(collections.contains(TEST_COLLECTIONID));
        Assert.assertTrue(collections.contains(EXTRA_COLLECTION));
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testRemoveNonExistingCollection() {
        addDescription("Tests that a non existing collection can't be removed from the integrity database.");
        String nonExistingCollectionID = "non-existing-collectionid";
        DBConnector dbCon = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        IntegrityDAO integrityDAO = new DerbyIntegrityDAO(dbCon, settings.getRepositorySettings().getCollections());
        IntegrityDBTools tool = new IntegrityDBTools(dbCon);
        List<String> collections = integrityDAO.retrieveCollectionsInDatabase();
        addStep("Extract initial list of collections.", "The list contains the expected collections.");
        Assert.assertTrue(collections.contains(TEST_COLLECTIONID));
        Assert.assertTrue(collections.contains(EXTRA_COLLECTION));
        Assert.assertFalse(collections.contains(nonExistingCollectionID));

        addStep("Attempt to remove the non-existing collection.", "An exception is thrown, the collection list is uneffected.");
        try {
            tool.removeCollection(nonExistingCollectionID);
            Assert.fail("removeCollection did not fail as expected");
        } catch (IntegrityDBStateException e) {
            
        }
        collections = integrityDAO.retrieveCollectionsInDatabase();
        Assert.assertTrue(collections.contains(TEST_COLLECTIONID));
        Assert.assertTrue(collections.contains(EXTRA_COLLECTION));
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testRemoveExistingCollection() {
        addDescription("Tests the removal of an existing collection and references to it in the integrity database");
        DBConnector dbCon = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        IntegrityDAO integrityDAO = new DerbyIntegrityDAO(dbCon, settings.getRepositorySettings().getCollections());
        IntegrityDBTools tool = new IntegrityDBTools(dbCon);
        
        List<String> collections = integrityDAO.retrieveCollectionsInDatabase();
        addStep("Extract initial list of collections.", "The list contains the expected collections.");
        Assert.assertTrue(collections.contains(TEST_COLLECTIONID));
        Assert.assertTrue(collections.contains(EXTRA_COLLECTION));
        
        addStep("Populate the database", 
                "The databse contains entries for the collection that is to be removed, and the one that stays.");
        populateCollection(integrityDAO, TEST_COLLECTIONID);
        populateCollection(integrityDAO, EXTRA_COLLECTION);
        
        Long collectionFileCount = integrityDAO.getNumberOfFilesInCollection(TEST_COLLECTIONID);
        Assert.assertNotNull("Number of files for the collection", collectionFileCount);
        Assert.assertTrue(collectionFileCount > 0);
        Assert.assertNotNull(integrityDAO.getLatestPillarStats(TEST_COLLECTIONID));
        Assert.assertFalse(integrityDAO.getLatestPillarStats(TEST_COLLECTIONID).isEmpty());
        Assert.assertNotNull(integrityDAO.getLatestCollectionStats(TEST_COLLECTIONID, 1L));
        Assert.assertFalse(integrityDAO.getLatestCollectionStats(TEST_COLLECTIONID, 1L).isEmpty());

        collectionFileCount = integrityDAO.getNumberOfFilesInCollection(EXTRA_COLLECTION);
        Assert.assertNotNull("Number of files for the collection", collectionFileCount);
        Assert.assertTrue(collectionFileCount > 0);
        Assert.assertNotNull(integrityDAO.getLatestPillarStats(EXTRA_COLLECTION));
        Assert.assertFalse(integrityDAO.getLatestPillarStats(EXTRA_COLLECTION).isEmpty());
        Assert.assertNotNull(integrityDAO.getLatestCollectionStats(EXTRA_COLLECTION, 1L));
        Assert.assertFalse(integrityDAO.getLatestCollectionStats(EXTRA_COLLECTION, 1L).isEmpty());
        
        
        addStep("Remove the collection TEST_COLLECTIONID", 
                "The collection is removed, references to the collection does not exist anymore. "
                + "The other collection is untouched");
        tool.removeCollection(TEST_COLLECTIONID);
        collections = integrityDAO.retrieveCollectionsInDatabase();
        Assert.assertFalse(collections.contains(TEST_COLLECTIONID));
        Assert.assertTrue(collections.contains(EXTRA_COLLECTION));
        
        collectionFileCount = integrityDAO.getNumberOfFilesInCollection(TEST_COLLECTIONID);
        Assert.assertNotNull("Number of files for the collection", collectionFileCount);
        Assert.assertTrue(collectionFileCount == 0);
        Assert.assertTrue(integrityDAO.getLatestPillarStats(TEST_COLLECTIONID).isEmpty());
        Assert.assertTrue(integrityDAO.getLatestCollectionStats(TEST_COLLECTIONID, 1L).isEmpty());
        
        collectionFileCount = integrityDAO.getNumberOfFilesInCollection(EXTRA_COLLECTION);
        Assert.assertNotNull("Number of files for the collection", collectionFileCount);
        Assert.assertTrue(collectionFileCount > 0);
        Assert.assertNotNull(integrityDAO.getLatestPillarStats(EXTRA_COLLECTION));
        Assert.assertFalse(integrityDAO.getLatestPillarStats(EXTRA_COLLECTION).isEmpty());
        Assert.assertNotNull(integrityDAO.getLatestCollectionStats(EXTRA_COLLECTION, 1L));
        Assert.assertFalse(integrityDAO.getLatestCollectionStats(EXTRA_COLLECTION, 1L).isEmpty());
    }
    
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

        insertChecksumDataForDAO(dao, csDataPillar1, TEST_PILLAR_1, collectionID);
        insertChecksumDataForDAO(dao, csDataPillar2, TEST_PILLAR_2, collectionID);
        
        dao.setOldUnknownFilesToMissing(new Date(), collectionID);
        dao.setFilesWithConsistentChecksumsToValid(collectionID);
        dao.setChecksumError(file3, TEST_PILLAR_1, collectionID);
        dao.makeStatisticsEntry(collectionID);
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
    
    private List<ChecksumDataForChecksumSpecTYPE> getChecksumResults(String fileId, String checksum) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        
        ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
        csData.setChecksumValue(Base16Utils.encodeBase16(checksum));
        csData.setCalculationTimestamp(CalendarUtils.getNow());
        csData.setFileID(fileId);
        res.add(csData);
        return res;
    }
}
