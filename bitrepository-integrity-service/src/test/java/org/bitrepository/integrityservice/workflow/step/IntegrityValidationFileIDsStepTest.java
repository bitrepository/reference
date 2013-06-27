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

import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.integrityservice.checking.reports.OldMissingFileReportModel;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class IntegrityValidationFileIDsStepTest extends WorkflowstepTest {
    private IntegrityValidationFileIDsStep step;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        super.setup();
        step = new IntegrityValidationFileIDsStep(checker, alerter, TEST_COLLECTION);
    }

    @Test(groups = {"regressiontest"})
    public void testGoodCase() {
        addDescription("Test the step for integrity validation of fileids when the report is positive.");
        addStep("Run the step with a integritychecker which will return a clean MissingFileReportModel",
                "No alerts should be generated");
        when(checker.checkFileIDs(FileIDsUtils.getAllFileIDs(), TEST_COLLECTION)).thenReturn(new OldMissingFileReportModel(TEST_COLLECTION));
        step.performStep();

        verify(checker).checkFileIDs(FileIDsUtils.getAllFileIDs(), TEST_COLLECTION);
        verifyNoMoreInteractions(alerter, checker);
    }

    @Test(groups = {"regressiontest"})
    public void testBadCase() {
        addDescription("Test the step for integrity validation of file ids when the report is negative.");
        addStep("Run the step with a integritychecker which will return a MissingFileReportModel with integrity issues",
                "The IntegrityAlerters integrityFailed method should be called with the MissingFileReportModel");
        final OldMissingFileReportModel report = mock(OldMissingFileReportModel.class);
        when(checker.checkFileIDs(FileIDsUtils.getAllFileIDs(), TEST_COLLECTION)).thenReturn(report);
        when(report.hasIntegrityIssues()).thenReturn(true);
        step.performStep();
        verify(alerter).integrityFailed(report);
        verifyNoMoreInteractions(alerter);
    }  
}
