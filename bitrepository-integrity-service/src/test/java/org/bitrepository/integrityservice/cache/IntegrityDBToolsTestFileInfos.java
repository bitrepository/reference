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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class IntegrityDBToolsTestFileInfos extends IntegrityDatabaseTestCase {

    String TEST_PILLAR_1 = "MY-TEST-PILLAR-1";
    String TEST_PILLAR_2 = "MY-TEST-PILLAR-2";
    String EXTRA_PILLAR = "MY-EXTRA-PILLAR";
    String EXTRA_COLLECTION = "extra-collection";
    String TEST_FILE_ID = "TEST-FILE-ID";
    String TEST_COLLECTIONID;

    @BeforeMethod(alwaysRun = true)
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
        } catch (IntegrityDBStateException e) {
            System.out.println(e);
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
            System.out.println(e);
        }
        collections = integrityDAO.getCollections();
        assertTrue(collections.contains(TEST_COLLECTIONID));
        assertTrue(collections.contains(EXTRA_COLLECTION));
    }

    private void populateCollection(IntegrityDAO dao, String collectionID) {
        String file2 = TEST_FILE_ID + "-2";
        String file3 = TEST_FILE_ID + "-3";
        String file4 = TEST_FILE_ID + "-4";
        String file5 = TEST_FILE_ID + "-5";
        long size1 = 100L;
        long size2 = 200L;
        long size3 = 300L;
        long size4 = 400L;
        long size5 = 500L;
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
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<>();

        ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
        csData.setChecksumValue(Base16Utils.encodeBase16(checksum));
        csData.setCalculationTimestamp(CalendarUtils.getNow());
        csData.setFileID(fileID);
        res.add(csData);

        return res;
    }
}