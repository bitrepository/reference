package org.bitrepository.integrityservice.checking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ChecksumValidatorTest extends ExtendedTestCase {
    
    private static final String TEST_FILE = "test-file";
    private static final String PILLAR_1 = "pillar-1";
    private static final String PILLAR_2 = "pillar-2";
    private static final String PILLAR_3 = "pillar-3";
    private static final String CHECKSUM = "CHECKSUM";
    private static final String BAD_CHECKSUM = "ERROR-CHECKSUM";
    
    
    @Test(groups = {"regressiontest"})
    public void testChecksumValidatorGoodCase() {
        addDescription("Test the checksum validator is able to validate a good case scenario.");
        addStep("Setup the variables, caches, and checksum data", "No errors");
        IntegrityModel cache = new TestIntegrityModel();
        MockIntegrityModel mockCache = new MockIntegrityModel(cache);

        ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
        csData.setFileID(TEST_FILE);
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setChecksumValue(CHECKSUM.getBytes());
        List<ChecksumDataForChecksumSpecTYPE> csDataList = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        csDataList.add(csData);
        
        addStep("Populate the cache with two identical checksums.", "No errors.");
        ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
        csType.setChecksumType(ChecksumType.MD5);
        cache.addChecksums(csDataList, csType, PILLAR_1);
        cache.addChecksums(csDataList, csType, PILLAR_2);
        
        addStep("Perform the checksum validation", "Should find the checksum spec issue.");
        ChecksumValidator validator = new ChecksumValidator(mockCache, TEST_FILE);
        IntegrityReport report = validator.validateChecksum();
        
        Assert.assertFalse(report.hasIntegrityIssues(), "Should not have any integrity issues.");
        Assert.assertEquals(report.getFilesWithoutIssues().size(), 1, "Should be one file without issues");
        Assert.assertEquals(report.getFilesWithoutIssues().get(0), TEST_FILE);

        addStep("Validate the calls for the cache", "Should be valid");
        Assert.assertEquals(mockCache.getCallsForGetFileInfos(), 1, "Should only be one call for GetFileInfos");
        Assert.assertEquals(mockCache.getCallsForSetChecksumAgreement(), 1, "Should be one call for agreement for the given file");
    }
    
    @Test(groups = {"regressiontest"})
    public void testChecksumValidatorChecksumErrorWithoutWinner() {
        addDescription("Test the checksum validator is able to validate a bad case scenario with 3 pillars all disagreeing.");
        addStep("Setup the variables, caches, and checksum data", "The different checksums should be the different pillar ids.");
        IntegrityModel cache = new TestIntegrityModel();
        MockIntegrityModel mockCache = new MockIntegrityModel(cache);

        ChecksumDataForChecksumSpecTYPE csData1 = new ChecksumDataForChecksumSpecTYPE();
        csData1.setFileID(TEST_FILE);
        csData1.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData1.setChecksumValue(PILLAR_1.getBytes());
        List<ChecksumDataForChecksumSpecTYPE> csDataList1 = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        csDataList1.add(csData1);

        ChecksumDataForChecksumSpecTYPE csData2 = new ChecksumDataForChecksumSpecTYPE();
        csData2.setFileID(TEST_FILE);
        csData2.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData2.setChecksumValue(PILLAR_2.getBytes());
        List<ChecksumDataForChecksumSpecTYPE> csDataList2 = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        csDataList2.add(csData2);

        ChecksumDataForChecksumSpecTYPE csData3 = new ChecksumDataForChecksumSpecTYPE();
        csData3.setFileID(TEST_FILE);
        csData3.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData3.setChecksumValue(PILLAR_3.getBytes());
        List<ChecksumDataForChecksumSpecTYPE> csDataList3= new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        csDataList3.add(csData3);

        addStep("Populate the cache with the three different checksums.", "No errors.");
        ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
        csType.setChecksumType(ChecksumType.MD5);
        cache.addChecksums(csDataList1, csType, PILLAR_1);
        cache.addChecksums(csDataList2, csType, PILLAR_2);
        cache.addChecksums(csDataList3, csType, PILLAR_3);
        
        addStep("Perform the checksum validation", "Should find the checksum errors.");
        ChecksumValidator validator = new ChecksumValidator(mockCache, TEST_FILE);
        IntegrityReport report = validator.validateChecksum();
        
        Assert.assertTrue(report.hasIntegrityIssues(), "Should have integrity issues when checksum errors.");
        Assert.assertEquals(report.getChecksumErrors().size(), 1, "Should be one file with checksum error");
        Assert.assertEquals(report.getChecksumErrors().get(0).getFileId(), TEST_FILE);
        
        addStep("Validate the calls for the cache", "Should be valid");
        Assert.assertEquals(mockCache.getCallsForGetFileInfos(), 1, "Should only be one call for GetFileInfos");
        Assert.assertEquals(mockCache.getCallsForSetChecksumError(), 1, "Should be one call for SetChecksumError");
        Assert.assertEquals(mockCache.getCallsForSetChecksumAgreement(), 0, "Should not have any checksum aggrements");
    }

    @Test(groups = {"regressiontest"})
    public void testChecksumValidatorChecksumErrorWithAWinner() {
        addDescription("Test the checksum validator is able to validate a bad case scenario with 1 pillar disagreeing with 2 pillars.");
        addStep("Setup the variables, caches, and checksum data. The bad", "No errors");
        IntegrityModel cache = new TestIntegrityModel();
        MockIntegrityModel mockCache = new MockIntegrityModel(cache);

        ChecksumDataForChecksumSpecTYPE badCsData = new ChecksumDataForChecksumSpecTYPE();
        badCsData.setFileID(TEST_FILE);
        badCsData.setCalculationTimestamp(CalendarUtils.getEpoch());
        badCsData.setChecksumValue(BAD_CHECKSUM.getBytes());
        List<ChecksumDataForChecksumSpecTYPE> badCsDataList = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        badCsDataList.add(badCsData);

        ChecksumDataForChecksumSpecTYPE goodCsData = new ChecksumDataForChecksumSpecTYPE();
        goodCsData.setFileID(TEST_FILE);
        goodCsData.setCalculationTimestamp(CalendarUtils.getEpoch());
        goodCsData.setChecksumValue(CHECKSUM.getBytes());
        List<ChecksumDataForChecksumSpecTYPE> goodCsDataList = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        goodCsDataList.add(goodCsData);

        addStep("Populate the cache with the different checksums. Pillar one will have be bad checksum.", "No errors.");
        ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
        csType.setChecksumType(ChecksumType.MD5);
        cache.addChecksums(badCsDataList, csType, PILLAR_1);
        cache.addChecksums(goodCsDataList, csType, PILLAR_2);
        cache.addChecksums(goodCsDataList, csType, PILLAR_3);
        
        addStep("Perform the checksum validation", "Should find the checksum errors.");
        ChecksumValidator validator = new ChecksumValidator(mockCache, TEST_FILE);
        IntegrityReport report = validator.validateChecksum();
        
        Assert.assertTrue(report.hasIntegrityIssues(), "Should have integrity issues when checksum errors.");
        Assert.assertEquals(report.getChecksumErrors().size(), 1, "Should be one file with checksum error");
        Assert.assertEquals(report.getChecksumErrors().get(0).getFileId(), TEST_FILE);
        
        addStep("Validate the calls for the cache", "Should be valid");
        Assert.assertEquals(mockCache.getCallsForGetFileInfos(), 1, "Should only be one call for GetFileInfos");
        Assert.assertEquals(mockCache.getCallsForSetChecksumError(), 1, "Should be one call for SetChecksumError");
        Assert.assertEquals(mockCache.getCallsForSetChecksumAgreement(), 1, "Should have a call for checksum agrement");
    }

    @Test(groups = {"regressiontest"})
    public void testChecksumValidatorChecksumSpecIssue() {
        addDescription("Test the checksum validator is able to find a checksum spec issues.");
        addStep("Setup the variables, caches, and checksum data", "No errors");
        IntegrityModel cache = new TestIntegrityModel();
        MockIntegrityModel mockCache = new MockIntegrityModel(cache);

        ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
        csData.setFileID(TEST_FILE);
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setChecksumValue(CHECKSUM.getBytes());
        List<ChecksumDataForChecksumSpecTYPE> csDataList = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        csDataList.add(csData);
        
        addStep("Populate the cache with two different checksums.", "No errors.");
        ChecksumSpecTYPE csType1 = new ChecksumSpecTYPE();
        csType1.setChecksumType(ChecksumType.MD5);
        cache.addChecksums(csDataList, csType1, PILLAR_1);
        ChecksumSpecTYPE csType2 = new ChecksumSpecTYPE();
        csType2.setChecksumType(ChecksumType.SHA1);
        cache.addChecksums(csDataList, csType2, PILLAR_2);

        addStep("Perform the checksum validation", "Should find the checksum spec issue.");
        ChecksumValidator validator = new ChecksumValidator(mockCache, TEST_FILE);
        IntegrityReport report = validator.validateChecksum();
        
        Assert.assertTrue(report.hasIntegrityIssues(), "Should have integrity issues when checksum spec differs.");
        Assert.assertEquals(report.getFilesWithChecksumSpecIssues().size(), 1, "Should be one file with checksum spec issues");
        Assert.assertEquals(report.getFilesWithChecksumSpecIssues().get(0), TEST_FILE);

        addStep("Validate the calls for the cache", "Should be valid");
        Assert.assertEquals(mockCache.getCallsForGetFileInfos(), 1, "Should only be one call for GetFileInfos");
    }
    
}
