package org.bitrepository.integrityservice.checking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.reports.ObsoleteChecksumReport;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
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
    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_1);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_2);
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(0L);
        auditManager = new MockAuditManager();
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoData() {
        addDescription("Test the obsolete checksum finder without any data in the cache.");
        IntegrityModel cache = getIntegrityModel();
        ObsoleteChecksumFinder finder = new ObsoleteChecksumFinder(cache);
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        ObsoleteChecksumReport report = finder.generateReport(DEFAULT_TIMEOUT);
        Assert.assertFalse(report.hasIntegrityIssues());
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testNewData() {
        addDescription("Test the obsolete checksum finder when the checksum of a file is new.");
        IntegrityModel cache = getIntegrityModel();
        ObsoleteChecksumFinder finder = new ObsoleteChecksumFinder(cache);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csData = createNewChecksumData("1234cccc4321", FILE_1);
        cache.addChecksums(csData, TEST_PILLAR_1);
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        ObsoleteChecksumReport report = finder.generateReport(DEFAULT_TIMEOUT);
        Assert.assertFalse(report.hasIntegrityIssues());
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testOldData() {
        addDescription("Test the obsolete checksum finder when the checksum of a file is very old.");
        IntegrityModel cache = getIntegrityModel();
        ObsoleteChecksumFinder finder = new ObsoleteChecksumFinder(cache);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csData = createOldChecksumData("1234cccc4321", FILE_1);
        cache.addChecksums(csData, TEST_PILLAR_1);
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        ObsoleteChecksumReport report = finder.generateReport(DEFAULT_TIMEOUT);
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
        cache.addChecksums(csOldData, TEST_PILLAR_1);
        List<ChecksumDataForChecksumSpecTYPE> csNewData = createNewChecksumData("1234cccc4321", FILE_1);
        cache.addChecksums(csNewData, TEST_PILLAR_2);
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        ObsoleteChecksumReport report = finder.generateReport(DEFAULT_TIMEOUT);
        Assert.assertTrue(report.hasIntegrityIssues());
        Assert.assertEquals(report.getObsoleteChecksum().size(), 1);
        Assert.assertNotNull(report.getObsoleteChecksum().get(FILE_1));
        Assert.assertEquals(report.getObsoleteChecksum().get(FILE_1).getPillarDates().size(), 1);
        Assert.assertTrue(report.getObsoleteChecksum().get(FILE_1).getPillarDates().containsKey(TEST_PILLAR_1));
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testMissingFile() {
        addDescription("Test the obsolete checksum finder for an old file, which has state 'MISSING'.");
        IntegrityModel cache = getIntegrityModel();
        ObsoleteChecksumFinder finder = new ObsoleteChecksumFinder(cache);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csData = createOldChecksumData("1234cccc4321", FILE_1);
        cache.addChecksums(csData, TEST_PILLAR_1);
        cache.setFileMissing(FILE_1, Arrays.asList(TEST_PILLAR_1));
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        ObsoleteChecksumReport report = finder.generateReport(DEFAULT_TIMEOUT);
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

    private IntegrityModel getIntegrityModel() {
        return new TestIntegrityModel(settings.getCollectionSettings().getClientSettings().getPillarIDs());
    }
}
