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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.IntegrityDatabaseTestCase;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IntegrityDAOTest extends IntegrityDatabaseTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    MockAuditManager auditManager;
    String TEST_PILLAR_1 = "MY-TEST-PILLAR-1";
    String TEST_PILLAR_2 = "MY-TEST-PILLAR-2";
    
    String TEST_FILE_ID = "TEST-FILE-ID";
    String TEST_CHECKSUM = "1234cccc4321";
    
    @BeforeMethod (alwaysRun = true)
    @Override
    public void setup() throws Exception {
        super.setup();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_1);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_2);
        auditManager = new MockAuditManager();
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
        DBConnector connector = new DBConnector(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
                        
        IntegrityDAO cache = new IntegrityDAO(connector, settings.getCollectionSettings().getClientSettings().getPillarIDs());
        Assert.assertNotNull(cache);

        addStep("Close the connection and create another one.", "Should not fail");
        connector.getConnection().close();

        synchronized(this) {
            wait(100);
        }
        
        DBConnector reconnector = new DBConnector(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        cache = new IntegrityDAO(reconnector, settings.getCollectionSettings().getClientSettings().getPillarIDs());
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void initialStateExtractionTest() throws Exception {
        addDescription("Tests the initial state of the IntegrityModel. Should not contain any data.");
        IntegrityDAO cache = createDAO();
        
        addStep("Test the 'findFilesWithOldChecksum'", "Should deliver an empty collection");
        Collection<String> oldChecksums = cache.findFilesWithOldChecksum(new Date(0));
        Assert.assertNotNull(oldChecksums);
        Assert.assertEquals(oldChecksums.size(), 0);
        
        addStep("Test the 'findMissingChecksums'", "Should deliver an empty collection");
        Collection<String> missingChecksums = cache.findMissingChecksums();
        Assert.assertNotNull(missingChecksums);
        Assert.assertEquals(missingChecksums.size(), 0);
        
        addStep("Test the 'findMissingFiles'", "Should deliver an empty collection");
        Collection<String> missingFiles = cache.findMissingFiles();
        Assert.assertNotNull(missingFiles);
        Assert.assertEquals(missingFiles.size(), 0);
        
        addStep("Test the 'getAllFileIDs'", "Should deliver an empty collection");
        Collection<String> allFileIDs = cache.getAllFileIDs();
        Assert.assertNotNull(allFileIDs);
        Assert.assertEquals(allFileIDs.size(), 0);
        
        addStep("Test the 'getFileInfosForFile'", "Should deliver an empty collection");
        Collection<FileInfo> fileInfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileInfos);
        Assert.assertEquals(fileInfos.size(), 0);
        
        addStep("Test the 'getNumberOfMissingFilesForAPillar'", "Should be zero for both pillars.");
        Assert.assertEquals(cache.getNumberOfChecksumErrorsForAPillar(TEST_PILLAR_1), 0);
        Assert.assertEquals(cache.getNumberOfChecksumErrorsForAPillar(TEST_PILLAR_2), 0);
        
        addStep("Test the 'getNumberOfExistingFilesForAPillar'", "Should be zero for both pillars.");
        Assert.assertEquals(cache.getNumberOfExistingFilesForAPillar(TEST_PILLAR_1), 0);
        Assert.assertEquals(cache.getNumberOfExistingFilesForAPillar(TEST_PILLAR_2), 0);
        
        addStep("Test the 'getNumberOfMissingFilesForAPillar'", "Should be zero for both pillars.");
        Assert.assertEquals(cache.getNumberOfMissingFilesForAPillar(TEST_PILLAR_1), 0);
        Assert.assertEquals(cache.getNumberOfMissingFilesForAPillar(TEST_PILLAR_2), 0);
        
        addStep("Test the 'getPillarsMissingFile'", "Should deliver an empty collection");
        Collection<String> pillarsMissingFile = cache.getMissingAtPillars(TEST_FILE_ID);
        Assert.assertNotNull(pillarsMissingFile);
        Assert.assertEquals(pillarsMissingFile.size(), 0);
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testIngestOfFileIDsData() throws Exception {
        addDescription("Tests the ingesting of file ids data");
        IntegrityDAO cache = createDAO();
        
        addStep("Create data", "Should be ingested into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        cache.updateFileIDs(data1, TEST_PILLAR_1);
        cache.updateFileIDs(data1, TEST_PILLAR_2);
        
        addStep("Extract the data", "Should be identical to the ingested data");
        Collection<FileInfo> fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getFileId(), TEST_FILE_ID);
            Assert.assertNull(fi.getChecksum());
            Assert.assertEquals(fi.getChecksumState(), ChecksumState.UNKNOWN);
            Assert.assertEquals(fi.getFileState(), FileState.EXISTING);
            Assert.assertEquals(fi.getDateForLastChecksumCheck(), CalendarUtils.getEpoch());
            Assert.assertEquals(fi.getDateForLastFileIDCheck(), data1.getFileIDsDataItems().getFileIDsDataItem().get(0).getLastModificationTime());
        }
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testIngestOfChecksumsData() throws Exception {
        addDescription("Tests the ingesting of checksums data");
        IntegrityDAO cache = createDAO();
        
        addStep("Create data", "Should be ingested into the database");
        List<ChecksumDataForChecksumSpecTYPE> csData = getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM);
        cache.updateChecksumData(csData, TEST_PILLAR_1);
        cache.updateChecksumData(csData, TEST_PILLAR_2);
        
        addStep("Extract the data", "Should be identical to the ingested data");
        Collection<FileInfo> fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getFileId(), TEST_FILE_ID);
            Assert.assertEquals(fi.getChecksum(), TEST_CHECKSUM);
            Assert.assertEquals(fi.getChecksumState(), ChecksumState.UNKNOWN);
            Assert.assertEquals(fi.getFileState(), FileState.EXISTING);
            Assert.assertEquals(fi.getDateForLastChecksumCheck(), csData.get(0).getCalculationTimestamp());
            Assert.assertEquals(fi.getDateForLastFileIDCheck(), CalendarUtils.getEpoch());
        }
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testDeletingEntry() throws Exception {
        addDescription("Tests the deletion of an FileID entry.");
        IntegrityDAO cache = createDAO();

        addStep("Create data", "Should be ingested into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        cache.updateFileIDs(data1, TEST_PILLAR_1);
        cache.updateFileIDs(data1, TEST_PILLAR_2);
        
        Collection<FileInfo> fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        
        addStep("Delete the entry", "No fileinfos should be extracted.");
        cache.removeFileId(TEST_FILE_ID);
        fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 0);
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testDeletingNonExistingEntry() throws Exception {
        addDescription("Tests the deletion of an nonexisting FileID entry.");
        IntegrityDAO cache = createDAO();
        
        String nonexistingFileEntry = "NON-EXISTING-FILE-ENTRY" + new Date().getTime();

        addStep("Create data", "Should be ingested into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        cache.updateFileIDs(data1, TEST_PILLAR_1);
        cache.updateFileIDs(data1, TEST_PILLAR_2);
        
        Assert.assertEquals(cache.getAllFileIDs().size(), 1);
        Collection<FileInfo> fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        
        addStep("Delete a nonexisting entry", "Should not change the state of the database.");
        cache.removeFileId(nonexistingFileEntry);
        Assert.assertEquals(cache.getAllFileIDs().size(), 1);
        fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testSettingStateToMissing() throws Exception {
        addDescription("Tests the ability to set an file to missing at a given pillar.");
        IntegrityDAO cache = createDAO();

        addStep("Create data", "Should be ingested into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        cache.updateFileIDs(data1, TEST_PILLAR_1);
        cache.updateFileIDs(data1, TEST_PILLAR_2);
        
        Collection<FileInfo> fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getFileState(), FileState.EXISTING);
        }
        
        addStep("Set the file to missing", "Should change state.");
        cache.setFileMissing(TEST_FILE_ID, TEST_PILLAR_1);
        fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        for(FileInfo fi : fileinfos) {
            if(fi.getPillarId().equals(TEST_PILLAR_2)) {
                Assert.assertEquals(fi.getFileState(), FileState.EXISTING);
            } else {
                Assert.assertEquals(fi.getFileState(), FileState.MISSING);
            }
        }
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testSettingChecksumStateToError() throws Exception {
        addDescription("Tests the ability to set the checksum stat to error for a given pillar.");
        IntegrityDAO cache = createDAO();

        addStep("Create data", "Should be ingested into the database");
        List<ChecksumDataForChecksumSpecTYPE> csData = getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM); 
        cache.updateChecksumData(csData, TEST_PILLAR_1);
        cache.updateChecksumData(csData, TEST_PILLAR_2);
        
        Collection<FileInfo> fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getChecksumState(), ChecksumState.UNKNOWN);
        }
        
        addStep("Set the file to missing", "Should change state.");
        cache.setChecksumError(TEST_FILE_ID, TEST_PILLAR_1);
        fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        for(FileInfo fi : fileinfos) {
            if(fi.getPillarId().equals(TEST_PILLAR_2)) {
                Assert.assertEquals(fi.getChecksumState(), ChecksumState.UNKNOWN);
            } else {
                Assert.assertEquals(fi.getChecksumState(), ChecksumState.ERROR);
            }
        }
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testSettingChecksumStateToValid() throws Exception {
        addDescription("Tests the ability to set the checksum stat to valid for a given pillar.");
        IntegrityDAO cache = createDAO();

        addStep("Create data", "Should be ingested into the database");
        List<ChecksumDataForChecksumSpecTYPE> csData = getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM); 
        cache.updateChecksumData(csData, TEST_PILLAR_1);
        cache.updateChecksumData(csData, TEST_PILLAR_2);
        
        Collection<FileInfo> fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getChecksumState(), ChecksumState.UNKNOWN);
        }
        
        addStep("Set the file to missing", "Should change state.");
        cache.setChecksumValid(TEST_FILE_ID, TEST_PILLAR_1);
        fileinfos = cache.getFileInfosForFile(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        for(FileInfo fi : fileinfos) {
            if(fi.getPillarId().equals(TEST_PILLAR_2)) {
                Assert.assertEquals(fi.getChecksumState(), ChecksumState.UNKNOWN);
            } else {
                Assert.assertEquals(fi.getChecksumState(), ChecksumState.VALID);
            }
        }
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testDistinctChecksum() throws Exception {
        addDescription("Testing the location of indistinct checksums");
        IntegrityDAO cache = createDAO();
        
        String checksum1_1 = "11";
        String checksum1_2 = "12";
        String checksum3 = "33";
        String checksum2_1 = "21";
        String checksum2_2 = "22";
        
        String BAD_FILE_ID_1 = "BAD-FILE-1";
        String BAD_FILE_ID_2 = "BAD-FILE-2";
        String GOOD_FILE_ID = "GOOD-FILE";

        addStep("Create data", "Should be ingested into the database");
        List<ChecksumDataForChecksumSpecTYPE> csData1_1 = getChecksumResults(BAD_FILE_ID_1, checksum1_1);
        List<ChecksumDataForChecksumSpecTYPE> csData1_2 = getChecksumResults(BAD_FILE_ID_2, checksum1_2);
        List<ChecksumDataForChecksumSpecTYPE> csData1_3 = getChecksumResults(GOOD_FILE_ID, checksum3);
        cache.updateChecksumData(csData1_1, TEST_PILLAR_1);
        cache.updateChecksumData(csData1_2, TEST_PILLAR_1);
        cache.updateChecksumData(csData1_3, TEST_PILLAR_1);
        List<ChecksumDataForChecksumSpecTYPE> csData2_1 = getChecksumResults(BAD_FILE_ID_1, checksum2_1);
        List<ChecksumDataForChecksumSpecTYPE> csData2_2 = getChecksumResults(BAD_FILE_ID_2, checksum2_2);
        List<ChecksumDataForChecksumSpecTYPE> csData2_3 = getChecksumResults(GOOD_FILE_ID, checksum3);
        cache.updateChecksumData(csData2_1, TEST_PILLAR_2);
        cache.updateChecksumData(csData2_2, TEST_PILLAR_2);
        cache.updateChecksumData(csData2_3, TEST_PILLAR_2);

        List<String> filesWithChecksumError = cache.getFilesWithDistinctChecksum();
        Assert.assertEquals(filesWithChecksumError, Arrays.asList(BAD_FILE_ID_1, BAD_FILE_ID_2));
        
        cache.setFilesWithUnanimousChecksumToValid();
        for(FileInfo fi : cache.getFileInfosForFile(BAD_FILE_ID_1)) {
            Assert.assertEquals(fi.getChecksumState(), ChecksumState.UNKNOWN);
        }
        for(FileInfo fi : cache.getFileInfosForFile(BAD_FILE_ID_2)) {
            Assert.assertEquals(fi.getChecksumState(), ChecksumState.UNKNOWN);
        }
        for(FileInfo fi : cache.getFileInfosForFile(GOOD_FILE_ID)) {
            Assert.assertEquals(fi.getChecksumState(), ChecksumState.VALID);
        }
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
    
    private FileIDsData getFileIDsData(String... fileIds) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        
        for(String fileId : fileIds) {
            FileIDsDataItem dataItem = new FileIDsDataItem();
            dataItem.setFileID(fileId);
            dataItem.setFileSize(BigInteger.valueOf(items.getFileIDsDataItem().size() + 1));
            dataItem.setLastModificationTime(CalendarUtils.getNow());
            items.getFileIDsDataItem().add(dataItem);
        } 
        
        res.setFileIDsDataItems(items);
        return res;
    }
    
    private IntegrityDAO createDAO() {
        return new IntegrityDAO(new DBConnector(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase()),
                settings.getCollectionSettings().getClientSettings().getPillarIDs());
    }
}
