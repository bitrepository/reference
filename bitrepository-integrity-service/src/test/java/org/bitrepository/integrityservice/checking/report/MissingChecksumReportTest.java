package org.bitrepository.integrityservice.checking.report;

import java.util.Arrays;

import org.bitrepository.integrityservice.checking.reports.MissingChecksumReport;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MissingChecksumReportTest extends ExtendedTestCase {
    
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    
    public static final String TEST_FILE_1 = "test-file-1";

    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testEmptyMissingChecksumReport() {
        addDescription("Tests the empty missing checksum report.");
        MissingChecksumReport report = new MissingChecksumReport();
        Assert.assertFalse(report.hasIntegrityIssues(), report.generateReport());
        Assert.assertEquals(report.getMissingChecksums().size(), 0);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testMissingChecksumReportAtPillar() {
        addDescription("Tests missing checksum report when checksum is missing at a pillar.");
        MissingChecksumReport report = new MissingChecksumReport();
        report.reportMissingChecksum(TEST_FILE_1, Arrays.asList(TEST_PILLAR_1));
        Assert.assertTrue(report.hasIntegrityIssues(), report.generateReport());
        Assert.assertEquals(report.getMissingChecksums().size(), 1);
        Assert.assertEquals(report.getMissingChecksums().get(0).getFileId(), TEST_FILE_1);
        Assert.assertEquals(report.getMissingChecksums().get(0).getPillarIds(), Arrays.asList(TEST_PILLAR_1));        
    }
}
