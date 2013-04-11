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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.reports.ObsoleteChecksumReportModel;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
import org.bitrepository.settings.referencesettings.ObsoleteChecksumSettings;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ObsoleteChecksumsFinderTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    MockAuditManager auditManager;
    
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_PILLAR_2 = "test-pillar-2";
    
    public static final String FILE_1 = "test-file-1";
    
    public static final Long DEFAULT_TIMEOUT = 60000L;
    
    String TEST_COLLECTIONID;
    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(TEST_PILLAR_1);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(TEST_PILLAR_2);
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(0L);
        TEST_COLLECTIONID = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        auditManager = new MockAuditManager();
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoData() {
        addDescription("Test the obsolete checksum finder without any data in the cache.");
        IntegrityModel cache = getIntegrityModel();
        ObsoleteChecksumFinder finder = new ObsoleteChecksumFinder(cache);
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        ObsoleteChecksumReportModel report = finder.generateReport(
            new MaxChecksumAgeProvider(DEFAULT_TIMEOUT, null), Arrays.asList(TEST_PILLAR_1, TEST_PILLAR_2), TEST_COLLECTIONID);
        Assert.assertFalse(report.hasIntegrityIssues());
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testNewData() {
        addDescription("Test the obsolete checksum finder when the checksum of a file is new.");
        IntegrityModel cache = getIntegrityModel();
        ObsoleteChecksumFinder finder = new ObsoleteChecksumFinder(cache);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csData = createNewChecksumData("1234cccc4321", FILE_1);
        cache.addChecksums(csData, TEST_PILLAR_1, TEST_COLLECTIONID);
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        ObsoleteChecksumReportModel report = finder.generateReport(
            new MaxChecksumAgeProvider(DEFAULT_TIMEOUT, null), Arrays.asList(TEST_PILLAR_1, TEST_PILLAR_2), TEST_COLLECTIONID);
        Assert.assertFalse(report.hasIntegrityIssues());
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testOldData() {
        addDescription("Test the obsolete checksum finder when the checksum of a file is very old.");
        IntegrityModel cache = getIntegrityModel();
        ObsoleteChecksumFinder finder = new ObsoleteChecksumFinder(cache);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csData = createOldChecksumData("1234cccc4321", FILE_1);
        cache.addChecksums(csData, TEST_PILLAR_1, TEST_COLLECTIONID);
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        ObsoleteChecksumReportModel report = finder.generateReport(
            new MaxChecksumAgeProvider(DEFAULT_TIMEOUT, null), Arrays.asList(TEST_PILLAR_1, TEST_PILLAR_2), TEST_COLLECTIONID);
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertEquals(report.getObsoleteChecksum().size(), 1);
        Assert.assertNotNull(report.getObsoleteChecksum().get(FILE_1));
        Assert.assertEquals(report.getObsoleteChecksum().get(FILE_1).getPillarDates().size(), 1);
        Assert.assertTrue(report.getObsoleteChecksum().get(FILE_1).getPillarDates().containsKey(TEST_PILLAR_1));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testOldOnOnePillarData() {
        addDescription("Test the obsolete checksum finder when the checksum of a file is old on one pillar but new on "
                + "the other pillar.");
        IntegrityModel cache = getIntegrityModel();
        ObsoleteChecksumFinder finder = new ObsoleteChecksumFinder(cache);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csOldData = createOldChecksumData("1234cccc4321", FILE_1);
        cache.addChecksums(csOldData, TEST_PILLAR_1, TEST_COLLECTIONID);
        List<ChecksumDataForChecksumSpecTYPE> csNewData = createNewChecksumData("1234cccc4321", FILE_1);
        cache.addChecksums(csNewData, TEST_PILLAR_2, TEST_COLLECTIONID);
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        ObsoleteChecksumReportModel report = finder.generateReport(
            new MaxChecksumAgeProvider(DEFAULT_TIMEOUT, null), Arrays.asList(TEST_PILLAR_1, TEST_PILLAR_2), TEST_COLLECTIONID);
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertEquals(report.getObsoleteChecksum().size(), 1);
        Assert.assertNotNull(report.getObsoleteChecksum().get(FILE_1));
        Assert.assertEquals(report.getObsoleteChecksum().get(FILE_1).getPillarDates().size(), 1);
        Assert.assertTrue(report.getObsoleteChecksum().get(FILE_1).getPillarDates().containsKey(TEST_PILLAR_1));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testDifferentPillarMaxAges() {
        addDescription("Verifies that the the obsolete checksum can handle different max ages for different pillars.");
        addReference("<a href=https://sbforge.org/jira/browse/BITMAG-628>" +
            "BITMAG-628 Introduce pr. pillar maxChecksumAge in the integrity service</a>");

        IntegrityModel cache = getIntegrityModel();
        ObsoleteChecksumFinder finder = new ObsoleteChecksumFinder(cache);

        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csYearData = createWeekOldChecksumData("1234cccc4321", FILE_1);
        cache.addChecksums(csYearData, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.addChecksums(csYearData, TEST_PILLAR_2, TEST_COLLECTIONID);
        ObsoleteChecksumSettings obsoleteChecksumSettings = new ObsoleteChecksumSettings();
        obsoleteChecksumSettings.getMaxChecksumAgeForPillar().add(MaxChecksumAgeProvider.createMaxChecksumAgeForPillar(
            TEST_PILLAR_1, TimeUtils.MS_PER_YEAR*2));

        addStep("Validate the file ids", "Should not have integrity issues.");
        ObsoleteChecksumReportModel report = finder.generateReport( new MaxChecksumAgeProvider(
                DEFAULT_TIMEOUT, obsoleteChecksumSettings), Arrays.asList(TEST_PILLAR_1, TEST_PILLAR_2), TEST_COLLECTIONID);
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertEquals(report.getObsoleteChecksum().size(), 1);
        Assert.assertNotNull(report.getObsoleteChecksum().get(FILE_1));
        Assert.assertEquals(report.getObsoleteChecksum().get(FILE_1).getPillarDates().size(), 1);
        Assert.assertTrue(report.getObsoleteChecksum().get(FILE_1).getPillarDates().containsKey(TEST_PILLAR_2));
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testMissingFile() {
        addDescription("Test the obsolete checksum finder for an old file, which has state 'MISSING'.");
        IntegrityModel cache = getIntegrityModel();
        ObsoleteChecksumFinder finder = new ObsoleteChecksumFinder(cache);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csData = createOldChecksumData("1234cccc4321", FILE_1);
        cache.addChecksums(csData, TEST_PILLAR_1, TEST_COLLECTIONID);
        cache.setFileMissing(FILE_1, Arrays.asList(TEST_PILLAR_1), TEST_COLLECTIONID);
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        ObsoleteChecksumReportModel report = finder.generateReport(
            new MaxChecksumAgeProvider(DEFAULT_TIMEOUT, null), Arrays.asList(TEST_PILLAR_1, TEST_PILLAR_2), TEST_COLLECTIONID);
        Assert.assertFalse(report.hasIntegrityIssues());
    }
    
    private List<ChecksumDataForChecksumSpecTYPE> createOldChecksumData(String checksum, String ... fileids) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        for(String fileId : fileids) {
            ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
            csData.setCalculationTimestamp(CalendarUtils.getEpoch());
            csData.setChecksumValue(Base16Utils.encodeBase16(checksum));
            csData.setFileID(fileId);
            res.add(csData);
        }
        return res;
    }

    private List<ChecksumDataForChecksumSpecTYPE> createNewChecksumData(String checksum, String ... fileids) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        for(String fileId : fileids) {
            ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
            csData.setCalculationTimestamp(CalendarUtils.getNow());
            csData.setChecksumValue(Base16Utils.encodeBase16(checksum));
            csData.setFileID(fileId);
            res.add(csData);
        }
        return res;
    }

    private List<ChecksumDataForChecksumSpecTYPE> createWeekOldChecksumData(String checksum, String ... fileids) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        for(String fileId : fileids) {
            ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
            XMLGregorianCalendar lastYear = CalendarUtils.getNow();
            lastYear.setYear(CalendarUtils.getNow().getYear() - 1);
            csData.setCalculationTimestamp(lastYear);
            csData.setChecksumValue(Base16Utils.encodeBase16(checksum));
            csData.setFileID(fileId);
            res.add(csData);
        }
        return res;
    }

    private IntegrityModel getIntegrityModel() {
        return new TestIntegrityModel(settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID());
    }
}
