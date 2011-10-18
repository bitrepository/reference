/*
 * #%L
 * bitrepository-access-client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getchecksums;

import java.math.BigInteger;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecs;
import org.bitrepository.bitrepositoryelements.ChecksumSpecs.ChecksumSpecsItems;
import org.bitrepository.bitrepositoryelements.ErrorcodeGeneralType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.clienttest.TestEventHandler;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.fileexchange.TestFileStore;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for the 'GetFileClient'.
 */
public class GetChecksumsClientComponentTest extends DefaultFixtureClientTest {
    private TestGetChecksumsMessageFactory testMessageFactory;
    private TestFileStore pillar1FileStore;
    private TestFileStore pillar2FileStore;
    
    private ChecksumSpecs DEFAULT_CHECKSUM_SPECS;
    private String DEFAULT_CHECKSUM_VALUE = "940a51b250e7aa82d8e8ea31217ff267";
    
    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        if (useMockupPillar()) {
            testMessageFactory = new TestGetChecksumsMessageFactory(
                    settings.getCollectionID());
            pillar1FileStore = new TestFileStore("Pillar1", TestFileStore.DEFAULT_TEST_FILE);
//            pillar2FileStore = new TestFileStore("Pillar2", TestFileStore.DEFAULT_TEST_FILE);
        }
        DEFAULT_CHECKSUM_SPECS = new ChecksumSpecs();
        DEFAULT_CHECKSUM_SPECS.setNoOfItems(BigInteger.ONE);
        ChecksumSpecsItems csItems = new ChecksumSpecsItems();
        ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
        csType.setChecksumSalt(null);
        csType.setChecksumType("MD5");
        csItems.getChecksumSpecsItem().add(csType);
        DEFAULT_CHECKSUM_SPECS.setChecksumSpecsItems(csItems);
    }

    @Test(groups = {"regressiontest"})
    public void verifyGetChecksumsClientFromFactory() throws Exception {
        Assert.assertTrue(AccessComponentFactory.getInstance().createGetChecksumsClient(settings) 
                instanceof BasicGetChecksumsClient, 
                "The default GetFileClient from the Access factory should be of the type '" + 
                BasicGetChecksumsClient.class.getName() + "'.");
    }

    @Test(groups = {"regressiontest"})
    public void getChecksumsDeliveredAtUrl() throws Exception {
        addDescription("Tests the delivery of checksums from a pillar at a given URL.");
        addStep("Initailise the variables for this test.", 
                "EventManager and GetChecksumsClient should be instantiated.");

        String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";
        FileIDs fileIDs = new FileIDs();
        fileIDs.getFileID().add(DEFAULT_FILE_ID);
        
        if(useMockupPillar()) {
            settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
            settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        }

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = new GetChecksumsClientTestWrapper(
                AccessComponentFactory.getInstance().createGetChecksumsClient(settings), 
                testEventManager);

        addStep("Ensure the delivery file isn't already present on the http server", 
        "Should be remove if it already exists.");
        httpServer.removeFile(deliveryFilename);
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.", 
        "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
        getChecksumsClient.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                DEFAULT_CHECKSUM_SPECS, deliveryUrl, testEventHandler);

        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;
        if (useMockupPillar()) {
            receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(
                    IdentifyPillarsForGetChecksumsRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    testMessageFactory.createIdentifyPillarsForGetChecksumsRequest(receivedIdentifyRequestMessage, 
                            collectionDestinationID));
        }

        addStep("The pillar sends a response to the identify message.", 
                "The callback listener should notify of the response and the client should send a GetChecksumsRequest "
                + "message to the pillar"); 

        GetChecksumsRequest receivedGetChecksumsRequest = null;
        if (useMockupPillar()) {
            IdentifyPillarsForGetChecksumsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                    receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(identifyResponse);
            receivedGetChecksumsRequest = pillar1Destination.waitForMessage(GetChecksumsRequest.class);
            Assert.assertEquals(receivedGetChecksumsRequest, 
                    testMessageFactory.createGetChecksumsRequest(receivedGetChecksumsRequest,PILLAR1_ID, pillar1DestinationId));
        }

        for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);

        addStep("The pillar sends a getChecksumsProgressResponse to the GetChecksumsClient.", 
                "The GetChecksumsClient should notify about the response through the callback interface."); 
        if (useMockupPillar()) {
            GetChecksumsProgressResponse getChecksumsProgressResponse = testMessageFactory.createGetChecksumsProgressResponse(
                    receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(getChecksumsProgressResponse);
        }
        
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);

        addStep("The resulting file is uploaded to the indicated url and the pillar sends a final response upload message", 
                "The GetChecksumsClient notifies that the file is ready through the callback listener and the uploaded file is present.");
        if (useMockupPillar()) {
            GetChecksumsFinalResponse completeMsg = testMessageFactory.createGetChecksumsFinalResponse(
                    receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);
            
            ResultingChecksums res = new ResultingChecksums();
            res.setResultAddress(receivedGetChecksumsRequest.getResultAddress());
            completeMsg.setResultingChecksums(res);
            
            messageBus.sendMessage(completeMsg);
        }

        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PartiallyComplete);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
    }
    
    @Test(groups = {"regressiontest"})
    public void checksumsDeliveredThroughMessage() throws Exception {
        addDescription("Tests that the GetChecksumsClient can deliver the results of a checksums operation through "
                + "the messages.");
        addStep("Initialise the variables.", "");
        
        FileIDs fileIDs = new FileIDs();
        fileIDs.getFileID().add(DEFAULT_FILE_ID);
        
        if(useMockupPillar()) {
            settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
            settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        }

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        
        addStep("Create a separate thread for handling the receival of the results of the conversation.", 
                "There should be no problems in creating the new thread.");
        ChecksumCallThread callChecksum = new ChecksumCallThread(fileIDs, DEFAULT_CHECKSUM_SPECS, testEventHandler);
        
        addStep("Starting the conversation.", 
                "Should be handled in the separate thread, along with the receival of the results.");
        callChecksum.start();
        Assert.assertTrue(callChecksum.isAlive());

        addStep("Receiving the identification for the pillar.", 
                "Should be sent by the GetCheckumsClient");
        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;
        if (useMockupPillar()) {
            receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(
                    IdentifyPillarsForGetChecksumsRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    testMessageFactory.createIdentifyPillarsForGetChecksumsRequest(receivedIdentifyRequestMessage, 
                            collectionDestinationID));
        }

        addStep("The pillar sends a response to the identify message.", 
                "The callback listener should notify of the response and the client should send a GetChecksumsRequest "
                + "message to the pillar"); 

        GetChecksumsRequest receivedGetChecksumsRequest = null;
        if (useMockupPillar()) {
            IdentifyPillarsForGetChecksumsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                    receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(identifyResponse);
            receivedGetChecksumsRequest = pillar1Destination.waitForMessage(GetChecksumsRequest.class);
            Assert.assertEquals(receivedGetChecksumsRequest, 
                    testMessageFactory.createGetChecksumsRequest(receivedGetChecksumsRequest,PILLAR1_ID, pillar1DestinationId));
        }

        for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);

        addStep("The pillar sends a getChecksumsProgressResponse to the GetChecksumsClient.", 
                "The GetChecksumsClient should notify about the response through the callback interface."); 
        if (useMockupPillar()) {
            GetChecksumsProgressResponse getChecksumsProgressResponse = testMessageFactory.createGetChecksumsProgressResponse(
                    receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(getChecksumsProgressResponse);
        }
        
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);

        addStep("The resulting file is uploaded to the indicated url and the pillar sends a final response upload message", 
                "The GetChecksumsClient notifies that the file is ready through the callback listener and the uploaded file is present.");
        if (useMockupPillar()) {
            GetChecksumsFinalResponse completeMsg = testMessageFactory.createGetChecksumsFinalResponse(
                    receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);
            
            ResultingChecksums res = new ResultingChecksums();
            res.setResultAddress(null);
            for(String fileID : receivedGetChecksumsRequest.getFileIDs().getFileID()) {
                ChecksumDataForChecksumSpecTYPE csSpecs = new ChecksumDataForChecksumSpecTYPE();
                csSpecs.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
                csSpecs.setChecksumValue(DEFAULT_CHECKSUM_VALUE);
                csSpecs.setFileID(fileID);
                res.getChecksumDataItems().add(csSpecs);
            }
            res.setResultAddress(receivedGetChecksumsRequest.getResultAddress());
            completeMsg.setResultingChecksums(res);
            
            messageBus.sendMessage(completeMsg);
        }

        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PartiallyComplete);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
        Assert.assertTrue(callChecksum.isAlive(), "The client thread should still be alive.");

        synchronized(callChecksum) {
            try {
                callChecksum.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        Assert.assertNotNull(callChecksum.results, "The results should have been received by the thread.");
        for(String pillar : settings.getCollectionSettings().getClientSettings().getPillarIDs()) {
            ResultingChecksums res = callChecksum.results.get(pillar);
            Assert.assertNotNull(res, "The checksums for '" + pillar + "' should exist.");
            Assert.assertNull(res.getResultAddress(), "No resulting address should be returned.");
            List<ChecksumDataForChecksumSpecTYPE> checksumItems = res.getChecksumDataItems();
            Assert.assertNotNull(checksumItems, "A list of checksums should be returned.");
            
            Assert.assertEquals(checksumItems.size(), 1, "There should only be one returned element.");
            Assert.assertEquals(checksumItems.get(0).getFileID(), DEFAULT_FILE_ID, 
                    "It should return the checksum for requested file.");
            Assert.assertEquals(checksumItems.get(0).getChecksumValue(), DEFAULT_CHECKSUM_VALUE, 
                    "It should return the expected checksum for requested file.");
        }
        
    }
    
    @Test(groups = {"regressiontest"})
    public void noIdentifyResponses() throws Exception {
        addDescription("Tests the GetChecksumsClient handles lack of IdentifyPillarResponses gracefully.");
        addStep("Set the number of pillars for this SLA to 1 and a 3 second timeout for identifying pillar.", "");
        
        String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";
        FileIDs fileIDs = new FileIDs();
        fileIDs.getFileID().add(DEFAULT_FILE_ID);
        
        settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));

        if(useMockupPillar()) {
            settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
            settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        }

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = new GetChecksumsClientTestWrapper(
                AccessComponentFactory.getInstance().createGetChecksumsClient(settings), 
                testEventManager);

        addStep("Ensure the delivery file isn't already present on the http server", 
                "Should be remove if it already exists.");
        httpServer.removeFile(deliveryFilename);
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.", 
                "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
        getChecksumsClient.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                DEFAULT_CHECKSUM_SPECS, deliveryUrl, testEventHandler);

        if (useMockupPillar()) {
            bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetChecksumsRequest.class);
        }

        addStep("Wait for at least 3 seconds", "An IdentifyPillarTimeout event should be received");
        Assert.assertEquals(testEventHandler.waitForEvent( 4, TimeUnit.SECONDS).getType(), OperationEventType.NoPillarFound);
    }

    @Test(groups = {"regressiontest"})
    public void conversationTimeout() throws Exception {
        addDescription("Tests the GetChecksumClient handles lack of GetChecksumsResponses gracefully");
        addStep("Set the number of pillars for this SLA to 1 and a 3 second timeout for the conversation.", "");

        String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";
        FileIDs fileIDs = new FileIDs();
        fileIDs.getFileID().add(DEFAULT_FILE_ID);
        
        settings.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(3000));

        if(useMockupPillar()) {
            settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
            settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        }

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = new GetChecksumsClientTestWrapper(
                AccessComponentFactory.getInstance().createGetChecksumsClient(settings), 
                testEventManager);

        addStep("Ensure the delivery file isn't already present on the http server", 
                "Should be remove if it already exists.");
        httpServer.removeFile(deliveryFilename);
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.", 
                "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
        getChecksumsClient.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                DEFAULT_CHECKSUM_SPECS, deliveryUrl, testEventHandler);

        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;
        if (useMockupPillar()) {
            receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(
                    IdentifyPillarsForGetChecksumsRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    testMessageFactory.createIdentifyPillarsForGetChecksumsRequest(receivedIdentifyRequestMessage, 
                            collectionDestinationID));
        }

        addStep("The pillar sends a response to the identify message.", 
                "The callback listener should notify of the response and the client should send a GetChecksumsRequest "
                + "message to the pillar"); 

        GetChecksumsRequest receivedGetChecksumsRequest = null;
        if (useMockupPillar()) {
            IdentifyPillarsForGetChecksumsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                    receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(identifyResponse);
            receivedGetChecksumsRequest = pillar1Destination.waitForMessage(GetChecksumsRequest.class);
            Assert.assertEquals(receivedGetChecksumsRequest, 
                    testMessageFactory.createGetChecksumsRequest(receivedGetChecksumsRequest,PILLAR1_ID, pillar1DestinationId));
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
        
        addStep("Wait for at least 3 seconds", "An IdentifyPillarTimeout event should be received");
        Assert.assertEquals(testEventHandler.waitForEvent( 4, TimeUnit.SECONDS).getType(), OperationEventType.Failed);
    }
    
    @Test(groups = {"regressiontest"})
    public void testNoSuchFile() throws Exception {
        addDescription("Testing how a request for a non-existing file is handled.");
        addStep("Setting up variables and such.", "Should be OK.");
        
        String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";
        FileIDs fileIDs = new FileIDs();
        fileIDs.getFileID().add(DEFAULT_FILE_ID);
        
        if(useMockupPillar()) {
            settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
            settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        }

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = new GetChecksumsClientTestWrapper(
                AccessComponentFactory.getInstance().createGetChecksumsClient(settings), 
                testEventManager);

        addStep("Ensure the delivery file isn't already present on the http server", 
        "Should be remove if it already exists.");
        httpServer.removeFile(deliveryFilename);
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.", 
        "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
        getChecksumsClient.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                DEFAULT_CHECKSUM_SPECS, deliveryUrl, testEventHandler);

        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;
        if (useMockupPillar()) {
            receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(
                    IdentifyPillarsForGetChecksumsRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    testMessageFactory.createIdentifyPillarsForGetChecksumsRequest(receivedIdentifyRequestMessage, 
                            collectionDestinationID));
        }

        addStep("The pillar sends a response to the identify message.", 
                "The callback listener should notify of the response and the client should send a GetChecksumsRequest "
                + "message to the pillar"); 

        GetChecksumsRequest receivedGetChecksumsRequest = null;
        if (useMockupPillar()) {
            IdentifyPillarsForGetChecksumsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                    receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(identifyResponse);
            receivedGetChecksumsRequest = pillar1Destination.waitForMessage(GetChecksumsRequest.class);
            Assert.assertEquals(receivedGetChecksumsRequest, 
                    testMessageFactory.createGetChecksumsRequest(receivedGetChecksumsRequest,PILLAR1_ID, pillar1DestinationId));
        }

        for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);

        addStep("Send a error that the file cannot be found.", "Should trigger a 'event failed'.");
        if (useMockupPillar()) {
            GetChecksumsFinalResponse completeMsg = testMessageFactory.createGetChecksumsFinalResponse(
                    receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);
            
            FinalResponseInfo rfInfo = new FinalResponseInfo();
            rfInfo.setFinalResponseCode(ErrorcodeGeneralType.FILE_NOT_FOUND.value().toString());
            rfInfo.setFinalResponseText("No such file.");
            completeMsg.setFinalResponseInfo(rfInfo);
            completeMsg.setResultingChecksums(null);
            
            messageBus.sendMessage(completeMsg);
        }

        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Failed);
    }
    
    private class ChecksumCallThread extends Thread {
            
        private GetChecksumsClient getChecksumsClient;
        FileIDs fileIDs;
        ChecksumSpecs csSpecs;
        TestEventHandler eventHandler;
        public ChecksumCallThread(FileIDs fileIDs, ChecksumSpecs csSpecs, TestEventHandler eventHandler) {
            this.fileIDs = fileIDs;
            this.csSpecs = csSpecs;
            this.eventHandler = eventHandler;
             getChecksumsClient = new GetChecksumsClientTestWrapper(
                    AccessComponentFactory.getInstance().createGetChecksumsClient(settings), 
                    testEventManager);
        }
        
        public Map<String, ResultingChecksums> results = null;
        
        @Override
        public void run() {
            results = getChecksumsClient.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                    csSpecs, eventHandler);
        }
    };

}
