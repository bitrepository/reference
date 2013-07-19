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
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.IntegrityDatabaseTestCase;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityDatabase;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.bitrepository.integrityservice.cache.database.DerbyIntegrityDAO;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.reports.BasicIntegrityReporter;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.database.DBConnector;
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
    public void testNoData() {
        addDescription("Test the checksum integrity validator without any data in the cache.");
        IntegrityModel cache = getIntegrityModel();
        IntegrityReporter reporter = new BasicIntegrityReporter(TEST_COLLECTION, "test", new File("target/"));
        HandleChecksumValidationStep step = new HandleChecksumValidationStep(cache, auditManager, reporter);
        
        addStep("Validate the file ids", "Should not have integrity issues.");
        step.performStep();
        
        Assert.assertFalse(reporter.hasIntegrityIssues(), reporter.generateSummaryOfReport());
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testSimilarData() {
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
    public void testMissingAtOnePillar() {
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
    public void testTwoDisagreeingChecksums() {
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
    public void testThreeDisagreeingChecksums() {
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
    public void testUpdatingFileIDsForValidChecksum() {
        addDescription("Test the checksum integrity validator when two pillars agreee about the checksum, but the third does not.");
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
}
