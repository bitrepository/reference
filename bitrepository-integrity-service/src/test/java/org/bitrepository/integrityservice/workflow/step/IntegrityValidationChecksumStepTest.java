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
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class IntegrityValidationChecksumStepTest extends WorkflowstepTest {
    @Test(groups = {"regressiontest", "integritytest"})
    public void testGoodCase() {
        addDescription("Test the step for integrity validation of checksum when the report is positive.");
        IntegrityValidationChecksumStep step = new IntegrityValidationChecksumStep(checker, alerter, TEST_COLLECTION);

        addStep("Run the step with a integritychecker which will return a clean ChecksumReportModel",
                "No alerts should be generated");
        when(checker.checkChecksum(TEST_COLLECTION)).thenReturn(new ChecksumReportModel(TEST_COLLECTION));
        step.performStep();
        verify(checker).checkChecksum(TEST_COLLECTION);
        verifyNoMoreInteractions(alerter, checker);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testBadCase() {
        addDescription("Test the step for integrity validation of checksum when the report is negative.");
        IntegrityValidationChecksumStep step = new IntegrityValidationChecksumStep(checker, alerter, TEST_COLLECTION);

        addStep("Run the step with a integritychecker which will return a ChecksumReportModel with integrity issues",
                "The IntegrityAlerters integrityFailed method should be called with the ChecksumReportModel");
        ChecksumReportModel report = mock(ChecksumReportModel.class);
        when(report.hasIntegrityIssues()).thenReturn(true);
        when(checker.checkChecksum(TEST_COLLECTION)).thenReturn(report);
        step.performStep();

        verify(alerter).integrityFailed(report);
        verifyNoMoreInteractions(alerter);
    }
}
