package org.bitrepository.integrityservice.workflow.step;

import java.util.Arrays;

import org.bitrepository.integrityservice.checking.reports.MissingChecksumReport;
import org.bitrepository.integrityservice.mocks.MockChecker;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FindMissingChecksumsTest extends ExtendedTestCase {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testGoodCase() {
        addDescription("Test the step for finding missing checksum when the report is positive.");
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();        
        MockChecker checker = new MockChecker();
        FindMissingChecksumsStep step = new FindMissingChecksumsStep(checker, alerter);
        
        step.performStep();
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 0);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 0);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testBadCase() {
        addDescription("Test the step for finding missing checksum when the report is negative.");
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();        
        MockChecker checker = new MockChecker() {
            @Override
            public MissingChecksumReport checkMissingChecksums() {
                MissingChecksumReport res = super.checkMissingChecksums();
                res.reportMissingChecksum(TEST_FILE_1, Arrays.asList(TEST_PILLAR_1));
                return res;
            }
        };
        FindMissingChecksumsStep step = new FindMissingChecksumsStep(checker, alerter);
        
        step.performStep();
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 1);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 0);
    }
}
