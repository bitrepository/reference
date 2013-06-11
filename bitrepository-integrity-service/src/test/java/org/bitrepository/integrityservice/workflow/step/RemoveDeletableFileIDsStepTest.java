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

import org.bitrepository.integrityservice.checking.reports.MissingFileReportModel;
import org.bitrepository.service.audit.MockAuditManager;
import org.bitrepository.service.workflow.WorkflowStep;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class RemoveDeletableFileIDsStepTest extends WorkflowstepTest {
    
    @Test(groups = {"regressiontest"})
    public void testNoFilesToDelete() {
        addDescription("Testing the case, when no files should be deleted from the database.");
        MissingFileReportModel report = new MissingFileReportModel(TEST_COLLECTION);
        WorkflowStep step = new RemoveDeletableFileIDsFromDatabaseStep(model, report, auditManager, settings);
        verifyNoMoreInteractions(model);
        
        addStep("Perform the step of deleting file id entries based on the report.", 
                "No calls for deleting entries in the IntegrityModel.");
        step.performStep();
        verifyNoMoreInteractions(model);
    }
    
    @Test(groups = {"regressiontest"})
    public void testFilesToDelete() {
        addDescription("Testing the case, when one file should be deleted from the database.");
        String TEST_FILE_1 = "test-file-1";
        String TEST_FILE_2 = "test-file-2";
        MissingFileReportModel report = new MissingFileReportModel(TEST_COLLECTION);
        report.reportDeletableFile(TEST_FILE_1);
        report.reportDeletableFile(TEST_FILE_2);
        MockAuditManager auditManager = new MockAuditManager();
        WorkflowStep step = new RemoveDeletableFileIDsFromDatabaseStep(model, report, auditManager, settings);
        verifyNoMoreInteractions(model);
        
        addStep("Perform the step of deleting file id entries based on the report.", 
                "One calls for deleting the entry in the IntegrityModel.");
        step.performStep();
        verify(model).deleteFileIdEntry(TEST_FILE_1, TEST_COLLECTION);
        verify(model).deleteFileIdEntry(TEST_FILE_2, TEST_COLLECTION);
        verifyNoMoreInteractions(model, alerter);
    }
}
