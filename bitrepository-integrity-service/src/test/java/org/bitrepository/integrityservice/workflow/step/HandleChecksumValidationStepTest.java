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

import static org.mockito.Mockito.mock;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.IntegrityDatabaseTestCase;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityDatabase;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.bitrepository.integrityservice.reports.BasicIntegrityReporter;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.service.audit.AuditTrailDatabaseResults;
import org.bitrepository.service.audit.AuditTrailManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Performs the validation of the integrity for the checksums.
 */
public class HandleChecksumValidationStepTest extends IntegrityDatabaseTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_PILLAR_2 = "test-pillar-2";
    public static final String TEST_PILLAR_3 = "test-pillar-3";
    
    public static final String FILE_1 = "test-file-1";
    private AuditTrailManager auditManager;
    public static final String FILE_2 = "test-file-2";
    
    String TEST_COLLECTION;
    
    @Override
    protected void customizeSettings() {
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(TEST_PILLAR_1);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(TEST_PILLAR_2);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(TEST_PILLAR_3);
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(0L);
        SettingsUtils.initialize(settings);
        TEST_COLLECTION = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        auditManager = mock(AuditTrailManager.class);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoData() throws Exception {
        addDescription("Test the checksum integrity validator without any data in the cache.");
        IntegrityModel cache = getIntegrityModel();
        IntegrityReporter reporter = new BasicIntegrityReporter(TEST_COLLECTION, "test", new File("target/"));
        HandleChecksumValidationStep step = new HandleChecksumValidationStep(cache, auditManager, reporter);
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        step.performStep();
        
        Assert.assertFalse(reporter.hasIntegrityIssues(), reporter.generateSummaryOfReport());
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testSimilarData() throws Exception {
        addDescription("Test the checksum integrity validator when all pillars have similar data.");
        IntegrityModel cache = getIntegrityModel();
        IntegrityReporter reporter = new BasicIntegrityReporter(TEST_COLLECTION, "test", new File("target/"));
        HandleChecksumValidationStep step = new HandleChecksumValidationStep(cache, auditManager, reporter);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csData = createChecksumData("1234cccc4321", FILE_1);
        insertChecksumDataForModel(cache, csData, TEST_PILLAR_1, TEST_COLLECTION);
        insertChecksumDataForModel(cache, csData, TEST_PILLAR_2, TEST_COLLECTION);
        insertChecksumDataForModel(cache, csData, TEST_PILLAR_3, TEST_COLLECTION);
        
        addStep("Perform the step", "");
        step.performStep();

        addStep("Validate the file ids", "Should not have integrity issues.");
        Assert.assertFalse(reporter.hasIntegrityIssues(), reporter.generateSummaryOfReport());
        for(FileInfo fi : cache.getFileInfos(FILE_1, TEST_COLLECTION)) {
            Assert.assertEquals(fi.getChecksum(), "1234cccc4321");
            Assert.assertEquals(fi.getChecksumState(), ChecksumState.VALID);
            Assert.assertEquals(fi.getFileState(), FileState.EXISTING);
        }
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testMissingAtOnePillar() throws Exception {
        addDescription("Test the checksum integrity validator when one pillar is missing the data.");
        IntegrityModel cache = getIntegrityModel();
        IntegrityReporter reporter = new BasicIntegrityReporter(TEST_COLLECTION, "test", new File("target/"));
        HandleChecksumValidationStep step = new HandleChecksumValidationStep(cache, auditManager, reporter);

        addStep("Update the cache with identitical data for both pillars.", "");
        List<ChecksumDataForChecksumSpecTYPE> csData = createChecksumData("1234cccc4321", FILE_1);
        insertChecksumDataForModel(cache, csData, TEST_PILLAR_1, TEST_COLLECTION);
        insertChecksumDataForModel(cache, csData, TEST_PILLAR_2, TEST_COLLECTION);
        
        addStep("Perform the step", "");
        step.performStep();

        addStep("Validate the file ids", "No integrity issues.");
        Assert.assertFalse(reporter.hasIntegrityIssues(), reporter.generateSummaryOfReport());
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testTwoDisagreeingChecksums() throws Exception {
        addDescription("Test the checksum integrity validator when only two pillar has data, but it it different.");
        IntegrityModel cache = getIntegrityModel();
        IntegrityReporter reporter = new BasicIntegrityReporter(TEST_COLLECTION, "test", new File("target/"));
        HandleChecksumValidationStep step = new HandleChecksumValidationStep(cache, auditManager, reporter);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csData1 = createChecksumData("1234cccc4321", FILE_1);
        insertChecksumDataForModel(cache, csData1, TEST_PILLAR_1, TEST_COLLECTION);
        List<ChecksumDataForChecksumSpecTYPE> csData2 = createChecksumData("1c2c3c44c3c2c1", FILE_1);
        insertChecksumDataForModel(cache, csData2, TEST_PILLAR_2, TEST_COLLECTION);
        
        addStep("Perform the step", "");
        step.performStep();

        addStep("Validate the file ids", "Should have integrity issues. No entry should be valid.");
        Assert.assertTrue(reporter.hasIntegrityIssues(), reporter.generateSummaryOfReport());
        for(FileInfo fi : cache.getFileInfos(FILE_1, TEST_COLLECTION)) {
            Assert.assertTrue(fi.getChecksumState() != ChecksumState.VALID);
        }
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testThreeDisagreeingChecksums() throws Exception {
        addDescription("Test the checksum integrity validator when all pillars have different checksums.");
        IntegrityModel cache = getIntegrityModel();
        IntegrityReporter reporter = new BasicIntegrityReporter(TEST_COLLECTION, "test", new File("target/"));
        HandleChecksumValidationStep step = new HandleChecksumValidationStep(cache, auditManager, reporter);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csData1 = createChecksumData("1234cccc4321", FILE_1);
        insertChecksumDataForModel(cache, csData1, TEST_PILLAR_1, TEST_COLLECTION);
        List<ChecksumDataForChecksumSpecTYPE> csData2 = createChecksumData("cccc12344321cccc", FILE_1);
        insertChecksumDataForModel(cache, csData2, TEST_PILLAR_2, TEST_COLLECTION);
        List<ChecksumDataForChecksumSpecTYPE> csData3 = createChecksumData("1c2c3c44c3c2c1", FILE_1);
        insertChecksumDataForModel(cache, csData3, TEST_PILLAR_3, TEST_COLLECTION);
        
        addStep("Perform the step", "");
        step.performStep();

        addStep("Validate the file ids", "Should have integrity issues.");
        Assert.assertTrue(reporter.hasIntegrityIssues(), reporter.generateSummaryOfReport());
        for(FileInfo fi : cache.getFileInfos(FILE_1, TEST_COLLECTION)) {
            Assert.assertTrue(fi.getChecksumState() == ChecksumState.ERROR);
        }
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testChecksumMajority() throws Exception {
        addDescription("Test the checksum integrity validator when two pillars have one checksum and the last pillar "
                + "has another checksum.");
        IntegrityModel cache = getIntegrityModel();
        IntegrityReporter reporter = new BasicIntegrityReporter(TEST_COLLECTION, "test", new File("target/"));
        HandleChecksumValidationStep step = new HandleChecksumValidationStep(cache, auditManager, reporter);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csData1 = createChecksumData("1234cccc4321", FILE_1);
        insertChecksumDataForModel(cache, csData1, TEST_PILLAR_1, TEST_COLLECTION);
        List<ChecksumDataForChecksumSpecTYPE> csData2 = createChecksumData("1234cccc4321", FILE_1);
        insertChecksumDataForModel(cache, csData2, TEST_PILLAR_2, TEST_COLLECTION);
        List<ChecksumDataForChecksumSpecTYPE> csData3 = createChecksumData("1c2c3c44c3c2c1", FILE_1);
        insertChecksumDataForModel(cache, csData3, TEST_PILLAR_3, TEST_COLLECTION);
        
        addStep("Perform the step", "");
        step.performStep();

        addStep("Validate the file ids", "Should only have integrity issues on pillar 3.");
        Assert.assertTrue(reporter.hasIntegrityIssues(), reporter.generateSummaryOfReport());
        for(FileInfo fi : cache.getFileInfos(FILE_1, TEST_COLLECTION)) {
            if(fi.getPillarId().equals(TEST_PILLAR_3)) {
                Assert.assertTrue(fi.getChecksumState() == ChecksumState.ERROR, fi.toString());
            } else {
                Assert.assertTrue(fi.getChecksumState() == ChecksumState.VALID, fi.toString());                
            }
        }
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testUpdatingFileIDsForValidChecksum() throws Exception {
        addDescription("Test that a file is set to having ChecksumState UNKNOWN, when it has a file-update.");
        IntegrityModel cache = getIntegrityModel();
        IntegrityReporter reporter = new BasicIntegrityReporter(TEST_COLLECTION, "test", new File("target/"));
        HandleChecksumValidationStep step = new HandleChecksumValidationStep(cache, auditManager, reporter);
        
        addStep("Add data to the cache", "");
        List<ChecksumDataForChecksumSpecTYPE> csData = createChecksumData("1234cccc4321", FILE_1);
        insertChecksumDataForModel(cache, csData, TEST_PILLAR_1, TEST_COLLECTION);
        insertChecksumDataForModel(cache, csData, TEST_PILLAR_2, TEST_COLLECTION);
        insertChecksumDataForModel(cache, csData, TEST_PILLAR_3, TEST_COLLECTION);
        
        addStep("Perform the step", "");
        step.performStep();

        addStep("Validate the file ids", "No integrity issues and all should be valid");
        Assert.assertFalse(reporter.hasIntegrityIssues(), reporter.generateSummaryOfReport());
        for(FileInfo fi : cache.getFileInfos(FILE_1, TEST_COLLECTION)) {
            Assert.assertEquals(fi.getChecksumState(), ChecksumState.VALID);
        }
        
        addStep("Add new fileids for one pillar", "The given pillar should have ChecksumState 'UNKNOWN', the others 'VALID'");
        FileIDsData fileidData = createFileIdData(FILE_1);
        cache.addFileIDs(fileidData, TEST_PILLAR_3, TEST_COLLECTION);
        
        for(FileInfo fi : cache.getFileInfos(FILE_1, TEST_COLLECTION)) {
            if(fi.getPillarId().equals(TEST_PILLAR_3)) {
                Assert.assertEquals(fi.getChecksumState(), ChecksumState.UNKNOWN);
            } else {
                Assert.assertEquals(fi.getChecksumState(), ChecksumState.VALID);
            }
        }
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testAuditTrailsForChecksumErrors() throws Exception {
        addDescription("Test audit trails for checksum errors. Verify that a pillar with a single checksum will"
                + " be pointed out as the possible cause.");
        IntegrityModel cache = getIntegrityModel();
        IntegrityReporter reporter = new BasicIntegrityReporter(TEST_COLLECTION, "test", new File("target/"));
        TestAuditTrailManager auditManager = new TestAuditTrailManager();
        HandleChecksumValidationStep step = new HandleChecksumValidationStep(cache, auditManager, reporter);
        
        addStep("Test step on data without checksum error", "No audit trails.");
        List<ChecksumDataForChecksumSpecTYPE> csData = createChecksumData("1234cccc4321", FILE_1);
        insertChecksumDataForModel(cache, csData, TEST_PILLAR_1, TEST_COLLECTION);
        insertChecksumDataForModel(cache, csData, TEST_PILLAR_2, TEST_COLLECTION);
        insertChecksumDataForModel(cache, csData, TEST_PILLAR_3, TEST_COLLECTION);
        step.performStep();
        Assert.assertNull(auditManager.latestAuditInfo);
        
        addStep("Test step on data where only two pillars have the file and they disagree about the checksum.",
                "An audit trail with fileId and collectionId, but no pillar pointed out as cause");
        insertChecksumDataForModel(cache, createChecksumData("1234cccc4321", FILE_2), TEST_PILLAR_1, TEST_COLLECTION);
        insertChecksumDataForModel(cache, createChecksumData("cc12344321cc", FILE_2), TEST_PILLAR_2, TEST_COLLECTION);
        step.performStep();
        Assert.assertNotNull(auditManager.latestAuditInfo);
        Assert.assertFalse(auditManager.latestAuditInfo.contains(TEST_PILLAR_1), auditManager.latestAuditInfo);
        Assert.assertFalse(auditManager.latestAuditInfo.contains(TEST_PILLAR_2), auditManager.latestAuditInfo);
        Assert.assertFalse(auditManager.latestAuditInfo.contains(TEST_PILLAR_3), auditManager.latestAuditInfo);
        Assert.assertTrue(auditManager.latestAuditInfo.contains(FILE_2), auditManager.latestAuditInfo);
        Assert.assertTrue(auditManager.latestAuditInfo.contains(TEST_COLLECTION), auditManager.latestAuditInfo);
        
        addStep("remove the last auditinfo", "");
        auditManager.latestAuditInfo = null;
        
        addStep("Test step on data where two pillars have one checksum and the last pillar has a different one",
                "An audit trail with fileId and collectionId, and the lone pillar is pointed out as possible cause");
        insertChecksumDataForModel(cache, createChecksumData("1234cccc4321", FILE_2), TEST_PILLAR_1, TEST_COLLECTION);
        insertChecksumDataForModel(cache, createChecksumData("cc12344321cc", FILE_2), TEST_PILLAR_2, TEST_COLLECTION);
        insertChecksumDataForModel(cache, createChecksumData("cc12344321cc", FILE_2), TEST_PILLAR_3, TEST_COLLECTION);
        step.performStep();
        Assert.assertNotNull(auditManager.latestAuditInfo);
        Assert.assertTrue(auditManager.latestAuditInfo.contains(TEST_PILLAR_1), auditManager.latestAuditInfo);
        Assert.assertFalse(auditManager.latestAuditInfo.contains(TEST_PILLAR_2), auditManager.latestAuditInfo);
        Assert.assertFalse(auditManager.latestAuditInfo.contains(TEST_PILLAR_3), auditManager.latestAuditInfo);
        Assert.assertTrue(auditManager.latestAuditInfo.contains(FILE_2), auditManager.latestAuditInfo);
        Assert.assertTrue(auditManager.latestAuditInfo.contains(TEST_COLLECTION), auditManager.latestAuditInfo);
    }
    
    private FileIDsData createFileIdData(String ... fileids) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        for(String fileid : fileids) {
            FileIDsDataItem item = new FileIDsDataItem();
            item.setFileID(fileid);
            item.setFileSize(BigInteger.ONE);
            item.setLastModificationTime(CalendarUtils.getNow());
            items.getFileIDsDataItem().add(item);
        }
        res.setFileIDsDataItems(items);
        return res;
    }
    
    private List<ChecksumDataForChecksumSpecTYPE> createChecksumData(String checksum, String ... fileids) {
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
        return new IntegrityDatabase(settings);
    }
    
    private class TestAuditTrailManager implements AuditTrailManager {
        String latestAuditInfo;

        @Override
        public void addAuditEvent(String collectionId, String fileId,
                String actor, String info, String auditTrail,
                FileAction operation, String operationID, String certificateID) {
            latestAuditInfo = info;
        }

        @Override
        public AuditTrailDatabaseResults getAudits(String collectionId,
                String fileId, Long minSeqNumber, Long maxSeqNumber,
                Date minDate, Date maxDate, Long maxNumberOfResults) {
            return null;
        }
    }
}
