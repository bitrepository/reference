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
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.IntegrityDatabaseTestCase;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DatabaseCacheTest extends IntegrityDatabaseTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    MockAuditManager auditManager;
    String TEST_PILLAR_1 = "MY-TEST-PILLAR-1";
    String TEST_PILLAR_2 = "MY-TEST-PILLAR-2";
    String TEST_PILLAR_3 = "MY-TEST-PILLAR-3";
    
    @BeforeMethod (alwaysRun = true)
    @Override
    public void setup() throws Exception {
        super.setup();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_1);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_2);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_3);
        auditManager = new MockAuditManager();
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void connectionTest() throws Exception {
        addDescription("Testing the connection to the integrity database.");
        addStep("Setup the variables and constants.", "Should be ok.");
        
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_1);

        String fileId = "TEST-FILE-ID-" + new Date().getTime();
        String fileId2 = "CONSISTEN-FILE-ID";
        
        addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
        IntegrityDatabase integrityCache = new IntegrityDatabase(settings);
        IntegrityDAO cache = integrityCache.getStore();
        
        addStep("Using the interface to put data into the database", "The database should be populated.");
        cache.updateFileIDs(getFileIDsData(fileId, fileId2), TEST_PILLAR_1);
        cache.updateChecksumData(getChecksumResults(fileId, fileId), TEST_PILLAR_1);
        cache.updateChecksumData(getChecksumResults(fileId2, fileId2), TEST_PILLAR_1);
        
        addStep("Using the interface to change state of some files.", "The files should change state.");
        cache.setFileMissing(fileId, TEST_PILLAR_1);
        cache.setChecksumError(fileId2, TEST_PILLAR_1);

        addStep("Testing the methods for extracting data from the database", "");
        List<String> fileIDs = cache.getAllFileIDs();
        Assert.assertNotNull(fileIDs);
        System.out.println("fileIDs: " + fileIDs);
        
        List<FileInfo> fileInfos = cache.getFileInfosForFile(fileId);
        Assert.assertNotNull(fileIDs);
        System.out.println("fileInfos: " + fileInfos);
        
        int numberOfChecksumErrors = cache.getNumberOfChecksumErrorsForAPillar(TEST_PILLAR_1);
        Assert.assertNotNull(numberOfChecksumErrors);
        Assert.assertTrue(numberOfChecksumErrors > 0);
        System.out.println("numberOfChecksumErrors: " + numberOfChecksumErrors);

        int numberOfExistingFiles = cache.getNumberOfExistingFilesForAPillar(TEST_PILLAR_1);
        Assert.assertNotNull(numberOfExistingFiles);
        Assert.assertTrue(numberOfExistingFiles > 0);
        System.out.println("numberOfExistingFiles: " + numberOfExistingFiles);

        int numberOfMissingFiles = cache.getNumberOfMissingFilesForAPillar(TEST_PILLAR_1);
        Assert.assertNotNull(numberOfMissingFiles);
        Assert.assertTrue(numberOfMissingFiles > 0);
        System.out.println("numberOfMissingFiles: " + numberOfMissingFiles);
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testMissingFiles() throws Exception {
        addDescription("Test whether the database can detect files with the filestate MISSING");
        addStep("Setup variables and constants.", "Should not be a problem.");
        String pillarId1 = "integrityCheckTest";
        String fileId1 = "TEST-FILE-ID";
        String fileId2 = "ANOTHER-TEST-FILE-ID";
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");

        addStep("Setup database cache and integrity checker", "Should not be a problem");
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(pillarId1);
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(0);
        IntegrityDatabase cache = new IntegrityDatabase(settings);
        
        addStep("Validate initial state", "Should not be any files or checksums missing");
        Assert.assertEquals(cache.findMissingFiles().size(), 0);

        addStep("Insert data for two files.", "Since no checksum");
        FileIDsData fileIdsBoth = getFileIDsData(fileId1, fileId2);
        cache.addFileIDs(fileIdsBoth, fileIDs, pillarId1);
        Assert.assertEquals(cache.findMissingFiles().size(), 0);

        addStep("Set one file to missing.", "Should be found");
        cache.setFileMissing(fileId1, Arrays.asList(pillarId1));
        List<String> missingFiles = cache.findMissingFiles();
        Assert.assertEquals(missingFiles.size(), 1);
        Assert.assertEquals(missingFiles.get(0), fileId1);
        
        addStep("Set the other file to missing.", "Should be found");
        cache.setFileMissing(fileId2, Arrays.asList(pillarId1));
        missingFiles = cache.findMissingFiles();
        Assert.assertEquals(missingFiles.size(), 2);
        Assert.assertEquals(missingFiles.get(0), fileId1);
        Assert.assertEquals(missingFiles.get(1), fileId2);
        
        addStep("Insert data for only one file", "The other should still be missing");
        fileIdsBoth = getFileIDsData(fileId1);
        cache.addFileIDs(fileIdsBoth, fileIDs, pillarId1);
        missingFiles = cache.findMissingFiles();
        Assert.assertEquals(missingFiles.size(), 1);
        Assert.assertEquals(missingFiles.get(0), fileId2);
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testMissingChecksums() throws Exception {
        addDescription("Test whether the database can detect files with the checksumstate UNKNOWN even though the file exists");
        addStep("Setup variables and constants.", "Should not be a problem.");
        String pillarId1 = "integrityCheckTest-3";
        String fileId1 = "TEST-FILE-ID"; // + new Date().getTime();
        String fileId2 = "ANOTHER-TEST-FILE-ID"; //-" + new Date().getTime();
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");

        addStep("Setup database cache and integrity checker", "Should not be a problem");
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(pillarId1);
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(0);
        IntegrityDatabase cache = new IntegrityDatabase(settings);
        
        List<String> missingChecksums;
        
        addStep("Validate initial state", "Should not be any files or checksums missing");
        Assert.assertEquals(cache.findMissingFiles().size(), 0);

        addStep("Insert file data for two files.", "Should give missing checksum for both files.");
        FileIDsData fileIdsBoth = getFileIDsData(fileId1, fileId2);
        cache.addFileIDs(fileIdsBoth, fileIDs, pillarId1);
        Assert.assertEquals(cache.findMissingFiles().size(), 0);
        Assert.assertEquals(cache.findMissingChecksums().size(), 2);

        addStep("Add some checksum data for the files", "Should still give missing checksums.");
        List<ChecksumDataForChecksumSpecTYPE> csData = getChecksumResults(fileId1, "checksum");
        csData.addAll(getChecksumResults(fileId2, "muskcehc"));
        cache.addChecksums(csData, pillarId1);
        Assert.assertEquals(cache.findMissingFiles().size(), 0);
        Assert.assertEquals(cache.findMissingChecksums().size(), 2);

        addStep("change the checksum state to valid for one file", "Only one file should miss its checksum");
        cache.setChecksumAgreement(fileId1, Arrays.asList(pillarId1));
        Assert.assertEquals(cache.findMissingFiles().size(), 0);
        missingChecksums = cache.findMissingChecksums();
        Assert.assertEquals(missingChecksums.size(), 1);
        Assert.assertEquals(missingChecksums.get(0), fileId2);
        
        addStep("Change the file state", "Should no longer be missing any checksums");
        cache.setFileMissing(fileId2, Arrays.asList(pillarId1));
        Assert.assertEquals(cache.findMissingFiles().size(), 1);
        missingChecksums = cache.findMissingChecksums();
        Assert.assertEquals(missingChecksums.size(), 0);
        
        addStep("Insert file data for two files.", "The second one should be missing its checksum again.");
        cache.addFileIDs(fileIdsBoth, fileIDs, pillarId1);
        Assert.assertEquals(cache.findMissingFiles().size(), 0);
        missingChecksums = cache.findMissingChecksums();
        Assert.assertEquals(missingChecksums.size(), 1);
        Assert.assertEquals(missingChecksums.get(0), fileId2);
        
        addStep("change the checksum state to error for the other file", "Should not be missing its checksum any more.");
        cache.setChecksumError(fileId2, Arrays.asList(pillarId1));
        Assert.assertEquals(cache.findMissingFiles().size(), 0);
        missingChecksums = cache.findMissingChecksums();
        Assert.assertEquals(missingChecksums.size(), 0);
    }
    
    private List<ChecksumDataForChecksumSpecTYPE> getChecksumResults(String fileId, String checksum) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        
        ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
        csData.setChecksumValue(checksum.getBytes());
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
}
