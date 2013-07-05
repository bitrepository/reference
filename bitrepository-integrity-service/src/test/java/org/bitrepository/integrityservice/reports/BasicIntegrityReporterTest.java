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

package org.bitrepository.integrityservice.reports;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class BasicIntegrityReporterTest extends ExtendedTestCase {
    private static final String REPORT_SUMMARY_START = "The following integrity issues where found:\n";

    @Test(groups = {"regressiontest"})
    public void hasIntegrityIssuesTest() {
        addDescription("Verifies that the hasIntegrityIssues() detects issues correctly");

        addStep("Report a missing file", "hasIntegrityIssues() should return true");
        BasicIntegrityReporter reporter = new BasicIntegrityReporter("CollectionWithIssues");
        reporter.reportMissingFile("TestFile", "Pillar1");
        assertTrue("Reporter didn't interpreted missing file as a integrity issue", reporter.hasIntegrityIssues());

        addStep("Report a corrupt file for a new Reporter", "hasIntegrityIssues() should return true");
        reporter = new BasicIntegrityReporter("CollectionWithIssues");
        reporter.reportChecksumIssue("TestFile", "Pillar1");
        assertTrue("Reporter didn't interpreted corrupt checksum as a integrity issue", reporter.hasIntegrityIssues());

        addStep("Report a missing checksum file for a new Reporter", "hasIntegrityIssues() should return true");
        reporter = new BasicIntegrityReporter("CollectionWithIssues");
        reporter.reportMissingChecksum("TestFile", "Pillar1");
        assertTrue("Reporter didn't interpreted missing checksum as a integrity issue", reporter.hasIntegrityIssues());

        addStep("Report a obsolete checksum file for a new Reporter", "hasIntegrityIssues() should return true");
        reporter = new BasicIntegrityReporter("CollectionWithIssues");
        reporter.reportObsoleteChecksum("TestFile", "Pillar1");
        assertTrue("Reporter didn't interpreted obsolete checksum as a integrity issue", reporter.hasIntegrityIssues());

        addStep("Report a delete file for a new Reporter", "hasIntegrityIssues() should return false");
        reporter = new BasicIntegrityReporter("CollectionWithIssues");
        reporter.reportDeletedFile("TestFile");
        assertFalse("Reporter interpreted delete file as a integrity issue", reporter.hasIntegrityIssues());
    }

    @Test(groups = {"regressiontest"})
    public void noIntegrityIssuesTest() {
        addDescription("Verifies that missing files are reported correctly");

        addStep("Create a clean reporter", "hasIntegrityIssues() should return false and the summary report should " +
                "state that no  inform of the missing file.");
        BasicIntegrityReporter reporter = new BasicIntegrityReporter("CollectionWithoutIssues");
        assertFalse("Reporter interpreted delete file as a integrity issue", reporter.hasIntegrityIssues());
        String expectedReport = "No integrity issues found";
        assertEquals("Reporter didn't create clean report", expectedReport, reporter.generateSummaryOfReport());
    }

    @Test(groups = {"regressiontest"})
    public void missingFilesTest() {
        addDescription("Verifies that missing files are reported correctly");

        addStep("Report a missing file", "hasIntegrityIssues() should return true and the summary report should " +
                "correctly inform of the missing file.");
        BasicIntegrityReporter reporter = new BasicIntegrityReporter("CollectionWithIssues");
        reporter.reportMissingFile("TestFile", "Pillar1");
        assertTrue("Reporter didn't interpreted missing file as a integrity issue", reporter.hasIntegrityIssues());
        String expectedReport = REPORT_SUMMARY_START + "Pillar1 is missing 1 file.";
        assertEquals("Wrong report returned on missing file", expectedReport, reporter.generateSummaryOfReport());

        addStep("Report another missing file on the same pillar", "The summary report should be update with the additional missing file.");
        reporter.reportMissingFile("TestFile2", "Pillar1");
        expectedReport = REPORT_SUMMARY_START + "Pillar1 is missing 2 files.";
        assertEquals("Wrong report returned on missing file", expectedReport, reporter.generateSummaryOfReport());

        addStep("Report a missing file on another pillar",
                "The summary report should be update with the new pillar problem.");
        reporter.reportMissingFile("TestFile3", "Pillar2");
        expectedReport = REPORT_SUMMARY_START + "Pillar1 is missing 2 files." + "\nPillar2 is missing 1 file.";
        assertEquals("Wrong report returned on missing file", expectedReport, reporter.generateSummaryOfReport());
    }
}
