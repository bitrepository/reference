package org.bitrepository.integrityservice.checking.report;

import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.checking.reports.ObsoleteChecksumReport;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ObsoleteChecksumReportTest extends ExtendedTestCase {
    
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    
    public static final String TEST_FILE_1 = "test-file-1";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testEmptyObsoleteChecksumReport() {
        addDescription("Tests the empty obsolete checksum report.");
        ObsoleteChecksumReport report = new ObsoleteChecksumReport();
        Assert.assertFalse(report.hasIntegrityIssues(), report.generateReport());
        Assert.assertEquals(report.getObsoleteChecksum().size(), 0);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testObsoleteChecksum() {
        addDescription("Tests obsolete checksum report when the file is obsolete at the pillar.");
        ObsoleteChecksumReport report = new ObsoleteChecksumReport();
        report.reportMissingChecksum(TEST_FILE_1, TEST_PILLAR_1, CalendarUtils.getEpoch());
        
        Assert.assertTrue(report.hasIntegrityIssues(), report.generateReport());
        Assert.assertEquals(report.getObsoleteChecksum().size(), 1);
        Assert.assertTrue(report.getObsoleteChecksum().containsKey(TEST_FILE_1));
        Assert.assertEquals(report.getObsoleteChecksum().get(TEST_FILE_1).getPillarDates().size(), 1);
        Assert.assertTrue(report.getObsoleteChecksum().get(TEST_FILE_1).getPillarDates().containsKey(TEST_PILLAR_1));
    }    
}
