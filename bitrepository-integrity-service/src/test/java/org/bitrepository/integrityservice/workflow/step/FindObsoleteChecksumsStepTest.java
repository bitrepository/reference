package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.checking.reports.ObsoleteChecksumReport;
import org.bitrepository.integrityservice.mocks.MockChecker;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FindObsoleteChecksumsStepTest extends ExtendedTestCase {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testGoodCase() {
        addDescription("Test the step for finding obsolete checksum when the report is positive.");
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();        
        MockChecker checker = new MockChecker();
        FindObsoleteChecksumsStep step = new FindObsoleteChecksumsStep(checker, alerter, Long.MAX_VALUE);
        
        step.performStep();
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 0);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 1);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testBadCase() {
        addDescription("Test the step for finding obsolete checksum when the report is negative.");
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();        
        MockChecker checker = new MockChecker() {
            @Override
            public ObsoleteChecksumReport checkObsoleteChecksums(long outdatedInterval) {
                ObsoleteChecksumReport res = super.checkObsoleteChecksums(outdatedInterval);
                res.reportMissingChecksum(TEST_FILE_1, TEST_PILLAR_1, CalendarUtils.getEpoch());
                return res;
            }
        };
        FindObsoleteChecksumsStep step = new FindObsoleteChecksumsStep(checker, alerter, Long.MAX_VALUE);
        
        step.performStep();
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 1);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 1);
    }   
}
