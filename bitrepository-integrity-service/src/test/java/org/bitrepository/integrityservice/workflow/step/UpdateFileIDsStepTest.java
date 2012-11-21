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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.mocks.MockCollector;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UpdateFileIDsStepTest extends ExtendedTestCase {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final List<String> PILLAR_IDS = Arrays.asList(TEST_PILLAR_1);
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String DEFAULT_CHECKSUM = "0123456789";
    
    protected Settings settings;
    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().addAll(PILLAR_IDS);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testPositiveReply() {
        addDescription("Test the step for updating the file ids can handle COMPLETE operation event.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getFileIDs(Collection<String> pillarIDs, String auditTrailInformation, ContributorQuery[] queries,
                    EventHandler eventHandler) {
                super.getFileIDs(pillarIDs, auditTrailInformation, queries, eventHandler);
                eventHandler.handleEvent(new CompleteEvent(null));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateFileIDsStep step = new UpdateFileIDsStep(collector, store, alerter, settings);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddFileIDs(), 0);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 1);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testNegativeReply() {
        addDescription("Test the step for updating the file ids can handle FAILED operation event.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getFileIDs(Collection<String> pillarIDs, String auditTrailInformation, ContributorQuery[] queries,
                    EventHandler eventHandler) {
                super.getFileIDs(pillarIDs, auditTrailInformation, queries, eventHandler);
                eventHandler.handleEvent(new OperationFailedEvent("Operation failed", null));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateFileIDsStep step = new UpdateFileIDsStep(collector, store, alerter, settings);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddFileIDs(), 0);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 1);
        Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 1);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testIngestOfResults() {
        addDescription("Test the step for updating the file ids can ingest the data correctly into the store.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getFileIDs(Collection<String> pillarIDs, String auditTrailInformation, ContributorQuery[] queries,
                    EventHandler eventHandler) {
                super.getFileIDs(pillarIDs, auditTrailInformation, queries, eventHandler);
                eventHandler.handleEvent(new IdentificationCompleteEvent(Arrays.asList(TEST_PILLAR_1)));
                FileIDsCompletePillarEvent event = new FileIDsCompletePillarEvent(
                        TEST_PILLAR_1, createResultingFileIDs(TEST_FILE_1), false);
                eventHandler.handleEvent(event);
                
                eventHandler.handleEvent(new CompleteEvent(null));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateFileIDsStep step = new UpdateFileIDsStep(collector, store, alerter, settings);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddFileIDs(), 1);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 1);
    }

    private ResultingFileIDs createResultingFileIDs(String ... fileIds) {
        ResultingFileIDs res = new ResultingFileIDs();
        res.setFileIDsData(getFileIDsData(fileIds));
        return res;
    }
    
    private FileIDsData getFileIDsData(String... fileIds) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        
        for(String fileId : fileIds) {
            FileIDsDataItem dataItem = new FileIDsDataItem();
            dataItem.setFileID(fileId);
            dataItem.setFileSize(BigInteger.valueOf(items.getFileIDsDataItem().size() + 1));
            dataItem.setLastModificationTime(CalendarUtils.getNow());
            items.getFileIDsDataItem().add(dataItem);
        } 
        
        res.setFileIDsDataItems(items);
        return res;
    }
}
