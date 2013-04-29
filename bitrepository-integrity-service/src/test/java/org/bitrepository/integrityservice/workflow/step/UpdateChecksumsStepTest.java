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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.mocks.MockCollector;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UpdateChecksumsStepTest extends ExtendedTestCase {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final List<String> PILLAR_IDS = Arrays.asList(TEST_PILLAR_1);
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String DEFAULT_CHECKSUM = "0123456789";
    
    public static final Integer NUMBER_OF_PARTIAL_RESULTS = 3;
    
    protected Settings settings;
    
    String TEST_COLLECTIONID;
    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().addAll(PILLAR_IDS);
        TEST_COLLECTIONID = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testPositiveReply() {
        addDescription("Test the step for updating the checksums can handle COMPLETE operation event.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getChecksums(String collectionID, Collection<String> pillarIDs, ChecksumSpecTYPE checksumType, String auditTrailInformation,
                    ContributorQuery[] queries, EventHandler eventHandler) {
                super.getChecksums(collectionID, pillarIDs, checksumType, auditTrailInformation, queries, eventHandler);
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTIONID, null));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, store, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTIONID);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddChecksums(), 0);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testNegativeReply() {
        addDescription("Test the step for updating the checksums can handle FAILURE operation event.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getChecksums(String collectionID, Collection<String> pillarIDs, ChecksumSpecTYPE checksumType, String auditTrailInformation,
                    ContributorQuery[] queries, EventHandler eventHandler) {
                super.getChecksums(collectionID, pillarIDs, checksumType, auditTrailInformation, queries, eventHandler);
                eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTIONID, "Problem encountered", null));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, store, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTIONID);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddChecksums(), 0);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 1);
        Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testIngestOfResults() {
        addDescription("Test the step for updating the checksums delivers the results to the integrity model.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getChecksums(String collectionID, Collection<String> pillarIDs, ChecksumSpecTYPE checksumType, String auditTrailInformation,
                    ContributorQuery[] queries, EventHandler eventHandler) {
                super.getChecksums(collectionID, pillarIDs, checksumType, auditTrailInformation, queries, eventHandler);
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTIONID, Arrays.asList(TEST_PILLAR_1)));
                ChecksumsCompletePillarEvent event = new ChecksumsCompletePillarEvent(
                        TEST_PILLAR_1, TEST_COLLECTIONID, createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1),
                        createChecksumSpecTYPE(), false);
                eventHandler.handleEvent(event);
                
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTIONID, null));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, store, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTIONID);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddChecksums(), 1);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testThreadedResults() {
        addDescription("Test the step for updating the checksums delivers the results to the integrity model.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getChecksums(String collectionID, Collection<String> pillarIDs, ChecksumSpecTYPE checksumType, String auditTrailInformation,
                    ContributorQuery[] queries, EventHandler eventHandler) {
                super.getChecksums(collectionID, pillarIDs, checksumType, auditTrailInformation, queries, eventHandler);
                new Thread(new TestEventHandler(eventHandler, 0)).start();
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, store, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTIONID);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddChecksums(), 1);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testPartialResults() {
        addDescription("Test that the number of partial is used for generating more than one request.");
        MockCollector collector = new MockCollector() {
            Integer numberOfPartialResultsLeft = NUMBER_OF_PARTIAL_RESULTS;
            @Override
            public void getChecksums(String collectionID, Collection<String> pillarIDs, ChecksumSpecTYPE checksumType, String auditTrailInformation,
                    ContributorQuery[] queries, EventHandler eventHandler) {
                super.getChecksums(collectionID, pillarIDs, checksumType, auditTrailInformation, queries, eventHandler);
                new Thread(new TestEventHandler(eventHandler, numberOfPartialResultsLeft)).start();
                numberOfPartialResultsLeft--;
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, store, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTIONID);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddChecksums(), NUMBER_OF_PARTIAL_RESULTS + 1);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), NUMBER_OF_PARTIAL_RESULTS + 1);
    }
    
    private class TestEventHandler implements Runnable {
        final EventHandler eventHandler;
        int partialsLeft;
        TestEventHandler(EventHandler eventhandler, int numberOfPartialCompletes) {
            this.eventHandler = eventhandler;
            this.partialsLeft = numberOfPartialCompletes; 
        }
        @Override
        public void run() {
            synchronized(this) {
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTIONID, Arrays.asList(TEST_PILLAR_1)));
                
                ChecksumsCompletePillarEvent event;
                if(partialsLeft > 0) {
                    event = new ChecksumsCompletePillarEvent(
                        TEST_PILLAR_1, TEST_COLLECTIONID, createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1),
                        createChecksumSpecTYPE(), true);
                } else {
                    event = new ChecksumsCompletePillarEvent(
                            TEST_PILLAR_1, TEST_COLLECTIONID, createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1),
                            createChecksumSpecTYPE(), false);                    
                }
                eventHandler.handleEvent(event);
                
                eventHandler.handleEvent(new ContributorEvent(TEST_PILLAR_1, TEST_COLLECTIONID));
            }
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTIONID, null));
        }
    }

    private ResultingChecksums createResultingChecksums(String checksum, String ... fileids) {
        ResultingChecksums res = new ResultingChecksums();
        res.getChecksumDataItems().addAll(createChecksumData(checksum, fileids));
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

    private ChecksumSpecTYPE createChecksumSpecTYPE() {
        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        res.setChecksumType(ChecksumType.MD5);
        return res;
    }
}
