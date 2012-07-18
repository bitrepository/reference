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
package org.bitrepository.integrityservice.checking.report;

import java.util.Arrays;

import org.bitrepository.integrityservice.checking.reports.ChecksumReport;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ChecksumReportTest extends ExtendedTestCase {
    
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_PILLAR_2 = "test-pillar-2";
    public static final String TEST_PILLAR_3 = "test-pillar-3";
    
    public static final String TEST_FILE_1 = "test-file-1";

    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testEmptyChecksumReport() {
        addDescription("Tests the empty checksum report.");
        ChecksumReport report = new ChecksumReport();
        Assert.assertFalse(report.hasIntegrityIssues(), report.generateReport());
        Assert.assertEquals(report.getFilesWithIssues().size(), 0);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testChecksumReportWithoutChecksumAgreement() {
        addDescription("Tests the checksum report when there is not checksum agreement.");
        ChecksumReport report = new ChecksumReport();
        
        report.reportChecksumError(TEST_FILE_1, TEST_PILLAR_1, "checksum1");
        report.reportChecksumError(TEST_FILE_1, TEST_PILLAR_2, "checksum2");
        report.reportChecksumError(TEST_FILE_1, TEST_PILLAR_3, "checksum3");
        
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertEquals(report.getFilesWithIssues().size(), 1);
        Assert.assertTrue(report.getFilesWithIssues().containsKey(TEST_FILE_1));
        Assert.assertNull(report.getFilesWithIssues().get(TEST_FILE_1).getChecksum());
        Assert.assertEquals(report.getFilesWithIssues().get(TEST_FILE_1).getAgreeingPillars().size(), 0);
        Assert.assertEquals(report.getFilesWithIssues().get(TEST_FILE_1).getFileId(), TEST_FILE_1);
        Assert.assertEquals(report.getFilesWithIssues().get(TEST_FILE_1).getDisagreeingPillars().size(), 3);
        
        Assert.assertTrue(report.generateReport().contains(TEST_FILE_1), report.generateReport());
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testChecksumReportForAgreement() {
        addDescription("Tests the checksum report when two pillars agree and one disagree.");
        ChecksumReport report = new ChecksumReport();
        
        String correctChecksum = "correctChecksum";
        
        report.reportChecksumAgreement(TEST_FILE_1, Arrays.asList(TEST_PILLAR_2, TEST_PILLAR_3), correctChecksum);
        report.reportChecksumError(TEST_FILE_1, TEST_PILLAR_1, "wrongChecksum");
        
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertEquals(report.getFilesWithIssues().size(), 1);
        Assert.assertTrue(report.getFilesWithIssues().containsKey(TEST_FILE_1));
        Assert.assertEquals(report.getFilesWithIssues().get(TEST_FILE_1).getChecksum(), correctChecksum);
        Assert.assertEquals(report.getFilesWithIssues().get(TEST_FILE_1).getAgreeingPillars().size(), 2);
        Assert.assertEquals(report.getFilesWithIssues().get(TEST_FILE_1).getAgreeingPillars(), Arrays.asList(TEST_PILLAR_2, TEST_PILLAR_3));
        Assert.assertEquals(report.getFilesWithIssues().get(TEST_FILE_1).getFileId(), TEST_FILE_1);
        Assert.assertEquals(report.getFilesWithIssues().get(TEST_FILE_1).getDisagreeingPillars().size(), 1);
        Assert.assertTrue(report.getFilesWithIssues().get(TEST_FILE_1).getDisagreeingPillars().containsKey(TEST_PILLAR_1));
        
        Assert.assertTrue(report.generateReport().contains(TEST_FILE_1));
    }
}
