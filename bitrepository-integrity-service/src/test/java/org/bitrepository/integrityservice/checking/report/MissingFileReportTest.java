package org.bitrepository.integrityservice.checking.report;

import java.util.Arrays;

import org.bitrepository.integrityservice.checking.reports.MissingFileReport;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MissingFileReportTest extends ExtendedTestCase {
    
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    
    public static final String TEST_FILE_1 = "test-file-1";

    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testEmptyMissingFileReport() {
        addDescription("Tests the empty missing file report.");
        MissingFileReport report = new MissingFileReport();
        Assert.assertFalse(report.hasIntegrityIssues(), report.generateReport());
        Assert.assertEquals(report.getDeleteableFiles().size(), 0);
        Assert.assertEquals(report.getMissingFiles().size(), 0);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testMissingFile() {
        addDescription("Tests missing file report when the file is missing at the pillar.");
        MissingFileReport report = new MissingFileReport();
        report.reportMissingFile(TEST_FILE_1, Arrays.asList(TEST_PILLAR_1));
        
        Assert.assertTrue(report.hasIntegrityIssues(), report.generateReport());
        Assert.assertEquals(report.getDeleteableFiles().size(), 0);
        Assert.assertEquals(report.getMissingFiles().size(), 1);
        Assert.assertTrue(report.getMissingFiles().containsKey(TEST_FILE_1));
        Assert.assertEquals(report.getMissingFiles().get(TEST_FILE_1), Arrays.asList(TEST_PILLAR_1));
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testDeletableFile() {
        addDescription("Tests missing file report when the file is marked as deletable.");
        MissingFileReport report = new MissingFileReport();
        report.reportDeletableFile(TEST_FILE_1);
        
        Assert.assertTrue(report.hasIntegrityIssues(), report.generateReport());
        Assert.assertEquals(report.getDeleteableFiles().size(), 1);
        Assert.assertEquals(report.getDeleteableFiles(), Arrays.asList(TEST_FILE_1));
        Assert.assertEquals(report.getMissingFiles().size(), 0);
    }
}
