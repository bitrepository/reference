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

import java.util.List;

import org.bitrepository.integrityservice.checking.MaxChecksumAgeProvider;
import org.bitrepository.integrityservice.checking.reports.IntegrityReportModel;
import org.bitrepository.integrityservice.checking.reports.ObsoleteChecksumReportModel;
import org.mockito.Matchers;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class FindObsoleteChecksumsStepTest extends WorkflowstepTest {
    @Test(groups = {"regressiontest", "integritytest"})
    public void testGoodCase() {
        addDescription("Test the step for finding obsolete checksum when the report is positive.");
        addStep("Run the step with a integritychecker which will return a clean ObsoleteChecksumReportModel",
                "No alerts should be generated");
        FindObsoleteChecksumsStep step = new FindObsoleteChecksumsStep(settings, checker, alerter, TEST_COLLECTION);

        when(checker.checkObsoleteChecksums(
                Matchers.<MaxChecksumAgeProvider>any(), Matchers.<List<String>>any(), eq(TEST_COLLECTION))).
                thenReturn(new ObsoleteChecksumReportModel(TEST_COLLECTION));
        step.performStep();
        verify(checker).checkObsoleteChecksums(
                Matchers.<MaxChecksumAgeProvider>any(), Matchers.<List<String>>any(), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(alerter, checker);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testBadCase() {
        addDescription("Test the step for finding obsolete checksum when the report contains obsplete.");
        addStep("Run the step with a integritychecker which will return a ObsoleteChecksumReportModel with integrity issues",
                "The IntegrityAlerter's integrityFailed method should be called with the ObsoleteChecksumReportModel");
        FindObsoleteChecksumsStep step = new FindObsoleteChecksumsStep(settings, checker, alerter, TEST_COLLECTION);

        final ObsoleteChecksumReportModel report = mock(ObsoleteChecksumReportModel.class);
        when(report.hasIntegrityIssues()).thenReturn(true);
        when(checker.checkObsoleteChecksums(
                Matchers.<MaxChecksumAgeProvider>any(), Matchers.<List<String>>any(), eq(TEST_COLLECTION))).
                thenReturn(report);
        step.performStep();
        verify(alerter).integrityFailed(any(IntegrityReportModel.class));
    }   
}
