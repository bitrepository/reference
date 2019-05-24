/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.integrityservice.cache;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.FileInfosData.FileInfosDataItems;
import org.bitrepository.bitrepositoryelements.FileInfosDataItem;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.IntegrityDatabaseTestCase;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.service.audit.AuditTrailManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IntegrityDatabaseTest extends IntegrityDatabaseTestCase {
    AuditTrailManager auditManager;
    String TEST_PILLAR_1 = "MY-TEST-PILLAR-1";
    String TEST_PILLAR_2 = "MY-TEST-PILLAR-2";
    
    String TEST_FILE_ID = "TEST-FILE-ID";
    String TEST_CHECKSUM = "1234cccc4321";
    
    String TEST_COLLECTIONID;
    
    @BeforeMethod (alwaysRun = true)
    @Override
    public void setup() throws Exception {
        super.setup();
        TEST_COLLECTIONID = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        auditManager = mock(AuditTrailManager.class);
    }
    
    @Override 
    protected void customizeSettings() {
        org.bitrepository.settings.repositorysettings.Collection c0 = 
                settings.getRepositorySettings().getCollections().getCollection().get(0);
        c0.getPillarIDs().getPillarID().clear();
        c0.getPillarIDs().getPillarID().add(TEST_PILLAR_1);
        c0.getPillarIDs().getPillarID().add(TEST_PILLAR_2);
        settings.getRepositorySettings().getCollections().getCollection().clear();
        settings.getRepositorySettings().getCollections().getCollection().add(c0);
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void instantiationTest() throws Exception {
        addDescription("Tests that the connection can be instantaited.");
        IntegrityDatabase integrityCache = new IntegrityDatabase(settings);
        Assert.assertNotNull(integrityCache);
    }

    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void initialStateExtractionTest() throws Exception {
        addDescription("Tests the initial state of the IntegrityModel. Should not contain any data.");
        IntegrityModel model = new IntegrityDatabase(settings);
        
        addStep("Test the 'findChecksumsOlderThan'", "Should deliver an empty collection");
        Collection<String> oldChecksums = getIssuesFromIterator(model.findChecksumsOlderThan(
            new Date(0), TEST_PILLAR_1, TEST_COLLECTIONID));
        Assert.assertNotNull(oldChecksums);
        Assert.assertEquals(oldChecksums.size(), 0);
        
        addStep("Test the 'findMissingChecksums'", "Should deliver an empty collection");
        for(String pillar : SettingsUtils.getPillarIDsForCollection(TEST_COLLECTIONID)) {
            Collection<String> missingChecksums 
                = getIssuesFromIterator(model.findFilesWithMissingChecksum(TEST_COLLECTIONID, pillar, new Date(0)));
            Assert.assertNotNull(missingChecksums);
            Assert.assertEquals(missingChecksums.size(), 0);    
        }
        
        addStep("Test the 'findMissingFiles'", "Should deliver an empty collection");
        Collection<String> missingFiles = getIssuesFromIterator(model.findFilesWithMissingCopies(TEST_COLLECTIONID, 
                SettingsUtils.getPillarIDsForCollection(TEST_COLLECTIONID).size(), 0L, Long.MAX_VALUE));
        Assert.assertNotNull(missingFiles);
        Assert.assertEquals(missingFiles.size(), 0);    

        addStep("Test the 'getAllFileIDs'", "Should deliver an empty collection");
        Assert.assertEquals(model.getNumberOfFilesInCollection(TEST_COLLECTIONID), 0);
        
        addStep("Test the 'getFileInfos'", "Should deliver an empty collection");
        Collection<FileInfo> fileInfos = model.getFileInfos(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fileInfos);
        Assert.assertEquals(fileInfos.size(), 0);
        
        addStep("Test the 'getPillarCollectionMetrics'", "The set of metrics should be empty.");
        Map<String, PillarCollectionMetric> metrics = model.getPillarCollectionMetrics(TEST_COLLECTIONID);
        Assert.assertTrue(metrics.isEmpty());
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testIngestOfFileInfosData() throws Exception {
        addDescription("Tests the ingesting of fileinfo data");
        IntegrityModel model = new IntegrityDatabase(settings);
        
        addStep("Create data", "Should be ingested into the database");
        List<FileInfosDataItem> fileInfoData = getFileInfoResults(TEST_FILE_ID, TEST_CHECKSUM);
        model.addFileInfos(fileInfoData, TEST_PILLAR_1, TEST_COLLECTIONID);
        model.addFileInfos(fileInfoData, TEST_PILLAR_2, TEST_COLLECTIONID);
        
        addStep("Extract the data", "Should be identical to the ingested data");
        Collection<FileInfo> fileinfos = model.getFileInfos(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fileinfos);
        for(FileInfo fi : fileinfos) {
            Assert.assertEquals(fi.getFileId(), TEST_FILE_ID);
            Assert.assertEquals(fi.getChecksum(), TEST_CHECKSUM);
            Assert.assertEquals(fi.getDateForLastChecksumCheck(), fileInfoData.get(0).getCalculationTimestamp());
        }
    }
    
    @Test(groups = {"regressiontest", "databasetest", "integritytest"})
    public void testDeletingEntry() throws Exception {
        addDescription("Tests the deletion of an FileID entry.");
        IntegrityModel model = new IntegrityDatabase(settings);

        addStep("Create data", "Should be ingested into the database");
        List<FileInfosDataItem> fileInfoData = getFileInfoResults(TEST_FILE_ID, TEST_CHECKSUM);
        model.addFileInfos(fileInfoData, TEST_PILLAR_1, TEST_COLLECTIONID);
        model.addFileInfos(fileInfoData, TEST_PILLAR_2, TEST_COLLECTIONID);
 
        Collection<FileInfo> fileinfos = model.getFileInfos(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 2);
        
        addStep("Delete the entry", "No fileinfos should be extracted.");
        model.deleteFileIdEntry(TEST_COLLECTIONID, TEST_PILLAR_1, TEST_FILE_ID);
        fileinfos = model.getFileInfos(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 1);
        model.deleteFileIdEntry(TEST_COLLECTIONID, TEST_PILLAR_2, TEST_FILE_ID);
        fileinfos = model.getFileInfos(TEST_FILE_ID, TEST_COLLECTIONID);
        Assert.assertNotNull(fileinfos);
        Assert.assertEquals(fileinfos.size(), 0);
    }

    private List<FileInfosDataItem> getFileInfoResults(String fileID, String checksum) {
        FileInfosDataItems fids = new FileInfosDataItems();
        FileInfosDataItem item = new FileInfosDataItem();
        item.setLastModificationTime(CalendarUtils.getNow());
        item.setCalculationTimestamp(CalendarUtils.getNow());
        item.setChecksumValue(Base16Utils.encodeBase16(checksum));
        item.setFileID(fileID);
        fids.getFileInfosDataItem().add(item);
        
        return fids.getFileInfosDataItem();
    }  
    
    /**
     * This is not the way to handle the iterators, as the lists might grow really long. 
     * It's here to make the tests simple, and can be done as there's only small amounts of test data in the tests. 
     */
    private List<String> getIssuesFromIterator(IntegrityIssueIterator it) {
        List<String> issues = new ArrayList<String>();
        String issue = null;
        while((issue = it.getNextIntegrityIssue()) != null) {
            issues.add(issue);
        }
        
        return issues;
    }
}
