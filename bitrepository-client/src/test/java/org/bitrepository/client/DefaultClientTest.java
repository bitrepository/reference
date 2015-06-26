package org.bitrepository.client;
/*
 * #%L
 * Bitrepository Client
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

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests the general client functionality. A number of abstract methods with needs to be implemented with concrete
 * functionality by the test for the specific operations.
 */
public abstract class DefaultClientTest extends DefaultFixtureClientTest {
    protected final TestEventHandler testEventHandler = new TestEventHandler(testEventManager);

    @Test(groups = {"regressiontest"})
    public void identificationNegativeTest() throws Exception {
        addDescription("Verify that the client works correctly when a contributor sends a negative response.");

        addStep("Start the operation.",
                "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
        startOperation(testEventHandler);
        MessageRequest identifyRequest = waitForIdentifyRequest();
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identification response from contributor1 with a IDENTIFICATION_NEGATIVE response code .",
                "A component failed event should be generated.");

        MessageResponse identifyResponse1 = createIdentifyResponse(identifyRequest, PILLAR1_ID, pillar1DestinationId);
        identifyResponse1.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        messageBus.sendMessage(identifyResponse1);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);

        addStep("Send a identification response from contributor2 with a IDENTIFICATION_POSITIVE response code .",
                "A component COMPONENT_IDENTIFIED event should be generated followed by a IDENTIFICATION_COMPLETE.");
        MessageResponse identifyResponse2 = createIdentifyResponse(identifyRequest, PILLAR2_ID, pillar2DestinationId);
        identifyResponse2.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        messageBus.sendMessage(identifyResponse2);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);

        addStep("Verify that the client continues to the performing phase.",
                "A REQUEST_SENT event should be generated and a OperationRequest should be sent only to contributor2" +
                        ".");
        checkNoRequestIsReceived(pillar1Receiver);
        MessageRequest request = waitForRequest(pillar2Receiver);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a final response message from contributor2",
                "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
        MessageResponse completeMsg = createFinalResponse(request, PILLAR2_ID, pillar2DestinationId);
        completeMsg.getResponseInfo().setResponseCode(ResponseCode.OPERATION_COMPLETED);
        messageBus.sendMessage(completeMsg);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups = {"regressiontest"})
    public void identificationFailureTest() throws Exception {
        addDescription("Verify that the client works correctly when a contributor sends a failure response.");

        addStep("Start the operation.",
                "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
        startOperation(testEventHandler);
        MessageRequest identifyRequest = waitForIdentifyRequest();
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identification response from contributor1 with a FAILURE response code.",
                "A component failed event should be generated.");
        MessageResponse identifyResponse1 = createIdentifyResponse(identifyRequest, PILLAR1_ID, pillar1DestinationId);
        identifyResponse1.getResponseInfo().setResponseCode(ResponseCode.FAILURE);
        messageBus.sendMessage(identifyResponse1);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);

        addStep("Send a identification response from contributor2 with a IDENTIFICATION_POSITIVE response code .",
                "A component COMPONENT_IDENTIFIED event should be generated followed by a IDENTIFICATION_COMPLETE.");
        MessageResponse identifyResponse2 = createIdentifyResponse(
                identifyRequest, PILLAR2_ID, pillar2DestinationId);
        identifyResponse2.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        messageBus.sendMessage(identifyResponse2);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);

        addStep("Verify that the client continues to the performing phase.",
                "A REQUEST_SENT event should be generated and a Request should be sent only to contributor2.");
        checkNoRequestIsReceived(pillar1Receiver);
        MessageRequest request = waitForRequest(pillar2Receiver);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a final response message from contributor2",
                "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
        MessageResponse completeMsg = createFinalResponse(request, PILLAR2_ID, pillar2DestinationId);
        completeMsg.getResponseInfo().setResponseCode(ResponseCode.OPERATION_COMPLETED);
        messageBus.sendMessage(completeMsg);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups = {"regressiontest"})
    public void oneContributorNotRespondingTest() throws Exception {
        addDescription("Verify that the client works correct without receiving identification responses from all " +
                "contributors.");
        addFixture("Set the a identification timeout to 100 ms.");
        settingsForCUT.getRepositorySettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(100));

        addStep("Start the operation.",
                "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
        startOperation(testEventHandler);
        MessageRequest identifyRequest = waitForIdentifyRequest();
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identification response from contributor1.", "A COMPONENT_IDENTIFIED event should be generated.");
        MessageResponse identifyResponse1 = createIdentifyResponse(identifyRequest, PILLAR1_ID, pillar1DestinationId);
        identifyResponse1.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        messageBus.sendMessage(identifyResponse1);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);

        addStep("Wait 1 second.",
                "A IDENTIFY_TIMEOUT event should be generated, followed by a IDENTIFICATION_COMPLETE.");
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);

        addStep("Verify that the client continues to the performing phase.",
                "A REQUEST_SENT event should be generated and a Request should be sent to pillar1.");
        MessageRequest request = waitForRequest(pillar1Receiver);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a final response upload message",
                "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
        MessageResponse completeMsg = createFinalResponse(request, PILLAR1_ID, pillar1DestinationId);
        completeMsg.getResponseInfo().setResponseCode(ResponseCode.OPERATION_COMPLETED);
        messageBus.sendMessage(completeMsg);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);

    }

    @Test(groups = {"regressiontest"})
    public void noContributorsRespondingTest() throws Exception {
        addDescription("Tests the the client handles lack of a IdentifyResponse gracefully. " +
                "More concrete this means that the occurrence of a identification timeout should be handled correctly");

        addStep("Set a 100 ms timeout for identifying contributors.", "");
        settingsForCUT.getRepositorySettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(100));

        addStep("Start the operation.", "A IDENTIFY_REQUEST_SENT event should be generated.");
        startOperation(testEventHandler);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Wait for 1 second", "An IdentifyPillarTimeout event should be received followed by a FAILED event");
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }


    @Test(groups = {"regressiontest"})
    public void operationTimeoutTest() throws Exception {
        addDescription("Tests the the client handles lack of final responses gracefully.");

        addStep("Set a 100 ms operation timeout.", "");
        settingsForCUT.getRepositorySettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(100));

        addStep("Start the operation",
                "A IDENTIFY_REQUEST_SENT event should be received.");
        startOperation(testEventHandler);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        MessageRequest identifyRequest = waitForIdentifyRequest();

        addStep("Send positive responses from the pillar1 and a negative response from pillar2",
                "A COMPONENT_IDENTIFIED + a " +
                "event should be generated followed by a IDENTIFICATION_COMPLETE. <p>" +
                "Finally a operation request should be sent to pillar1 and a REQUEST_SENT event be " +
                "generated");
        messageBus.sendMessage(createIdentifyResponse(identifyRequest, PILLAR1_ID, pillar1DestinationId));
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        MessageResponse identifyResponse2 = createIdentifyResponse(identifyRequest, PILLAR2_ID, pillar2DestinationId);
        identifyResponse2.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        messageBus.sendMessage(identifyResponse2);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        waitForRequest(pillar1Receiver);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Wait for 1 second", "An FAILED event should be received");
        assertEquals(testEventHandler.waitForEvent().getEventType(),
                OperationEventType.FAILED);
    }
    
    @Test(groups = {"regressiontest"})
    public void collectionIDIncludedInEventsTest() throws Exception {
        addDescription("Tests the the client provides collectionID in events.");

        addStep("Set a 0.5 second operation timeout.", "");
        settingsForCUT.getRepositorySettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(500));

        addStep("Start the operation", "A IDENTIFY_REQUEST_SENT event should be received.");
        startOperation(testEventHandler);
        
        OperationEvent event1 = testEventHandler.waitForEvent();
        assertEquals(event1.getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        assertEquals(event1.getCollectionID(), collectionID);
        MessageRequest identifyRequest = waitForIdentifyRequest();

        addStep("Send positive responses from the pillar1 and a negative response from pillar2",
                "A COMPONENT_IDENTIFIED + a " +
                "event should be generated followed by a IDENTIFICATION_COMPLETE. <p>" +
                "Finally a operation request should be sent to pillar1 and a REQUEST_SENT event be " +
                "generated");
        messageBus.sendMessage(createIdentifyResponse(identifyRequest, PILLAR1_ID, pillar1DestinationId));
        
        OperationEvent event2 = testEventHandler.waitForEvent();
        assertEquals(event2.getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(event2.getCollectionID(), collectionID);
        
        MessageResponse identifyResponse2 = createIdentifyResponse(identifyRequest, PILLAR2_ID, pillar2DestinationId);
        identifyResponse2.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        messageBus.sendMessage(identifyResponse2);
        
        OperationEvent event3 = testEventHandler.waitForEvent();
        assertEquals(event3.getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(event3.getCollectionID(), collectionID);
        
        OperationEvent event4 = testEventHandler.waitForEvent();
        assertEquals(event4.getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(event4.getCollectionID(), collectionID);
        
        waitForRequest(pillar1Receiver);
        
        OperationEvent event5 = testEventHandler.waitForEvent();
        assertEquals(event5.getEventType(), OperationEventType.REQUEST_SENT);
        assertEquals(event5.getCollectionID(), collectionID);

        addStep("Wait for 1 second", "An FAILED event should be received");
        OperationEvent event6 = testEventHandler.waitForEvent();
        assertEquals(event6.getEventType(), OperationEventType.FAILED);
        assertEquals(event6.getCollectionID(), collectionID);
    }    

    @Test(groups = {"regressiontest"})
    public void conversationTimeoutTest() throws Exception {
        addDescription("Tests the the client handles lack of IdentifyPillarResponses gracefully  ");

        addStep("Set a 100 ms ConversationTimeout.", "");
        settingsForCUT.getReferenceSettings().getClientSettings().setConversationTimeout(BigInteger.valueOf(100));
        renewConversationMediator();

        addStep("Start the operation",
                "A IDENTIFY_REQUEST_SENT event should be generated followed by a FAILED event after 100 ms.");
        startOperation(testEventHandler);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        assertNotNull(waitForIdentifyRequest());
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }


    protected abstract MessageResponse createIdentifyResponse(MessageRequest identifyRequest, String from, String to);
    protected abstract MessageResponse createFinalResponse(MessageRequest request, String from, String to);

    protected abstract MessageRequest waitForIdentifyRequest();
    protected abstract MessageRequest waitForRequest(MessageReceiver receiver);
    protected abstract void checkNoRequestIsReceived(MessageReceiver receiver);

    /** Makes a default call to the client for the operation */
    protected abstract void startOperation(TestEventHandler testEventHandler);
}
