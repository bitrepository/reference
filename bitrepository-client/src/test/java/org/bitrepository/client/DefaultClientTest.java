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
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identification response from contributor1 with a IDENTIFICATION_NEGATIVE response code .",
                "A component failed event should be generated.");

        MessageResponse identifyResponse1 = createIdentifyResponse(identifyRequest, PILLAR1_ID, pillar1DestinationId);
        identifyResponse1.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        messageBus.sendMessage(identifyResponse1);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);

        addStep("Send a identification response from contributor2 with a IDENTIFICATION_POSITIVE response code .",
                "A component COMPONENT_IDENTIFIED event should be generated followed by a IDENTIFICATION_COMPLETE.");
        MessageResponse identifyResponse2 = createIdentifyResponse(identifyRequest, PILLAR2_ID, pillar2DestinationId);
        identifyResponse2.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        messageBus.sendMessage(identifyResponse2);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);

        addStep("Verify that the client continues to the performing phase.",
                "A REQUEST_SENT event should be generated and a OperationRequest should be sent only to contributor2" +
                        ".");
        checkNoRequestIsReceived(pillar1Receiver);
        MessageRequest request = waitForRequest(pillar2Receiver);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a final response message from contributor2",
                "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
        MessageResponse completeMsg = createFinalResponse(request, PILLAR2_ID, pillar2DestinationId);
        completeMsg.getResponseInfo().setResponseCode(ResponseCode.OPERATION_COMPLETED);
        messageBus.sendMessage(completeMsg);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups = {"regressiontest"})
    public void identificationFailureTest() throws Exception {
        addDescription("Verify that the client works correctly when a contributor sends a failure response.");

        addStep("Start the operation.",
                "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
        startOperation(testEventHandler);
        MessageRequest identifyRequest = waitForIdentifyRequest();
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identification response from contributor1 with a FAILURE response code.",
                "A component failed event should be generated.");
        MessageResponse identifyResponse1 = createIdentifyResponse(identifyRequest, PILLAR1_ID, pillar1DestinationId);
        identifyResponse1.getResponseInfo().setResponseCode(ResponseCode.FAILURE);
        messageBus.sendMessage(identifyResponse1);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);

        addStep("Send a identification response from contributor2 with a IDENTIFICATION_POSITIVE response code .",
                "A component COMPONENT_IDENTIFIED event should be generated followed by a IDENTIFICATION_COMPLETE.");
        MessageResponse identifyResponse2 = createIdentifyResponse(
                identifyRequest, PILLAR2_ID, pillar2DestinationId);
        identifyResponse2.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        messageBus.sendMessage(identifyResponse2);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);

        addStep("Verify that the client continues to the performing phase.",
                "A REQUEST_SENT event should be generated and a Request should be sent only to contributor2.");
        checkNoRequestIsReceived(pillar1Receiver);
        MessageRequest request = waitForRequest(pillar2Receiver);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a final response message from contributor2",
                "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
        MessageResponse completeMsg = createFinalResponse(request, PILLAR2_ID, pillar2DestinationId);
        completeMsg.getResponseInfo().setResponseCode(ResponseCode.OPERATION_COMPLETED);
        messageBus.sendMessage(completeMsg);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups = {"regressiontest"})
    public void oneContributorNotRespondingTest() throws Exception {
        addDescription("Verify that the client works correct without receiving identification responses from all " +
                "contributors.");
        addFixtureSetup("Set the a identification timeout to 3 second.");
        settingsForCUT.getRepositorySettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));

        addStep("Start the operation.",
                "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
        startOperation(testEventHandler);
        MessageRequest identifyRequest = waitForIdentifyRequest();
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identification response from contributor1.", "A COMPONENT_IDENTIFIED event should be generated.");
        MessageResponse identifyResponse1 = createIdentifyResponse(identifyRequest, PILLAR1_ID, pillar1DestinationId);
        identifyResponse1.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        messageBus.sendMessage(identifyResponse1);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);

        addStep("Wait 5 seconds.",
                "A IDENTIFY_TIMEOUT event should be generated, followed by a IDENTIFICATION_COMPLETE.");
        Assert.assertEquals(testEventHandler.waitForEvent(
               5, TimeUnit.SECONDS).getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);

        addStep("Verify that the client continues to the performing phase.",
                "A REQUEST_SENT event should be generated and a Request should be sent to pillar1.");
        MessageRequest request = waitForRequest(pillar1Receiver);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a final response upload message",
                "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
        MessageResponse completeMsg = createFinalResponse(request, PILLAR1_ID, pillar1DestinationId);
        completeMsg.getResponseInfo().setResponseCode(ResponseCode.OPERATION_COMPLETED);
        messageBus.sendMessage(completeMsg);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);

    }

    @Test(groups = {"regressiontest"})
    public void noContributorsRespondingTest() throws Exception {
        addDescription("Tests the the client handles lack of a IdentifyResponse gracefully. " +
                "More concrete this means that the occurence of a identification timeout should be handled correctly");

        addStep("Set a 1 second timeout for identifying contributors.", "");
        settingsForCUT.getRepositorySettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));

        addStep("Start the operation.", "A IDENTIFY_REQUEST_SENT event should be generated.");
        startOperation(testEventHandler);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Wait for 5 seconds", "An IdentifyPillarTimeout event should be received followed by a FAILED event");
        Assert.assertEquals(testEventHandler.waitForEvent(3, TimeUnit.SECONDS).getEventType(),
                OperationEventType.IDENTIFY_TIMEOUT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }


    @Test(groups = {"regressiontest"})
    public void operationTimeoutTest() throws Exception {
        addDescription("Tests the the client handles lack of final responses gracefully.");

        addStep("Set a 3 second operation timeout.", "");
        settingsForCUT.getRepositorySettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(3000));

        addStep("Start the operation",
                "A IDENTIFY_REQUEST_SENT event should be received.");
        startOperation(testEventHandler);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        MessageRequest identifyRequest = waitForIdentifyRequest();

        addStep("Send positive responses from the pillar1 and a negative response from pillar2",
                "A COMPONENT_IDENTIFIED + a " +
                "event should be generated followed by a IDENTIFICATION_COMPLETE. <p>" +
                "Finally a operation request should be sent to pillar1 and a REQUEST_SENT event be " +
                "generated");
        messageBus.sendMessage(createIdentifyResponse(identifyRequest, PILLAR1_ID, pillar1DestinationId));
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        MessageResponse identifyResponse2 = createIdentifyResponse(identifyRequest, PILLAR2_ID, pillar2DestinationId);
        identifyResponse2.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        messageBus.sendMessage(identifyResponse2);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        MessageRequest request1 = waitForRequest(pillar1Receiver);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Wait for 5 seconds", "An FAILED event should be received");
        Assert.assertEquals(testEventHandler.waitForEvent(5, TimeUnit.SECONDS).getEventType(),
                OperationEventType.FAILED);
    }

    @Test(groups = {"regressiontest"})
    public void conversationTimeoutTest() throws Exception {
        addDescription("Tests the the client handles lack of IdentifyPillarResponses gracefully  ");

        addStep("Set a 3 second ConversationTimeout.", "");
        settingsForCUT.getReferenceSettings().getClientSettings().setConversationTimeout(BigInteger.valueOf(3000));
        renewConversationMediator();

        addStep("Start the operation",
                "A IDENTIFY_REQUEST_SENT event should be generated followed by a FAILED event after 2 seconds.");
        startOperation(testEventHandler);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        Assert.assertNotNull(waitForIdentifyRequest());
        Assert.assertEquals(testEventHandler.waitForEvent(5, TimeUnit.SECONDS).getEventType(),
                OperationEventType.FAILED);
    }


    protected abstract MessageResponse createIdentifyResponse(MessageRequest identifyRequest, String from, String to);
    protected abstract MessageResponse createFinalResponse(MessageRequest request, String from, String to);

    protected abstract MessageRequest waitForIdentifyRequest();
    protected abstract MessageRequest waitForRequest(MessageReceiver receiver);
    protected abstract void checkNoRequestIsReceived(MessageReceiver receiver);

    /** Makes a default call to the client for the operation */
    protected abstract void startOperation(TestEventHandler testEventHandler);
}
