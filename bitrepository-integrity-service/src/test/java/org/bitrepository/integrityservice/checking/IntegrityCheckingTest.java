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

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class IntegrityCheckingTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    MockAuditManager auditManager;
    
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_PILLAR_2 = "test-pillar-2";
    
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
        addStep("Setup the environment for this test.", "Should define the pillars and fileids, and the clear the cache.");
        
        IntegrityModel cache = getIntegrityModel();
        String[] fileids = new String[]{"test-file-1", "test-file-2"};
        FileIDs allFileIDs = new FileIDs();
        allFileIDs.setAllFileIDs("true");
        
        addStep("Initialise the file ids data.", "Should be created and put into the cache.");
        FileIDsData fileidsData1 = new FileIDsData();
        FileIDsDataItems items1 = new FileIDsDataItems();

        for(String fileid : fileids) {
            FileIDsDataItem item = new FileIDsDataItem();
            item.setFileID(fileid);
            item.setLastModificationTime(CalendarUtils.getNow());
            items1.getFileIDsDataItem().add(item);
        }
        fileidsData1.setFileIDsDataItems(items1);
        
        // add the files for two pillars.
        cache.addFileIDs(fileidsData1, allFileIDs, TEST_PILLAR_1);
        cache.addFileIDs(fileidsData1, allFileIDs, TEST_PILLAR_2);
        
        addStep("Instantiate the IntegrityChecker and the file ids to validate", "Should validate all the files.");
        IntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
        
        FileIDs fileidsToCheck = new FileIDs();
        fileidsToCheck.setAllFileIDs("true");
        
        addStep("Check whether all pillars have all the file ids", "They should contain the same files.");
        Assert.assertFalse(checker.checkFileIDs(fileidsToCheck).hasIntegrityIssues(), "The file ids should be validated");
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testFileidsMissingAtOnePillar() {
        addDescription("Tests the file ids validation is able to give bad result, when only one pillars has delivered "
                + "fileids results.");
        addStep("Setup the environment for this test.", "Should define the pillars and fileids, and the clear the cache.");

        IntegrityModel cache = getIntegrityModel();
        String[] fileids = new String[]{"test-file-1", "test-file-2"};
        FileIDs allFileIDs = new FileIDs();
        allFileIDs.setAllFileIDs("true");

        addStep("Initialise the file ids data.", "Should be created and put into the cache.");
        FileIDsData fileidsData1 = new FileIDsData();
        FileIDsDataItems items1 = new FileIDsDataItems();

        for(String fileid : fileids) {
            FileIDsDataItem item = new FileIDsDataItem();
            item.setFileID(fileid);
            item.setLastModificationTime(CalendarUtils.getNow());
            items1.getFileIDsDataItem().add(item);
        }
        fileidsData1.setFileIDsDataItems(items1);
        
        // add the files for two pillars.
        cache.addFileIDs(fileidsData1, allFileIDs, TEST_PILLAR_1);
        
        addStep("Instantiate the IntegrityChecker and the file ids to validate", "Should validate all the files.");
        IntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
        
        FileIDs fileidsToCheck = new FileIDs();
        fileidsToCheck.setAllFileIDs("true");
        
        addStep("Check whether all pillars have all the file ids", "Only one should contain the fileids, so it should return false");
        Assert.assertTrue(checker.checkFileIDs(fileidsToCheck).hasIntegrityIssues(), "The file ids should be validated");
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testFileidsMissingButTooNew() {
        addDescription("Tests the file ids validation is able to give positive result, when the file is too new, "
                + "even though one pillars has not delivered fileids results.");
        addStep("Setup the environment for this test.", "Should define the pillars and fileids, and the clear the cache.");
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(3600000L);
        
        IntegrityModel cache = getIntegrityModel();
        String[] fileids = new String[]{"test-file-1", "test-file-2"};
        FileIDs allFileIDs = new FileIDs();
        allFileIDs.setAllFileIDs("true");

        addStep("Initialise the file ids data.", "Should be created and put into the cache.");
        FileIDsData fileidsData1 = new FileIDsData();
        FileIDsDataItems items1 = new FileIDsDataItems();

        for(String fileid : fileids) {
            FileIDsDataItem item = new FileIDsDataItem();
            item.setFileID(fileid);
            item.setLastModificationTime(CalendarUtils.getNow());
            items1.getFileIDsDataItem().add(item);
        }
        fileidsData1.setFileIDsDataItems(items1);
        
        // add the files for two pillars.
        cache.addFileIDs(fileidsData1, allFileIDs, TEST_PILLAR_1);
        
        addStep("Instantiate the IntegrityChecker and the file ids to validate", "Should validate all the files.");
        IntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
        
        FileIDs fileidsToCheck = new FileIDs();
        fileidsToCheck.setAllFileIDs("true");
        
        addStep("Check whether all pillars have all the file ids", "Only one should contain the fileids, so it should return false");
        Assert.assertFalse(checker.checkFileIDs(fileidsToCheck).hasIntegrityIssues(), "The file ids should be validated");
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testChecksumValid() {
        addDescription("Tests that the checksum validation is able to give good result, when two pillars give the same "
                + "checksum results.");
        addStep("Setup the environment for this test.", "Should define the pillars and fileids, and the clear the cache.");
        
        IntegrityModel cache = getIntegrityModel();
        String[] fileids = new String[]{"test-file-1", "test-file-2"};
       
        addStep("Initialise the checksum results data.", "Should be created and put into the cache.");
        ChecksumSpecTYPE checksumtype = new ChecksumSpecTYPE();
        checksumtype.setChecksumSalt(null);
        checksumtype.setChecksumType(ChecksumType.MD5);
        
        List<ChecksumDataForChecksumSpecTYPE> checksumData = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        for(String fileid : fileids) {
            ChecksumDataForChecksumSpecTYPE checksumCalculation = new ChecksumDataForChecksumSpecTYPE();
            checksumCalculation.setFileID(fileid);
            checksumCalculation.setCalculationTimestamp(CalendarUtils.getNow());
            checksumCalculation.setChecksumValue(new String("123" + fileid + "123").getBytes());
            checksumData.add(checksumCalculation);
        }
        
        // add the checksums for two pillars.
        cache.addChecksums(checksumData, checksumtype, TEST_PILLAR_1);
        cache.addChecksums(checksumData, checksumtype, TEST_PILLAR_2);
        
        addStep("Instantiate the IntegrityChecker and the file ids to validate", "Should validate all the files.");
        IntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
        
        FileIDs fileidsToCheck = new FileIDs();
        fileidsToCheck.setAllFileIDs("true");
        
        addStep("Check whether all pillars have all the file ids", "They should contain the same files.");
        Assert.assertFalse(checker.checkFileIDs(fileidsToCheck).hasIntegrityIssues(), "The file ids should be validated.");
        
        addStep("Check that the checksum for these file ids are the same", "They should be.");
        Assert.assertFalse(checker.checkChecksum(fileidsToCheck).hasIntegrityIssues(), "The checksums should be validated");
    }
    
/*    @Test(groups = {"regressiontest"})
    public void testChecksumsMissingFromOnePillar() {
        addDescription("Tests that the checksum validation is able to give good result, even though not all pillars have the requested files.");
        addStep("Setup the environment for this test.", "Should define the pillars and fileids, and the clear the cache.");
        
        IntegrityModel cache = getIntegrityModel();
        String[] fileids = new String[]{"test-file-1", "test-file-2"};
       
        addStep("Initialise the checksum results data.", "Should be created and put into the cache.");
        ChecksumSpecTYPE checksumtype = new ChecksumSpecTYPE();
        checksumtype.setChecksumSalt(null);
        checksumtype.setChecksumType(ChecksumType.MD5);

        List<ChecksumDataForChecksumSpecTYPE> checksumData = new ArrayList<ChecksumDataForChecksumSpecTYPE>();        
        for(String fileid : fileids) {
            ChecksumDataForChecksumSpecTYPE checksumCalculation = new ChecksumDataForChecksumSpecTYPE();
            checksumCalculation.setFileID(fileid);
            checksumCalculation.setCalculationTimestamp(CalendarUtils.getNow());
            checksumCalculation.setChecksumValue(new String("123" + fileid + "123").getBytes());
            checksumData.add(checksumCalculation);
        }
        
        // add the checksums for two pillars.
        cache.addChecksums(checksumData, checksumtype, TEST_PILLAR_1);
        
        addStep("Instantiate the IntegrityChecker and the file ids to validate", "Should validate all the files.");
        IntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
        
        FileIDs fileidsToCheck = new FileIDs();
        fileidsToCheck.setAllFileIDs("true");
        
        addStep("Check whether all pillars have all the file ids", "Only one pillar should contain the file ids.");
        Assert.assertTrue(checker.checkFileIDs(fileidsToCheck).hasIntegrityIssues(), "The file ids should not be valid");
        
        addStep("Check that the checksum for these file ids are valid", "They should be, since only pillar has the checksums.");
        Assert.assertFalse(checker.checkChecksum(fileidsToCheck).hasIntegrityIssues(), "The checksums should be valid");
    }*/
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testChecksumsDifferForOneFile() {
        addDescription("Tests that the checksum validation is able to give a negative result, when two pillars give "
                + "different checksum results.");
        addStep("Setup the environment for this test.", "Should define the pillars and fileids, and the clear the cache.");
        
        IntegrityModel cache = getIntegrityModel();
        String[] fileids = new String[]{"test-file-1", "test-file-2"};
       
        addStep("Initialise the two different checksum results data.", "Should be created and put into the cache for each pillar.");
        ChecksumSpecTYPE checksumtype = new ChecksumSpecTYPE();
        checksumtype.setChecksumSalt(null);
        checksumtype.setChecksumType(ChecksumType.MD5);
        List<ChecksumDataForChecksumSpecTYPE> checksumData1 = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        for(String fileid : fileids) {
            ChecksumDataForChecksumSpecTYPE checksumCalculation = new ChecksumDataForChecksumSpecTYPE();
            checksumCalculation.setFileID(fileid);
            checksumCalculation.setCalculationTimestamp(CalendarUtils.getNow());
            checksumCalculation.setChecksumValue(new String("123" + fileid + "123").getBytes());
            checksumData1.add(checksumCalculation);
        }
        
        List<ChecksumDataForChecksumSpecTYPE> checksumData2 = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        for(String fileid : fileids) {
            ChecksumDataForChecksumSpecTYPE checksumCalculation = new ChecksumDataForChecksumSpecTYPE();
            checksumCalculation.setFileID(fileid);
            checksumCalculation.setCalculationTimestamp(CalendarUtils.getNow());
            checksumCalculation.setChecksumValue(new String("abc" + fileid + "abc").getBytes());
            checksumData2.add(checksumCalculation);
        }
        
        // add the checksums for two pillars.
        cache.addChecksums(checksumData1, checksumtype, TEST_PILLAR_1);
        cache.addChecksums(checksumData2, checksumtype, TEST_PILLAR_2);
        
        addStep("Instantiate the IntegrityChecker and the file ids to validate", "Should validate all the files.");
        IntegrityChecker checker = new SimpleIntegrityChecker(settings, cache, auditManager);
        
        FileIDs fileidsToCheck = new FileIDs();
        fileidsToCheck.setAllFileIDs("true");
        
        addStep("Check whether all pillars have all the file ids", "They should contain the same files.");
        Assert.assertFalse(checker.checkFileIDs(fileidsToCheck).hasIntegrityIssues(), "The file ids should be validated");
        
        addStep("Check whether the checksum for these file ids are the same", "They should NOT be.");
        Assert.assertTrue(checker.checkChecksum(fileidsToCheck).hasIntegrityIssues(), "The checksums should not be validated");
    }
    
    private IntegrityModel getIntegrityModel() {
        return new TestIntegrityModel();
    }
}
