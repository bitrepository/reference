package org.bitrepository.integrityservice.workflow.step;

import java.util.Arrays;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityservice.checking.reports.MissingFileReport;
import org.bitrepository.integrityservice.mocks.MockChecker;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntegrityValidationFileIDsStepTest extends ExtendedTestCase {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testGoodCase() {
        addDescription("Test the step for integrity validation of fileids when the report is positive.");
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();        
        MockChecker checker = new MockChecker();
        IntegrityValidationFileIDsStep step = new IntegrityValidationFileIDsStep(checker, alerter);
        
        step.performStep();
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 0);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 1);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 0);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testBadCase() {
        addDescription("Test the step for integrity validation of file ids when the report is negative.");
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();        
        MockChecker checker = new MockChecker() {
            @Override
            public MissingFileReport checkFileIDs(FileIDs fileIDs) {
                MissingFileReport res = super.checkFileIDs(fileIDs);
                res.reportMissingFile(TEST_FILE_1, Arrays.asList(TEST_PILLAR_1));
                return res;
            }
        };
        IntegrityValidationFileIDsStep step = new IntegrityValidationFileIDsStep(checker, alerter);
        
        step.performStep();
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 1);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 1);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 0);
    }  
}
