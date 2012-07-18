/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice.checking;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.reports.ChecksumReport;
import org.bitrepository.integrityservice.checking.reports.MissingChecksumReport;
import org.bitrepository.integrityservice.checking.reports.MissingFileReport;
import org.bitrepository.integrityservice.checking.reports.ObsoleteChecksumReport;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IntegrityCheckingTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    MockAuditManager auditManager;
    
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_PILLAR_2 = "test-pillar-2";
    
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String TEST_FILE_2 = "test-file-2";
    
    public static final Long DEFAULT_TIMEOUT = 60000L;

    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_1);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_2);
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(0L);
        auditManager = new MockAuditManager();
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testFileidsValid() {
        addDescription("Tests the file ids validation is able to give good result, when two pillars give the same "
                + "fileids results.");
        IntegrityModel cache = getIntegrityModel();
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
        
        addStep("Initialise the file ids data.", "Should be created and put into the cache.");
        FileIDsData fileidsData1 = createFileIdData(TEST_FILE_1);
        cache.addFileIDs(fileidsData1, createAllFileIDs(), TEST_PILLAR_1);
        cache.addFileIDs(fileidsData1, createAllFileIDs(), TEST_PILLAR_2);
        
        addStep("Check whether all pillars have all the file ids", "They should contain the same files.");
        MissingFileReport report = checker.checkFileIDs(createAllFileIDs());
        Assert.assertFalse(report.hasIntegrityIssues());
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testFileidsMissingAtOnePillar() {
        addDescription("Tests the file ids validation is able to give bad result, when only one pillars has delivered "
                + "fileids results.");
        IntegrityModel cache = getIntegrityModel();
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);

        addStep("Initialise the file ids data.", "Should be created and put into the cache.");
        FileIDsData fileidsData1 = createFileIdData(TEST_FILE_1);
        cache.addFileIDs(fileidsData1, createAllFileIDs(), TEST_PILLAR_1);
        
        addStep("Check whether all pillars have all the file ids", "Only one should contain the fileids, so it should return false");
        MissingFileReport report = checker.checkFileIDs(createAllFileIDs());
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertEquals(report.getMissingFiles().size(), 1);
        Assert.assertEquals(report.getDeleteableFiles().size(), 0);
        Assert.assertTrue(report.getMissingFiles().containsKey(TEST_FILE_1));
        Assert.assertEquals(report.getMissingFiles().get(TEST_FILE_1).size(), 1);
        Assert.assertEquals(report.getMissingFiles().get(TEST_FILE_1).get(0), TEST_PILLAR_2);
        
        Assert.assertEquals(cache.getFileInfos(TEST_FILE_1).size(), 2);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testDeletableFile() {
        addDescription("Tests the file validation, but with a file that does not exist anywhere.");
        IntegrityModel cache = getIntegrityModel();
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);

        addStep("Initialise the file ids data.", "Should be created and put into the cache.");
        FileIDsData fileidsData1 = createFileIdData(TEST_FILE_1);
        cache.addFileIDs(fileidsData1, createAllFileIDs(), TEST_PILLAR_1);
        cache.setFileMissing(TEST_FILE_1, Arrays.asList(TEST_PILLAR_1));
        
        addStep("Check whether all pillars have all the file ids", "Only one should contain the fileids, so it should return false");
        MissingFileReport report = checker.checkFileIDs(createAllFileIDs());
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertEquals(report.getMissingFiles().size(), 0);
        Assert.assertEquals(report.getDeleteableFiles().size(), 1);
        Assert.assertEquals(report.getDeleteableFiles().get(0), TEST_FILE_1);
        
        Assert.assertEquals(cache.getFileInfos(TEST_FILE_1).size(), 0);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testChecksumValid() {
        addDescription("Tests that the checksum validation is able to give good result, when two pillars give the same "
                + "checksum results.");
        IntegrityModel cache = getIntegrityModel();
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
       
        addStep("Initialise the checksum results data.", "Should be created and put into the cache.");
        List<ChecksumDataForChecksumSpecTYPE> checksumData = createChecksumData("1234cccc4321", TEST_FILE_1);
        
        // add the checksums for two pillars.
        cache.addChecksums(checksumData, TEST_PILLAR_1);
        cache.addChecksums(checksumData, TEST_PILLAR_2);
        
        addStep("Check the checksum status.", "Should not have issues.");
        ChecksumReport report = checker.checkChecksum(createAllFileIDs());
        Assert.assertFalse(report.hasIntegrityIssues());
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testChecksumInvalid() {
        addDescription("Tests that the checksum validation is able to give find a file with checksum error and "
                + "a file without.");
        IntegrityModel cache = getIntegrityModel();
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
       
        addStep("Initialise the checksum results data.", "Should be created and put into the cache.");
        List<ChecksumDataForChecksumSpecTYPE> checksumData = createChecksumData("1234cccc4321", TEST_FILE_1);
        cache.addChecksums(checksumData, TEST_PILLAR_2);
        cache.addChecksums(checksumData, TEST_PILLAR_2);
        
        List<ChecksumDataForChecksumSpecTYPE> checksumData1 = createChecksumData("1234567890", TEST_FILE_2);
        List<ChecksumDataForChecksumSpecTYPE> checksumData2 = createChecksumData("0987654321", TEST_FILE_2);
        cache.addChecksums(checksumData1, TEST_PILLAR_1);
        cache.addChecksums(checksumData2, TEST_PILLAR_2);
        
        addStep("Check the checksum status.", "Should have checksum issues.");
        ChecksumReport report = checker.checkChecksum(createAllFileIDs());
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertTrue(report.getFilesWithIssues().containsKey(TEST_FILE_2));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testLimitedChecksumValidation() {
        addDescription("Tests that is is possible to validate only one file. Starts by inserting common data for one "
                + "file for both pillars, and different checksum data for the other file. By limiting the validation "
                + "to only contain the good-case file, then no checksum issues should be found.");
        IntegrityModel cache = getIntegrityModel();
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
       
        FileIDs singleFileID = new FileIDs();
        singleFileID.setFileID(TEST_FILE_1);
        
        addStep("Initialise the checksum results data.", "Should be created and put into the cache.");
        List<ChecksumDataForChecksumSpecTYPE> checksumData = createChecksumData("1234cccc4321", TEST_FILE_1);
        cache.addChecksums(checksumData, TEST_PILLAR_2);
        cache.addChecksums(checksumData, TEST_PILLAR_2);
        
        List<ChecksumDataForChecksumSpecTYPE> checksumData1 = createChecksumData("1234567890", TEST_FILE_2);
        List<ChecksumDataForChecksumSpecTYPE> checksumData2 = createChecksumData("0987654321", TEST_FILE_2);
        cache.addChecksums(checksumData1, TEST_PILLAR_1);
        cache.addChecksums(checksumData2, TEST_PILLAR_2);
        
        addStep("Check the checksum status.", "Should not find the issue.");
        ChecksumReport report = checker.checkChecksum(singleFileID);
        Assert.assertFalse(report.hasIntegrityIssues());
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoMissingChecksums() {
        addDescription("Test that adding checksums data will result in no missing checksums, and both are valid.");
        IntegrityModel cache = getIntegrityModel();
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
       
        addStep("Initialise the data cache", "Should be created and put into the cache.");
        List<ChecksumDataForChecksumSpecTYPE> checksumData = createChecksumData("1234cccc4321", TEST_FILE_1);
        cache.addChecksums(checksumData, TEST_PILLAR_1);
        cache.addChecksums(checksumData, TEST_PILLAR_2);
        cache.setChecksumAgreement(TEST_FILE_1, Arrays.asList(TEST_PILLAR_1, TEST_PILLAR_2));
        
        addStep("Check the checksum status.", "Should not find the issue.");
        MissingChecksumReport report = checker.checkMissingChecksums();
        Assert.assertFalse(report.hasIntegrityIssues(), report.generateReport());
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testMissingChecksums() {
        addDescription("Test that adding only file ids data will result in missing checksums.");
        IntegrityModel cache = getIntegrityModel();
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
       
        addStep("Initialise the data cache", "Should be created and put into the cache.");
        FileIDsData fileidsData = createFileIdData(TEST_FILE_1);
        cache.addFileIDs(fileidsData, createAllFileIDs(), TEST_PILLAR_1);
        cache.addFileIDs(fileidsData, createAllFileIDs(), TEST_PILLAR_2);
        
        addStep("Check the checksum status.", "Should not find the issue.");
        MissingChecksumReport report = checker.checkMissingChecksums();
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertEquals(report.getMissingChecksums().size(), 1);
        Assert.assertEquals(report.getMissingChecksums().get(0).getFileId(), TEST_FILE_1);
        Assert.assertEquals(report.getMissingChecksums().get(0).getPillarIds(), Arrays.asList(TEST_PILLAR_1, TEST_PILLAR_2));
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testChecksumsMissingAtOnePillar() {
        addDescription("Test that it is possible to find a single pillar with missing checksum.");
        IntegrityModel cache = getIntegrityModel();
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
       
        addStep("Initialise the data cache", "Should be created and put into the cache.");
        FileIDsData fileidsData = createFileIdData(TEST_FILE_1, TEST_FILE_2);
        cache.addFileIDs(fileidsData, createAllFileIDs(), TEST_PILLAR_1);
        cache.addFileIDs(fileidsData, createAllFileIDs(), TEST_PILLAR_2);
        List<ChecksumDataForChecksumSpecTYPE> checksumData1 = createChecksumData("1234cccc4321", TEST_FILE_1);
        cache.addChecksums(checksumData1, TEST_PILLAR_1);
        cache.addChecksums(checksumData1, TEST_PILLAR_2);
        cache.setChecksumAgreement(TEST_FILE_1, Arrays.asList(TEST_PILLAR_1, TEST_PILLAR_2));
        List<ChecksumDataForChecksumSpecTYPE> checksumData2 = createChecksumData("1234cccc4321", TEST_FILE_2);
        cache.addChecksums(checksumData2, TEST_PILLAR_1);
        cache.setChecksumAgreement(TEST_FILE_2, Arrays.asList(TEST_PILLAR_1));
        
        addStep("Check the checksum status.", "Should be checksum missing at pillar 2.");
        MissingChecksumReport report = checker.checkMissingChecksums();
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertEquals(report.getMissingChecksums().size(), 1);
        Assert.assertEquals(report.getMissingChecksums().get(0).getFileId(), TEST_FILE_2);
        Assert.assertEquals(report.getMissingChecksums().get(0).getPillarIds(), Arrays.asList(TEST_PILLAR_2));
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoObsoleteChecksums() {
        addDescription("Test that new checksums are not obsolete.");
        IntegrityModel cache = getIntegrityModel();
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
       
        addStep("Initialise the data cache", "Should be created and put into the cache.");
        List<ChecksumDataForChecksumSpecTYPE> checksumData1 = createChecksumData("1234cccc4321", TEST_FILE_1);
        cache.addChecksums(checksumData1, TEST_PILLAR_1);
        cache.addChecksums(checksumData1, TEST_PILLAR_2);
        
        addStep("Check the checksum status.", "Should be checksum missing at pillar 2.");
        ObsoleteChecksumReport report = checker.checkObsoleteChecksums(DEFAULT_TIMEOUT);
        Assert.assertFalse(report.hasIntegrityIssues());
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testFindObsoleteChecksums() {
        addDescription("Test that new checksums are not obsolete.");
        IntegrityModel cache = getIntegrityModel();
        SimpleIntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
       
        addStep("Initialise the data cache", "Should be created and put into the cache.");
        List<ChecksumDataForChecksumSpecTYPE> checksumData = createChecksumData("1234cccc4321", TEST_FILE_1);
        cache.addChecksums(checksumData, TEST_PILLAR_1);
        checksumData.get(0).setCalculationTimestamp(CalendarUtils.getEpoch());
        cache.addChecksums(checksumData, TEST_PILLAR_2);
        
        addStep("Check the checksum status.", "Should be checksum missing at pillar 2.");
        ObsoleteChecksumReport report = checker.checkObsoleteChecksums(DEFAULT_TIMEOUT);
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertEquals(report.getObsoleteChecksum().size(), 1);
        Assert.assertNotNull(report.getObsoleteChecksum().get(TEST_FILE_1));
        Assert.assertEquals(report.getObsoleteChecksum().get(TEST_FILE_1).getPillarDates().size(), 1);
        Assert.assertTrue(report.getObsoleteChecksum().get(TEST_FILE_1).getPillarDates().containsKey(TEST_PILLAR_2));
    }
    
    private FileIDs createAllFileIDs() {
        FileIDs allFileIDs = new FileIDs();
        allFileIDs.setAllFileIDs("true");
        return allFileIDs;
    }

    private FileIDsData createFileIdData(String ... fileids) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        for(String fileid : fileids) {
            FileIDsDataItem item = new FileIDsDataItem();
            item.setFileID(fileid);
            item.setFileSize(BigInteger.ONE);
            item.setLastModificationTime(CalendarUtils.getNow());
            items.getFileIDsDataItem().add(item);
        }
        res.setFileIDsDataItems(items);
        return res;
    }
    
    private List<ChecksumDataForChecksumSpecTYPE> createChecksumData(String checksum, String ... fileids) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        for(String fileId : fileids) {
            ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
            csData.setCalculationTimestamp(CalendarUtils.getNow());
            csData.setChecksumValue(Base16Utils.encodeBase16(checksum));
            csData.setFileID(fileId);
            res.add(csData);
        }
        return res;
    }
    
    private IntegrityModel getIntegrityModel() {
        return new TestIntegrityModel(settings.getCollectionSettings().getClientSettings().getPillarIDs());
    }
}
