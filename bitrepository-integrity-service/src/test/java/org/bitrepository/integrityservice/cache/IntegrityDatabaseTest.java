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
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.IntegrityDatabaseTestCase;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IntegrityDatabaseTest extends IntegrityDatabaseTestCase {
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
        addDescription("Tests that the connection can be instantaited.");
        IntegrityDatabase integrityCache = new IntegrityDatabase(settings);
        Assert.assertNotNull(integrityCache);
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void initialStateExtractionTest() throws Exception {
        addDescription("Tests the initial state of the IntegrityModel. Should not contain any data.");
        IntegrityModel model = new IntegrityDatabase(settings);
        
        addStep("Test the 'findChecksumsOlderThan'", "Should deliver an empty collection");
        Collection<String> oldChecksums = model.findChecksumsOlderThan(new Date(0));
        Assert.assertNotNull(oldChecksums);
        Assert.assertEquals(oldChecksums.size(), 0);
        
        addStep("Test the 'findMissingChecksums'", "Should deliver an empty collection");
        Collection<String> missingChecksums = model.findMissingChecksums();
        Assert.assertNotNull(missingChecksums);
        Assert.assertEquals(missingChecksums.size(), 0);
        
        addStep("Test the 'findMissingFiles'", "Should deliver an empty collection");
        Collection<String> missingFiles = model.findMissingFiles();
        Assert.assertNotNull(missingFiles);
        Assert.assertEquals(missingFiles.size(), 0);
        
        addStep("Test the 'getAllFileIDs'", "Should deliver an empty collection");
        Collection<String> allFileIDs = model.getAllFileIDs();
        Assert.assertNotNull(allFileIDs);
        Assert.assertEquals(allFileIDs.size(), 0);
        
        addStep("Test the 'getFileInfos'", "Should deliver an empty collection");
        Collection<FileInfo> fileInfos = model.getFileInfos(TEST_FILE_ID);
        Assert.assertNotNull(fileInfos);
        Assert.assertEquals(fileInfos.size(), 0);
        
        addStep("Test the 'getNumberOfChecksumErrors'", "Should be zero for both pillars.");
        Assert.assertEquals(model.getNumberOfChecksumErrors(TEST_PILLAR_1), 0);
        Assert.assertEquals(model.getNumberOfChecksumErrors(TEST_PILLAR_2), 0);
        
        addStep("Test the 'getNumberOfFiles'", "Should be zero for both pillars.");
        Assert.assertEquals(model.getNumberOfFiles(TEST_PILLAR_1), 0);
        Assert.assertEquals(model.getNumberOfFiles(TEST_PILLAR_2), 0);
        
        addStep("Test the 'getNumberOfMissingFiles'", "Should be zero for both pillars.");
        Assert.assertEquals(model.getNumberOfMissingFiles(TEST_PILLAR_1), 0);
        Assert.assertEquals(model.getNumberOfMissingFiles(TEST_PILLAR_2), 0);
        
        addStep("Test the 'getPillarsMissingFile'", "Should deliver an empty collection");
        Collection<String> pillarsMissingFiel = model.getPillarsMissingFile(TEST_FILE_ID);
        Assert.assertNotNull(pillarsMissingFiel);
        Assert.assertEquals(pillarsMissingFiel.size(), 0);
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testIngestOfFileIDsData() throws Exception {
        addDescription("Tests the ingesting of file ids data");
        IntegrityModel model = new IntegrityDatabase(settings);
        
        addStep("Create data", "Should be ingested into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        model.addFileIDs(data1, getAllFileIDs(), TEST_PILLAR_1);
        model.addFileIDs(data1, getAllFileIDs(), TEST_PILLAR_2);
        
        addStep("Extract the data", "Should be identical to the ingested data");
        Collection<FileInfo> fileinfos = model.getFileInfos(TEST_FILE_ID);
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
        IntegrityModel model = new IntegrityDatabase(settings);
        
        addStep("Create data", "Should be ingested into the database");
        List<ChecksumDataForChecksumSpecTYPE> csData = getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM);
        model.addChecksums(csData, TEST_PILLAR_1);
        model.addChecksums(csData, TEST_PILLAR_2);
        
        addStep("Extract the data", "Should be identical to the ingested data");
        Collection<FileInfo> fileinfos = model.getFileInfos(TEST_FILE_ID);
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
        IntegrityModel model = new IntegrityDatabase(settings);

        addStep("Create data", "Should be ingested into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        model.addFileIDs(data1, getAllFileIDs(), TEST_PILLAR_1);
        model.addFileIDs(data1, getAllFileIDs(), TEST_PILLAR_2);
        
        Collection<FileInfo> fileinfos = model.getFileInfos(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        
        addStep("Delete the entry", "No fileinfos should be extracted.");
        model.deleteFileIdEntry(TEST_FILE_ID);
        fileinfos = model.getFileInfos(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 0);
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testSettingStateToMissing() throws Exception {
        addDescription("Tests the ability to set an file to missing at a given pillar.");
        IntegrityModel model = new IntegrityDatabase(settings);

        addStep("Create data", "Should be ingested into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        model.addFileIDs(data1, getAllFileIDs(), TEST_PILLAR_1);
        model.addFileIDs(data1, getAllFileIDs(), TEST_PILLAR_2);
        
        Collection<FileInfo> fileinfos = model.getFileInfos(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getFileState(), FileState.EXISTING);
        }
        
        addStep("Set the file to missing", "Should change state.");
        model.setFileMissing(TEST_FILE_ID, Arrays.asList(TEST_PILLAR_1));
        fileinfos = model.getFileInfos(TEST_FILE_ID);
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
    public void testFileMissingThroughUpdate() throws Exception {
        addDescription("Tests that updating file ids can result in a missing file.");
        IntegrityModel model = new IntegrityDatabase(settings);

        addStep("Create data", "Should be ingested into the database");
        FileIDsData data1 = getFileIDsData(TEST_FILE_ID);
        model.addFileIDs(data1, getAllFileIDs(), TEST_PILLAR_1);
        model.addFileIDs(data1, getAllFileIDs(), TEST_PILLAR_2);
        
        Collection<FileInfo> fileinfos = model.getFileInfos(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getFileState(), FileState.EXISTING);
        }
        
        addStep("Add file ids, but without any content", "Should change state.");
        FileIDsData data2 = getFileIDsData();
        model.addFileIDs(data2, getSpecificFileIDs(TEST_FILE_ID), TEST_PILLAR_1);
        fileinfos = model.getFileInfos(TEST_FILE_ID);
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
        IntegrityModel model = new IntegrityDatabase(settings);

        addStep("Create data", "Should be ingested into the database");
        List<ChecksumDataForChecksumSpecTYPE> csData = getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM); 
        model.addChecksums(csData, TEST_PILLAR_1);
        model.addChecksums(csData, TEST_PILLAR_2);
        
        Collection<FileInfo> fileinfos = model.getFileInfos(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getChecksumState(), ChecksumState.UNKNOWN);
        }
        
        addStep("Set the file to missing", "Should change state.");
        model.setChecksumError(TEST_FILE_ID, Arrays.asList(TEST_PILLAR_1));
        fileinfos = model.getFileInfos(TEST_FILE_ID);
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
        IntegrityModel model = new IntegrityDatabase(settings);

        addStep("Create data", "Should be ingested into the database");
        List<ChecksumDataForChecksumSpecTYPE> csData = getChecksumResults(TEST_FILE_ID, TEST_CHECKSUM); 
        model.addChecksums(csData, TEST_PILLAR_1);
        model.addChecksums(csData, TEST_PILLAR_2);
        
        Collection<FileInfo> fileinfos = model.getFileInfos(TEST_FILE_ID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getChecksumState(), ChecksumState.UNKNOWN);
        }
        
        addStep("Set the file to missing", "Should change state.");
        model.setChecksumAgreement(TEST_FILE_ID, Arrays.asList(TEST_PILLAR_1));
        fileinfos = model.getFileInfos(TEST_FILE_ID);
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
    
    private FileIDs getAllFileIDs() {
        FileIDs res = new FileIDs();
        res.setAllFileIDs("true");
        return res;
    }
    
    private FileIDs getSpecificFileIDs(String fileId) {
        FileIDs res = new FileIDs();
        res.setFileID(fileId);
        return res;
    }
}