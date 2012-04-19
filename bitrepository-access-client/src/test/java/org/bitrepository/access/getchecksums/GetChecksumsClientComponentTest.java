/*
 * #%L
 * Bitrepository Access
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
/*
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
import java.util.concurrent.TimeUnit;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.client.DefaultFixtureClientTest;
import org.bitrepository.client.TestEventHandler;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.mediator.CollectionBasedConversationMediator;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for the 'GetFileClient'.
 */
public class GetChecksumsClientComponentTest extends DefaultFixtureClientTest {
    private TestGetChecksumsMessageFactory testMessageFactory;
    
    private final String TEST_CLIENT_ID = "test-client";
    
    private ChecksumSpecTYPE DEFAULT_CHECKSUM_SPECS;
    
    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        if (useMockupPillar()) {
            testMessageFactory = new TestGetChecksumsMessageFactory(
                    settings.getCollectionID());
        }
        DEFAULT_CHECKSUM_SPECS = new ChecksumSpecTYPE();
        DEFAULT_CHECKSUM_SPECS.setChecksumSalt(null);
        DEFAULT_CHECKSUM_SPECS.setChecksumType(ChecksumType.MD5);
    }

    @Test(groups = {"regressiontest"})
    public void verifyGetChecksumsClientFromFactory() throws Exception {
        Assert.assertTrue(AccessComponentFactory.getInstance().createGetChecksumsClient(settings, securityManager, 
                TEST_CLIENT_ID) instanceof CollectionBasedGetChecksumsClient, 
                "The default GetFileClient from the Access factory should be of the type '" + 
                CollectionBasedGetChecksumsClient.class.getName() + "'.");
    }

    @Test(groups = {"regressiontest"})
    public void getChecksumsDeliveredAtUrl() throws Exception {
        addDescription("Tests the delivery of checksums from a pillar at a given URL.");
        addStep("Initailise the variables for this test.", 
                "EventManager and GetChecksumsClient should be instantiated.");

        String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";
        FileIDs fileIDs = new FileIDs();
        fileIDs.setFileID(DEFAULT_FILE_ID);
        
        if(useMockupPillar()) {
            settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
            settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        }

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = createGetCheckSumsClient();

        addStep("Ensure the delivery file isn't already present on the http server", 
        "Should be remove if it already exists.");
        httpServer.removeFile(deliveryFilename);
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.", 
        "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
        getChecksumsClient.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                DEFAULT_CHECKSUM_SPECS, deliveryUrl, testEventHandler, "TEST-AUDIT");

        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;
        if (useMockupPillar()) {
            receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                    IdentifyPillarsForGetChecksumsRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    testMessageFactory.createIdentifyPillarsForGetChecksumsRequest(receivedIdentifyRequestMessage, 
                            collectionDestinationID, TEST_CLIENT_ID));
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);

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
                    testMessageFactory.createGetChecksumsRequest(receivedGetChecksumsRequest, PILLAR1_ID, 
                            pillar1DestinationId, TEST_CLIENT_ID));
        }

        for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);

        addStep("The pillar sends a getChecksumsProgressResponse to the GetChecksumsClient.", 
                "The GetChecksumsClient should notify about the response through the callback interface."); 
        if (useMockupPillar()) {
            GetChecksumsProgressResponse getChecksumsProgressResponse = testMessageFactory.createGetChecksumsProgressResponse(
                    receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(getChecksumsProgressResponse);
        }
        
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PROGRESS);

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

        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPLETE);
    }
    
    @Test(groups = {"regressiontest"})
    public void noIdentifyResponses() throws Exception {
        addDescription("Tests the GetChecksumsClient handles lack of IdentifyPillarResponses gracefully.");
        addStep("Set the number of pillars for this SLA to 1 and a 3 second timeout for identifying pillar.", "");
        
        String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";
        FileIDs fileIDs = new FileIDs();
        fileIDs.setFileID(DEFAULT_FILE_ID);
        
        settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));

        if(useMockupPillar()) {
            settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
            settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        }

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = createGetCheckSumsClient();

        addStep("Ensure the delivery file isn't already present on the http server", 
                "Should be remove if it already exists.");
        httpServer.removeFile(deliveryFilename);
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.", 
                "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
        getChecksumsClient.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                DEFAULT_CHECKSUM_SPECS, deliveryUrl, testEventHandler, "TEST-AUDIT");

        if (useMockupPillar()) {
            collectionDestination.waitForMessage(IdentifyPillarsForGetChecksumsRequest.class);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Wait for at least 3 seconds", "An IdentifyPillarTimeout event should be received");
        Assert.assertEquals(testEventHandler.waitForEvent( 4, TimeUnit.SECONDS).getType(), OperationEventType.FAILED);
    }

    @Test(groups = {"regressiontest"})
    public void conversationTimeout() throws Exception {
        addDescription("Tests the GetChecksumClient handles lack of GetChecksumsResponses gracefully");
        addStep("Set the number of pillars for this SLA to 1 and a 3 second timeout for the conversation.", "");

        String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";
        FileIDs fileIDs = new FileIDs();
        fileIDs.setFileID(DEFAULT_FILE_ID);
        
        settings.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(3000));

        if(useMockupPillar()) {
            settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
            settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        }

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = createGetCheckSumsClient();

        addStep("Ensure the delivery file isn't already present on the http server", 
                "Should be remove if it already exists.");
        httpServer.removeFile(deliveryFilename);
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.", 
                "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
        getChecksumsClient.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                DEFAULT_CHECKSUM_SPECS, deliveryUrl, testEventHandler, "TEST-AUDIT");

        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;
        if (useMockupPillar()) {
            receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                    IdentifyPillarsForGetChecksumsRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    testMessageFactory.createIdentifyPillarsForGetChecksumsRequest(receivedIdentifyRequestMessage, 
                            collectionDestinationID, TEST_CLIENT_ID));
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);

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
                    testMessageFactory.createGetChecksumsRequest(receivedGetChecksumsRequest, PILLAR1_ID, 
                            pillar1DestinationId, TEST_CLIENT_ID));
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);
        
        addStep("Wait for at least 3 seconds", "An IdentifyPillarTimeout event should be received");
        Assert.assertEquals(testEventHandler.waitForEvent( 4, TimeUnit.SECONDS).getType(), OperationEventType.FAILED);
    }
    
    @Test(groups = {"regressiontest"})
    public void testNoSuchFile() throws Exception {
        addDescription("Testing how a request for a non-existing file is handled.");
        addStep("Setting up variables and such.", "Should be OK.");
        
        String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";
        FileIDs fileIDs = new FileIDs();
        fileIDs.setFileID(DEFAULT_FILE_ID);
        
        if(useMockupPillar()) {
            settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
            settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        }

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = createGetCheckSumsClient();

        addStep("Ensure the delivery file isn't already present on the http server", 
        "Should be remove if it already exists.");
        httpServer.removeFile(deliveryFilename);
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.", 
        "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
        getChecksumsClient.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                DEFAULT_CHECKSUM_SPECS, deliveryUrl, testEventHandler, "TEST-AUDIT");

        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;
        if (useMockupPillar()) {
            receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                    IdentifyPillarsForGetChecksumsRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    testMessageFactory.createIdentifyPillarsForGetChecksumsRequest(receivedIdentifyRequestMessage, 
                            collectionDestinationID, TEST_CLIENT_ID));
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);

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
                    testMessageFactory.createGetChecksumsRequest(receivedGetChecksumsRequest, PILLAR1_ID, 
                            pillar1DestinationId, TEST_CLIENT_ID));
        }

        for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);

        addStep("Send a error that the file cannot be found.", "Should trigger a 'event failed'.");
        if (useMockupPillar()) {
            GetChecksumsFinalResponse completeMsg = testMessageFactory.createGetChecksumsFinalResponse(
                    receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);
            
            ResponseInfo rfInfo = new ResponseInfo();
            rfInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            rfInfo.setResponseText("No such file.");
            completeMsg.setResponseInfo(rfInfo);
            completeMsg.setResultingChecksums(null);
            
            messageBus.sendMessage(completeMsg);
        }

        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_FAILED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPLETE);
    }

    /**
     * Creates a new test GetCheckSumsClient based on the supplied settings. 
     * 
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     * @return A new GetFileClient(Wrapper).
     */
    private GetChecksumsClient createGetCheckSumsClient() {
        MessageBus messageBus = new ActiveMQMessageBus(settings.getMessageBusConfiguration(), securityManager);
        ConversationMediator conversationMediator = new CollectionBasedConversationMediator(settings, securityManager);
        return new GetChecksumsClientTestWrapper(new CollectionBasedGetChecksumsClient(
                messageBus, conversationMediator, settings, TEST_CLIENT_ID)
        , testEventManager);
    }
}
