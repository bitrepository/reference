/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.integrityservice.checking.reports.ChecksumReportModel;
import org.bitrepository.integrityservice.mocks.MockChecker;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntegrityValidationChecksumStepTest extends ExtendedTestCase {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String DEFAULT_CHECKSUM = "0123456789";
    public static final String TEST_COLLECTION = "collection1";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testGoodCase() {
        addDescription("Test the step for integrity validation of checksum when the report is positive.");
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();        
        MockChecker checker = new MockChecker();
        IntegrityValidationChecksumStep step = new IntegrityValidationChecksumStep(checker, alerter, TEST_COLLECTION);
        
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
            public ChecksumReportModel checkChecksum(String collectionId) {
                ChecksumReportModel res = super.checkChecksum(collectionId);
                res.reportChecksumIssue(TEST_FILE_1, TEST_PILLAR_1, DEFAULT_CHECKSUM);
                return res;
            }
        };
        IntegrityValidationChecksumStep step = new IntegrityValidationChecksumStep(checker, alerter, TEST_COLLECTION);
        
        step.performStep();
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 1);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 0);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 0);
    }
}
