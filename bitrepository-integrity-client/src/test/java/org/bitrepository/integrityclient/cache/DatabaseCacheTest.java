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
package org.bitrepository.integrityclient.cache;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.database.DatabaseUtils;
import org.bitrepository.common.database.DerbyDBConnector;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityclient.cache.database.DatabaseStoragedCache;
import org.bitrepository.integrityclient.checking.SimpleIntegrityChecker;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DatabaseCacheTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
    }
    
//    @Test(groups = {"regressiontest"})
//    @Test(groups = {"databasetest"})
    public void connectionTest() throws Exception {
        addDescription("Testing the connection to the integrity database.");
        addStep("Setup the variables and constants.", "Should be ok.");
        String url = "jdbc:derby:integritydb";
        String fileId = "TEST-FILE-ID-" + new Date().getTime();
        String fileId2 = "CONSISTEN-FILE-ID";
        String pillarId = "MY-TEST-PILLAR";
        
        addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
        settings.getReferenceSettings().getIntegrityServiceSettings().setDatabaseUrl(url);
        DatabaseBasedIntegrityCached integrityCache = new DatabaseBasedIntegrityCached(settings);
        DatabaseStoragedCache cache = integrityCache.getStore();

        addStep("Using the interface to put data into the database", "The database should be populated.");
        cache.updateFileIDs(getFileIDsData(fileId, fileId2), pillarId);
        cache.updateChecksumData(getChecksumResults(fileId, fileId), getChecksumSpec(), pillarId);
        cache.updateChecksumData(getChecksumResults(fileId2, fileId2), getChecksumSpec(), pillarId);
        
        addStep("Using the interface to change state of some files.", "The files should change state.");
        cache.setFileMissing(fileId, pillarId);
        cache.setChecksumError(fileId2, pillarId);

        addStep("Testing the methods for extracting data from the database", "");
        List<String> fileIDs = cache.getAllFileIDs();
        Assert.assertNotNull(fileIDs);
        System.out.println("fileIDs: " + fileIDs);
        
        List<FileInfo> fileInfos = cache.getFileInfosForFile(fileId);
        Assert.assertNotNull(fileIDs);
        System.out.println("fileInfos: " + fileInfos);
        
        Date lastChecksumUpdate = cache.getLastChecksumUpdate(pillarId);
        Assert.assertNotNull(lastChecksumUpdate);
        System.out.println("lastChecksumUpdate: " + lastChecksumUpdate);
        
        Date lastFileUpdate = cache.getLastFileListUpdate(pillarId);
        Assert.assertNotNull(lastFileUpdate);
        System.out.println("lastFileUpdate: " + lastFileUpdate);
        
        int numberOfChecksumErrors = cache.getNumberOfChecksumErrorsForAPillar(pillarId);
        Assert.assertNotNull(numberOfChecksumErrors);
        Assert.assertTrue(numberOfChecksumErrors > 0);
        System.out.println("numberOfChecksumErrors: " + numberOfChecksumErrors);

        int numberOfExistingFiles = cache.getNumberOfExistingFilesForAPillar(pillarId);
        Assert.assertNotNull(numberOfExistingFiles);
        Assert.assertTrue(numberOfExistingFiles > 0);
        System.out.println("numberOfExistingFiles: " + numberOfExistingFiles);

        int numberOfMissingFiles = cache.getNumberOfMissingFilesForAPillar(pillarId);
        Assert.assertNotNull(numberOfMissingFiles);
        Assert.assertTrue(numberOfMissingFiles > 0);
        System.out.println("numberOfMissingFiles: " + numberOfMissingFiles);
    }
    
    @Test(groups = {"databasetest"})
    public void integrityCheckTest() throws Exception {
        addDescription("Testing whether the integrity check can interact with the database cache.");
        addStep("Setup variables and constants.", "Should not be a problem.");
        String url = "jdbc:derby:integritydb";
        String fileId1 = "TEST-FILE-ID"; // + new Date().getTime();
        String fileId2 = "ANOTHER-TEST-FILE-ID"; //-" + new Date().getTime();
        String pillarId1 = "integrityCheckTest-1";
        String pillarId2 = "integrityCheckTest-2";
        
        clearDatabase(url);
        
        addStep("Setup database cache and integrity checker", "Should not be a problem");
        settings.getReferenceSettings().getIntegrityServiceSettings().setDatabaseUrl(url);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(pillarId1);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(pillarId2);
        DatabaseBasedIntegrityCached cache = new DatabaseBasedIntegrityCached(settings);
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache);
        
        addStep("Create file id data to populate the cache.", "Data should be different for the different pillars.");
        FileIDsData fileIdsAll = getFileIDsData(fileId1, fileId2);
        cache.addFileIDs(fileIdsAll, pillarId1);
        
        FileIDsData fileIdsOne = getFileIDsData(fileId1);
        cache.addFileIDs(fileIdsOne, pillarId2);
        
        addStep("Validate that a new file is missing from one pillar", "Should be pillar 3");
        long miss1 = cache.getNumberOfMissingFiles(pillarId1);
        long miss2 = cache.getNumberOfMissingFiles(pillarId2);
        
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");
        checker.checkFileIDs(fileIDs);
        
        Assert.assertEquals(cache.getNumberOfMissingFiles(pillarId1), miss1, "No new missing files at pillar 1");
        Assert.assertFalse(miss2 == cache.getNumberOfMissingFiles(pillarId2), "Should be one missing file at pillar 2");
        
        addStep("Adding the file to the pillar, where it is missing", "Should no longer be missing.");
        
        FileIDsData fileIdsTwo = getFileIDsData(fileId2);
        cache.addFileIDs(fileIdsTwo, pillarId2);
        Assert.assertEquals(cache.getNumberOfMissingFiles(pillarId2), 0, "No more missing files at pillar 2");
        
        addStep("Create checksum data and populate the cache.", "Data should differ for achieving checksum errors.");
        List<ChecksumDataForChecksumSpecTYPE> cs1 = getChecksumResults(fileId1, "cs1");
        cache.addChecksums(cs1, getChecksumSpec(), pillarId1);
        cache.addChecksums(cs1, getChecksumSpec(), pillarId2);
        List<ChecksumDataForChecksumSpecTYPE> cs2good = getChecksumResults(fileId2, "cs2");
        cache.addChecksums(cs2good, getChecksumSpec(), pillarId1);
        List<ChecksumDataForChecksumSpecTYPE> cs2Pillar2 = getChecksumResults(fileId2, "ERORORORORORORRORORORORO");
        cache.addChecksums(cs2Pillar2, getChecksumSpec(), pillarId2);

        addStep("Performing the checksum check", "Should find an checksum error.");
        printDatabase(url);
        checker.checkChecksum(fileIDs);
        printDatabase(url);
        
        Assert.assertEquals(cache.getNumberOfChecksumErrors(pillarId1), 1, "They should disagree upon one.");
        Assert.assertEquals(cache.getNumberOfChecksumErrors(pillarId2), 1, "They should disagree upon one.");
    }
    
    @Test(groups = {"databasetest"})
    public void integrityCheckForChecksumTest() throws Exception {
        addDescription("Testing whether the integrity check can perform different checksum votes, "
                + "select a winner and find a draw.");
        addStep("Setup variables and constants.", "Should not be a problem.");
        String url = "jdbc:derby:integritydb";
        String fileId1 = "GOOD-CASE-EVERYBODY-AGREE"; 
        String fileId2 = "BAD-CASE-EVERYBODY-DISAGREE"; 
        String fileId3 = "VOTE-WINNER-CASE"; 
        String pillarId1 = "integrityCheckForChecksumTest-1";
        String pillarId2 = "integrityCheckForChecksumTest-2";
        String pillarId3 = "integrityCheckForChecksumTest-3";
        
        clearDatabase(url);
        
        addStep("Setup database cache and integrity checker", "Should not be a problem");
        settings.getReferenceSettings().getIntegrityServiceSettings().setDatabaseUrl(url);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(pillarId1);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(pillarId2);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(pillarId3);
        DatabaseBasedIntegrityCached cache = new DatabaseBasedIntegrityCached(settings);
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache);
        
        FileIDs fileIds = new FileIDs();
        
        addStep("Create checksum data and populate the cache with the good case scenario where the file has "
                + "same checksum on every pillar.", "Should be inserted into database.");
        List<ChecksumDataForChecksumSpecTYPE> csGood = getChecksumResults(fileId1, "good-checksum");
        cache.addChecksums(csGood, getChecksumSpec(), pillarId1);
        cache.addChecksums(csGood, getChecksumSpec(), pillarId2);
        cache.addChecksums(csGood, getChecksumSpec(), pillarId3);

        addStep("Performing the checksum check on the good case.", "Should not find any checksum errors.");
        printDatabase(url);
        fileIds.setFileID(fileId1);
        checker.checkChecksum(fileIds);
        printDatabase(url);
        
        Assert.assertEquals(cache.getNumberOfChecksumErrors(pillarId1), 0, "They should disagree upon one.");
        Assert.assertEquals(cache.getNumberOfChecksumErrors(pillarId2), 0, "They should disagree upon one.");
        Assert.assertEquals(cache.getNumberOfChecksumErrors(pillarId3), 0, "They should disagree upon one.");

        addStep("Create checksum data and populate the cache with the good case scenario where the file has "
                + "same checksum on every pillar.", "Should be inserted into database.");
        List<ChecksumDataForChecksumSpecTYPE> csBad1 = getChecksumResults(fileId2, "bad-checksum-1");
        List<ChecksumDataForChecksumSpecTYPE> csBad2 = getChecksumResults(fileId2, "bad-checksum-2");
        List<ChecksumDataForChecksumSpecTYPE> csBad3 = getChecksumResults(fileId2, "bad-checksum-3");
        cache.addChecksums(csBad1, getChecksumSpec(), pillarId1);
        cache.addChecksums(csBad2, getChecksumSpec(), pillarId2);
        cache.addChecksums(csBad3, getChecksumSpec(), pillarId3);

        addStep("Performing the checksum check on the good case.", "Should not find any checksum errors.");
        printDatabase(url);
        fileIds.setFileID(fileId2);
        checker.checkChecksum(fileIds);
        printDatabase(url);
        
        Assert.assertEquals(cache.getNumberOfChecksumErrors(pillarId1), 1, "They should disagree upon one.");
        Assert.assertEquals(cache.getNumberOfChecksumErrors(pillarId2), 1, "They should disagree upon one.");
        Assert.assertEquals(cache.getNumberOfChecksumErrors(pillarId3), 1, "They should disagree upon one.");

        addStep("Create checksum data and populate the cache with the good case scenario where the file has "
                + "same checksum on every pillar.", "Should be inserted into database.");
        List<ChecksumDataForChecksumSpecTYPE> csVoteWin = getChecksumResults(fileId3, "win-checksum");
        List<ChecksumDataForChecksumSpecTYPE> csVoteLoss = getChecksumResults(fileId3, "loss-checksum");
        cache.addChecksums(csVoteWin, getChecksumSpec(), pillarId1);
        cache.addChecksums(csVoteWin, getChecksumSpec(), pillarId2);
        cache.addChecksums(csVoteLoss, getChecksumSpec(), pillarId3);

        addStep("Performing the checksum check on the good case.", "Should not find any checksum errors.");
        printDatabase(url);
        fileIds.setFileID(fileId3);
        checker.checkChecksum(fileIds);
        printDatabase(url);
        
        Assert.assertEquals(cache.getNumberOfChecksumErrors(pillarId1), 1, "They should disagree upon one.");
        Assert.assertEquals(cache.getNumberOfChecksumErrors(pillarId2), 1, "They should disagree upon one.");
        Assert.assertEquals(cache.getNumberOfChecksumErrors(pillarId3), 2, "They should disagree upon one.");

    }
    
    private List<ChecksumDataForChecksumSpecTYPE> getChecksumResults(String fileId, String checksum) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        
        ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
        csData.setChecksumValue(checksum.getBytes());
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setFileID(fileId);
        res.add(csData);
        return res;
    }
    
    private ChecksumSpecTYPE getChecksumSpec() {
        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        res.setChecksumSalt(new byte[0]);
        res.setChecksumType(ChecksumType.MD5);
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
    
    private void printDatabase(String url) throws Exception {
        Connection con = DerbyDBConnector.getEmbeddedDBConnection(url);
        
        // Print out file info
        String fileInfoSql = "SELECT guid, file_guid, pillar_guid, checksum, checksum_state, file_state FROM fileinfo";
        ResultSet res = DatabaseUtils.selectObject(con, fileInfoSql, new Object[0]);
        
        while(res.next()) {
            System.out.println(res.getLong(1) + " : " + res.getLong(2) + " : " + res.getLong(3) + " : " 
                    + res.getString(4) + " : " + res.getInt(5) + " : " + res.getInt(6));
        }
        
    }
    
    /**
     * Cleans up the database after use.
     * @param url
     * @throws Exception
     */
    private void clearDatabase(String url) throws Exception {
        Connection con = DerbyDBConnector.getEmbeddedDBConnection(url);
        
        String sqlFI = "DELETE FROM fileinfo";
        DatabaseUtils.executeStatement(con, sqlFI, new Object[0]);
        String sqlFiles = "DELETE FROM files";
        DatabaseUtils.executeStatement(con, sqlFiles, new Object[0]);
        String sqlPillar = "DELETE FROM pillar";
        DatabaseUtils.executeStatement(con, sqlPillar, new Object[0]);
        String sqlCs = "DELETE FROM checksumspec";
        DatabaseUtils.executeStatement(con, sqlCs, new Object[0]);
    }
}
