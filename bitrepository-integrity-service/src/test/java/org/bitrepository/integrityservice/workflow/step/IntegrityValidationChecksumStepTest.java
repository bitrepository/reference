package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityservice.checking.reports.ChecksumReport;
import org.bitrepository.integrityservice.mocks.MockChecker;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntegrityValidationChecksumStepTest extends ExtendedTestCase {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String DEFAULT_CHECKSUM = "0123456789";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testGoodCase() {
        addDescription("Test the step for integrity validation of checksum when the report is positive.");
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();        
        MockChecker checker = new MockChecker();
        IntegrityValidationChecksumStep step = new IntegrityValidationChecksumStep(checker, alerter);
        
        step.performStep();
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 0);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 0);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testBadCase() {
        addDescription("Test the step for integrity validation of checksum when the report is negative.");
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();        
        MockChecker checker = new MockChecker() {
            @Override
            public ChecksumReport checkChecksum(FileIDs fileIDs) {
                ChecksumReport res = super.checkChecksum(fileIDs);
                res.reportChecksumError(TEST_FILE_1, TEST_PILLAR_1, DEFAULT_CHECKSUM);
                return res;
            }
        };
        IntegrityValidationChecksumStep step = new IntegrityValidationChecksumStep(checker, alerter);
        
        step.performStep();
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 1);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 0);
    }
}
