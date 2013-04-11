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

import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.checking.reports.ObsoleteChecksumReportModel;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ObsoleteChecksumReportTest extends ExtendedTestCase {
    
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_PILLAR_2 = "test-pillar-2";
    
    public static final String TEST_FILE_1 = "test-file-1";
    
    public static final String TEST_COLLECTION = "collection1";
    @Test(groups = {"regressiontest", "integritytest"})
    public void testEmptyObsoleteChecksumReport() {
        addDescription("Tests the empty obsolete checksum report.");
        ObsoleteChecksumReportModel report = new ObsoleteChecksumReportModel(TEST_COLLECTION);
        Assert.assertFalse(report.hasIntegrityIssues(), report.generateReport());
        Assert.assertEquals(report.getObsoleteChecksum().size(), 0);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testObsoleteChecksum() {
        addDescription("Tests obsolete checksum report when the file is obsolete at the pillar.");
        ObsoleteChecksumReportModel report = new ObsoleteChecksumReportModel(TEST_COLLECTION);
        report.reportObsoleteChecksum(TEST_FILE_1, TEST_PILLAR_1, CalendarUtils.getEpoch());
        
        Assert.assertTrue(report.hasIntegrityIssues(), report.generateReport());
        Assert.assertEquals(report.getObsoleteChecksum().size(), 1);
        Assert.assertTrue(report.getObsoleteChecksum().containsKey(TEST_FILE_1));
        Assert.assertEquals(report.getObsoleteChecksum().get(TEST_FILE_1).getPillarDates().size(), 1);
        Assert.assertTrue(report.getObsoleteChecksum().get(TEST_FILE_1).getPillarDates().containsKey(TEST_PILLAR_1));
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testTwoPillarsWithObsoleteChecksum() {
        addDescription("Tests obsolete checksum when two different pillars report the checksum to be obsolete.");
        ObsoleteChecksumReportModel report = new ObsoleteChecksumReportModel(TEST_COLLECTION);
        report.reportObsoleteChecksum(TEST_FILE_1, TEST_PILLAR_1, CalendarUtils.getEpoch());
        report.reportObsoleteChecksum(TEST_FILE_1, TEST_PILLAR_2, CalendarUtils.getNow());
        
        addStep("Validate the report.", "Contains only one file, but both pillars for the given file.");
        Assert.assertTrue(report.hasIntegrityIssues(), report.generateReport());
        Assert.assertEquals(report.getObsoleteChecksum().size(), 1);
        Assert.assertTrue(report.getObsoleteChecksum().containsKey(TEST_FILE_1));
        Assert.assertEquals(report.getObsoleteChecksum().get(TEST_FILE_1).getPillarDates().size(), 2);
        Assert.assertTrue(report.getObsoleteChecksum().get(TEST_FILE_1).getPillarDates().containsKey(TEST_PILLAR_1));
        Assert.assertTrue(report.getObsoleteChecksum().get(TEST_FILE_1).getPillarDates().containsKey(TEST_PILLAR_2));
    }   
}
