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
package org.bitrepository.integrityservice.checking;

import java.util.Arrays;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntegrityReportTest extends ExtendedTestCase {
    private static final String pillarId = "TEST-PILLAR";
    private static final String goodFile = "GOOD-FILE";
    private static final String newFile = "NEW-FILE";
    private static final String missingFile = "MISSING-FILE";
    private static final String checksumErrorFile = "CHECKSUM-ERROR-FILE"; 
    private static final String checksumSpecIssueFile = "CHECKSUM-SPEC-ERROR-FILE";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testIntegrityReportFilesWithoutIssues() {
        addDescription("Test the functionality of the integrity report.");
        addStep("Setup variables", "No errors");
        IntegrityReport report = new IntegrityReport();
        addStep("Add some good case information.", "Should not have any integrity issues. Not any mention of them in the report.");
        report.addFileWithoutIssue(goodFile);
        report.addTooNewFile(newFile);
        Assert.assertTrue(report.getFilesWithoutIssues().contains(goodFile), "The report should contain the good file");
        Assert.assertTrue(report.getNewUncheckedFiles().contains(newFile));
        Assert.assertFalse(report.hasIntegrityIssues());
        Assert.assertFalse(report.generateReport().contains("Integrity issues found:"));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testIntegrityReportMissingFiles() {
        addDescription("Test the handling of missing files for the integrity report.");
        addStep("Test the handling of missing files.", "Should give the report integrity issues.");
        IntegrityReport report = new IntegrityReport();
        report.addMissingFile(missingFile, Arrays.asList(pillarId));
        Assert.assertEquals(report.getMissingFiles().size(), 1, "Should have one missing file");
        Assert.assertEquals(report.getMissingFiles().get(0).getFileId(), missingFile, report.getMissingFiles() + " should contain " + missingFile);
        Assert.assertTrue(report.hasIntegrityIssues(), "Should have integrity issues when a file is missing.");
        Assert.assertTrue(report.generateReport().contains("Integrity issues found:"));
    }
            
    @Test(groups = {"regressiontest", "integritytest"})
    public void testIntegrityReportChecksumErrors() {
        addDescription("Test the handling of files with checksum errors for the integrity report.");
        addStep("Test the handling of incorrect checksums", "Should give the report integrity issues.");
        IntegrityReport report = new IntegrityReport();
        report.addIncorrectChecksums(checksumErrorFile, Arrays.asList(pillarId));
        Assert.assertEquals(report.getChecksumErrors().size(), 1, "Should have one file with checksum errors");
        Assert.assertEquals(report.getChecksumErrors().get(0).getFileId(), checksumErrorFile, report.getChecksumErrors() + " should contain " + checksumErrorFile);
        Assert.assertTrue(report.hasIntegrityIssues(), "Should have integrity issues when a file has checksum errors.");
    }
        
    @Test(groups = {"regressiontest", "integritytest"})
    public void testIntegrityReportChecksumSpecIssues() {
        addDescription("Test the handling of files with checksum spec issues for the integrity report.");
        addStep("The the handling of checksum spec issues.", "Should give the report integrity issues.");
        IntegrityReport report = new IntegrityReport();
        report.addFileWithCheksumSpecIssues(checksumSpecIssueFile);
        Assert.assertEquals(report.getFilesWithChecksumSpecIssues().size(), 1, "Should have one file with checksum spec errors");
        Assert.assertEquals(report.getFilesWithChecksumSpecIssues().get(0), checksumSpecIssueFile, report.getFilesWithChecksumSpecIssues() + " should contain " + checksumSpecIssueFile);
        Assert.assertTrue(report.hasIntegrityIssues(), "Should have integrity issues when a file has checksum errors.");
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testIntegrityReportCombineReports() {
        addDescription("Test the combination of a good and a bad integrity report.");
        addStep("Check the combination between a report without issues and another with issues", "Should have issues.");
        IntegrityReport goodReport = new IntegrityReport();
        goodReport.addTooNewFile(newFile);
        goodReport.addFileWithoutIssue(goodFile);
        IntegrityReport badReport = new IntegrityReport();
        badReport.addFileWithCheksumSpecIssues(checksumSpecIssueFile);
        badReport.addIncorrectChecksums(checksumErrorFile, Arrays.asList(pillarId));
        badReport.addMissingFile(missingFile, Arrays.asList(pillarId));
        Assert.assertFalse(goodReport.hasIntegrityIssues());
        Assert.assertTrue(badReport.hasIntegrityIssues());
        goodReport.combineWithReport(badReport);
        Assert.assertTrue(goodReport.hasIntegrityIssues());
    }
}
