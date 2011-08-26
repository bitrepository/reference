/*
 * #%L
 * Bitrepository Access Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.modify.putfile;

import java.io.File;
import java.net.URL;

import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.clienttest.TestEventHandler;
import org.bitrepository.collection.settings.standardcollectionsettings.MessageBusConfiguration;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the components of the PutFileClient.
 * TODO need more test-cases, e.g. the User-Stories...
 */
public class PutFileClientComponentTest extends DefaultFixtureClientTest {
    /** The settings. */
    MutablePutFileClientSettings putSettings;
    
    private TestPutFileMessageFactory messageFactory;
    
    private URL downloadUrl = null;

    
    private File testFile;
    private String fileId;
    
    @BeforeMethod(alwaysRun=true)
    public void initialise() throws Exception {
        putSettings = new MutablePutFileClientSettings(settings);
        putSettings.setAuditTrailInformation(null);
        putSettings.setPutFileDefaultTimeout(1000L);
        
        putSettings.setPillarIDs(new String[]{PILLAR1_ID});
//        System.setProperty("useMockupPillar", "false");
//        putSettings.setBitRepositoryCollectionID("JOLF-TEST");
//        putSettings.setBitRepositoryCollectionID("I DO NOT KNOW YOUR COLLECTION ID!!!");
//        putSettings.setBitRepositoryCollectionTopicID("JOLF-TESTING-TOPIC");
//        putSettings.setPillarIDs(new String[]{"ReferencePillar_1", "ReferencePillar_2"});
//        putSettings.setPillarIDs(new String[]{"ReferencePillar_1"});
//        org.bitrepository.protocol.configuration.MessageBusConfiguration busConf = new org.bitrepository.protocol.configuration.MessageBusConfiguration();
//        busConf.setId("distribueret-test-messagebus");
//        busConf.setUrl("failover:(tcp://sandkasse-01.kb.dk:60001,tcp://sandkasse-01.kb.dk:60002,tcp://sandkasse-01.kb.dk:60003)");
//        messageBusConfigurations.setPrimaryMessageBusConfiguration(busConf);
        
        putSettings.setMessageBusConfiguration(messageBusConfigurations);
        
        fileId = DEFAULT_FILE_ID;
        
        if(useMockupPillar()) {
            messageFactory = new TestPutFileMessageFactory(putSettings.getBitRepositoryCollectionID());
        }

        testFile = new File("src/test/resources/test-files/", fileId);
    }
    
//    @Test(groups={"regressiontest"})
    public void verifyPutClientFromFactory() {
        addDescription("Testing the initialization through the ModifyComponentFactory.");
        addStep("Use the ModifyComponentFactory to instantiate a PutFileClient.", 
                "It should be an instance of SimplePutFileClient");
        PutFileClient pfc = ModifyComponentFactory.getInstance().retrievePutClient(putSettings);
        Assert.assertTrue(pfc instanceof ConversationBasedPutFileClient, "The PutFileClient '" + pfc + "' should be instance of '" 
                + ConversationBasedPutFileClient.class.getName() + "'");
    }
    
    @Test(groups={"regressiontest"})
    public void putClientTester() throws Exception {
        addDescription("Tests the PutClient. Makes a whole conversation for the put client for a 'good' scenario.");
        addStep("Initialise the number of pillars and the PutClient.", "Should be OK.");
        
        downloadUrl = new URL("http://sandkasse-01.kb.dk/dav/test.txt");
        
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        PutFileClient putClient = new PutClientTestWrapper(
                ModifyComponentFactory.getInstance().retrievePutClient(putSettings), 
                testEventManager);

        addStep("Ensure that the test-file is placed on the HTTP server.", "Should be removed an reuploaded.");
        
        addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
                "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
        putClient.putFileWithId(downloadUrl, fileId, new Long(testFile.length()), testEventHandler);
        
        IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = null;
        if(useMockupPillar()) {
            receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(
                    IdentifyPillarsForPutFileRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    messageFactory.createIdentifyPillarsForPutFileRequest(
                            receivedIdentifyRequestMessage.getCorrelationID(),
                            receivedIdentifyRequestMessage.getReplyTo(),
                            receivedIdentifyRequestMessage.getTo()
                            ));
        }

        addStep("Make response for the pillar.", "The client should then send the actual PutFileRequest.");
        
        PutFileRequest receivedPutFileRequest = null;
        if(useMockupPillar()) {
            IdentifyPillarsForPutFileResponse identifyResponse = messageFactory
                    .createIdentifyPillarsForPutFileResponse(
                            receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(identifyResponse);
            receivedPutFileRequest = pillar1Destination.waitForMessage(PutFileRequest.class);
            Assert.assertEquals(receivedPutFileRequest, 
                    messageFactory.createPutFileRequest(
                            PILLAR1_ID, pillar1DestinationId,
                            receivedPutFileRequest.getReplyTo(),
                            receivedPutFileRequest.getCorrelationID(),
                            receivedPutFileRequest.getFileAddress(),
                            receivedPutFileRequest.getFileSize(),
                            fileId
                            ));
        }
        
        addStep("Validate the steps of the PutClient by going through the events.", "Should be 'PillarIdentified', "
                + "'PillarSelected' and 'RequestSent'");
        for(String s : putSettings.getPillarIDs()) {
            Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);

        addStep("The pillar sends a progress response to the PutClient.", "Should be caught by the event handler.");
        if(useMockupPillar()) {
            PutFileProgressResponse putFileProgressResponse = messageFactory.createPutFileProgressResponse(
                    receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(putFileProgressResponse);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);

        addStep("Send a final response message to the PutClient.", 
                "Should be caught by the event handler. First a PartiallyComplete, then a Complete.");
        if(useMockupPillar()) {
            PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                    receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(putFileFinalResponse);
        }
        for(int i = 1; i < 2* putSettings.getPillarIDs().length; i++) {
            OperationEventType eventType = testEventHandler.waitForEvent().getType();
            Assert.assertTrue( (eventType == OperationEventType.PartiallyComplete)
                    || (eventType == OperationEventType.Progress),
                    "Expected either PartiallyComplete or Progress, but was: " + eventType);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
    }
    
    @Test(groups={"regressiontest"})
    public void putClientBadURLTester() throws Exception {
        addDescription("Tests the PutClient. Makes a whole conversation for the put client, for a 'bad' scenario. "
                + "The URL is not valid, which should give errors in the FinalResponse.");
        addStep("Initialise the number of pillars and the PutClient.", "Should be OK.");
        
        downloadUrl = new URL("http://sandkasse-01.kb.dk/ERROR/ERROR/ERROR/ERROR/ERROR/ERROR/ERROR/test.xml");
        fileId = "ERROR-BAD-URL";
        
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        PutFileClient putClient = new PutClientTestWrapper(
                ModifyComponentFactory.getInstance().retrievePutClient(putSettings), 
                testEventManager);

        addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
                "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
        putClient.putFileWithId(downloadUrl, fileId, new Long(testFile.length()), testEventHandler);
        
        IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = null;
        if(useMockupPillar()) {
            receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(
                    IdentifyPillarsForPutFileRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    messageFactory.createIdentifyPillarsForPutFileRequest(
                            receivedIdentifyRequestMessage.getCorrelationID(),
                            receivedIdentifyRequestMessage.getReplyTo(),
                            receivedIdentifyRequestMessage.getTo()
                            ));
        }

        addStep("Make response for the pillar.", "The client should then send the actual PutFileRequest.");
        
        PutFileRequest receivedPutFileRequest = null;
        if(useMockupPillar()) {
            IdentifyPillarsForPutFileResponse identifyResponse = messageFactory
                    .createIdentifyPillarsForPutFileResponse(
                            receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(identifyResponse);
            receivedPutFileRequest = pillar1Destination.waitForMessage(PutFileRequest.class);
            Assert.assertEquals(receivedPutFileRequest, 
                    messageFactory.createPutFileRequest(
                            PILLAR1_ID, pillar1DestinationId,
                            receivedPutFileRequest.getReplyTo(),
                            receivedPutFileRequest.getCorrelationID(),
                            receivedPutFileRequest.getFileAddress(),
                            receivedPutFileRequest.getFileSize(),
                            fileId
                            ));
        }
        
        addStep("Validate the steps of the PutClient by going through the events.", "Should be 'PillarIdentified', "
                + "'PillarSelected' and 'RequestSent'");
        for(String s : putSettings.getPillarIDs()) {
            Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);

        addStep("The pillar sends a progress response to the PutClient.", "Should be caught by the event handler.");
        if(useMockupPillar()) {
            PutFileProgressResponse putFileProgressResponse = messageFactory.createPutFileProgressResponse(
                    receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(putFileProgressResponse);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);

        addStep("Send a 'bad' final response message to the PutClient.", 
                "Should be caught by the event handler. First a Failed, then a Complete.");
        
        if(useMockupPillar()) {
            PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                    receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
            FinalResponseInfo frInfo = new FinalResponseInfo();
            frInfo.setFinalResponseCode("500");
            // TODO has to contain 'Error' to be caught.
            frInfo.setFinalResponseText("Error: could not use URL!"); 
            putFileFinalResponse.setFinalResponseInfo(frInfo);
            messageBus.sendMessage(putFileFinalResponse);
        }
        
        for(int i = 1; i < 3* putSettings.getPillarIDs().length; i++) {
            OperationEventType eventType = testEventHandler.waitForEvent().getType();
            Assert.assertTrue( (eventType == OperationEventType.Failed)
                    || (eventType == OperationEventType.Progress) 
                    || (eventType == OperationEventType.PartiallyComplete),
                    "Expected either Failed, Progress or PartiallyComplete, but was: " + eventType);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
    }
}
